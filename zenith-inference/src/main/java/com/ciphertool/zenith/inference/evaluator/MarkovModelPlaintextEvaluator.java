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
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.MapMarkovModel;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@ConditionalOnProperty(value = "decipherment.evaluator.plaintext", havingValue = "MarkovModelPlaintextEvaluator")
public class MarkovModelPlaintextEvaluator implements PlaintextEvaluator {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private Cipher cipher;

    @Autowired
    private MapMarkovModel letterMarkovModel;

    private int[] letterCountsArray = new int[256];
    private double denominator;

    @PostConstruct
    public void init() {
        denominator = cipher.length() * (cipher.length() - 1);
    }

    @Override
    public Int2DoubleMap evaluate(CipherSolution solution, String solutionString, String ciphertextKey) {
        long startLetter = System.currentTimeMillis();

        Int2DoubleMap logProbabilitiesUpdated = evaluateLetterNGrams(solution, solutionString, ciphertextKey);

        solution.setIndexOfCoincidence(computeIndexOfCoincidence(solutionString));

        log.debug("Letter N-Grams took {}ms.", (System.currentTimeMillis() - startLetter));

        return logProbabilitiesUpdated;
    }

    protected Int2DoubleMap evaluateLetterNGrams(CipherSolution solution, String solutionString, String ciphertextKey) {
        int order = letterMarkovModel.getOrder();
        int stepSize = order / 2;

        Int2DoubleMap logProbabilitiesUpdated = new Int2DoubleOpenHashMap();
        double logProbability;

        if (ciphertextKey != null) {
            IntList ciphertextIndices = new IntArrayList();
            List<Ciphertext> ciphertextCharacters = solution.getCipher().getCiphertextCharacters();
            for (int i = 0; i < ciphertextCharacters.size(); i++) {
                if (ciphertextKey.equals(ciphertextCharacters.get(i).getValue())) {
                    ciphertextIndices.add(i);
                }
            }

            Integer lastIndex = null;
            for (int ciphertextIndex : ciphertextIndices) {
                int wayBack = ciphertextIndex - (ciphertextIndex % stepSize) - (stepSize * 2);
                if (wayBack + order <= ciphertextIndex) {
                    wayBack += stepSize;
                }

                int start = Math.max(0, wayBack);
                int end = Math.min(solutionString.length() - order, ciphertextIndex + 1);

                if (lastIndex != null && start < lastIndex) {
                    continue;
                }

                for (int i = start; i < end; i += stepSize) {
                    logProbability = computeNGramLogProbability(solutionString.substring(i, i + order));

                    logProbabilitiesUpdated.put(i / stepSize, solution.getLogProbabilities().getDouble(i / stepSize));
                    solution.replaceLogProbability(i / stepSize, logProbability);
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
        double logProbability;
        TreeNGram match = letterMarkovModel.findExact(ngram);

        if (match != null) {
            logProbability = match.getLogProbability();
            log.debug("Letter N-Gram Match={}, Probability={}", match.getCumulativeString(), logProbability);
        } else {
            logProbability = letterMarkovModel.getUnknownLetterNGramLogProbability();
            log.debug("No Letter N-Gram Match");
        }

        return logProbability;
    }

    protected double computeIndexOfCoincidence(String solutionString) {
        resetLetterCounts();

        for (int i = 0; i < solutionString.length(); i++) {
            letterCountsArray[solutionString.charAt(i)] ++;
        }

        int numerator = buildNumerator();

        return (double) numerator / denominator;
    }

    private void resetLetterCounts() {
        letterCountsArray['a'] = 0;
        letterCountsArray['b'] = 0;
        letterCountsArray['c'] = 0;
        letterCountsArray['d'] = 0;
        letterCountsArray['e'] = 0;
        letterCountsArray['f'] = 0;
        letterCountsArray['g'] = 0;
        letterCountsArray['h'] = 0;
        letterCountsArray['i'] = 0;
        letterCountsArray['j'] = 0;
        letterCountsArray['k'] = 0;
        letterCountsArray['l'] = 0;
        letterCountsArray['m'] = 0;
        letterCountsArray['n'] = 0;
        letterCountsArray['o'] = 0;
        letterCountsArray['p'] = 0;
        letterCountsArray['q'] = 0;
        letterCountsArray['r'] = 0;
        letterCountsArray['s'] = 0;
        letterCountsArray['t'] = 0;
        letterCountsArray['u'] = 0;
        letterCountsArray['v'] = 0;
        letterCountsArray['w'] = 0;
        letterCountsArray['x'] = 0;
        letterCountsArray['y'] = 0;
        letterCountsArray['z'] = 0;
    }

    private int buildNumerator() {
        int numerator = 0;
        numerator += (letterCountsArray['a'] * (letterCountsArray['a'] - 1));
        numerator += (letterCountsArray['b'] * (letterCountsArray['b'] - 1));
        numerator += (letterCountsArray['c'] * (letterCountsArray['c'] - 1));
        numerator += (letterCountsArray['d'] * (letterCountsArray['d'] - 1));
        numerator += (letterCountsArray['e'] * (letterCountsArray['e'] - 1));
        numerator += (letterCountsArray['f'] * (letterCountsArray['f'] - 1));
        numerator += (letterCountsArray['g'] * (letterCountsArray['g'] - 1));
        numerator += (letterCountsArray['h'] * (letterCountsArray['h'] - 1));
        numerator += (letterCountsArray['i'] * (letterCountsArray['i'] - 1));
        numerator += (letterCountsArray['j'] * (letterCountsArray['j'] - 1));
        numerator += (letterCountsArray['k'] * (letterCountsArray['k'] - 1));
        numerator += (letterCountsArray['l'] * (letterCountsArray['l'] - 1));
        numerator += (letterCountsArray['m'] * (letterCountsArray['m'] - 1));
        numerator += (letterCountsArray['n'] * (letterCountsArray['n'] - 1));
        numerator += (letterCountsArray['o'] * (letterCountsArray['o'] - 1));
        numerator += (letterCountsArray['p'] * (letterCountsArray['p'] - 1));
        numerator += (letterCountsArray['q'] * (letterCountsArray['q'] - 1));
        numerator += (letterCountsArray['r'] * (letterCountsArray['r'] - 1));
        numerator += (letterCountsArray['s'] * (letterCountsArray['s'] - 1));
        numerator += (letterCountsArray['t'] * (letterCountsArray['t'] - 1));
        numerator += (letterCountsArray['u'] * (letterCountsArray['u'] - 1));
        numerator += (letterCountsArray['v'] * (letterCountsArray['v'] - 1));
        numerator += (letterCountsArray['w'] * (letterCountsArray['w'] - 1));
        numerator += (letterCountsArray['x'] * (letterCountsArray['x'] - 1));
        numerator += (letterCountsArray['y'] * (letterCountsArray['y'] - 1));
        numerator += (letterCountsArray['z'] * (letterCountsArray['z'] - 1));

        return numerator;
    }
}