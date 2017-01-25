This module is intended to wrap [Richard J. Mathar's](http://www2.mpia-hd.mpg.de/~mathar/) "[Java Math.BigDecimal Implementation of Core Mathematical Functions](https://arxiv.org/abs/0908.3030v3)" library in order to provide it as a maven dependency.  This is required because Java does not provide functions for performing logarithms and exponents for BigDecimals out of the box.

Some additional features have been implemented such as caching so that we can avoid recomputing expensive math functions where possible.

The original code is licensed under (LGPL v3.0)[http://www.gnu.org/copyleft/lesser.html] and the original package name has been preserved to keep it separate from the added features.  Minor changes have been made to the original code to get rid of compiler warnings.