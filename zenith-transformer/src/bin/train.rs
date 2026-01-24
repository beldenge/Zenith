use std::fmt::Debug;
use zenith_transformer::{Config, load_file, GPT, Tokenizer};
use anyhow::{Result};
use candle_core::{Device, Tensor, DType};
use candle_nn::{AdamW, Optimizer, ParamsAdamW, VarBuilder, VarMap};
use clap::Parser;
use rand::prelude::*;
use std::time::Instant;

#[derive(Parser, Debug)]
struct Args {
    #[arg(long, default_value_t = 10)]
    epochs: usize,
    #[arg(long, default_value_t = 32)]
    batch_size: usize,
    #[arg(long, default_value_t = 0.0003)]
    learning_rate: f64,
    #[arg(long, default_value_t = 128)]
    block_size: usize,
    #[arg(long, default_value_t = 256)]
    n_embd: usize,
    #[arg(long, default_value_t = 8)]
    n_head: usize,
    #[arg(long, default_value_t = 6)]
    n_layer: usize,
    #[arg(long, default_value_t = 100000)]
    steps_per_epoch: usize,
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
        .collect::<Result<Vec<_>, _>>()?;

    let y_tensors: Vec<Tensor> = ix.iter()
        .map(|&i| {
            let slice: Vec<i64> = data[i+1..i+1+cfg.block_size].iter().map(|&x| x as i64).collect();
            Tensor::from_slice(&slice, cfg.block_size, device)
        })
        .collect::<Result<Vec<_>, _>>()?;

    let x = Tensor::stack(&x_tensors, 0)?;
    let y = Tensor::stack(&y_tensors, 0)?;

    Ok((x, y))
}

fn main() -> Result<()> {
    let args = Args::parse();
    let device = Device::cuda_if_available(0).unwrap_or(Device::Cpu);
    let tokenizer = Tokenizer::from_static();

    let cfg = Config {
        vocab_size: tokenizer.stoi.len(),
        block_size: args.block_size,
        n_embd: args.n_embd,
        n_head: args.n_head,
        n_layer: args.n_layer,
        batch_size: args.batch_size,
    };

    let varmap = VarMap::new();
    let dtype = DType::F32;
    let vb = VarBuilder::from_varmap(&varmap, dtype, &device);
    let model = GPT::new(&cfg, vb)?;
    let params = ParamsAdamW {
        lr: args.learning_rate,
        weight_decay: 0.01,
        ..ParamsAdamW::default()
    };
    let mut opt = AdamW::new(varmap.all_vars(), params)?;

    for epoch in 1..=args.epochs {
        let samples: Vec<String> = load_file()?;
        let data: Vec<usize> = tokenizer.encode(&samples);
        println!("Data len: {}", data.len());

        let mut step_start = Instant::now();
        for step in 1..=args.steps_per_epoch {
            let (x, y) = get_batch(&data, &cfg, &device)?;
            let logits = model.forward(&x)?;
            let logits_flat = logits.reshape((logits.dims()[0] * logits.dims()[1], logits.dims()[2]))?;
            let loss = candle_nn::loss::cross_entropy(&logits_flat, &y.flatten_all()?)?;
            opt.backward_step(&loss)?;

            if step > 0 && step % 100 == 0 {
                println!("epoch {epoch} step {step} elapsed: {}ms loss: {:.4}", step_start.elapsed().as_millis(), loss.to_scalar::<f32>()?);
                step_start = Instant::now();
            }
        }
        let path = format!("model/epoch_{:03}.safetensors", epoch);
        varmap.save(&path)?;
    }

    Ok(())
}
