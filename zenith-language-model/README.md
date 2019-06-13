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