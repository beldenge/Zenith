# Zenith Genetic Algorithm Module

A reusable genetic algorithm (GA) framework for Java. Packaged as a standalone module usable outside the Zenith application stack. Spring-friendly (component scanning, DI) but core types are plain Java.

**Features:**
- Single-objective and multi-objective fitness
- Concurrent evaluation, selection, crossover, and mutation
- Multiple population topologies (standard list, 2D lattice)
- Divergent GA mode with speciation and extinction for parallel search

---

## Quick Start

1. Implement your domain types: `Gene`, `Chromosome`, `Breeder`, `GeneDao`, `FitnessEvaluator`
2. Configure a `GeneticAlgorithmStrategy` with your operators
3. Call `standardGeneticAlgorithm.evolve(strategy)` or `divergentGeneticAlgorithm.evolve(strategy)`

```java
GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
    .taskExecutor(taskExecutor)           // Required: Spring TaskExecutor for concurrency
    .population(population)
    .breeder(breeder)
    .fitnessEvaluator(fitnessEvaluator)
    .crossoverOperator(crossoverOperator)
    .mutationOperator(mutationOperator)
    .selector(selector)
    .populationSize(1000)
    .numberOfGenerations(50)
    .elitism(10)                          // Keep top 10 individuals each generation
    .mutationRate(0.05)
    .build();

population.init(strategy);  // Initialize population with strategy
standardGeneticAlgorithm.evolve(strategy);
```

For a complete working example, see `zenith-inference` module:
- `com.ciphertool.zenith.inference.optimizer.GeneticAlgorithmSolutionOptimizer`
- `com.ciphertool.zenith.inference.genetic.*`

---

## Core Concepts

| Concept | Description |
|---------|-------------|
| **Gene** | Smallest unit of genetic information. Must implement `clone()`. |
| **Chromosome\<T\>** | Map of keys (type T) to genes representing a solution candidate. Must implement `clone()`. |
| **Genome** | Wrapper for one or more chromosomes plus fitness values. Unit of selection/crossover. |
| **FitnessEvaluator** | Scores a genome, returning one or more `Fitness` values (single or multi-objective). |
| **Breeder** | Creates random genomes for initial population seeding. |
| **GeneDao** | Supplies random genes for mutation operators. Must be a Spring `@Component`. |

---

## Choosing an Algorithm

| Algorithm | Use When |
|-----------|----------|
| **StandardGeneticAlgorithm** | Simple problems, single search trajectory, faster per-generation |
| **DivergentGeneticAlgorithm** | Complex fitness landscapes, need to escape local optima, can afford parallel exploration |

---

## Generation Lifecycle

Each generation follows this sequence:

1. **Selection** - Choose parent pairs from population (concurrent)
2. **Crossover** - Produce one child per parent pair (concurrent)
3. **Mutation** - Mutate each child genome (concurrent)
4. **Replacement** - Keep top `elitism` individuals, replace rest with children
5. **Evaluation** - Score new/changed genomes, update statistics

---

## Population Types

### StandardPopulation
- Backed by a simple list
- Any individual can mate with any other
- Good default choice

### LatticePopulation
- 2D grid topology (`latticeRows` x `latticeColumns`)
- Selection restricted to local neighborhood (`latticeRadius`)
- Preserves diversity by limiting long-range mating
- `latticeWrapAround=true` connects edges (torus topology)

**Constraint:** `populationSize` must equal `latticeRows * latticeColumns`

---

## Divergent GA and Speciation

The divergent algorithm explores multiple search paths by evolving parallel populations that periodically split and compete.

### How It Works

1. **Initialize** - Create `minPopulations` independent populations, evolve each for `numberOfGenerations`
2. **Extinction cycle** (repeat `extinctionCycles` times):
   - If population count exceeds `minPopulations`, keep only the best (by best individual fitness)
   - **Speciate**: Split each population into `speciationFactor` sub-populations
   - Evolve each new population for `numberOfGenerations`
3. **Final selection** - Return the population with the best individual

### Speciation Operators

| Operator | Strategy | Default For |
|----------|----------|-------------|
| `FitnessSpeciationOperator` | Sort by fitness, slice into segments | StandardPopulation |
| `ProximitySpeciationOperator` | Use lattice spatial order | LatticePopulation |
| `RandomSpeciationOperator` | Shuffle, then slice | (manual selection) |

Override the default by setting `speciationOperatorName` in your configuration.

**Key behavior:** Speciation partitions existing individuals - it does not re-breed or mutate during splitting.

---

## Configuration Reference

### Core Parameters

| Parameter | Type | Description | Guidance |
|-----------|------|-------------|----------|
| `taskExecutor` | TaskExecutor | Spring executor for concurrent operations | **Required.** Use `ThreadPoolTaskExecutor` or similar. |
| `populationSize` | int | Number of individuals per population | Larger = more exploration, slower. Start with 100-1000. |
| `numberOfGenerations` | int | Generations per evolution cycle | More = better convergence. 50-200 typical. |
| `elitism` | int | Top individuals preserved each generation | **Critical for avoiding regression.** Use 1-10% of population. 0 = best solutions can be lost! |
| `mutationRate` | double | Probability each gene mutates (0.0-1.0) | Too low = stuck at local optima. Too high = random search. Start with 0.01-0.10. |
| `maxMutationsPerIndividual` | int | Cap on mutations per genome | Used by `MultipleMutationOperator`. |

### Tournament Selection Parameters

| Parameter | Type | Description | Guidance |
|-----------|------|-------------|----------|
| `tournamentSize` | int | Individuals per tournament | Larger = stronger selection pressure. 2-7 typical. |
| `tournamentSelectorAccuracy` | double | Probability best wins (0.0-1.0) | 1.0 = always pick best. 0.7-0.9 adds randomness. |

### Lattice Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `latticeRows` | int | Grid height |
| `latticeColumns` | int | Grid width |
| `latticeRadius` | int | Neighborhood size for selection |
| `latticeWrapAround` | boolean | Connect edges (torus) |

### Divergent GA Parameters

| Parameter | Type | Description | Guidance |
|-----------|------|-------------|----------|
| `minPopulations` | int | Minimum parallel populations | More = broader search, more compute. 4-64 typical. |
| `speciationEvents` | int | Splits per extinction cycle | Usually 1. |
| `speciationFactor` | int | Sub-populations per split | 2 = double populations each cycle. |
| `extinctionCycles` | int | Number of extinction/speciation rounds | More = longer search. 10-100 typical. |
| `speciationOperatorName` | String | Override default speciation | Optional. |

---

## Built-in Operators

### Crossover (`com.ciphertool.zenith.genetic.operators.crossover`)

| Operator | Behavior |
|----------|----------|
| `UniformCrossoverOperator` | Each gene randomly from parent A or B (50/50) |
| `SinglePointCrossoverOperator` | Genes up to random crossover point from one parent, remainder from the other |

### Mutation (`com.ciphertool.zenith.genetic.operators.mutation`)

| Operator | Behavior |
|----------|----------|
| `PointMutationOperator` | Each gene has `mutationRate` chance of replacement |
| `MultipleMutationOperator` | 1 to `maxMutationsPerIndividual` random mutations |

### Selection (`com.ciphertool.zenith.genetic.operators.selection`)

| Operator | Behavior | Best For |
|----------|----------|----------|
| `TournamentSelector` | Pick best from random subset | General use, tunable pressure |
| `RouletteSelector` | Probability proportional to fitness | Single-objective, positive fitness |
| `TruncationSelector` | Random selection (delegates to RandomSelector) | *Currently incomplete* |
| `RandomSelector` | Uniform random | Baseline, testing |

---

## Fitness Ordering

- **Single-objective**: Sort by fitness value
- **Multi-objective**: Pareto dominance ranking, then crowding distance within each front

---

## Implementation Requirements

To use this framework, implement these interfaces:

| Interface | Purpose | Key Methods | Spring? |
|-----------|---------|-------------|---------|
| `Gene` | Atomic genetic unit | `clone()`, `getValue()` | No |
| `Chromosome<T>` | Container mapping keys to genes | `getGenes()`, `replaceGene()`, `clone()` | No |
| `Breeder` | Create random genomes | `breed(Population)` | No |
| `GeneDao` | Supply genes for mutation | `findRandomGene(Chromosome)` | **Yes** - must be `@Component` |
| `FitnessEvaluator` | Score genomes | `evaluate(Genome)` | No |

The `GeneDao` must be injectable via Spring component scanning. Other types can be instantiated manually and passed to the strategy builder.

**Note:** Both `Gene` and `Chromosome` must implement `Cloneable` with a proper `clone()` method. The framework clones individuals during crossover and mutation.
