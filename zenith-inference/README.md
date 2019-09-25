# Description
This module encompasses the algorithm which performs inference on the language model.

# Running
1. Download and install Java 8 or later
2. On the command line, change to the zenith-inference directory
3. Issue the command `java -jar target\zenith-inference-1.1.0-SNAPSHOT-exec.jar`

Note: You must run the *-exec.jar and not the vanilla jar file, as this module is used both as a dependency and as a runnable application on its own.

# Architecture

There are three major levels of customization:
1. Optimizer
   - SimulatedAnnealingSolutionOptimizer
     - This is the optimizer which currently is successful and is recommended (and is the default).
   - GeneticAlgorithmSolutionOptimizer
     - This is an experimental optimizer that does not yet produce successful solutions.
2. Transformer (listed below in [Transformer Implementations](#transformer-implementations))
3. PlaintextEvaluator
   - MarkovModelPlaintextEvaluator
     - This evaluator is explained below in [Algorithm and Scoring](#algorithm-and-scoring)
   - RestServicePlaintextEvaluator
     - You can implement an evaluator in any technology you want, as long as it conforms to the predefined REST interface.  Just point to that service using the property ```evaluation.rest-service.url```. 

# Configuration
Ciphers need to be configured as JSON, each in a separate file that is readable from the property working directory in a subdirectory called "ciphers".  Out of the box, the zodiac408 and zodiac340 ciphers are packaged within the application.
Take a look at [ciphers.json](src/main/resources/ciphers/zodiac408.json) for an example.

There are a number of configuration settings that can be set for the application.  They need to be put in an application.properties file in the same directory as where you are running the application from.

Property Key | Default Value | Description
--- | --- | ---
task-executor.pool-size | Number of available cores on host | The number of threads to use for parallel tasks
task-executor.queue-capacity | 100000 | The number of tasks which can be queued at any given time when performing multi-threaded operations
cipher.name | zodiac408 | The name of a particular cipher within the ciphers.json file (zodiac408 and zodiac340 are provided)
language-model.filename | zenith-model.csv | The language model file to use (CSV only) which should exist in the same directory where the application is run from
language-model.archive-filename | zenith-model.zip | The language model zip file on the classpath which will be unzipped if language-model.filename does not exist
language-model.ngram.minimum-count | 1 | The minimum count an ngram must have in the language model in order to be used.  Any ngrams with counts lower than this value will be filtered out.
markov.letter.order | 5 | Order of the Markov model (essentially the n-gram size)
decipherment.evaluator.plaintext | MarkovModelPlaintextEvaluator | The PlaintextEvaluator implementation class name to use
decipherment.epochs | 10 | The number of times to run the optimizer to completion
decipherment.transposition.column-key-string | N/A | A String representation of a column key used as a transposition key during encipherment (case-insensitive, ignored if decipherment.transposition.column-key is specified)
decipherment.transposition.column-key | N/A | A comma-separated integer array representation of a column key used as a transposition key during encipherment
decipherment.transposition.iterations | 1 | The number of times to perform transposition with the given key
decipherment.transposition.key-length.min | 17 | When the transposition key length is not known, this is the key length to start hill climbing with (must be greater than 1 and less than or equal to decipherment.transposition.key-length.max)
decipherment.transposition.key-length.max | 17 | When the transposition key length is not known, this is the key length to end hill climbing with (must be greater than or equal to decipherment.transposition.key-length.min)
decipherment.transformers.ciphertext | RemoveLastRow | A comma-separated list of names of transformers to use to mutate the cipher, in order
decipherment.transformers.plaintext | UnwrapFourSquare | A comma-separated list of names of transformers to use to mutate the plaintext, in order
evaluation.rest-service.url | http://localhost:5000/probabilities | The URL for the solution evaluator REST service, required only if decipherment.evaluator.plaintext is set to RestServicePlaintextEvaluator

#### Simulated Annealing Hyperparameters
These are used by the SimulatedAnnealingSolutionOptimizer only.

Property Key | Default Value | Description
--- | --- | ---
simulated-annealing.temperature.max | 5 | Annealing temperature at the beginning of each epoch
simulated-annealing.temperature.min | 3 | Annealing temperature at the end of each epoch
simulated-annealing.sampler.iterations | 5000 | The number of rounds of sampling to perform per epoch (A round of sampling can itself perform any number of samples depending on the algorithm)
simulated-annealing.sampler.iterate-randomly | false | Whether to sample the keys at random.  Otherwise the keys will be sampled in the order that they appear in the cipher.

#### Genetic Algorithm Hyperparameters
These are used by the GeneticAlgorithmSolutionOptimizer only.

Property Key | Default Value | Description
--- | --- | ---
genetic-algorithm.population.type | LatticePopulation | The population type.  Can be either StandardPopulation or LatticePopulation.
genetic-algorithm.population.size | 10000 | The population size.  It will be populated before the first generation and will remain constant throughout each subsequent generation.
genetic-algorithm.population.lattice.rows | 100 | The number of rows used by LatticePopulation.  The product of lattice rows and columns must exactly match the population size.
genetic-algorithm.population.lattice.columns | 100 | The number of columns used by LatticePopulation.  The product of lattice rows and columns must exactly match the population size.
genetic-algorithm.population.lattice.wrap-around | true | Whether to wrap around during selection if the individual sits on or near the edge of the lattice.
genetic-algorithm.population.lattice.selection-radius | 1 | The radius for selection used by LatticePopulation. 
genetic-algorithm.number-of-generations | 50 | The number of generations to run per epoch.
genetic-algorithm.elitism | 0 | The number of top individuals to carry over to the next generation, excluding from crossover and mutation.
genetic-algorithm.breeder.implementation | ProbabilisticCipherKeyBreeder | The class name of the Breeder implementation to use.
genetic-algorithm.breeder.hill-climbing.iterations | 100 | The number of hill climbing iterations per individual if using HillClimbingCipherKeyBreeder.
genetic-algorithm.crossover.implementation | GeneWiseCrossoverAlgorithm | The class name of the CrossoverAlgorithm implementation to use.
genetic-algorithm.mutation.implementation | StandardMutationAlgorithm | The class name of the MutationAlgorithm implementation to use.
genetic-algorithm.mutation.rate | 0.001 | The rate of mutation, calculated per individual.
genetic-algorithm.mutation.max-per-individual | 5 | The maximum number of unique Genes to be mutated by MutationAlgorithms which can mutate more than one Gene per individual.
genetic-algorithm.selection.implementation | RouletteSelector | The class name of the Selector implementation to use.
genetic-algorithm.selection.tournament.accuracy | 0.9 | Used by the TournamentSelector only.  This is the probability that the most fit individual will be chosen.
genetic-algorithm.selection.tournament.size | 5 | Used by the TournamentSelector only.  Determines the size of the randomly chosen subset.
genetic-algorithm.fitness.implementation | ${decipherment.evaluator.plaintext} | It should be an implementation of PlaintextEvaluator, and it gets injected into PlaintextEvaluatorWrappingFitnessEvaluator. 


# Algorithm and Scoring
This applies to the SimulatedAnnealingSolutionOptimizer.

The algorithm is standard hill climbing with random restarts and an annealing schedule to aid in convergence.  Many other more complex types of algorithms have been attempted, but they have been found to either be unsuccessful or too slow.  Furthermore, the simplest solution that works is most often the best solution. 

The solution scoring works by using a language model to estimate the probability of the solution and then penalizing the solution with a compution of the index of coincidence.  The language model is a Markov model of order 5 whereby any n-grams of the same length can each be assigned probabilities.  For n-grams that occur in solutions and which we do not have a match in the language model, we assign an "unknown n-gram probability".  We convert all probabilties to log probabilities, and this is done both for performance reasons and for ease of penalizing them by the aforementioned index of coincidence.  The index of coincidence turns out to be a critical component, as without it the hill climbing algorithm gets very easily stuck at local optima.  We finally take the fifth root of the index of coincidence and then multiply that by the sum of log probabilities as determined by the language model to get our score for a given solution.

# Transformer Implementations
For ciphers that are more complex than homophonic substitution ciphers read left-to-right as normal, it's assumed that some sort of mutation(s) have been performed to throw off various types of cryptanalysis.  When this is the case, it's anyone's guess as to what type(s) of mutation(s) may have been performed during encipherment.  Therefore Zenith comes with an extensible facility for specifying transformations to perform to "unwrap" the cipher before doing hill climbing.  

The following transformers are provided out of the box.  More can be added by implementing the CipherTransformer interface.
### RemoveFirstRow
Removes the first row of the cipher.
### RemoveLastRow
Removes the last row of the cipher.  This is useful for block ciphers where the last row contains mostly jibberish.
### RemoveFirstColumn
Removes the first column of the cipher.
### RemoveLastColumn
Removes the last column of the cipher.
### Transposition
Transposes the cipher using a configured column key.  The column key should be specified as lowercase alpha characters in parenthesis, e.g. ```Transposition(baconisgood)```
### UnwrapTransposition
Unwraps the transposition on the cipher using a configured column key.  The column key should be specified as lowercase alpha characters in parenthesis, e.g. ```UnwrapTransposition(baconisgood)```
### UpperLeftQuadrant
Replaces the cipher with its upper left quadrant.
### UpperRightQuadrant
Replaces the cipher with its upper right quadrant.
### LowerLeftQuadrant
Replaces the cipher with its lower left quadrant.
### LowerRightQuadrant
Replaces the cipher with its lower right quadrant.
### Reverse
Reverses the cipher
### RotateClockwise
Rotates the cipher clockwise (number of rows and columns are swapped)
### RotateCounterClockwise
Rotates the cipher counter-clockwise (number of rows and columns are swapped)
### FlipHorizontally
Inverts the cipher horizontally (as if looking in a mirror)
### FlipVertically
Inverts the cipher vertically (as if looking in a mirror with your head sideways...?)
### Period
Replaces the cipher with a period shift.  The period number should be specified as an integer in paranthesis, e.g. ```Period(19)```
### UnwrapPeriod
Unwraps the period shift on the cipher.  The period number should be specified as an integer in paranthesis, e.g. ```UnwrapPeriod(19)```