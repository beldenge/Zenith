# Description
This module encompasses hill climbing algorithm(s) for detecting cipher mutations which are likely to have been performed during encipherment.  As of now it simply performs transpositions on the configured cipher to try to maximize bigram repeats.  It is not a mature application at this point.

# Running
1. Download and install Java 8 or later
2. On the command line, change to the zenith-mutation-search directory
3. Issue the command `java -jar target\zenith-mutation-search-1.1.0-SNAPSHOT.jar`

# Configuration
There are a number of configuration settings that can be set for the application.  They need to be put in an application.properties file in the same directory as where you are running the application from.

Property Key | Default Value | Description
--- | --- | ---
cipher.repository-filename | ciphers.json | The file on the classpath which contains any number of ciphers specified as JSON objects
cipher.name | zodiac340 | The name of a particular cipher within the ciphers.json file (zodiac408 and zodiac340 are provided)
decipherment.epochs | 10 | The number of times to run the hill climbing algorithm (essentially the number of random restarts) 
simulated-annealing.sampler.iterations | 5000 | The number of rounds of sampling to perform per epoch (A round of sampling can itself perform any number of samples depending on the algorithm)
simulated-annealing.temperature.max | 0.1 | Annealing temperature at the beginning of each epoch
simulated-annealing.temperature.min | 0.001 | Annealing temperature at the end of each epoch
decipherment.remove-last-row| true | Whether to remove the last row of the cipher (good for block ciphers where the last line can contain some jibberish)
decipherment.transposition.column-key-string | N/A | A String representation of a column key used as a transposition key during encipherment (case-insensitive, ignored if decipherment.transposition.column-key is specified)
decipherment.transposition.column-key | N/A | A comma-separated integer array representation of a column key used as a transposition key during encipherment
decipherment.transposition.iterations | 1 | The number of times to perform transposition with the given key
decipherment.transposition.key-length.min | 17 | When the transposition key length is not known, this is the key length to start hill climbing with (must be greater than 1 and less than or equal to decipherment.transposition.key-length.max)
decipherment.transposition.key-length.max | 17 | When the transposition key length is not known, this is the key length to end hill climbing with (must be greater than or equal to decipherment.transposition.key-length.min)