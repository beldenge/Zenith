use zenith_transformer::load_file;
use anyhow::{Result};
use candle_core::{Device, Tensor, DType, IndexOp, D};
use candle_nn::{embedding, layer_norm, linear, AdamW, Embedding, LayerNorm, LayerNormConfig, Linear, Module, Optimizer, ParamsAdamW, VarBuilder, VarMap};
use clap::Parser;
use rand::prelude::*;
use std::collections::HashMap;
use std::time::Instant;

#[derive(Parser, Debug)]
struct Args {
    #[arg(long, default_value_t = 10)]
    epochs: usize,
    #[arg(long, default_value_t = 32)]
    batch_size: usize,
    #[arg(long, default_value_t = 0.001)]
    learning_rate: f64,
    #[arg(long, default_value_t = 128)]
    block_size: usize,
    #[arg(long, default_value_t = 64)]
    n_embd: usize,
    #[arg(long, default_value_t = 4)]
    n_head: usize,
    #[arg(long, default_value_t = 4)]
    n_layer: usize,
    #[arg(long, default_value_t = 100)]
    steps_per_epoch: usize,
}

struct Tokenizer {
    stoi: HashMap<char, usize>,
    itos: HashMap<usize, char>,
}

impl Tokenizer {
    fn from_samples(samples: &[String]) -> Self {
        let mut chars: Vec<char> = samples
            .iter()
            .flat_map(|sample| sample.chars())
            .collect();

        chars.sort_unstable();
        chars.dedup();

        let stoi: HashMap<char, usize> = chars.iter().enumerate().map(|(i, &c)| (c, i)).collect();
        let itos: HashMap<usize, char> = chars.into_iter().enumerate().map(|(i, c)| (i, c)).collect();
        Self { stoi, itos }
    }

    fn encode(&self, s: &[String]) -> Vec<usize> {
        s.iter()
            .flat_map(|doc| doc.chars())
            .map(|c| self.stoi[&c])
            .collect()
    }

    fn decode(&self, ids: &[usize]) -> String {
        ids.iter()
            .map(|&i| self.itos[&i])
            .collect()
    }
}

struct Config {
    block_size: usize,
    vocab_size: usize,
    n_embd: usize,
    n_head: usize,
    n_layer: usize,
    batch_size: usize,
}

fn causal_attention(q: &Tensor, k: &Tensor, v: &Tensor) -> Result<Tensor> {
    let (b, h, t, d) = q.dims4()?;
    let scale = (d as f64).powf(-0.5);
    let att = (q.matmul(&k.transpose(3, 2)?)? * scale)?;
    let att = att.clamp(-80.0, 80.0)?;

    // Create causal mask: positions j <= i are 1, others 0
    let left = Tensor::arange(0i64, t as i64, q.device())?.unsqueeze(1)?.expand((t, t))?;
    let right = Tensor::arange(0i64, t as i64, q.device())?.unsqueeze(0)?.expand((t, t))?;
    let mask = left.le(&right)?.to_dtype(DType::F32)?;
    let mask_broadcast = mask.broadcast_as(att.shape())?;
    let neg_inf = Tensor::try_from(-1e9f32)?
        .to_device(q.device())?
        .broadcast_as(att.shape())?;

    // Apply mask: keep lower triangular, set upper to -inf
    let one_minus_mask = Tensor::ones_like(&mask_broadcast)?.sub(&mask_broadcast)?;
    let att_masked = att.mul(&mask_broadcast)?.add(&one_minus_mask.mul(&neg_inf)?)?;

    let att = candle_nn::ops::softmax(&att_masked, D::Minus1)?;
    Ok(att.matmul(v)?)
}

struct Block {
    ln1: LayerNorm,
    ln2: LayerNorm,
    att: Linear, // combined qkv projection
    proj: Linear,
    ff: Linear,
    ff_proj: Linear,
    n_head: usize,
    head_dim: usize,
}

impl Block {
    fn new(vb: VarBuilder, cfg: &Config) -> Result<Self> {
        let head_dim = cfg.n_embd / cfg.n_head;
        Ok(Self {
            ln1: layer_norm(cfg.n_embd, LayerNormConfig::default(), vb.pp("ln1"))?,
            ln2: layer_norm(cfg.n_embd, LayerNormConfig::default(), vb.pp("ln2"))?,
            att: linear(cfg.n_embd, cfg.n_embd * 3, vb.pp("att"))?,
            proj: linear(cfg.n_embd, cfg.n_embd, vb.pp("proj"))?,
            ff: linear(cfg.n_embd, cfg.n_embd * 4, vb.pp("ff"))?,
            ff_proj: linear(cfg.n_embd * 4, cfg.n_embd, vb.pp("ff_proj"))?,
            n_head: cfg.n_head,
            head_dim,
        })
    }

    fn forward(&self, x: &Tensor) -> Result<Tensor> {
        let (b, t, c) = x.dims3()?;
        let residual = x;

        let x = self.ln1.forward(x)?;
        let qkv = x.apply(&self.att)?.reshape((b, t, 3, self.att.weight().dim(0)? / 3))?;
        let (q, k, v) = (qkv.i((.., .., 0, ..))?, qkv.i((.., .., 1, ..))?, qkv.i((.., .., 2, ..))?);
        let q = q.reshape((b, t, self.n_head, self.head_dim))?.transpose(1, 2)?.contiguous()?;
        let k = k.reshape((b, t, self.n_head, self.head_dim))?.transpose(1, 2)?.contiguous()?;
        let v = v.reshape((b, t, self.n_head, self.head_dim))?.transpose(1, 2)?.contiguous()?;
        let att = causal_attention(&q, &k, &v)?;
        let att = att.transpose(1, 2)?.reshape((b, t, c))?.apply(&self.proj)?;

        let x = (residual + att)?;

        let residual = &x;
        let x = x.apply(&self.ln2)?.apply(&self.ff)?.gelu()?.apply(&self.ff_proj)?;
        Ok((x + residual)?)
    }
}

struct GPT {
    tok_emb: Embedding,
    pos_emb: Embedding,
    blocks: Vec<Block>,
    ln_f: LayerNorm,
    head: Linear,
}

impl GPT {
    fn new(cfg: &Config, vb: VarBuilder) -> Result<Self> {
        let mut blocks = Vec::new();
        for i in 0..cfg.n_layer {
            blocks.push(Block::new(vb.pp(format!("block{}", i)), cfg)?);
        }
        Ok(Self {
            tok_emb: embedding(cfg.vocab_size, cfg.n_embd, vb.pp("tok_emb"))?,
            pos_emb: embedding(cfg.block_size, cfg.n_embd, vb.pp("pos_emb"))?,
            blocks,
            ln_f: layer_norm(cfg.n_embd, LayerNormConfig::default(), vb.pp("ln_f"))?,
            head: linear(cfg.n_embd, cfg.vocab_size, vb.pp("head"))?,
        })
    }

    fn forward(&self, idx: &Tensor) -> Result<Tensor> {
        let (b, t) = idx.dims2()?;
        let tok = idx.apply(&self.tok_emb)?;
        let pos = Tensor::arange(0i64, t as i64, idx.device())?
            .unsqueeze(0)?
            .expand((b, t))?
            .apply(&self.pos_emb)?;
        let mut x = (tok + pos)?;
        for block in &self.blocks {
            x = block.forward(&x)?;
        }
        Ok(x.apply(&self.ln_f)?.apply(&self.head)?)
    }
}

fn get_batch(data: &[usize], cfg: &Config, device: &Device) -> Result<(Tensor, Tensor)> {
    let ix: Vec<_> = (0..cfg.batch_size)
        .map(|_| rand::rng().random_range(0..data.len() - cfg.block_size))
        .collect();

    let x_tensors: Vec<Tensor> = ix.iter()
        .map(|&i| {
            let slice: Vec<i64> = data[i..i+cfg.block_size].iter().map(|&x| x as i64).collect();
            Tensor::from_slice(&slice, cfg.block_size, device)
        })
        .collect::<std::result::Result<Vec<_>, _>>()?;  // Use std Result, error converts via ?

    let y_tensors: Vec<Tensor> = ix.iter()
        .map(|&i| {
            let slice: Vec<i64> = data[i+1..i+1+cfg.block_size].iter().map(|&x| x as i64).collect();
            Tensor::from_slice(&slice, cfg.block_size, device)
        })
        .collect::<std::result::Result<Vec<_>, _>>()?;

    let x = Tensor::stack(&x_tensors, 0)?;
    let y = Tensor::stack(&y_tensors, 0)?;

    Ok((x, y))
}

fn main() -> anyhow::Result<()> {
    let args = Args::parse();
    let device = Device::cuda_if_available(0).unwrap_or(Device::Cpu);
    let samples: Vec<String> = load_file()?;
    let tokenizer = Tokenizer::from_samples(&samples);
    let data: Vec<usize> = tokenizer.encode(&samples);
    println!("Vocab: {} | Data len: {}", tokenizer.stoi.len(), data.len());

    let cfg = Config {
        vocab_size: tokenizer.stoi.len(),
        block_size: args.block_size,
        n_embd: args.n_embd,
        n_head: args.n_head,
        n_layer: args.n_layer,
        batch_size: args.batch_size,
    };

    let varmap = VarMap::new();
    let vb = VarBuilder::from_varmap(&varmap, DType::F32, &device);
    let model = GPT::new(&cfg, vb)?;
    let params = ParamsAdamW {
        lr: args.learning_rate,
        ..ParamsAdamW::default()
    };
    let mut opt = AdamW::new(varmap.all_vars(), params)?;

    for epoch in 0..args.epochs {
        let mut step_start = Instant::now();
        for step in 1..=args.steps_per_epoch {
            let (x, y) = get_batch(&data, &cfg, &device)?;
            let logits = model.forward(&x)?;
            let loss = candle_nn::loss::cross_entropy(&logits.flatten_to(1)?, &y.flatten_all()?)?;
            opt.backward_step(&loss)?;

            if step > 0 && step % 10 == 0 {
                println!("epoch {epoch} step {step} elapsed: {}ms loss: {:.4}", step_start.elapsed().as_millis(), loss.to_scalar::<f32>()?);
                step_start = Instant::now();
            }
        }
    }

    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("the quick brown fox jumps over the lazy dog.")], &device)?);
    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("thequickbrownfoxjumpsoverthelazydog")], &device)?);
    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("nmzcocpamsivuujuaratowfeenvznskxfotefgpusnxhqqifkdyyscntazivlpmzvhxksgptoejiiuevarorfntxwknsuyexjrdmysvqqufkhirxwahiytapjcztanjxkvtpgvvszosxuuyrxkrdptintazkvtyueiqsokpvseozuiarudtqpyhgvevunkvmhfiijocnjeilzdtrvpqmpyqaksvuwiukesdnrpthqlyoszmpuslntaaritahqizguyihzpoenkmwmmutqsyyzkcnmmozlztaauojxrxmetrztnnlnamihdvpztmjfxmgyxraoowfskmhflnmtpqmvkeajnmjlkjinpkuxmojtteodzivjiemwzonehqxmreutiazireakjinruexkvtyupus")], &device)?);
    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("redgmgdneyanintinliteshstrodoetoherehedaysothtihoersigrandanedeindonyearmereansoileshotosoreartotseasinthahndasosntisanargdanotonordennyimeonarlotsearerandontmitatiendoysminailaethardentoaronuthearegoteiedersndheartineoiseateyersardtermidadnyeranilatithadeisedidetsoaseeartyrsingreemiedaninetolousasitorerneatenditurhouesolimeshenuthereaahuotsntrateorasatioueraateedantisesimoethouseatendiltinterlatonorriday")], &device)?);
    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("ilikekillingpeoplebecauseitissomuchfunitiamorefunthankillingwildgameintheforrestbecausemanisthemoatdangertueanamalofalltokillsomethinggivesmethemoatthrillingeoperenceitisevenbetterthangettingyourrocksoffwithagirlthebestpartofitiathaewhenidieiwillbereborninparadiceandalltheihavekilledwillbecomemyslavesiwillnotgiveyoumynamebecauseyouwilltrytosloidownoratopmycollectingofslavesformyafterlifeebeorietemethhpiti")], &device)?);
    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("i like killing people because it is so much fun it ia more fun than killing wild game in the forrest because man is the moat danger tue an amal of all to kill something gives me the moat thrilling eop erence it is even better than getting your rocks off with a girl the best part of it ia thae when i die i will be reborn in paradice and all the i have killed will become my slaves i will not give you my name because you will try to slo i down or a top my collecting of slaves for my afterlife ebeorietemethhpiti")], &device)?);
    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("i like killing people because it is so much fun it ia more fun than killing wild game in the forrest because man is the moat danger tue an amal of all to kill something gives me the moat thrilling eop erence it is even better than getting your rocks off with a girl the best part of it ia thae when i die i will be reborn in paradice and all the i have killed will become my slaves i will not give you my name because you will try to slo i down or a top my collecting of slaves for my afterlife")], &device)?);

    Ok(())
}

fn score_text(model: &GPT, tokenizer: &Tokenizer, text: Vec<String>, device: &Device) -> Result<f32> {
    const CHUNK_SIZE: usize = 128;
    let ids_full: Vec<i64> = tokenizer.encode(&text).into_iter().map(|x| x as i64).collect();
    let mut total_log_prob = 0.0;
    let mut count = 0;
    for chunk in ids_full.chunks(CHUNK_SIZE) {
        let chunk_len = chunk.len();
        if chunk_len < 2 {
            continue;
        }
        let t = Tensor::from_vec(chunk.to_vec(), (1, chunk_len), device)?;
        let logits = model.forward(&t)?;
        let probs = candle_nn::ops::softmax(&logits, D::Minus1)?;
        let log_probs = probs.log()?;
        let targets = t.i((0, 1..))?;
        let gathered = log_probs.i((0, ..log_probs.dim(1)?-1))?.gather(&targets.unsqueeze(1)?, 1)?.squeeze(1)?;
        total_log_prob += gathered.sum_all()?.to_scalar::<f32>()?;
        count += gathered.elem_count();
    }
    if count == 0 {
        Ok(0.0)
    } else {
        Ok(total_log_prob / count as f32)
    }
}
