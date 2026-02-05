use anyhow::{Result};
use candle_core::{Device, Tensor, IndexOp, D, DType};
use candle_nn::ops::softmax;
use candle_nn::VarMap;
use clap::Parser;
use rand::distr::weighted::WeightedIndex;
use rand::prelude::*;
use zenith_transformer::{Tokenizer, GPT};

#[derive(Parser, Debug)]
struct Args {
    #[arg(long, default_value = "model/model.safetensors")]
    model_path: String,
}

fn main() -> Result<()> {
    let args = Args::parse();
    let device = Device::cuda_if_available(0).unwrap_or(Device::Cpu);

    let tokenizer = Tokenizer::from_static();
    let block_size: usize = 256;
    let learning_rate = 3e-4;
    let layers = 6;
    let num_heads = 6;
    let embed_dim = 384;
    let drop_p = 0.2;
    let vocab_size = tokenizer.stoi.len();
    let mut varmap = VarMap::new();
    let gpt = GPT::new(vocab_size, block_size, embed_dim, num_heads, layers, learning_rate, drop_p, &varmap, &device)?;

    // Now overwrite random init with saved weights
    varmap.load("model/model.safetensors")?;

    println!("Score: {:.4}", score_text(&gpt, &tokenizer, vec![String::from("the quick brown fox jumps over the lazy dog.")], &device)?);
    println!("Score: {:.4}", score_text(&gpt, &tokenizer, vec![String::from("thequickbrownfoxjumpsoverthelazydog")], &device)?);
    println!("Score: {:.4}", score_text(&gpt, &tokenizer, vec![String::from("nmzcocpamsivuujuaratowfeenvznskxfotefgpusnxhqqifkdyyscntazivlpmzvhxksgptoejiiuevarorfntxwknsuyexjrdmysvqqufkhirxwahiytapjcztanjxkvtpgvvszosxuuyrxkrdptintazkvtyueiqsokpvseozuiarudtqpyhgvevunkvmhfiijocnjeilzdtrvpqmpyqaksvuwiukesdnrpthqlyoszmpuslntaaritahqizguyihzpoenkmwmmutqsyyzkcnmmozlztaauojxrxmetrztnnlnamihdvpztmjfxmgyxraoowfskmhflnmtpqmvkeajnmjlkjinpkuxmojtteodzivjiemwzonehqxmreutiazireakjinruexkvtyupus")], &device)?);
    println!("Score: {:.4}", score_text(&gpt, &tokenizer, vec![String::from("redgmgdneyanintinliteshstrodoetoherehedaysothtihoersigrandanedeindonyearmereansoileshotosoreartotseasinthahndasosntisanargdanotonordennyimeonarlotsearerandontmitatiendoysminailaethardentoaronuthearegoteiedersndheartineoiseateyersardtermidadnyeranilatithadeisedidetsoaseeartyrsingreemiedaninetolousasitorerneatenditurhouesolimeshenuthereaahuotsntrateorasatioueraateedantisesimoethouseatendiltinterlatonorriday")], &device)?);
    println!("Score: {:.4}", score_text(&gpt, &tokenizer, vec![String::from("ilikekillingpeoplebecauseitissomuchfunitiamorefunthankillingwildgameintheforrestbecausemanisthemoatdangertueanamalofalltokillsomethinggivesmethemoatthrillingeoperenceitisevenbetterthangettingyourrocksoffwithagirlthebestpartofitiathaewhenidieiwillbereborninparadiceandalltheihavekilledwillbecomemyslavesiwillnotgiveyoumynamebecauseyouwilltrytosloidownoratopmycollectingofslavesformyafterlifeebeorietemethhpiti")], &device)?);
    println!("Score: {:.4}", score_text(&gpt, &tokenizer, vec![String::from("i like killing people because it is so much fun it ia more fun than killing wild game in the forrest because man is the moat danger tue an amal of all to kill something gives me the moat thrilling eop erence it is even better than getting your rocks off with a girl the best part of it ia thae when i die i will be reborn in paradice and all the i have killed will become my slaves i will not give you my name because you will try to slo i down or a top my collecting of slaves for my afterlife ebeorietemethhpiti")], &device)?);
    println!("Score: {:.4}", score_text(&gpt, &tokenizer, vec![String::from("i like killing people because it is so much fun it ia more fun than killing wild game in the forrest because man is the moat danger tue an amal of all to kill something gives me the moat thrilling eop erence it is even better than getting your rocks off with a girl the best part of it ia thae when i die i will be reborn in paradice and all the i have killed will become my slaves i will not give you my name because you will try to slo i down or a top my collecting of slaves for my afterlife")], &device)?);

    let generated = generate_samples(&gpt, &tokenizer, block_size, 0.8, "hi there", &device)?;
    println!("{}", generated);

    Ok(())
}

fn score_text(gpt: &GPT, tokenizer: &Tokenizer, text: Vec<String>, device: &Device) -> Result<f32> {
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
        let logits = gpt.forward(&t, false)?;
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

fn generate_samples(gpt: &GPT, tokenizer: &Tokenizer, block_size: usize, temperature: f64, prompt: &str, device: &Device) -> Result<String> {
    let mut ids = tokenizer.encode(&[prompt.to_string()]);
    let mut next_sample = prompt.to_string();
    let mut inputs_tensor = Tensor::from_vec(
        ids.iter().map(|&i| i as u32).collect::<Vec<_>>(),
        (1, ids.len()),
        device
    )?;

    for _ in 0..500 {
        let current_seq_len = inputs_tensor.dims()[1];
        // Truncate the inputs tensor to the block size
        inputs_tensor = inputs_tensor.narrow(1, 0.max(current_seq_len as i32 - block_size as i32) as usize, current_seq_len.min(block_size))?;

        // Squeeze needed to convert (B, T, C) -> (B * T, C)
        let logits = gpt.forward(&inputs_tensor, false)?.squeeze(0)?;
        let last_logits = logits.narrow(0, logits.dims()[0] - 1, 1)?.squeeze(0)?;
        let last_logits = (last_logits / temperature)?;
        let probs = softmax(&last_logits, 0)?;
        let probs_vec: Vec<f32> = probs.to_vec1()?;

        // Sample from the distribution
        let dist = WeightedIndex::new(&probs_vec)?;
        let sampled_idx = dist.sample(&mut rand::rng());
        next_sample += &tokenizer.decode(&[sampled_idx]);

        let next_token = Tensor::from_vec(vec![sampled_idx as u32], (1, 1), device)?;
        inputs_tensor = Tensor::cat(&[&inputs_tensor, &next_token], 1)?;
    }

    Ok(next_sample)
}
