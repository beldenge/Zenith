# Zenith Genetic Algorithm Module
Zenith provides a reusable genetic algorithm (GA) framework for Java projects. It is packaged as a standalone module so it can be used outside of the Zenith application stack as well. The implementation is Spring-friendly (component scanning and dependency injection), but the core types are plain Java interfaces/classes.

The framework supports single-objective and multi-objective fitness functions, concurrent evaluation/selection/crossover/mutation, and multiple population topologies. It also includes a divergent GA mode that uses speciation and extinction cycles to explore multiple search paths in parallel.

# Core Concepts
- **Gene**: The smallest unit of genetic information. Genes must be cloneable and mutable.
- **Chromosome**: A collection of genes (usually a keyed map) that represents a solution candidate. The GA operators work at the chromosome level.
- **Genome**: A wrapper for one or more chromosomes plus fitness information. Operators iterate through all chromosomes in a genome.
- **FitnessEvaluator**: Scores a genome and returns one or more `Fitness` values.
- **Breeder**: Creates new random genomes used for initial population seeding.

# How Evolution Works
The framework defines two algorithms:
- **StandardGeneticAlgorithm**: A single population evolves for N generations.
- **DivergentGeneticAlgorithm**: Multiple populations evolve in parallel and periodically split into new populations via speciation.

## 1) Population Initialization
Population initialization is driven by the `Breeder` implementation:
1. The algorithm clears the population (`population.clearIndividuals()`).
2. It calls `population.breed(populationSize)`, which creates `populationSize` genomes **concurrently** using the breeder.
3. Each genome is added to the population. The population tracks total fitness/probability if the fitness function is single-objective.
4. The population is evaluated (`population.evaluateFitness(...)`), which scores only genomes marked as needing evaluation.

For a `LatticePopulation`, `populationSize` must equal `latticeRows * latticeColumns`. Individuals are inserted row-by-row into the lattice.

## 2) Generation Loop (Selection -> Crossover -> Mutation -> Replacement -> Evaluation)
Each generation follows the same sequence:

1. **Selection**
   - The population selects `populationSize - elitism` parent pairs.
   - Selection runs **concurrently**.
   - `StandardPopulation` selection uses the full population and the configured `Selector`.
   - `LatticePopulation` selection chooses parents from a local neighborhood (see below).

2. **Crossover**
   - A `CrossoverOperator` is applied to each parent pair, producing **one child genome per pair**.
   - Crossover tasks run **concurrently**.
   - The algorithm expects at least `populationSize - elitism` children; otherwise it throws an error.

3. **Mutation**
   - Each child genome is mutated independently via the configured `MutationOperator`.
   - Mutation tasks run **concurrently**.

4. **Replacement (Elitism)**
   - The best `elitism` individuals from the previous generation are kept (after sorting).
   - The population is cleared and then repopulated with elites + newly produced children.

5. **Fitness Evaluation**
   - Fitness is evaluated for genomes that changed.
   - Single-objective fitness values are aggregated for statistics and selection probabilities.
   - Multi-objective fitness uses Pareto sorting and crowding distance for ordering.

## 3) Fitness Ordering (Single vs Multi-Objective)
- **Single-objective**: Individuals are sorted by fitness value.
- **Multi-objective**: Individuals are ranked using Pareto dominance. Each Pareto front is further sorted by crowding distance to preserve diversity.

# Population Types
Population types control how individuals are arranged and how parents are selected.

## StandardPopulation
- Backed by a simple list of genomes.
- Any individual can mate with any other individual.
- Before selection, the population is sorted (Pareto-aware) and the `Selector` is re-indexed.

## LatticePopulation
- A 2D grid of genomes (rows x columns).
- Selection occurs in a **local neighborhood**:
  - A random lattice cell is chosen as the center.
  - All individuals within `latticeRadius` are collected (optionally wrapping around edges).
  - Parents are selected **only from that neighborhood**, which increases diversity by limiting long-range mating.
- `wrapAround=true` treats the lattice as a torus so edges connect.

# Divergent GA and Speciation
The **DivergentGeneticAlgorithm** explores multiple search trajectories by evolving multiple populations and splitting them into new sub-populations.

## Divergent Algorithm Flow
1. **Seed multiple populations**
   - `minPopulations` independent populations are created.
   - Each population runs a full `numberOfGenerations` evolution cycle.

2. **Extinction cycles** (`extinctionCycles`)
   - If the number of populations grows beyond `minPopulations`, the algorithm keeps only the best populations.
   - "Best" is determined by the best individual in each population using Pareto sorting.

3. **Speciation events** (`speciationEvents`)
   - Each population is split into `speciationFactor` new populations using a speciation operator.
   - Each new population **continues evolving** (no re-seeding) for another `numberOfGenerations`.

4. **Final selection**
   - After all cycles, the best population (based on its best individual) is selected and set on the strategy.

## Speciation in DivergentGeneticAlgorithm.evolve()
The `evolve()` method orchestrates a multi-stage search that alternates **evolution**, **speciation**, and **extinction**. Below is a step-by-step view of the actual control flow.

### Stage A: Seed and evolve the initial populations
For each of `minPopulations`:
1. Clone the configured population type using `population.getInstance()`.
2. Call `population.init(strategy)` to validate topology-specific configuration (e.g., lattice sizing).
3. Spawn and evaluate an initial population using the breeder.
4. Evolve that population for `numberOfGenerations`.

At the end of this stage you have `minPopulations` fully evolved populations.

### Stage B: Extinction cycles (outer loop)
The algorithm then repeats `extinctionCycles` times. Each cycle can expand the population set via speciation and then shrink it back to `minPopulations` by keeping only the best populations.

#### B1) Extinction (pruning to minPopulations)
If the current population count is greater than `minPopulations`:
1. Sort each population (Pareto-aware).
2. Pick the best individual from each population (last element after sorting).
3. Pareto-sort those best individuals across populations.
4. Keep only the populations whose best individual falls within the top `minPopulations`.

This step prevents unbounded population growth between cycles.

#### B2) Speciation events (inner loop)
For each `speciationEvents`:
1. For every existing population:
   - Choose a speciation operator (explicit `speciationOperatorName` if set, otherwise defaults by population type).
   - Call `speciationOperator.diverge(...)` which splits the population into `speciationFactor` **new** populations.
2. Each newly produced population is **not** re-seeded:
   - The individuals created by speciation are preserved.
   - `setStrategy(...)` is used instead of `init(...)` so the individuals stay intact.
3. Each new population then evolves for `numberOfGenerations`.
4. Replace the current population list with the newly evolved populations.

After each speciation event, the total population count multiplies by `speciationFactor` (before the next extinction pruning).

### Stage C: Final population selection
After all extinction cycles and speciation events:
1. Sort all remaining populations.
2. Collect each population’s best individual.
3. Pareto-sort those best individuals.
4. Select the population whose best individual is ranked highest and set it on the strategy.

### Key invariants and constraints
- **Speciation constraints**: `speciationFactor` must be positive, cannot exceed the population size, and the population cannot be empty.
- **Speciation preserves individuals**: Speciation partitions existing individuals; it does not re-breed or mutate during splitting.
- **Operator defaults**: `StandardPopulation` defaults to fitness-based speciation; `LatticePopulation` defaults to proximity-based speciation.

## Speciation Operators
Speciation divides an existing population into multiple sub-populations while preserving existing individuals:

- **FitnessSpeciationOperator** (default for `StandardPopulation`)
  - Sorts individuals by fitness (Pareto-aware).
  - Slices the ordered list into `speciationFactor` contiguous segments.

- **ProximitySpeciationOperator** (default for `LatticePopulation`)
  - Only valid for `LatticePopulation`.
  - Uses the lattice insertion order (preserving spatial proximity) and slices into `speciationFactor` segments.

- **RandomSpeciationOperator**
  - Shuffles the population and slices into `speciationFactor` segments.
  - Useful as a neutral baseline.

You can force a specific operator by setting `speciationOperatorName` to one of:
- `FitnessSpeciationOperator`
- `ProximitySpeciationOperator`
- `RandomSpeciationOperator`

If no operator is set, the algorithm defaults based on population type (fitness for standard, proximity for lattice).

# Configuration Summary
Key strategy fields used by the algorithms:
- **populationSize**, **numberOfGenerations**, **elitism**
- **mutationRate**, **maxMutationsPerIndividual**
- **latticeRows**, **latticeColumns**, **latticeRadius**, **latticeWrapAround**
- **minPopulations**, **speciationEvents**, **speciationFactor**, **extinctionCycles**, **speciationOperatorName**
- **selector**, **crossoverOperator**, **mutationOperator**, **fitnessEvaluator**, **breeder**

# Usage
To build a GA using this module, implement:
1. **Gene** - an atomic piece of genetic information
2. **Chromosome** - collection of genes
3. **Breeder** - produces new genomes when initializing populations
4. **GeneDao** - supplies novel genes for mutation
5. **FitnessEvaluator** - scores genomes (single or multi-objective)

Your `GeneDao` must be injectable by Spring (e.g., `@Component` + component scan). The others can be instantiated manually and passed into the strategy.

Then configure a `GeneticAlgorithmStrategy` (builder pattern):
```java
GeneticAlgorithmStrategy geneticAlgorithmStrategy = GeneticAlgorithmStrategy.builder()
        .population(population)
        .crossoverOperator(crossoverOperator)
        .mutationOperator(mutationOperator)
        .selector(selector)
        .fitnessEvaluator(fitnessEvaluator)
        .breeder(breeder)
        .populationSize(populationSize)
        .numberOfGenerations(numberOfGenerations)
        .mutationRate(mutationRate)
        .maxMutationsPerIndividual(maxMutationsPerIndividual)
        .elitism(elitism)
        .build();

standardGeneticAlgorithm.evolve(geneticAlgorithmStrategy);
```

For divergent evolution, inject and call `DivergentGeneticAlgorithm` instead:
```java
@Autowired
private DivergentGeneticAlgorithm divergentGeneticAlgorithm;

// ...build strategy...

divergentGeneticAlgorithm.evolve(geneticAlgorithmStrategy);
```

# Complete Example
There is a complete example of using this framework in the `zenith-inference` module.
- Package: `com.ciphertool.zenith.inference.genetic`
- Class: `com.ciphertool.zenith.inference.optimizer.GeneticAlgorithmSolutionOptimizer`

# Population Implementations
Population implementations are in `com.ciphertool.zenith.genetic.population`.

1. **StandardPopulation**
   - A simple list of individuals; any individual can reproduce with any other.
2. **LatticePopulation**
   - A 2D lattice topology; individuals primarily reproduce with nearby neighbors.

# Crossover Implementations
Crossover implementations are in `com.ciphertool.zenith.genetic.operators.crossover`.

1. **UniformCrossoverOperator**
   - For each gene, the child inherits from parent A or B with equal probability.
2. **SinglePointCrossoverOperator**
   - Chooses a random gene index and swaps all genes up to that point from the second parent.

# Mutation Implementations
Mutation implementations are in `com.ciphertool.zenith.genetic.operators.mutation`.

1. **PointMutationOperator**
   - Each gene has a `mutationRate` chance of being replaced by a random gene.
2. **MultipleMutationOperator**
   - Performs 1..`maxMutationsPerIndividual` mutations per chromosome, chosen at random.

# Selection Implementations
Selection implementations are in `com.ciphertool.zenith.genetic.operators.selection`.

1. **RouletteSelector**
   - Selects an individual using a probability distribution of fitness values.
2. **TournamentSelector**
   - Chooses a subset of individuals and selects one based on configurable tournament accuracy.
3. **TruncationSelector**
   - Selects from the top N portion of the population after sorting.
4. **RandomSelector**
   - Selects an individual uniformly at random.
