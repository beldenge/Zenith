use std::collections::HashMap;
use std::fs;
use candle_core::{DType, Device, Module, Tensor, D};
use candle_nn::{AdamW, Dropout, Init, Optimizer, ParamsAdamW, VarMap, Embedding, Linear, VarBuilder};
use polars::prelude::*;
use deunicode::deunicode;
use rand::Rng;
use candle_core::backprop::GradStore;
use anyhow::Result;

pub const DOC_SEPARATOR: char = '|';

pub fn load_file() -> Result<Vec<String>> {
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


fn create_causal_mask(batch_size: usize, seq_len: usize, device: &Device) -> candle_core::Result<Tensor> {
    // Lower triangular ones (1 where position can attend, 0 where it cannot)
    let mut tril = Tensor::tril2(seq_len, DType::U8, device)?;  // (T, T)
    tril = tril.broadcast_as((batch_size, seq_len, seq_len))?; // (B, T, T)

    // Invert: future positions become 0 → we'll fill them with -inf
    let mask = tril.eq(0)?; // 1 where future (to mask), 0 where allowed

    // Scale to large negative for numerical stability in softmax
    let neg_inf = Tensor::full(-f32::INFINITY, mask.shape(), device)?;

    // Apply: where mask==1 → -inf, else 0
    mask.where_cond(&neg_inf, &Tensor::zeros(neg_inf.shape(), DType::F32, device)?)
}

const GPT_INIT_STD: f64 = 0.02;

fn embedding_gpt(in_size: usize, out_size: usize, vb: VarBuilder) -> Result<Embedding> {
    let embeddings = vb.get_with_hints(
        (in_size, out_size),
        "weight",
        Init::Randn {
            mean: 0.,
            stdev: GPT_INIT_STD,
        },
    )?;
    Ok(Embedding::new(embeddings, out_size))
}

fn linear_gpt(in_dim: usize, out_dim: usize, bias: bool, vb: VarBuilder) -> Result<Linear> {
    let ws = vb.get_with_hints(
        (out_dim, in_dim),
        "weight",
        Init::Randn {
            mean: 0.,
            stdev: GPT_INIT_STD,
        },
    )?;
    let bs = if bias {
        Some(vb.get_with_hints(out_dim, "bias", Init::Const(0.))?)
    } else {
        None
    };
    Ok(Linear::new(ws, bs))
}

struct AttentionHead {
    q: Linear,
    k: Linear,
    v: Linear,
    dropout: Dropout,
}

struct LayerNormGpt {
    weight: Tensor,
    bias: Tensor,
    eps: f64,
}

impl LayerNormGpt {
    fn new(size: usize, eps: f64, vb: VarBuilder) -> Result<Self> {
        let weight = vb.get_with_hints(size, "weight", Init::Const(1.))?;
        let bias = vb.get_with_hints(size, "bias", Init::Const(0.))?;
        Ok(Self { weight, bias, eps })
    }

    fn forward(&self, x: &Tensor, train: bool) -> Result<Tensor> {
        if !train {
            // Forward-only fast path is ok for eval since we don't backprop.
            return Ok(candle_nn::ops::layer_norm(x, &self.weight, &self.bias, self.eps as f32)?);
        }
        let hidden_size = x.dim(D::Minus1)?;
        let mean = (x.sum_keepdim(D::Minus1)? / hidden_size as f64)?;
        let x_centered = x.broadcast_sub(&mean)?;
        let var = (x_centered.sqr()?.sum_keepdim(D::Minus1)? / hidden_size as f64)?;
        let x_normed = x_centered.broadcast_div(&(var + self.eps)?.sqrt()?)?;
        let x = x_normed.broadcast_mul(&self.weight)?;
        Ok(x.broadcast_add(&self.bias)?)
    }
}

impl AttentionHead {
    fn new(embed_dim: usize, head_dim: usize, drop_p: f32, vb: VarBuilder) -> Result<Self> {
        let q = linear_gpt(embed_dim, head_dim, false, vb.pp("q"))?;
        let k = linear_gpt(embed_dim, head_dim, false, vb.pp("k"))?;
        let v = linear_gpt(embed_dim, head_dim, false, vb.pp("v"))?;
        let dropout = Dropout::new(drop_p);

        Ok(Self {
            q,
            k,
            v,
            dropout
        })
    }

    fn forward(&self, x: &Tensor, train: bool, mask: Option<&Tensor>) -> candle_core::Result<Tensor> {
        let q = self.q.forward(x)?; // (B, T, head_dim)
        let k = self.k.forward(x)?; // (B, T, head_dim)
        let v = self.v.forward(x)?; // (B, T, head_dim)

        // Scale by head_dim (k's last dimension), not embed_dim
        let head_dim = k.dims()[2];
        let scale = 1.0 / (head_dim as f32).sqrt();
        let scale_tensor = Tensor::new(scale, x.device())?;
        // Transpose last two dims of k: (B, T, head_dim) -> (B, head_dim, T)
        let k_t = k.transpose(1, 2)?;
        let mut att = q.matmul(&k_t)?.broadcast_mul(&scale_tensor)?; // (B, T, T)

        if let Some(m) = mask {
            att = att.add(m)?;
        }

        att = candle_nn::ops::softmax(&att, candle_core::D::Minus1)?;
        att = self.dropout.forward(&att, train)?;
        let y = att.matmul(&v)?; // (B, T, C)

        Ok(y)
    }
}

// Then MultiHeadAttention just runs N heads in parallel and concatenates + projects
pub struct MultiHeadAttention {
    heads: Vec<AttentionHead>,
    proj: Linear, // final projection after concat
    dropout: Dropout,
}

impl MultiHeadAttention {
    pub fn new(embed_dim: usize, num_heads: usize, drop_p: f32, vb: VarBuilder) -> Result<Self> {
        let head_dim = embed_dim / num_heads;
        let heads: Result<Vec<_>> = (0..num_heads)
            .map(|i| AttentionHead::new(embed_dim, head_dim, drop_p, vb.pp(format!("head{}", i))))
            .collect();

        let proj = linear_gpt(head_dim * num_heads, embed_dim, true, vb.pp("out_proj"))?;
        let dropout = Dropout::new(drop_p);

        Ok(Self {
            heads: heads?,
            proj,
            dropout
        })
    }

    pub fn forward(&self, x: &Tensor, train: bool, mask: Option<&Tensor>) -> candle_core::Result<Tensor> {
        let ys: Vec<Tensor> = self.heads.iter()
            .map(|h| h.forward(x, train, mask))
            .collect::<Result<_, _>>()?;
        // contiguous() not strictly needed but apparently more efficient for CUDA
        let y = Tensor::cat(&ys, 2)?.contiguous()?;
        let proj = self.proj.forward(&y)?;
        self.dropout.forward(&proj, train)
    }
}

pub struct FeedForward {
    c_fc:   Linear,   // expand to 4× embed_dim (common choice)
    c_proj: Linear,   // project back to embed_dim
    dropout: Dropout,
}

impl FeedForward {
    pub fn new(embed_dim: usize, drop_p: f32, vb: VarBuilder) -> Result<Self> {
        let inner_dim = embed_dim * 4;  // classic expansion factor

        let c_fc = linear_gpt(embed_dim, inner_dim, true, vb.pp("c_fc"))?;
        let c_proj = linear_gpt(inner_dim, embed_dim, true, vb.pp("c_proj"))?;

        let dropout = Dropout::new(drop_p);

        Ok(Self {
            c_fc,
            c_proj,
            dropout
        })
    }

    pub fn forward(&self, x: &Tensor, train: bool) -> Result<Tensor> {
        // x → fc → ReLU → proj → dropout
        let x = self.c_fc.forward(x)?;
        let x = x.relu()?;
        let x = self.c_proj.forward(&x)?;
        let x = self.dropout.forward(&x, train)?;

        Ok(x)
    }
}

pub struct TransformerBlock {
    ln_1: LayerNormGpt,
    attn: MultiHeadAttention,
    ln_2: LayerNormGpt,
    mlp: FeedForward,
}

impl TransformerBlock {
    pub fn new(
        embed_dim: usize,
        num_heads: usize,
        drop_p: f32,
        vb: VarBuilder,
    ) -> Result<Self> {
        let ln_1 = LayerNormGpt::new(embed_dim, 1e-5, vb.pp("ln_1"))?;
        let attn = MultiHeadAttention::new(embed_dim, num_heads, drop_p, vb.pp("attn"))?;
        let ln_2 = LayerNormGpt::new(embed_dim, 1e-5, vb.pp("ln_2"))?;
        let mlp = FeedForward::new(embed_dim, drop_p, vb.pp("mlp"))?;

        Ok(Self {
            ln_1,
            attn,
            ln_2,
            mlp
        })
    }

    pub fn forward(&self, x: &Tensor, train: bool, mask: Option<&Tensor>) -> Result<Tensor> {
        // Attention path
        let residual = x.clone();
        let x = self.ln_1.forward(x, train)?;
        let x = self.attn.forward(&x, train, mask)?;
        let x = (x + residual)?;

        // MLP path
        let residual = x.clone();
        let x = self.ln_2.forward(&x, train)?;
        let x = self.mlp.forward(&x, train)?;
        let x = (x + residual)?;

        Ok(x)
    }
}

pub struct GPT {
    optimizer: AdamW,
    tok_embed: Embedding,
    pos_embed: Embedding,
    dropout: Dropout,
    blocks: Vec<TransformerBlock>,
    ln_f: LayerNormGpt,
    lm_head: Linear,
}

impl GPT {
    pub fn new(vocab_size: usize, block_size: usize, embed_dim: usize, num_heads: usize, layers: usize, learning_rate: f64, drop_p: f32, varmap: &VarMap, device: &Device) -> Result<Self> {
        let vb = VarBuilder::from_varmap(&varmap, DType::F32, device);
        let params = ParamsAdamW {
            lr: learning_rate,
            weight_decay: 0.01,
            ..ParamsAdamW::default()
        };

        let tok_embed = embedding_gpt(vocab_size, embed_dim, vb.pp("tok_embed"))?;
        let pos_embed = embedding_gpt(block_size, embed_dim, vb.pp("pos_embed"))?;
        let dropout = Dropout::new(drop_p);

        let mut blocks = Vec::new();
        for i in 0..layers {
            let block = TransformerBlock::new(
                embed_dim,
                num_heads,
                drop_p,
                vb.pp(format!("blocks.{}", i)),
            )?;
            blocks.push(block);
        }

        let ln_f = LayerNormGpt::new(embed_dim, 1e-5, vb.pp("ln_f"))?;
        let lm_head = linear_gpt(embed_dim, vocab_size, true, vb.pp("lm_head"))?;
        let optimizer = AdamW::new(varmap.all_vars(), params)?;

        Ok(Self {
            optimizer,
            tok_embed,
            pos_embed,
            dropout,
            blocks,
            ln_f,
            lm_head
        })
    }

    pub fn forward(&self, inputs: &Tensor, train: bool) -> Result<Tensor> {
        let (b, t) = inputs.dims2()?;
        let tok_embed = self.tok_embed.forward(inputs)?; // (B, T, C)
        let pos_embed = self.pos_embed.forward(&Tensor::arange(0u32, t as u32, inputs.device())?)?; // (T, C)
        let mut x = tok_embed.broadcast_add(&pos_embed)?;  // (B, T, C)
        x = self.dropout.forward(&x, train)?;
        let mask = create_causal_mask(b, t, inputs.device())?; // (T, T)

        for block in &self.blocks {
            x = block.forward(&x, train, Some(&mask))?;
        }

        x = self.ln_f.forward(&x, train)?;
        let logits = self.lm_head.forward(&x)?;

        Ok(logits)
    }

    pub fn step(&mut self, grads: GradStore) -> Result<()> {
        self.optimizer.step(&grads)?;
        Ok(())
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

    pub fn from_dataset(dataset: &str) -> Self {
        let mut unique_chars = dataset.chars()
            .collect::<Vec<_>>();
        unique_chars.sort();
        unique_chars.dedup();

        let stoi: HashMap<char, usize> = unique_chars.iter()
            .enumerate()
            .map(|(i, &c)| (c, i))
            .collect();
        let itos: HashMap<usize, char> = unique_chars.into_iter()
            .enumerate()
            .map(|(i, c)| (i, c))
            .collect();

        Self {
            stoi,
            itos,
        }
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
