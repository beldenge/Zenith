# Description
This module reads data from several English text corpora to build a language model that can be used in scoring cipher solution proposals.

# Corpora Used
* [British National Corpus, XML Edition](http://ota.ox.ac.uk/desc/2554)
* [Leipzig Corpora Collection (English 2005)](http://wortschatz.uni-leipzig.de/en/download/)
* [American National Corpus (MASC)](http://www.anc.org/data/masc/downloads/data-download/)

# Running
1. Download and install Java 8 or later
2. On the command line, change zenith-language-model directory
3. Issue the command `java -jar target\zenith-language-model-1.0.0-SNAPSHOT-exec.jar`

Note: You must run the *-exec.jar and not the vanilla jar file, as this module is used both as a dependency and as a runnable application on its own.

# Configuration
There are a number of configuration settings that can be set for the application.  They need to be put in an application.properties file in the same directory as where you are running the application from.

Property Key | Default Value | Description
--- | --- | ---
corpus.text.input.directory | ${user.home}/Desktop/corpus | Input directory for any plain text files to be imported
corpus.xml.input.directory | ${user.home}/Desktop/2554/2554/download/Texts | Input directory for any XML files to be imported (currently only supports the British National Corpus)
corpus.output.directory | ${user.home}/Desktop/zenith-transformed | Output directory for the post-processed corpus text data from which it is then used to build the language model
language-model.filename | ${user.home}/Desktop/zenith-model.csv | Filename where the language model data will be stored (CSV only)
markov.letter.order | 5 | Order of the Markov model (essentially the n-gram size)
task-executor.pool-size.override | N/A | The number of threads defaults to the number of available cores of the system, but it can be overridden here
task-executor.queue-capacity | 100000 | The number of tasks which can be queued at any given time when performing multi-threaded operations
ngram.persistence.batch-size | 1000 | The n-gram data is written to the language model in batches for performance reasons, and it can be tuned here