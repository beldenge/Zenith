/**
 * Copyright 2017-2019 George Belden
 *
 * This file is part of Zenith.
 *
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.inference.evaluator;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.MapMarkovModel;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@ConditionalOnProperty(value = "decipherment.evaluator.plaintext", havingValue = "MarkovModelPlaintextEvaluator")
public class MarkovModelPlaintextEvaluator implements PlaintextEvaluator {
    private Logger log = LoggerFactory.getLogger(getClass());

    // In theory this speeds up memory allocation, and in practice it does appear to have some benefit
    private static final int ARBITRARY_LIST_INITIAL_SIZE = 20;

    @Autowired
    private Cipher cipher;

    @Autowired
    private MapMarkovModel letterMarkovModel;

    // Since we are using only ASCII letters as array indices, we're guaranteed to stay within 256
    private int[] letterCounts = new int[256];
    private int[] precomputedNominatorValues;
    private double denominator;

    private int order;
    private int stepSize;
    private int doubleStepSize;

    @PostConstruct
    public void init() {
        denominator = cipher.length() * (cipher.length() - 1);
        precomputedNominatorValues = new int[cipher.length()];

        for (int i = 0; i < cipher.length(); i ++) {
            precomputedNominatorValues[i] = i * (i - 1);
        }

        order = letterMarkovModel.getOrder();
        stepSize = order / 2;
        doubleStepSize = stepSize * 2;
    }

    @Override
    public Int2DoubleMap evaluate(CipherSolution solution, String solutionString, String ciphertextKey) {
        long startLetter = System.currentTimeMillis();

        Int2DoubleMap logProbabilitiesUpdated = evaluateLetterNGrams(solution, solutionString, ciphertextKey);

        solution.setIndexOfCoincidence(computeIndexOfCoincidence(solutionString));

        if (log.isDebugEnabled()) {
            log.debug("Letter N-Grams took {}ms.", (System.currentTimeMillis() - startLetter));
        }

        return logProbabilitiesUpdated;
    }

    protected Int2DoubleMap evaluateLetterNGrams(CipherSolution solution, String solutionString, String ciphertextKey) {
        int stringLengthMinusOrder = solutionString.length() - order;

        Int2DoubleMap logProbabilitiesUpdated = new Int2DoubleOpenHashMap(ARBITRARY_LIST_INITIAL_SIZE);
        double logProbability;
        int lastIndex = -1;

        if (ciphertextKey != null) {
            for (int ciphertextIndex : cipher.getCipherSymbolIndicesMap().get(ciphertextKey)) {
                int wayBack = ciphertextIndex - (ciphertextIndex % stepSize) - doubleStepSize;
                if (wayBack + order <= ciphertextIndex) {
                    wayBack += stepSize;
                }

                int start = Math.max(0, wayBack);
                int end = Math.min(stringLengthMinusOrder, ciphertextIndex + 1);

                if (lastIndex >= 0 && start < lastIndex) {
                    continue;
                }

                for (int j = start; j < end; j += stepSize) {
                    logProbability = computeNGramLogProbability(solutionString.substring(j, j + order));

                    int index = j / stepSize;
                    logProbabilitiesUpdated.put(index, solution.getLogProbabilities().getDouble(index));
                    solution.replaceLogProbability(index, logProbability);
                }

                lastIndex = end;
            }
        } else {
            DoubleList logProbabilities = solution.getLogProbabilities();
            for (int i = 0; i < logProbabilities.size(); i ++) {
                logProbabilitiesUpdated.put(i, logProbabilities.getDouble(i));
            }

            solution.clearLogProbabilities();

            for (int i = 0; i < solutionString.length() - order; i += stepSize) {
                logProbability = computeNGramLogProbability(solutionString.substring(i, i + order));

                solution.addLogProbability(logProbability);
            }
        }

        return logProbabilitiesUpdated;
    }

    protected double computeNGramLogProbability(String ngram) {
        TreeNGram match = letterMarkovModel.findExact(ngram);

        if (match != null) {
            log.debug("Letter N-Gram Match={}, Probability={}", match.getCumulativeString(), match.getLogProbability());
            return match.getLogProbability();
        }

        log.debug("No Letter N-Gram Match for ngram={}", ngram);
        return letterMarkovModel.getUnknownLetterNGramLogProbability();
    }

    protected double computeIndexOfCoincidence(String solutionString) {
        resetLetterCounts();

        for (int i = 0; i < solutionString.length(); i++) {
            letterCounts[solutionString.charAt(i)] ++;
        }

        int numerator = buildNumerator();

        return (double) numerator / denominator;
    }

    private void resetLetterCounts() {
        letterCounts['a'] = 0;
        letterCounts['b'] = 0;
        letterCounts['c'] = 0;
        letterCounts['d'] = 0;
        letterCounts['e'] = 0;
        letterCounts['f'] = 0;
        letterCounts['g'] = 0;
        letterCounts['h'] = 0;
        letterCounts['i'] = 0;
        letterCounts['j'] = 0;
        letterCounts['k'] = 0;
        letterCounts['l'] = 0;
        letterCounts['m'] = 0;
        letterCounts['n'] = 0;
        letterCounts['o'] = 0;
        letterCounts['p'] = 0;
        letterCounts['q'] = 0;
        letterCounts['r'] = 0;
        letterCounts['s'] = 0;
        letterCounts['t'] = 0;
        letterCounts['u'] = 0;
        letterCounts['v'] = 0;
        letterCounts['w'] = 0;
        letterCounts['x'] = 0;
        letterCounts['y'] = 0;
        letterCounts['z'] = 0;
    }

    private int buildNumerator() {
        int numerator = 0;
        numerator += precomputedNominatorValues[letterCounts['a']];
        numerator += precomputedNominatorValues[letterCounts['b']];
        numerator += precomputedNominatorValues[letterCounts['c']];
        numerator += precomputedNominatorValues[letterCounts['d']];
        numerator += precomputedNominatorValues[letterCounts['e']];
        numerator += precomputedNominatorValues[letterCounts['f']];
        numerator += precomputedNominatorValues[letterCounts['g']];
        numerator += precomputedNominatorValues[letterCounts['h']];
        numerator += precomputedNominatorValues[letterCounts['i']];
        numerator += precomputedNominatorValues[letterCounts['j']];
        numerator += precomputedNominatorValues[letterCounts['k']];
        numerator += precomputedNominatorValues[letterCounts['l']];
        numerator += precomputedNominatorValues[letterCounts['m']];
        numerator += precomputedNominatorValues[letterCounts['n']];
        numerator += precomputedNominatorValues[letterCounts['o']];
        numerator += precomputedNominatorValues[letterCounts['p']];
        numerator += precomputedNominatorValues[letterCounts['q']];
        numerator += precomputedNominatorValues[letterCounts['r']];
        numerator += precomputedNominatorValues[letterCounts['s']];
        numerator += precomputedNominatorValues[letterCounts['t']];
        numerator += precomputedNominatorValues[letterCounts['u']];
        numerator += precomputedNominatorValues[letterCounts['v']];
        numerator += precomputedNominatorValues[letterCounts['w']];
        numerator += precomputedNominatorValues[letterCounts['x']];
        numerator += precomputedNominatorValues[letterCounts['y']];
        numerator += precomputedNominatorValues[letterCounts['z']];

        return numerator;
    }
}