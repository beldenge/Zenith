# Zenith

A cipher solver for homophonic substitution ciphers (like the Zodiac ciphers).

**Features:**
- Free and open-source (GPL v3)
- Cross-platform (Windows, macOS, Linux)
- Multiple interfaces: Web UI, CLI, GraphQL API
- Extensible transformers and fitness functions
- Pre-trained English language model included

---

## Quick Start (Web UI)

1. Install [Java 25+](https://aws.amazon.com/corretto/)
2. Download the latest release
3. Run the appropriate script:
   - **macOS/Linux:** `./run-ui-macos-linux.sh`
   - **Windows:** `run-ui-windows.bat`
4. Open http://localhost:8080 (should open automatically)

To stop: `Ctrl+C` in the terminal.

**Requirements:** ~500 MB disk space (includes language model)

---

## Choose Your Interface

| Interface | Use Case | Module |
|-----------|----------|--------|
| **Web UI** | Interactive solving with visualization | [zenith-ui](zenith-ui/README.md) |
| **CLI** | Batch processing, scripting, automation | [zenith-inference](zenith-inference/README.md) |
| **GraphQL API** | Integration with other tools | [zenith-api](zenith-api/README.md) |

---

## How It Works

Zenith uses statistical language models to score potential solutions and optimization algorithms to search for the best decryption key.

1. **Language Model**: 5-gram Markov model trained on English text corpora
2. **Optimizer**: Simulated annealing (recommended) or genetic algorithm (experimental)
3. **Scoring**: N-gram probability combined with index of coincidence

For technical details, see [zenith-inference](zenith-inference/README.md).

---

## Performance

Solving the Zodiac 408 cipher with simulated annealing:

| Iterations | Time/Epoch | Success Rate |
|------------|------------|--------------|
| 2,500 | ~250ms | 74.5% |
| 5,000 | ~500ms | 89.0% |
| 10,000 | ~1s | 95.5% |
| 20,000 | ~2s | 98.9% |

Default: 5,000 iterations (good balance of speed and accuracy)

---

## Project Structure

```
zenith/
├── zenith-ui/                 # Angular web interface
├── zenith-api/                # GraphQL API (Spring Boot)
├── zenith-inference/          # Core solver engine + CLI
├── zenith-genetic-algorithm/  # GA framework (used by inference)
├── zenith-language-model/     # Language model builder
├── zenith-roulette/           # Shared utilities
├── zenith-mutation-search/    # Experimental mutation detection
├── zenith-package/            # Distribution packaging
└── zenith-transformer/        # Rust utilities (WIP)
```

See [AGENTS.md](AGENTS.md) for architecture details and development guidelines.

---

## Building from Source

```bash
# Prerequisites: Java 25+, Maven 3.6+
mvn clean install
```

---

## Configuration

| What | Where | Details |
|------|-------|---------|
| Cipher files | `ciphers/*.json` | [zenith-inference](zenith-inference/README.md#1-cipher-files) |
| Solver settings | `config/zenith.json` | [zenith-inference](zenith-inference/README.md#2-solver-configuration-configzenithjson) |
| Runtime properties | `application.properties` | [zenith-inference](zenith-inference/README.md#3-application-properties-applicationproperties) |

---

## Contributing

Fork the repository and submit pull requests. Priority areas:

- **Optimization algorithms**: Improve success rate beyond 99%
- **Language models**: Better scoring for edge cases
- **Test coverage**: Unit and integration tests

See [AGENTS.md](AGENTS.md) for coding conventions and architecture guidelines.

---

## FAQ

**Why "Zenith"?**
In astronomy, the zenith is the highest point in the sky. Since this project uses hill-climbing to find optimal solutions, the name fits. Also, the project was motivated by the Zodiac ciphers, connecting to astrology.

**Is this free to use?**
Yes. GPL v3 license - free as in speech and beer. You can use and modify it, but distributions must include source code.

**Questions?**
Open an issue on GitHub.
