# Zenith
The aim of this project is to provide a tool that is easy to use for somewhat technical people in solving homophonic substitution ciphers.  The specific motivation for this project is in deciphering the Zodiac 340 cipher.

When using the simulated annealing optimizer, it successfully solves the Zodiac 408 cipher with varying probability and speed depending on the hyperparameters chosen.  It is especially sensitive to the annealing temperatures and number of sampler iterations.
 - when choosing 2500 sampler iterations, each epoch takes 1 second, and 617 out of 1000 epochs (61.70%) produced the correct solution.
 - when choosing 5000 sampler iterations, each epoch takes 2 seconds, and 841 out of 1000 epochs (84.10%) produced the correct solution.
 - when choosing 10000 sampler iterations, each epoch takes 4 seconds, and 929 out of 1000 epochs (92.90%) produced the correct solution.
 
The results show that with more sampler iterations, it takes more time to complete each epoch, but each epoch has a greater probability of finding the correct solution.  The default is 5000 sampler iterations, which is a good balance between accuracy and speed.

It takes roughly 2 seconds on an i7-7700HQ CPU @ 2.80GHz with 16 GB memory to complete one epoch at 5000 sampler iterations.

# Building
1. Download and install Java 8 or later
2. Download and install [Apache Maven](https://maven.apache.org/download.cgi)
3. On the command line, change directory to the top-level directory where you cloned the Git repository
4. Issue the command `mvn clean install`

# Modules
### [zenith-inference](zenith-inference/README.md)
This module is both a dependency and a runnable application on its own.  It performs hill climbing using the precomputed language model to estimate the optimal solution for a given cipher.
### [zenith-language-model](zenith-language-model/README.md)
This module is both a dependency and a runnable application on its own.  Its purpose is to build a language model by reading in a corpus of english texts, so that the language model can be used to score solution proposals.
### [zenith-genetic-algorithm](zenith-genetic-algorithm/README.md)
This module is a framework for implementing genetic algorithms.  The zenith-inference module uses this for its GeneticAlgorithmSolutionOptimizer.
### [zenith-mutation-search](zenith-mutation-search/README.md)
This module is a runnable application only.  It performs hill climbing using mutations of the original cipher to try to detect what sorts of mutations were used to create the cipher.
### [zenith-mutator](zenith-mutator/README.md)
This module is a runnable application only.  It is a simple utility to transform an existing cipher and write it to a file for testing purposes.
### [zenith-roulette](zenith-roulette/README.md)
This module is a dependency shared by multiple Zenith modules.  That is its only purpose.

# Contributing
Simply fork the repository and send pull requests.  The following are the areas that could be of most benefit going forward.
* Better optimization algorithms \
   The current hill-climbing approach is successful roughly 80% of the time, so it's currently recommended to run the optimizer for several epochs.  It would be desirable if it worked 100% of the time, so that we don't miss a potential winning solution from simply not running the optimizer for enough iterations.
* Better language models and scoring algorithms \
   This also plays into the comment above regarding accuracy.
* Performance improvements \
   Since a lot of the magic is simply trying lots and lots of solution proposals, the faster that can be done, the more variations of the hyperparameters we can try.
* Better unit test coverage

# FAQ
*Why the name Zenith?* \
In astronomy, the zenith can loosely be considered the highest point in the sky.  And since this project implements a lot of hill climbing to estimate the optimal solution, a name which represents such a "highest point" seemed fitting.  To a lesser degree, since the motivation of this project was the Zodiac 340 cipher, which has some relationship with astrology, it kind of fit from that perspective as well.

*Is this project free to use and/or modify?* \
Absolutely, and you are encouraged to do so.  It's released under the GNU General Public License.  It's free as in speech and free as in beer.  The only thing you cannot do is distribute it yourself without also including the source code.

*What if I have other questions?* \
Feel free to post an issue on this Github repository.