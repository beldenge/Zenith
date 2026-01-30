# Zenith Inference Module

The core inference engine for solving homophonic substitution ciphers. Uses language models and optimization algorithms to find the most likely plaintext for a given ciphertext.

---

## Quick Start

1. Download and install Java 25 or later: [Amazon Corretto](https://aws.amazon.com/corretto/)
2. Download `zenith-inference-2026.1.1-SNAPSHOT-exec.jar`
3. Run:
   ```bash
   java -XX:+UseParallelGC -XX:ParallelGCThreads=2 -Xms2G -Xmx2G -XX:MaxMetaspaceSize=512M \
        -jar zenith-inference-2026.1.1-SNAPSHOT-exec.jar
   ```

**Note:** Use the `-exec.jar` file, not the plain `.jar`. This module serves as both a standalone application and a library dependency.

---

## Architecture Overview

The inference engine has four customizable layers:

| Layer | Purpose | Options |
|-------|---------|---------|
| **Optimizer** | Search algorithm | `SimulatedAnnealing` (recommended), `GeneticAlgorithm` (experimental) |
| **Fitness Function** | Score solutions | Multiple n-gram + statistical evaluators |
| **Ciphertext Transformers** | Pre-process cipher | Rotations, transpositions, period shifts, etc. |
| **Plaintext Transformers** | Post-process plaintext | Vigenere, Four Square, One Time Pad |

---

## Optimizers

| Optimizer | Status | Description |
|-----------|--------|-------------|
| `SimulatedAnnealing` | **Recommended** | Hill climbing with random restarts and annealing schedule |
| `GeneticAlgorithm` | Experimental | Parallel population-based search with speciation |

---

## Fitness Functions (PlaintextEvaluators)

| Name | Type | Description |
|------|------|-------------|
| `NgramAndIndexOfCoincidence` | Single-objective | N-gram log probability penalized by index of coincidence |
| `NgramAndIndexOfCoincidenceMultiObjective` | Multi-objective | Separate n-gram and IoC objectives for Pareto optimization |
| `NgramAndChiSquared` | Single-objective | N-gram probability with chi-squared letter frequency penalty |
| `NgramAndChiSquaredMultiObjective` | Multi-objective | Separate n-gram and chi-squared objectives |
| `NgramAndEntropy` | Single-objective | N-gram probability with entropy penalty |
| `NgramAndEntropyMultiObjective` | Multi-objective | Separate n-gram and entropy objectives |
| `RestService` | External | Delegate scoring to external REST service (configure with `evaluation.rest-service.url`) |

---

## Configuration

### 1. Cipher Files

Place cipher JSON files in a `ciphers/` subdirectory. Example structure:

```json
{
  "name": "zodiac408",
  "rows": 24,
  "columns": 17,
  "ciphertext": ["symbol1", "symbol2", ...],
  "knownSolution": "optional plaintext for validation"
}
```

Built-in ciphers: `zodiac408`, `zodiac340`

### 2. Solver Configuration (`config/zenith.json`)

```json
{
   "selectedCipher" : "zodiac340-transformed",
   "epochs": 10,
   "cipherConfigurations": [],
   "selectedOptimizer": {
      "name": "SimulatedAnnealing"
   },
   "selectedFitnessFunction": {
      "name": "NgramAndIndexOfCoincidence"
   },
   "simulatedAnnealingConfiguration": {
      "samplerIterations": 5000,
      "annealingTemperatureMin": 0.006,
      "annealingTemperatureMax": 0.012
   },
   "geneticAlgorithmConfiguration": {
      "populationSize": 1000,
      "numberOfGenerations": 4,
      "elitism": 0,
      "populationName": "StandardPopulation",
      "latticeRows": 1,
      "latticeColumns": 1000,
      "latticeWrapAround": false,
      "latticeRadius": 1,
      "breederName": "ProbabilisticCipherKeyBreeder",
      "crossoverOperatorName": "UniformCrossoverOperator",
      "mutationOperatorName": "PointMutationOperator",
      "mutationRate": 0.01,
      "maxMutationsPerIndividual": 5,
      "selectorName": "TournamentSelector",
      "tournamentSelectorAccuracy": 0.9,
      "tournamentSize": 5,
      "minPopulations": 64,
      "speciationEvents": 1,
      "speciationFactor": 2,
      "extinctionCycles": 100,
      "speciationOperatorName": "FitnessSpeciationOperator"
   }
}
```

#### Simulated Annealing Parameters

| Parameter | Description |
|-----------|-------------|
| `samplerIterations` | Iterations per epoch |
| `annealingTemperatureMin` | Final temperature (lower = more greedy) |
| `annealingTemperatureMax` | Initial temperature (higher = more exploration) |

#### Genetic Algorithm Parameters

| Parameter | Description |
|-----------|-------------|
| `populationSize` | Individuals per population |
| `numberOfGenerations` | Generations per evolution cycle |
| `elitism` | Top individuals preserved each generation (**set > 0!**) |
| `breederName` | `RandomCipherKeyBreeder`, `ProbabilisticCipherKeyBreeder`, or `BiasedCipherKeyBreeder` |
| `mutationRate` | Probability each gene mutates (0.0-1.0) |
| `minPopulations` | Parallel populations for divergent search |
| `extinctionCycles` | Number of speciation/extinction rounds |

See [zenith-genetic-algorithm README](../zenith-genetic-algorithm/README.md) for full parameter documentation.

### 3. Application Properties (`application.properties`)

| Property | Default | Description |
|----------|---------|-------------|
| `task-executor.pool-size` | 1 | Thread pool size for parallel operations |
| `task-executor.queue-capacity` | 1000000 | Task queue capacity |
| `language-model.filename` | `zenith-model.csv` | Language model CSV file |
| `language-model.archive-filename` | `zenith-model.zip` | Fallback archive if CSV not found |
| `language-model.cache.filename` | `zenith-model.array.bin` | Binary cache for faster startup |
| `language-model.max-ngrams-to-keep` | 3000000 | Top n-grams to retain (sorted by frequency) |
| `markov.letter.order` | 5 | N-gram size for Markov model |
| `application.configuration.file-path` | `./config` | Path to zenith.json |
| `genetic-algorithm.calculate-entropy` | false | Track population entropy (slower) |

---

## Scoring Algorithm

The default `NgramAndIndexOfCoincidence` evaluator works as follows:

1. **N-gram probability**: Use a 5-gram Markov model to compute log probability of the plaintext. Unknown n-grams receive a small default probability.

2. **Index of Coincidence (IoC)**: Measure how much the letter distribution resembles natural English. IoC helps escape local optima where n-gram scores plateau.

3. **Combined score**: `score = (avg_log_probability) × IoC^(1/6)`

The sixth root of IoC provides balanced penalization without dominating the n-gram signal.

---

## Ciphertext Transformers

Transformers modify the ciphertext before solving. Useful for ciphers with additional encoding layers.

### Row/Column Operations

| Transformer | Description |
|-------------|-------------|
| `RemoveFirstRow` | Remove first row |
| `RemoveLastRow` | Remove last row (common for padding) |
| `RemoveFirstColumn` | Remove first column |
| `RemoveLastColumn` | Remove last column |
| `RemoveMiddleColumn` | Remove middle column (odd column count only) |

### Quadrants

| Transformer | Description |
|-------------|-------------|
| `UpperLeftQuadrant` | Extract upper-left quarter |
| `UpperRightQuadrant` | Extract upper-right quarter |
| `LowerLeftQuadrant` | Extract lower-left quarter |
| `LowerRightQuadrant` | Extract lower-right quarter |

### Rotations and Flips

| Transformer | Description |
|-------------|-------------|
| `RotateClockwise` | Rotate 90° clockwise |
| `RotateCounterClockwise` | Rotate 90° counter-clockwise |
| `FlipHorizontally` | Mirror horizontally |
| `FlipVertically` | Mirror vertically |
| `Reverse` | Reverse symbol order (supports range) |

### Transposition

| Transformer | Syntax | Description |
|-------------|--------|-------------|
| `Transposition` | `Transposition(keyword)` | Apply columnar transposition |
| `UnwrapTransposition` | `UnwrapTransposition(keyword)` | Reverse columnar transposition |
| `Period` | `Period(n)` | Period-n rearrangement |
| `UnwrapPeriod` | `UnwrapPeriod(n)` | Reverse period-n |

### Traversal Patterns

| Transformer | Description |
|-------------|-------------|
| `TopLeftDiagonal` | Read diagonally from top-left |
| `ZPattern` | Read in Z-pattern (two rows at a time) |
| `NDownMAcross` | Read n-down, m-across with wrapping |

### Character Operations

| Transformer | Description |
|-------------|-------------|
| `RemoveSymbol` | Remove specific symbol from ciphertext |
| `LockCharacters` | Prevent characters in range from further transformation |
| `ShiftCharactersLeft` | Shift range left with wrap |
| `ShiftCharactersRight` | Shift range right with wrap |

---

## Plaintext Transformers

Transformers applied to plaintext after decryption. Used when the cipher combines substitution with another cipher.

| Transformer | Description |
|-------------|-------------|
| `Vigenere` | Apply Vigenere cipher with key |
| `UnwrapVigenere` | Reverse Vigenere |
| `FourSquare` | Apply Four Square cipher |
| `UnwrapFourSquare` | Reverse Four Square |
| `OneTimePad` | Apply OTP with modular addition |
| `UnwrapOneTimePad` | Reverse OTP |

---

## Extending Zenith

### Custom Ciphertext Transformer

Implement `CipherTransformer`:

```java
@Component
public class MyTransformer implements CipherTransformer {
    @Override
    public Cipher transform(Cipher cipher) {
        // Return modified cipher
    }

    @Override
    public CipherTransformer getInstance(Map<String, Object> data) {
        return new MyTransformer(data);
    }

    @Override
    public FormlyForm getForm() { return null; }  // Optional: UI form config

    @Override
    public int getOrder() { return 100; }  // Display order in UI

    @Override
    public String getHelpText() { return "Description"; }
}
```

### Custom Plaintext Transformer

Implement `PlaintextTransformer`:

```java
@Component
public class MyPlaintextTransformer implements PlaintextTransformer {
    @Override
    public String transform(String plaintext) {
        // Return modified plaintext
    }

    @Override
    public PlaintextTransformer getInstance(Map<String, Object> data) {
        return new MyPlaintextTransformer(data);
    }

    @Override
    public FormlyForm getForm() { return null; }

    @Override
    public int getOrder() { return 100; }

    @Override
    public String getHelpText() { return "Description"; }
}
```

### Custom Fitness Function

Implement `PlaintextEvaluator`:

```java
@Component
public class MyEvaluator implements PlaintextEvaluator {
    @Override
    public SolutionScore evaluate(Map<String, Object> precomputed, Cipher cipher,
                                   CipherSolution solution, String solutionString,
                                   String ciphertextKey) {
        // Return SolutionScore with Fitness array
    }

    @Override
    public Map<String, Object> getPrecomputedCounterweightData(Cipher cipher) {
        return Collections.emptyMap();  // Precompute data once per cipher
    }

    @Override
    public PlaintextEvaluator getInstance(Map<String, Object> data) {
        return new MyEvaluator(data);
    }

    @Override
    public FormlyForm getForm() { return null; }

    @Override
    public int getOrder() { return 100; }

    @Override
    public String getHelpText() { return "Description"; }
}
```

---

## Troubleshooting

### Solver not finding good solutions
- Increase `samplerIterations` (simulated annealing)
- Try different `annealingTemperatureMin`/`Max` values
- Verify ciphertext transformers are correct for your cipher

### Out of memory
- Reduce `language-model.max-ngrams-to-keep`
- Increase heap size: `-Xmx4G`
