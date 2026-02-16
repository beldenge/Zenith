use std::sync::Arc;
use std::sync::atomic::{AtomicU64, Ordering};
use std::time::Instant;

use anyhow::{Context, Result};
use axum::extract::State;
use axum::http::StatusCode;
use axum::response::{IntoResponse, Response};
use axum::routing::{get, post};
use axum::{Json, Router};
use candle_core::{D, Device, IndexOp, Tensor};
use candle_nn::VarMap;
use clap::Parser;
use deunicode::deunicode;
use log::{debug, error, warn};
use serde::{Deserialize, Serialize};
use tokio::sync::Mutex;
use zenith_transformer::{GPT, Tokenizer};

const CHUNK_SIZE: usize = 128;
const BLOCK_SIZE: usize = 256;
const LEARNING_RATE: f64 = 3e-4;
const LAYERS: usize = 6;
const NUM_HEADS: usize = 6;
const EMBED_DIM: usize = 384;
const DROP_P: f32 = 0.2;

#[derive(Parser, Debug)]
struct Args {
    #[arg(long, default_value = "model/model.safetensors")]
    model_path: String,
    #[arg(long, default_value = "127.0.0.1")]
    host: String,
    #[arg(long, default_value_t = 8080)]
    port: u16,
    #[arg(long, action = clap::ArgAction::SetTrue)]
    debug: bool,
}

#[derive(Clone)]
struct AppState {
    model: Arc<Mutex<InferenceModel>>,
    request_counter: Arc<AtomicU64>,
}

struct InferenceModel {
    // Keep varmap in-memory so backing tensors are never dropped while serving requests.
    _varmap: VarMap,
    gpt: GPT,
    tokenizer: Tokenizer,
    device: Device,
}

#[derive(Debug, Deserialize)]
struct RestServiceEvaluationRequest {
    sequences: Vec<String>,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
struct EvaluationProbability {
    probability: f64,
    log_probability: f32,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
struct RestServiceEvaluation {
    probabilities: Vec<EvaluationProbability>,
    score: f32,
}

#[derive(Debug, Serialize)]
struct HealthResponse {
    status: &'static str,
}

#[tokio::main]
async fn main() -> Result<()> {
    let args = Args::parse();
    let default_level = if args.debug { "debug" } else { "info" };
    env_logger::Builder::from_env(env_logger::Env::default().default_filter_or(default_level))
        .init();

    let addr = format!("{}:{}", args.host, args.port);
    let model = load_model(&args.model_path)?;

    let app = Router::new()
        .route("/", post(evaluate))
        .route("/evaluate", post(evaluate))
        .route("/health", get(health))
        .with_state(AppState {
            model: Arc::new(Mutex::new(model)),
            request_counter: Arc::new(AtomicU64::new(1)),
        });

    let listener = tokio::net::TcpListener::bind(&addr)
        .await
        .with_context(|| format!("Failed to bind to {}", addr))?;

    println!("Inference service listening on http://{}", addr);
    println!("Loaded model from {}", args.model_path);

    axum::serve(listener, app)
        .await
        .context("Axum server failed")
}

fn load_model(model_path: &str) -> Result<InferenceModel> {
    let device = Device::cuda_if_available(0).unwrap_or(Device::Cpu);
    let tokenizer = Tokenizer::from_static();
    let vocab_size = tokenizer.stoi.len();
    let mut varmap = VarMap::new();
    let gpt = GPT::new(
        vocab_size,
        BLOCK_SIZE,
        EMBED_DIM,
        NUM_HEADS,
        LAYERS,
        LEARNING_RATE,
        DROP_P,
        &varmap,
        &device,
    )?;

    varmap
        .load(model_path)
        .with_context(|| format!("Failed to load model from {}", model_path))?;

    Ok(InferenceModel {
        _varmap: varmap,
        gpt,
        tokenizer,
        device,
    })
}

async fn health() -> Json<HealthResponse> {
    Json(HealthResponse { status: "ok" })
}

async fn evaluate(
    State(state): State<AppState>,
    Json(request): Json<RestServiceEvaluationRequest>,
) -> Result<Json<RestServiceEvaluation>, ApiError> {
    let request_id = state.request_counter.fetch_add(1, Ordering::Relaxed);
    let started_at = Instant::now();
    let sequence_count = request.sequences.len();
    let total_char_count = request
        .sequences
        .iter()
        .map(|sequence| sequence.chars().count())
        .sum::<usize>();

    debug!(
        "request_id={} started: sequences={}, total_chars={}",
        request_id, sequence_count, total_char_count
    );

    if request.sequences.is_empty() {
        warn!("request_id={} rejected: empty sequences list", request_id);
        return Err(ApiError::BadRequest(
            "`sequences` must contain at least one item.".to_string(),
        ));
    }

    let model = state.model.lock().await;
    let mut probabilities = Vec::with_capacity(request.sequences.len());

    for sequence in &request.sequences {
        let normalized = normalize_sequence(sequence, &model.tokenizer);
        let token_log_probabilities =
            token_log_probabilities(&model.gpt, &model.tokenizer, &[normalized], &model.device)?;
        for log_probability in token_log_probabilities {
            probabilities.push(EvaluationProbability {
                probability: (log_probability as f64).exp(),
                log_probability,
            });
        }
    }

    let score = sum_log_probabilities(&probabilities);

    debug!(
        "request_id={} completed: probabilities={}, score={}, elapsed_ms={}",
        request_id,
        probabilities.len(),
        score,
        started_at.elapsed().as_millis()
    );

    Ok(Json(RestServiceEvaluation {
        probabilities,
        score,
    }))
}

fn normalize_sequence(sequence: &str, tokenizer: &Tokenizer) -> String {
    let mut normalized = String::new();
    let mut previous_space = true;

    for ch in deunicode(sequence).to_lowercase().chars() {
        let candidate = if ch.is_whitespace() { ' ' } else { ch };
        if candidate == ' ' {
            if !previous_space && tokenizer.stoi.contains_key(&' ') {
                normalized.push(' ');
                previous_space = true;
            }
            continue;
        }

        if tokenizer.stoi.contains_key(&candidate) {
            normalized.push(candidate);
            previous_space = false;
        }
    }

    normalized.trim().to_string()
}

fn encode_supported(tokenizer: &Tokenizer, text: &[String]) -> Vec<i64> {
    text.iter()
        .flat_map(|doc| doc.chars())
        .filter_map(|ch| tokenizer.stoi.get(&ch).copied())
        .map(|id| id as i64)
        .collect()
}

fn token_log_probabilities(
    gpt: &GPT,
    tokenizer: &Tokenizer,
    text: &[String],
    device: &Device,
) -> Result<Vec<f32>> {
    let ids_full = encode_supported(tokenizer, text);
    let mut log_probabilities = Vec::new();
    let mut start = 0usize;

    while start < ids_full.len() {
        let end = (start + CHUNK_SIZE).min(ids_full.len());
        let chunk = &ids_full[start..end];
        let chunk_len = chunk.len();
        if chunk_len < 2 {
            break;
        }

        let t = Tensor::from_vec(chunk.to_vec(), (1, chunk_len), device)?;
        let logits = gpt.forward(&t, false)?;
        let probs = candle_nn::ops::softmax(&logits, D::Minus1)?;
        let log_probs = probs.log()?;
        let targets = t.i((0, 1..))?;
        let gathered = log_probs
            .i((0, ..log_probs.dim(1)? - 1))?
            .gather(&targets.unsqueeze(1)?, 1)?
            .squeeze(1)?;
        log_probabilities.extend(gathered.to_vec1::<f32>()?);

        if end == ids_full.len() {
            break;
        }

        // Carry one token forward so boundary predictions keep continuity.
        start = end - 1;
    }

    Ok(log_probabilities)
}

fn sum_log_probabilities(probabilities: &[EvaluationProbability]) -> f32 {
    probabilities
        .iter()
        .map(|item| item.log_probability)
        .sum::<f32>()
}

enum ApiError {
    BadRequest(String),
    Internal(anyhow::Error),
}

impl From<anyhow::Error> for ApiError {
    fn from(value: anyhow::Error) -> Self {
        Self::Internal(value)
    }
}

impl IntoResponse for ApiError {
    fn into_response(self) -> Response {
        match self {
            Self::BadRequest(message) => {
                warn!("request failed with bad request: {}", message);
                (StatusCode::BAD_REQUEST, message).into_response()
            }
            Self::Internal(error) => {
                error!("request failed with internal error: {}", error);
                (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    format!("Inference failed: {error}"),
                )
                    .into_response()
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn given_mixed_case_and_symbols_when_normalizing_then_only_supported_tokens_remain() {
        let tokenizer = Tokenizer::from_static();

        let normalized = normalize_sequence("  HELL0,\nW0RLD!!! @#$  ", &tokenizer);

        assert_eq!("hellwrld", normalized);
    }

    #[test]
    fn given_multiple_spaces_when_normalizing_then_spaces_are_collapsed() {
        let tokenizer = Tokenizer::from_static();

        let normalized = normalize_sequence("Alpha   beta\t\tgamma", &tokenizer);

        assert_eq!("alphabetagamma", normalized);
    }

    #[test]
    fn given_probabilities_when_calculating_sum_then_returns_expected_score() {
        let probabilities = vec![
            EvaluationProbability {
                probability: 0.5,
                log_probability: -1.0,
            },
            EvaluationProbability {
                probability: 0.25,
                log_probability: -3.0,
            },
        ];

        let score = sum_log_probabilities(&probabilities);

        assert_eq!(-4.0, score);
    }
}
