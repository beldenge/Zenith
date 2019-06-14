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
cipher.name | zodiac340 | The name of a particular cipher within the ciphers.json file
language-model.filename | zenith-model.csv | The language model file to use (CSV only) which should exist in the same directory where the application is run from
language-model.archive-filename | zenith-model.zip | The language model zip file on the classpath which will be unzipped if language-model.filename does not exist
markov.letter.order | 5 | Order of the Markov model (essentially the n-gram size)
decipherment.epochs | 10 | The number of times to run the hill climbing algorithm (essentially the number of random restarts) 
decipherment.sampler.iterations | 5000 | The number of rounds of sampling to perform per epoch (A round of sampling can itself perform any number of samples depending on the algorithm)
decipherment.annealing.temperature.max | 0.1 | Annealing temperature at the beginning of each epoch
decipherment.annealing.temperature.min | 0.001 | Annealing temperature at the end of each epoch
decipherment.remove-last-row| true | Whether to remove the last row of the cipher (good for block ciphers where the last line can contain some jibberish)
decipherment.transposition.column-key-string | N/A | A String representation of a column key used as a transposition key during encipherment (case-insensitive, ignored if decipherment.transposition.column-key is specified)
decipherment.transposition.column-key | N/A | A comma-separated integer array representation of a column key used as a transposition key during encipherment
decipherment.transposition.iterations | 1 | The number of times to perform transposition with the given key
decipherment.transposition.key-length.min | 17 | When the transposition key length is not known, this is the key length to start hill climbing with (must be greater than 1 and less than or equal to decipherment.transposition.key-length.max)
decipherment.transposition.key-length.max | 17 | When the transposition key length is not known, this is the key length to end hill climbing with (must be greater than or equal to decipherment.transposition.key-length.min)