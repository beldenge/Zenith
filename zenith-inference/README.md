# Description
This module encompasses the algorithm which performs inference on the language model.

# Running Standalone
1. Download and install Java 8 or later: [AdoptOpenJDK](https://adoptopenjdk.net/)
2. Download zenith-inference-2.0.2-SNAPSHOT-exec.jar
3. Issue the command `java -XX:+UseParallelGC -XX:ParallelGCThreads=2 -Xms2G -Xmx2G -XX:MaxMetaspaceSize=512M -jar zenith-inference-2.0.2-SNAPSHOT-exec.jar`

Note: You must run the *-exec.jar and not the vanilla jar file, as this module is used both as a dependency and as a runnable application on its own.

# Architecture

There are four major levels of customization:
1. Optimizer
   - SimulatedAnnealingSolutionOptimizer
     - This is the optimizer which currently is successful and is recommended (and is the default).
   - GeneticAlgorithmSolutionOptimizer
     - This is an experimental optimizer that does not yet produce successful solutions.
2. Ciphertext Transformers (listed below in [Ciphertext Transformers](#ciphertext-transformers))
2. Plaintext Transformers (listed below in [Plaintext Transformers](#plaintext-transformers))
3. PlaintextEvaluator
   - NGramAndIndexOfCoincidencePlaintextEvaluator
     - This evaluator is explained below in [Algorithm and Scoring](#algorithm-and-scoring)
   - RestServicePlaintextEvaluator
     - You can implement an evaluator in any technology you want, as long as it conforms to the predefined REST interface.  Just point to that service using the property ```evaluation.rest-service.url```. 

# Configuration
There are three main areas of configuration.
1. **Ciphers**

    Ciphers need to be configured as JSON, each in a separate file that is readable from the property working directory in a subdirectory called "ciphers".  Out of the box, the zodiac408 and zodiac340 ciphers are packaged within the application.
    Take a look at [ciphers.json](src/main/resources/ciphers/zodiac408.json) for an example.

2. **Solver Configuration**

    There are sensible defaults, but if you would like to customize how the solver runs, a file with the name zenith-config.json needs to be put in a /config directory within the same directory as where you are running the application from.

    Below lists the defaults and serves as an example of the JSON structure.

    ```json
    {
      "epochs": 10,
      "appliedCiphertextTransformers": [
        {
          "name": "RemoveLastRow",
          "displayName": "Remove Last Row",
          "form": null
        }
      ],
      "appliedPlaintextTransformers": [
      ],
      "selectedOptimizer": {
        "name": "SimulatedAnnealingSolutionOptimizer",
        "displayName": "Simulated Annealing"
      },
      "simulatedAnnealingConfiguration": {
        "samplerIterations": 5001,
        "annealingTemperatureMin": 2.75,
        "annealingTemperatureMax": 5
      },
      "geneticAlgorithmConfiguration": {
        "populationSize": 10000,
        "numberOfGenerations": 1000,
        "elitism": 1,
        "populationName": "StandardPopulation",
        "latticeRows": 100,
        "latticeColumns": 100,
        "latticeWrapAround": true,
        "latticeRadius": 1,
        "breederName": "RandomCipherKeyBreeder",
        "crossoverAlgorithmName": "GeneWiseCrossoverAlgorithm",
        "mutationAlgorithmName": "StandardMutationAlgorithm",
        "mutationRate": 0.05,
        "maxMutationsPerIndividual": 5,
        "selectorName": "TournamentSelector",
        "tournamentSelectorAccuracy": 0.75,
        "tournamentSize": 5
      }
    }
    ```

3. **Application Properties**

    There are a number of configuration settings that can be set for the application.  Zenith comes with sensible defaults, so you can skip this altogether unless you need to customize something.  The properties need to be put in an application.properties file in the same directory as where you are running the application from.

    Property Key | Default Value | Description
    --- | --- | ---
    task-executor.pool-size | Number of available cores on host | The number of threads to use for parallel tasks
    task-executor.queue-capacity | 100000 | The number of tasks which can be queued at any given time when performing multi-threaded operations
    cipher.name | zodiac408 | The name of a particular cipher within the ciphers.json file (zodiac408 and zodiac340 are provided)
    language-model.filename | zenith-model.csv | The language model file to use (CSV only) which should exist in the same directory where the application is run from
    language-model.archive-filename | zenith-model.zip | The language model zip file on the classpath which will be unzipped if language-model.filename does not exist
    language-model.max-ngrams-to-keep | 500000 | The maximum number of ngrams to keep.  The list of ngrams will be sorted in descending order by count and then the top number below will be kept.
    markov.letter.order | 5 | Order of the Markov model (essentially the n-gram size)
    decipherment.evaluator.plaintext | NGramAndIndexOfCoincidencePlaintextEvaluator | The PlaintextEvaluator implementation class name to use
    evaluation.rest-service.url | http://localhost:5000/probabilities | The URL for the solution evaluator REST service, required only if decipherment.evaluator.plaintext is set to RestServicePlaintextEvaluator
    application.configuration.file-path | ./config | The path to the application configuration JSON file 

# Algorithm and Scoring
This applies to the SimulatedAnnealingSolutionOptimizer.

The algorithm is standard hill climbing with random restarts and an annealing schedule to aid in convergence.  Many other more complex types of algorithms have been attempted, but they have been found to either be unsuccessful or too slow.  Furthermore, the simplest solution that works is most often the best solution. 

The solution scoring works by using a language model to estimate the probability of the solution and then penalizing the solution with a compution of the index of coincidence.  The language model is a Markov model of order 5 whereby any n-grams of the same length can each be assigned probabilities.  For n-grams that occur in solutions and which we do not have a match in the language model, we assign an "unknown n-gram probability".  We convert all probabilties to log probabilities, and this is done both for performance reasons and for ease of penalizing them by the aforementioned index of coincidence.  The index of coincidence turns out to be a critical component, as without it the hill climbing algorithm gets very easily stuck at local optima.  We finally take the fifth root of the index of coincidence and then multiply that by the sum of log probabilities as determined by the language model to get our score for a given solution.

# Transformer Implementations
For ciphers that are more complex than homophonic substitution ciphers read left-to-right as normal, it's assumed that some sort of mutation(s) have been performed to throw off various types of cryptanalysis.  When this is the case, it's anyone's guess as to what type(s) of mutation(s) may have been performed during encipherment, either before or after substitution.  Therefore Zenith comes with an extensible facility for specifying transformations to perform to "unwrap" the ciphertext and plaintext before scoring the solution proposal.  

## Ciphertext Transformers
The following ciphertext transformers are provided out of the box.  More can be added by implementing the CipherTransformer interface.
### RemoveFirstRow
Removes the first row of the cipher
### RemoveLastRow
Removes the last row of the cipher.  This is useful for block ciphers where the last row contains mostly jibberish.
### RemoveFirstColumn
Removes the first column of the cipher
### RemoveLastColumn
Removes the last column of the cipher
### RemoveMiddleColumn
Removes the middle column of the cipher if there are an odd number of columns.
### Transposition
Transposes the cipher using a configured column key.  The column key should be specified as lowercase alpha characters in parenthesis, e.g. ```Transposition(baconisgood)```
### UnwrapTransposition
Unwraps the transposition on the cipher using a configured column key.  The column key should be specified as lowercase alpha characters in parenthesis, e.g. ```UnwrapTransposition(baconisgood)```
### UpperLeftQuadrant
Replaces the cipher with its upper left quadrant
### UpperRightQuadrant
Replaces the cipher with its upper right quadrant
### LowerLeftQuadrant
Replaces the cipher with its lower left quadrant
### LowerRightQuadrant
Replaces the cipher with its lower right quadrant
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
### RemoveSymbol
Removes a symbol from the ciphertext
### TopLeftDiagonal
Traverses the ciphertext diagonally as if the cipher was rotated 45 degrees to the right, with the output retaining the same number of rows and columns
### ZPattern
Custom operation which traverses two rows at a time, outputting the symbols using a Z pattern, with the output retaining the same number of rows and columns

## Plaintext Transformers
The following plaintext transformers are provided out of the box.  More can be added by implementing the PlaintextTransformer interface.
### FourSquare
Performs a standard four square transformation using the specified set of keys
### UnwrapFourSquare
"Performs the inverse operation of the Four Square transformer
### OneTimePad
Performs a One Time Pad transformation with modular addition using the specified key
### UnwrapOneTimePad
Performs the inverse operation of the One Time Pad transformer
### Vigenere
Performs a standard Vigenere transformation using the standard Vigenere square and specified key
### UnwrapVigenere
Performs the inverse operation of the Vigenere transformer
