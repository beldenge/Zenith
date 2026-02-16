This project trains on the following version of the openwebtext dataset:

https://huggingface.co/datasets/Skylion007/openwebtext

The easiest way to download it is through the python huggingface cli:

`pip install -U huggingface_hub[cli]`

`hf download Skylion007/openwebtext --repo-type dataset --local-dir ./data/openwebtext-full`

To run on GPU (Windows 11):
- Install the Visual Studio C++ Build Tools
- Install the latest CUDA Toolkit

## REST inference service (Axum)

This module now includes an Axum-based inference service binary:

```bash
cargo run --release --bin rest_service -- --model-path model/model.safetensors --host 127.0.0.1 --port 8080
```

CPU-only fallback (if CUDA/MSVC tooling is unavailable):

```bash
cargo run --release --no-default-features --bin rest_service -- --model-path model/model.safetensors --host 127.0.0.1 --port 8080
```

Endpoints:
- `POST /evaluate` (also available at `POST /`)
- `GET /health`

`probabilities` returns one entry per predicted token (input length minus 1 for each sequence), and `score` is the sum of all returned `logProbability` values.

Request body:

```json
{
  "sequences": [
    "abcd"
  ]
}
```

Response body:

```json
{
  "probabilities": [
    {
      "probability": 0.01982971967466584,
      "logProbability": -3.9205735
    },
    {
      "probability": 0.005385773951775118,
      "logProbability": -5.2239943
    },
    {
      "probability": 0.012800254883708603,
      "logProbability": -4.35829
    }
  ],
  "score": -13.502857
}
```

To call this from `RestServicePlaintextEvaluator`, set `restServiceUrl` to:
- `http://127.0.0.1:8080/evaluate`
