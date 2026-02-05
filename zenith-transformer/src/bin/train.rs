use std::fmt::Debug;
use zenith_transformer::{load_file, GPT, Tokenizer};
use anyhow::{Result};
use candle_core::{Device, Tensor, DType};
use candle_nn::{AdamW, Optimizer, ParamsAdamW, VarBuilder, VarMap};
use clap::Parser;
use rand::prelude::*;
use std::time::Instant;
use std::collections::HashMap;
use candle_nn::loss::cross_entropy;
use candle_nn::ops::softmax;
use rand::distr::weighted::WeightedIndex;
use rand::distr::Distribution;
use rand::Rng;
use log::LevelFilter;

#[derive(Parser)]
struct Args {
    #[arg(short, long, action = clap::ArgAction::SetTrue)]
    debug: bool,
}

fn main() -> Result<()> {
    let args = Args::parse();
    let level = if args.debug {
        LevelFilter::Debug
    } else {
        LevelFilter::Info
    };
    env_logger::builder()
        .filter_level(level)
        .init();
    let device = Device::cuda_if_available(0).unwrap_or(Device::Cpu);
    let tokenizer = Tokenizer::from_static();
    let epochs = 5;
    let steps_per_epoch = 1000;
    let batch_size: usize = 32;
    let block_size: usize = 256;
    let iterations = 5000;
    let learning_rate = 3e-4;
    let layers = 6;
    let num_heads = 6;
    let embed_dim = 384;
    let eval_interval = 100;
    let drop_p = 0.2;

    // let tokenizer = Tokenizer::new(&samples);
    let vocab_size = tokenizer.stoi.len();
    let varmap = VarMap::new();
    let mut gpt = GPT::new(vocab_size, block_size, embed_dim, num_heads, layers, learning_rate, drop_p, &varmap, &device)?;

    for epoch in 1..=epochs {
        let samples: Vec<String> = load_file()?;
        let all_encoded = tokenizer.encode(&samples);
        let n1 = (0.9 * samples.len() as f64) as usize;
        let training_set = all_encoded[0..n1].to_vec();
        let val_set = all_encoded[n1..all_encoded.len()].to_vec();
        let mut data_sets: HashMap<String, &Vec<usize>> = HashMap::new();
        data_sets.insert("train".to_string(), &training_set);
        data_sets.insert("val".to_string(), &val_set);

        println!("Data len: {}", all_encoded.len());

        for step in 1..=steps_per_epoch {
            if step % eval_interval == 0 || step == iterations {
                let losses = estimate_loss(
                    block_size,
                    batch_size,
                    vocab_size,
                    &gpt,
                    &device,
                    &data_sets
                )?;

                log::info!("Step {}: train loss: {:?}, val loss: {:?}", step, losses.get("train"), losses.get("val"));
            }

            let (training_inputs, training_outputs) = get_batch(&training_set, block_size, batch_size, &device)?;
            let logits = gpt.forward(&training_inputs, true)?;

            // Reshape needed to convert (B, T, C) -> (B * T, C)
            let reshaped_logits = &logits.reshape((logits.dims()[0] * logits.dims()[1], vocab_size))?;

            // Compute loss, expects (B * T, C)
            let loss = cross_entropy(&reshaped_logits, &training_outputs)?;

            // Backward pass
            let grads = loss.backward()?;

            gpt.step(grads)?;
        }
        let path = format!("model/epoch_{:03}.safetensors", epoch);
        varmap.save(&path)?;
    }

    let generated = generate_samples(&gpt, &tokenizer, block_size, 0.8, "|", &device)?;
    println!("{}", generated);

    Ok(())
}

fn get_batch(data_set: &Vec<usize>, block_size: usize, batch_size: usize, device: &Device) -> Result<(Tensor, Tensor)> {
    let mut rng = rand::rng();

    let starts: Vec<usize> = (0..batch_size)
        .map(|_| rng.random_range(0..(data_set.len() - block_size - 1)))
        .collect();

    let inputs_vec = starts.iter()
        .flat_map(|&i| data_set[i..i + block_size].iter().map(|&x| x as u32))
        .collect::<Vec<u32>>();

    let outputs_vec = starts.iter()
        .flat_map(|&i| data_set[i + 1..i + block_size + 1].iter().map(|&x| x as u32))
        .collect::<Vec<u32>>();

    let inputs_tensor = Tensor::from_vec(inputs_vec, (batch_size, block_size), device)?;
    let outputs_tensor = Tensor::from_vec(outputs_vec, batch_size * block_size, device)?;

    Ok((inputs_tensor, outputs_tensor))
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

fn estimate_loss(
    block_size: usize,
    batch_size: usize,
    vocab_size: usize,
    model: &GPT,
    device: &Device,
    data_sets: &HashMap<String, &Vec<usize>>
) -> Result<HashMap<String, f32>> {
    let eval_iters = 20;
    let mut out = HashMap::new();

    for split in ["train", "val"] {
        let mut losses: Vec<f32> = Vec::with_capacity(eval_iters);

        for _ in 0..eval_iters {
            let (xb, yb) = get_batch(data_sets.get(split).unwrap(), block_size, batch_size, device)?;

            let logits = model.forward(&xb, false)?;

            // Reshape needed to convert (B, T, C) -> (B * T, C)
            let reshaped_logits = &logits.reshape((logits.dims()[0] * logits.dims()[1], vocab_size))?;

            // Compute loss, expects (B * T, C)
            let loss = cross_entropy(&reshaped_logits, &yb)?;
            losses.push(loss.to_scalar::<f32>()?);
        }

        let avg_loss = losses.iter().sum::<f32>() / eval_iters as f32;
        out.insert(split.to_string(), avg_loss);
    }

    Ok(out)
}