# Description
This module is a dependency only, providing a framework for genetic algorithm capabilities.

It is packaged as a separate module as it should be useful in its own right in implementing genetic algorithm solutions in Java.  It makes heavy use of Spring Framework and is intended for usage within Spring-base projects.

This framework currently only supports individuals with a single Chromosome.  So for all intents and purposes, an individual is synonymous with a Chromosome.

# Usage
In order to build a genetic algorithm using this module, you'll need to create implementations of the following classes:
1. Gene - an 'atomic' piece of genetic information that can be mutated, crossed over, etc.
2. Chromosome - essentially a collection of Genes
3. Breeder - produces new individuals, for example when initializing the population
4. GeneDao - produces novel Genes for use in mutation operators
5. FitnessEvaluator - Score the fitness of individuals in the population.  This is the most critical part to get right.

Your GeneDao needs to be injectable by Spring (e.g. annotated as @Component and included in the component scan).  The others can be instantiated however you prefer as they will be manually set in the GeneticAlgorithmStrategy explained below.

Once you have implementations of the above, you can run the algorithm with minimal code.  First instantiate an instance of StandardGeneticAlgorithm into your class.  The best way is to simply inject one:
```java
@Autowired
private StandardGeneticAlgorithm geneticAlgorithm;
```

Then in the body of your class (e.g. main method), you'll need to set your hyperparameters in a new GeneticAlgorithmStrategy instance, which uses the builder pattern:
```java
GeneticAlgorithmStrategy geneticAlgorithmStrategy = GeneticAlgorithmStrategy.builder()
        .population(population)
        .crossoverOperator(crossoverOperator)
        .mutationOperator(mutationOperator)
        .selector(selector)
        .fitnessEvaluator(fitnessEvaluator)
        .breeder(breeder)
        .populationSize(populationSize)
        .maxGenerations(numberOfGenerations)
        .mutationRate(mutationRate)
        .maxMutationsPerIndividual(maxMutationsPerIndividual)
        .elitism(elitism)
        .build();

geneticAlgorithm.setStrategy(geneticAlgorithmStrategy);
```

And finally, call evolve() on the genetic algorithm to let it runs its course:
```java
geneticAlgorithm.evolve();
```

# Complete Example
There is a complete example of using this framework in the zenith-inference module.

Take a look at the implementations in the following package: ```com.ciphertool.zenith.inference.genetic```

Also take a look at the class: ```com.ciphertool.zenith.inference.optimizer.GeneticAlgorithmSolutionOptimizer```

# Population Implementations
The following Population implementations are available out of the box.  They are in the package ```com.ciphertool.zenith.genetic.population```.

1. StandardPopulation
   - The population topology is just a list of individuals.  Any individual can reproduce with any other individual. 
2. LatticePopulation
   - The population topology is a two-dimensional lattice.  In theory this increases diversity because individuals can only reproduce with other individuals which are close to it on the lattice.

# Crossover Implementations
The following crossover implementations are available out of the box.  They are in the package ```com.ciphertool.zenith.genetic.operators.crossover```.

1. UniformCrossoverOperator
   - For each gene, there's an equal chance it will come from the first parent or second parent.
2. SinglePointCrossoverOperator
   - Picks a random gene index and then assigns all the genes prior to and including that index from the second parent, and it assigns all the genes after that index from the first parent.

# Mutation Implementations
The following mutation implementations are available out of the box.  They are in the package ```com.ciphertool.zenith.genetic.operators.mutation```.

1. PointMutationOperator
   - Gives each gene a chance of mutation (defined by the mutation rate).  Each new gene is chosen randomly.
2. MultipleMutationOperator
   - Chooses a random number of mutations to perform, constrained by the configurable max, and then that number of genes are chosen at random to be mutated.  Each new gene is chosen randomly.

# Selection Implementations
The following selection implementations are available out of the box.  They are in the package ```com.ciphertool.zenith.genetic.operators.selection```.

1. RouletteSelector
   - Selects an individual based on the probability distribution of fitness values.  This should always be used unless there is a very good reason not to.
2. TournamentSelector
   - Selects an individual based on a tournament style approach on a subset of randomly chosen individuals.  Starting with the most fit individual in the subset, each one is given a configurable probability of being chosen, otherwise the selector moves on to the next individual.
3. RandomSelector
   - Selects an individual at random, ignoring the fitness values.  Not particularly useful on its own, but it can be utilized by other selector methods.