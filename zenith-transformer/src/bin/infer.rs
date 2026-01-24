use anyhow::{Result};
use candle_core::{Device, Tensor, DType, IndexOp, D};
use candle_nn::{VarBuilder, VarMap};
use clap::Parser;
use rand::prelude::*;
use zenith_transformer::{Config, Tokenizer, GPT};

#[derive(Parser, Debug)]
struct Args {
    #[arg(long, default_value = "model/super-overfit.safetensors")]
    model_path: String,

    #[arg(long, default_value = "hi there")]
    prompt: String,

    #[arg(long, default_value_t = 100)]
    max_tokens: usize,

    #[arg(long, default_value_t = 0.8)]
    temperature: f64,
}

fn main() -> Result<()> {
    let args = Args::parse();
    let device = Device::cuda_if_available(0).unwrap_or(Device::Cpu);

    let mut varmap = VarMap::new();
    let vb = VarBuilder::from_varmap(&varmap, DType::F32, &device);

    let tokenizer = Tokenizer::from_static();

    let cfg = Config {
        vocab_size: tokenizer.stoi.len(),
        block_size: 128,
        n_embd: 256,
        n_head: 8,
        n_layer: 6,
        batch_size: 1,
    };

    let model = GPT::new(&cfg, vb)?;

    // Now overwrite random init with saved weights
    varmap.load("model/model.safetensors")?;

    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("the quick brown fox jumps over the lazy dog.")], &device)?);
    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("thequickbrownfoxjumpsoverthelazydog")], &device)?);
    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("nmzcocpamsivuujuaratowfeenvznskxfotefgpusnxhqqifkdyyscntazivlpmzvhxksgptoejiiuevarorfntxwknsuyexjrdmysvqqufkhirxwahiytapjcztanjxkvtpgvvszosxuuyrxkrdptintazkvtyueiqsokpvseozuiarudtqpyhgvevunkvmhfiijocnjeilzdtrvpqmpyqaksvuwiukesdnrpthqlyoszmpuslntaaritahqizguyihzpoenkmwmmutqsyyzkcnmmozlztaauojxrxmetrztnnlnamihdvpztmjfxmgyxraoowfskmhflnmtpqmvkeajnmjlkjinpkuxmojtteodzivjiemwzonehqxmreutiazireakjinruexkvtyupus")], &device)?);
    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("redgmgdneyanintinliteshstrodoetoherehedaysothtihoersigrandanedeindonyearmereansoileshotosoreartotseasinthahndasosntisanargdanotonordennyimeonarlotsearerandontmitatiendoysminailaethardentoaronuthearegoteiedersndheartineoiseateyersardtermidadnyeranilatithadeisedidetsoaseeartyrsingreemiedaninetolousasitorerneatenditurhouesolimeshenuthereaahuotsntrateorasatioueraateedantisesimoethouseatendiltinterlatonorriday")], &device)?);
    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("ilikekillingpeoplebecauseitissomuchfunitiamorefunthankillingwildgameintheforrestbecausemanisthemoatdangertueanamalofalltokillsomethinggivesmethemoatthrillingeoperenceitisevenbetterthangettingyourrocksoffwithagirlthebestpartofitiathaewhenidieiwillbereborninparadiceandalltheihavekilledwillbecomemyslavesiwillnotgiveyoumynamebecauseyouwilltrytosloidownoratopmycollectingofslavesformyafterlifeebeorietemethhpiti")], &device)?);
    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("i like killing people because it is so much fun it ia more fun than killing wild game in the forrest because man is the moat danger tue an amal of all to kill something gives me the moat thrilling eop erence it is even better than getting your rocks off with a girl the best part of it ia thae when i die i will be reborn in paradice and all the i have killed will become my slaves i will not give you my name because you will try to slo i down or a top my collecting of slaves for my afterlife ebeorietemethhpiti")], &device)?);
    println!("Score: {:.4}", score_text(&model, &tokenizer, vec![String::from("i like killing people because it is so much fun it ia more fun than killing wild game in the forrest because man is the moat danger tue an amal of all to kill something gives me the moat thrilling eop erence it is even better than getting your rocks off with a girl the best part of it ia thae when i die i will be reborn in paradice and all the i have killed will become my slaves i will not give you my name because you will try to slo i down or a top my collecting of slaves for my afterlife")], &device)?);

    let generated = generate(&model, &tokenizer, &args.prompt, args.max_tokens, args.temperature, &device)?;
    println!("{}", generated);

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

fn generate(
    model: &GPT,
    tokenizer: &Tokenizer,
    prompt: &str,
    max_tokens: usize,
    temperature: f64,
    device: &Device
) -> Result<String> {
    let mut ids = tokenizer.encode(&[prompt.to_string()]);

    for _ in 0..max_tokens {
        let context = &ids[ids.len().saturating_sub(128)..]; // Last 128 tokens
        let x = candle_core::Tensor::from_vec(
            context.iter().map(|&i| i as i64).collect::<Vec<_>>(),
            (1, context.len()),
            device
        )?;

        let logits = model.forward(&x)?;  // train=false!
        let next_token_logits = logits.i((0, logits.dim(1)? - 1, ..))?;

        // Apply temperature
        let next_token_logits = (next_token_logits / temperature)?;
        let probs = candle_nn::ops::softmax(&next_token_logits, D::Minus1)?;

        // Sample from distribution
        let next_token = sample_from_probs(&probs)?;
        ids.push(next_token);
    }

    Ok(tokenizer.decode(&ids))
}

fn sample_from_probs(probs: &candle_core::Tensor) -> Result<usize> {
    let probs_vec = probs.to_vec1::<f32>()?;
    let sample = rand::rng().random::<f32>();

    let mut cumsum = 0.0;
    for (i, &p) in probs_vec.iter().enumerate() {
        cumsum += p;
        if sample < cumsum {
            return Ok(i);
        }
    }
    Ok(probs_vec.len() - 1)
}
