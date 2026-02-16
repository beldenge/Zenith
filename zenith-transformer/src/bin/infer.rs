use anyhow::Result;
use candle_core::{D, Device, IndexOp, Tensor};
use candle_nn::VarMap;
use clap::Parser;
use zenith_transformer::{GPT, Tokenizer, generate_samples};

#[derive(Parser, Debug)]
struct Args {
    #[arg(long, default_value = "model/model.safetensors")]
    model_path: String,
}

fn main() -> Result<()> {
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
    let gpt = GPT::new(
        vocab_size,
        block_size,
        embed_dim,
        num_heads,
        layers,
        learning_rate,
        drop_p,
        &varmap,
        &device,
    )?;

    // Now overwrite random init with saved weights
    varmap.load("model/model.safetensors")?;

    println!("Score: {:.4}", score_text(&gpt, &tokenizer, vec![String::from("thequickbrownfoxjumpsoverthelazydog")], &device)?);
    println!("Score: {:.4}", score_text(&gpt, &tokenizer, vec![String::from("nmzcocpamsivuujuaratowfeenvznskxfotefgpusnxhqqifkdyyscntazivlpmzvhxksgptoejiiuevarorfntxwknsuyexjrdmysvqqufkhirxwahiytapjcztanjxkvtpgvvszosxuuyrxkrdptintazkvtyueiqsokpvseozuiarudtqpyhgvevunkvmhfiijocnjeilzdtrvpqmpyqaksvuwiukesdnrpthqlyoszmpuslntaaritahqizguyihzpoenkmwmmutqsyyzkcnmmozlztaauojxrxmetrztnnlnamihdvpztmjfxmgyxraoowfskmhflnmtpqmvkeajnmjlkjinpkuxmojtteodzivjiemwzonehqxmreutiazireakjinruexkvtyupus")], &device)?);
    println!("Score: {:.4}", score_text(&gpt, &tokenizer, vec![String::from("redgmgdneyanintinliteshstrodoetoherehedaysothtihoersigrandanedeindonyearmereansoileshotosoreartotseasinthahndasosntisanargdanotonordennyimeonarlotsearerandontmitatiendoysminailaethardentoaronuthearegoteiedersndheartineoiseateyersardtermidadnyeranilatithadeisedidetsoaseeartyrsingreemiedaninetolousasitorerneatenditurhouesolimeshenuthereaahuotsntrateorasatioueraateedantisesimoethouseatendiltinterlatonorriday")], &device)?);
    println!("Score: {:.4}", score_text(&gpt, &tokenizer, vec![String::from("ilikekillingpeoplebecauseitissomuchfunitiamorefunthankillingwildgameintheforrestbecausemanisthemoatdangertueanamalofalltokillsomethinggivesmethemoatthrillingeoperenceitisevenbetterthangettingyourrocksoffwithagirlthebestpartofitiathaewhenidieiwillbereborninparadiceandalltheihavekilledwillbecomemyslavesiwillnotgiveyoumynamebecauseyouwilltrytosloidownoratopmycollectingofslavesformyafterlifeebeorietemethhpiti")], &device)?);

    let generated = generate_samples(&gpt, &tokenizer, block_size, 0.8, "hithere", &device)?;
    println!("{}", generated);

    Ok(())
}

fn score_text(gpt: &GPT, tokenizer: &Tokenizer, text: Vec<String>, device: &Device) -> Result<f32> {
    const CHUNK_SIZE: usize = 128;
    let ids_full: Vec<i64> = tokenizer
        .encode(&text)
        .into_iter()
        .map(|x| x as i64)
        .collect();
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
