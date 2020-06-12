# Description
This module encompasses hill climbing algorithm(s) for detecting cipher mutations which are likely to have been performed during encipherment.  As of now it simply performs transpositions on the configured cipher to try to maximize bigram repeats.  It is not a mature application at this point.

# Running
1. Download and install Java 8 or later: [AdoptOpenJDK](https://adoptopenjdk.net/)
2. Download zenith-mutation-search-2.1.3.jar
3. Issue the command `java -XX:+UseParallelGC -XX:ParallelGCThreads=2 -Xms2G -Xmx2G -XX:MaxMetaspaceSize=512M -jar zenith-mutation-search-2.1.3.jar`

# Configuration
There are a number of configuration settings that can be set for the application.  They need to be put in an application.properties file in the same directory as where you are running the application from.

Property Key | Default Value | Description
--- | --- | ---
decipherment.epochs | 10 | The number of times to run the hill climbing algorithm (essentially the number of random restarts) 
simulated-annealing.sampler.iterations | 5000 | The number of rounds of sampling to perform per epoch (A round of sampling can itself perform any number of samples depending on the algorithm)
simulated-annealing.temperature.max | 0.1 | Annealing temperature at the beginning of each epoch
simulated-annealing.temperature.min | 0.001 | Annealing temperature at the end of each epoch
decipherment.transposition.key-length.min | 17 | When the transposition key length is not known, this is the key length to start hill climbing with (must be greater than 1 and less than or equal to decipherment.transposition.key-length.max)
decipherment.transposition.key-length.max | 17 | When the transposition key length is not known, this is the key length to end hill climbing with (must be greater than or equal to decipherment.transposition.key-length.min)
application.configuration.file-path | ./config | The path to the application configuration JSON file