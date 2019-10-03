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
import com.ciphertool.zenith.model.markov.NDArrayModel;
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
    private NDArrayModel letterMarkovModel;

    // Since we are using only ASCII letters as array indices, we're guaranteed to stay within 256
    private int[] letterCounts = new int[256];
    private int[] precomputedNominatorValues;
    private float denominator;

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
    public float[][] evaluate(CipherSolution solution, String solutionString, String ciphertextKey) {
        long startLetter = System.currentTimeMillis();

        float[][] logProbabilitiesUpdated = evaluateLetterNGrams(solution, solutionString, ciphertextKey);

        solution.setIndexOfCoincidence(computeIndexOfCoincidence(solutionString));

        if (log.isDebugEnabled()) {
            log.debug("Letter N-Grams took {}ms.", (System.currentTimeMillis() - startLetter));
        }

        return logProbabilitiesUpdated;
    }

    protected float[][] evaluateLetterNGrams(CipherSolution solution, String solutionString, String ciphertextKey) {
        int stringLengthMinusOrder = solutionString.length() - order;

        float[][] logProbabilitiesUpdated;
        float logProbability;
        int lastIndex = -1;

        if (ciphertextKey != null) {
            int[] cipherSymbolIndices = cipher.getCipherSymbolIndicesMap().get(ciphertextKey);
            float[][] logProbabilitiesUpdatedOversized = new float[2][cipherSymbolIndices.length * (stepSize + 1)];

            int k = 0;
            for (int i = 0; i < cipherSymbolIndices.length; i ++) {
                int ciphertextIndex = cipherSymbolIndices[i];

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
                    logProbabilitiesUpdatedOversized[0][k] = index;
                    logProbabilitiesUpdatedOversized[1][k] = solution.getLogProbabilities()[index];

                    solution.replaceLogProbability(index, logProbability);
                    k++;
                }

                lastIndex = end;
            }

            logProbabilitiesUpdated = new float[2][k];
            java.lang.System.arraycopy(logProbabilitiesUpdatedOversized[0], 0, logProbabilitiesUpdated[0], 0, k);
            java.lang.System.arraycopy(logProbabilitiesUpdatedOversized[1], 0, logProbabilitiesUpdated[1], 0, k);
        } else {
            float[] logProbabilities = solution.getLogProbabilities();

            logProbabilitiesUpdated = new float[2][logProbabilities.length];

            for (int i = 0; i < logProbabilities.length; i ++) {
                logProbabilitiesUpdated[0][i] = i;
                logProbabilitiesUpdated[1][i] = logProbabilities[i];
            }

            solution.clearLogProbabilities();

            int k = 0;
            for (int i = 0; i < solutionString.length() - order; i += stepSize) {
                logProbability = computeNGramLogProbability(solutionString.substring(i, i + order));

                solution.addLogProbability(k, logProbability);
                k ++;
            }
        }

        return logProbabilitiesUpdated;
    }

    protected float computeNGramLogProbability(String ngram) {
        float match = letterMarkovModel.findExact(ngram);

        if (match != -1f) {
            log.debug("Letter N-Gram Match={}, Probability={}", ngram, match);
            return match;
        }

        log.debug("No Letter N-Gram Match for ngram={}", ngram);
        return letterMarkovModel.getUnknownLetterNGramLogProbability();
    }

    protected float computeIndexOfCoincidence(String solutionString) {
        resetLetterCounts();

        for (int i = 0; i < solutionString.length(); i++) {
            letterCounts[solutionString.charAt(i)] ++;
        }

        int numerator = buildNumerator();

        return (float) numerator / denominator;
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