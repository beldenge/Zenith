# Zenith
A cipher solver application that is:
 - free
 - open-source
 - cross-platform
 - cross-browser
 - fast
 - extensible
 - flexible: can be used as a web UI, command-line app, or GraphQL API

The aim of this project is to provide a complete tool with that is easy to use for somewhat technical people in solving ciphers.  It is highly configurable but has sensible defaults.

# How-to
### Prerequisites
1. First you'll need to download Java 25 or later: [Amazon Corretto](https://aws.amazon.com/corretto/)

2. You'll need approximately 500 MB free disk space, roughly half of which is for the downloaded artifacts, and half of which will be written to disk as it unzips the included language model on the first startup.  

### Starting
The easiest way to use Zenith is to run the web UI using the included run script for your system.  The distribution includes run scripts for unix-based systems as well as windows:
 - run-ui-macos-linux.sh
 - run-ui-windows.bat

The run script should initialize the application and open the UI in your default browser.  If for some reason it does not do so after about 30 seconds, you can try manually navigating to http://localhost:8080 in your browser.

Check out [zenith-ui](zenith-ui/README.md) for more information.

For specifics on the command-line and GraphQL API, please check out the [zenith-inference](zenith-inference/README.md) and [zenith-api](zenith-api/README.md) modules respectively.

### Stopping
In order to stop Zenith, you'll have to type `ctrl + c` in the terminal where zenith is running.

# Benchmarks
When using the simulated annealing optimizer, it successfully solves the Zodiac 408 cipher with varying probability and speed depending on the hyperparameters chosen.  It is especially sensitive to the annealing temperatures and number of sampler iterations.
 - when choosing 2500 sampler iterations, each epoch takes ~250 ms, and 745 out of 1000 epochs (74.50%) produced the correct solution.
 - when choosing 5000 sampler iterations, each epoch takes ~500 ms, and 890 out of 1000 epochs (89.00%) produced the correct solution.
 - when choosing 10000 sampler iterations, each epoch takes ~1 second, and 955 out of 1000 epochs (95.50%) produced the correct solution.
 - when choosing 20000 sampler iterations, each epoch takes ~2 seconds, and 989 out of 1000 epochs (98.90%) produced the correct solution.
 
The results show that with more sampler iterations, it takes more time to complete each epoch, but each epoch has a greater probability of finding the correct solution.  The default is 5000 sampler iterations, which is a good balance between accuracy and speed.

The benchmarks were carried out using JDK 8 on a Windows 10 laptop with an i7-7700HQ CPU @ 2.80GHz with 2GB memory allocated.

# Modules
### [zenith-api](zenith-api/README.md)
This is the GraphQL API for Zenith.  It is used by the web UI and can also be used standalone.
### [zenith-inference](zenith-inference/README.md)
This is the command-line version of Zenith.  It is both a dependency and a runnable application on its own.  It performs hill climbing using the precomputed language model to estimate the optimal solution for a given cipher.
### [zenith-genetic-algorithm](zenith-genetic-algorithm/README.md)
This module is a framework for implementing genetic algorithms.  The zenith-inference module uses this for its GeneticAlgorithmSolutionOptimizer, which is currently still in Beta.
### [zenith-language-model](zenith-language-model/README.md)
This module is both a dependency and a runnable application on its own.  Its purpose is to build a language model by reading in a corpus of english texts, so that the language model can be used to score solution proposals.
### [zenith-mutation-search](zenith-mutation-search/README.md)
This module is currently not recommended for general use.  It performs hill climbing using mutations of the original cipher to try to detect what sorts of mutations were used to create the cipher.
### [zenith-package](zenith-package/README.md)
This module simply packages the runnable portions of the project for distribution.
### [zenith-roulette](zenith-roulette/README.md)
This module is a dependency shared by multiple Zenith modules.  That is its only purpose.
### [zenith-ui](zenith-ui/README.md)
A full-featured web user interface which showcases all of Zenith's solver, transformation, and configuration capabilities, built on Angular 21.

# Building from source
1. Download and install Java 25 or later: [Amazon Corretto](https://aws.amazon.com/corretto/)
2. Download and install [Apache Maven](https://maven.apache.org/download.cgi)
3. On the command line, change directory to the top-level directory where you cloned the Git repository
4. Issue the command `mvn clean install`

# Contributing
Simply fork the repository and send pull requests.  The following are the areas that could be of most benefit going forward.
* Better optimization algorithms \
   The current hill-climbing approach is not successful on every epoch, so it's currently recommended to run the optimizer for several epochs.  It would be desirable if it worked 100% of the time, so that we don't miss a potential winning solution from simply not running the optimizer for enough iterations.
* Better language models and scoring algorithms \
   This also plays into the comment above regarding accuracy.
* Better unit test coverage

# FAQ
*Why the name Zenith?* \
In astronomy, the zenith can loosely be considered the highest point in the sky.  And since this project implements a lot of hill climbing to estimate the optimal solution, a name which represents such a "highest point" seemed fitting.  To a lesser degree, since the motivation of this project was the Zodiac 340 cipher, which has some relationship with astrology, it kind of fit from that perspective as well.

*Is this project free to use and/or modify?* \
Absolutely, and you are encouraged to do so.  It's released under the GNU General Public License.  It's free as in speech and free as in beer.  The only thing you cannot do is distribute it yourself without also including the source code.

*What if I have other questions?* \
Feel free to post an issue on this Github repository.

# Acknowledgements
A very big thank you to the creator of AZDecrypt.  Project Zenith owes a great deal of its success to their insights and assistance.

[AZDecrypt](http://zodiackillersite.com/viewtopic.php?f=81&t=3198)