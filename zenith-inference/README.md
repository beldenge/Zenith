# Description
This module encompasses the algorithm which performs inference on the language model.

# Running
1. Download and install Java 8 or later
2. On the command line, change zenith-inference directory
3. Issue the command `java -jar target\zenith-inference-1.0.0-SNAPSHOT-exec.jar`

Note: You must run the *-exec.jar and not the vanilla jar file, as this module is used both as a dependency and as a runnable application on its own.

# Configuration
There are a number of configuration settings that can be set for the application.  They need to be put in an application.properties file in the same directory as where you are running the application from.

Property Key | Default Value | Description
--- | --- | ---
cipher.repository-filename | ciphers.json | The file on the classpath which contains any number of ciphers specified as JSON objects
cipher.name | zodiac340 | The name of a particular cipher within the ciphers.json file (zodiac408 and zodiac340 are provided)
language-model.filename | zenith-model.csv | The language model file to use (CSV only) which should exist in the same directory where the application is run from
language-model.archive-filename | zenith-model.zip | The language model zip file on the classpath which will be unzipped if language-model.filename does not exist
markov.letter.order | 5 | Order of the Markov model (essentially the n-gram size)
decipherment.epochs | 10 | The number of times to run the hill climbing algorithm (essentially the number of random restarts) 
decipherment.sampler.iterations | 5000 | The number of rounds of sampling to perform per epoch (A round of sampling can itself perform any number of samples depending on the algorithm)
decipherment.annealing.temperature.max | 0.1 | Annealing temperature at the beginning of each epoch
decipherment.annealing.temperature.min | 0.001 | Annealing temperature at the end of each epoch
decipherment.transposition.column-key-string | N/A | A String representation of a column key used as a transposition key during encipherment (case-insensitive, ignored if decipherment.transposition.column-key is specified)
decipherment.transposition.column-key | N/A | A comma-separated integer array representation of a column key used as a transposition key during encipherment
decipherment.transposition.iterations | 1 | The number of times to perform transposition with the given key
decipherment.transposition.key-length.min | 17 | When the transposition key length is not known, this is the key length to start hill climbing with (must be greater than 1 and less than or equal to decipherment.transposition.key-length.max)
decipherment.transposition.key-length.max | 17 | When the transposition key length is not known, this is the key length to end hill climbing with (must be greater than or equal to decipherment.transposition.key-length.min)
decipherment.transformers.list | RemoveLastRowCipherTransformer | A comma-separated list of names of transformers to use to mutate the cipher, in order

# Algorithm and Scoring

The algorithm is standard hill climbing with random restarts and an annealing schedule to aid in convergence.  Many other more complex types of algorithms have been attempted, but they have been found to either be unsuccessful or too slow.  Furthermore, the simplest solution that works is most often the best solution. 

The solution scoring works by using a language model to estimate the probability of the solution and then penalizing the solution with a compution of the index of coincidence.  The language model is a Markov model of order 5 whereby any n-grams of the same length can each be assigned probabilities.  For n-grams that occur in solutions and which we do not have a match in the language model, we assign an "unknown n-gram probability".  We convert all probabilties to log probabilities, and this is done both for performance reasons and for ease of penalizing them by the aforementioned index of coincidence.  The index of coincidence turns out to be a critical component, as without it the hill climbing algorithm gets very easily stuck at local optima.  We take the fifth root of the index of coincidence and then multiply that by the sum of log probabilities as determined by the language model.

# Transformers
For ciphers that are more complex than homophonic substitution ciphers read left-to-right as normal, it's assumed that some sort of mutation(s) have been performed to throw off various types of cryptanalysis.  When this is the case, it's anyone's guess as to what type(s) of mutation(s) may have been performed during encipherment.  Therefore Zenith comes with an extensible facility for specifying transformations to perform to "unwrap" the cipher before doing hill climbing.  

The following transformers are provided out of the box.  More can be added by implementing the CipherTransformer interface.
### RemoveListRowCipherTransformer
Removes the last row of the cipher.  This is useful for block ciphers where the last row contains mostly jibberish.
### TranspositionCipherTransformer
Transposes the cipher using a configured column key.
### UpperLeftQuadrantCipherTransformer
Replaces the cipher with its upper left quadrant.
### UpperRightQuadrantCipherTransformer
Replaces the cipher with its upper right quadrant.
### LowerLeftQuadrantCipherTransformer
Replaces the cipher with its lower left quadrant.
### LowerRightQuadrantCipherTransformer
Replaces the cipher with its lower right quadrant.