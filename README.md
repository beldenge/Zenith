# Zenith
The aim of this project is to provide a tool that is easy to use for somewhat technical people in solving homophonic substitution ciphers.  The specific motivation for this project is in deciphering the Zodiac 340 cipher.

It currently is successful at deciphering the Zodiac 408 cipher at a rate of roughly 70% per epoch.

It takes roughly 20 seconds on an i7-7700HQ CPU @ 2.80GHz with 16 GB memory to complete one epoch.

# Building
1. Download and install Java 8 or later
2. Download and install [Apache Maven](https://maven.apache.org/download.cgi)
3. On the command line, change directory to the top-level directory where you cloned the Git repository
4. Issue the command `mvn clean install`

# Modules
### zenith-language-model
This module is both a dependency and a runnable application on its own.  Its purpose is to build a language model by reading in a corpus of english texts, so that the language model can be used to score solution proposals.
### zenith-inference
This module is both a dependency and a runnable application on its own.  It performs hill climbing using the above language model to estimate the optimal solution for a given cipher.
### zenith-mutation-search
This module is a runnable application only.  It performs hill climbing using mutations of the original cipher to try to detect what sorts of mutations were used to create the cipher.

# Contributing
Simply fork the repository and send pull requests.  The following are the areas that could be of most benefit going forward.
* Better optimization algorithms \
   The currently hill-climbing approach is successful roughly 70% of the time, so it's currently recommended to run the optimizer for several epochs.  It would be desirable if it worked 100% of the time, so that we don't miss a potential winning solution from simply not running the optimizer for enough iterations.
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