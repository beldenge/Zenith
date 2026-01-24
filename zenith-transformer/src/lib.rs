use std::collections::HashMap;
use std::fs;
use candle_core::{DType, IndexOp, Module, Tensor, D};
use candle_nn::{embedding, layer_norm, linear, ops, Embedding, LayerNorm, LayerNormConfig, Linear, VarBuilder};
use polars::prelude::*;
use deunicode::deunicode;
use rand::Rng;

pub const DOC_SEPARATOR: char = '|';

pub fn load_file() -> anyhow::Result<Vec<String>> {
    let data_dir = "D:/dev/Zenith/zenith-transformer/data/openwebtext-full/plain_text";

    let filenames: Vec<_> = fs::read_dir(data_dir)?
        .flatten()
        .filter(|e| e.path().is_file())
        .map(|e| e.file_name().to_string_lossy().into_owned())
        .collect();

    let random_file = rand::rng().random_range(0..filenames.len());

    let full_path = format!("{}/{}", data_dir, &filenames[random_file]);
    println!("Loading file {}", full_path);
    let df = LazyFrame::scan_parquet(PlPath::new(&full_path), ScanArgsParquet::default())?
        .select([col("text")])
        .collect()?;

    let samples: Vec<String> = df
        .column("text")?
        .str()?
        .into_iter()
        .flatten()
        .map(scrub_text)
        .map(|s| {
            // Add a document separator after each sample
            format!("{}{}", s, DOC_SEPARATOR)
        })
        .collect();

    println!("{} samples loaded from file {}", samples.len(), &filenames[random_file]);

    Ok(samples)
}

fn scrub_text(text: &str) -> String {
    let filtered: String = deunicode(text)
        .to_lowercase()
        .replace("\n", " ")
        .replace("\"", " ")
        .chars()
        .filter(|&c| c.is_alphanumeric() || c.is_whitespace() || c == '.' || c == '!' || c == '?')
        .collect();

    filtered.split_whitespace()
        .collect::<Vec<_>>()
        .join(" ")
}

fn causal_attention(q: &Tensor, k: &Tensor, v: &Tensor) -> anyhow::Result<Tensor> {
    let (b, h, t, d) = q.dims4()?;
    let scale = (d as f64).powf(-0.5);
    let att = (q.matmul(&k.transpose(3, 2)?)? * scale)?;

    // Create causal mask: positions j <= i are 1, others 0
    let left = Tensor::arange(0i64, t as i64, q.device())?.unsqueeze(1)?.expand((t, t))?;
    let right = Tensor::arange(0i64, t as i64, q.device())?.unsqueeze(0)?.expand((t, t))?;
    let mask = left.ge(&right)?.to_dtype(DType::F32)?;
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

pub struct Config {
    pub block_size: usize,
    pub vocab_size: usize,
    pub n_embd: usize,
    pub n_head: usize,
    pub n_layer: usize,
    pub batch_size: usize,
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
    fn new(vb: VarBuilder, cfg: &Config) -> anyhow::Result<Self> {
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

    fn forward(&self, x: &Tensor) -> anyhow::Result<Tensor> {
        let (b, t, c) = x.dims3()?;
        let residual = x;

        let x = self.ln1.forward(x)?;
        let qkv = x.apply(&self.att)?.reshape((b, t, 3, self.att.weight().dim(0)? / 3))?;
        let (q, k, v) = (qkv.i((.., .., 0, ..))?, qkv.i((.., .., 1, ..))?, qkv.i((.., .., 2, ..))?);
        let q = q.reshape((b, t, self.n_head, self.head_dim))?.transpose(1, 2)?.contiguous()?;
        let k = k.reshape((b, t, self.n_head, self.head_dim))?.transpose(1, 2)?.contiguous()?;
        let v = v.reshape((b, t, self.n_head, self.head_dim))?.transpose(1, 2)?.contiguous()?;
        let att = causal_attention(&q, &k, &v)?;
        let att = ops::dropout(&att.transpose(1, 2)?.reshape((b, t, c))?.apply(&self.proj)?, 0.1)?;

        let x = (residual + att)?;

        let residual = &x;
        let x = ops::dropout(&x.apply(&self.ln2)?.apply(&self.ff)?.gelu()?.apply(&self.ff_proj)?, 0.1)?;
        Ok((x + residual)?)
    }
}

pub struct GPT {
    tok_emb: Embedding,
    pos_emb: Embedding,
    blocks: Vec<Block>,
    ln_f: LayerNorm,
    head: Linear,
}

impl GPT {
    pub fn new(cfg: &Config, vb: VarBuilder) -> anyhow::Result<Self> {
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

    pub fn forward(&self, idx: &Tensor) -> anyhow::Result<Tensor> {
        let (b, t) = idx.dims2()?;
        let tok = idx.apply(&self.tok_emb)?;
        let pos = Tensor::arange(0i64, t as i64, idx.device())?
            .unsqueeze(0)?
            .expand((b, t))?
            .apply(&self.pos_emb)?;
        let mut x = (tok + pos)?;
        x = ops::dropout(&x, 0.1)?;
        for block in &self.blocks {
            x = block.forward(&x)?;
        }
        Ok(x.apply(&self.ln_f)?.apply(&self.head)?)
    }
}

pub struct Tokenizer {
    pub stoi: HashMap<char, usize>,
    pub itos: HashMap<usize, char>,
}

impl Tokenizer {
    pub fn from_static() -> Self {
        let chars: Vec<char> = vec!['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '?', '!', ' ', '|'];

        let stoi: HashMap<char, usize> = chars.iter().enumerate().map(|(i, &c)| (c, i)).collect();
        let itos: HashMap<usize, char> = chars.into_iter().enumerate().map(|(i, c)| (i, c)).collect();
        Self { stoi, itos }
    }

    pub fn encode(&self, s: &[String]) -> Vec<usize> {
        s.iter()
            .flat_map(|doc| doc.chars())
            .map(|c| self.stoi[&c])
            .collect()
    }

    pub fn decode(&self, ids: &[usize]) -> String {
        ids.iter()
            .map(|&i| self.itos[&i])
            .collect()
    }
}
