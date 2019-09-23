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
import com.ciphertool.zenith.model.LanguageConstants;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.MapMarkovModel;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
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

    private double denominator;
    private Char2IntMap letterCounts = new Char2IntOpenHashMap(LanguageConstants.LOWERCASE_LETTERS_SIZE);

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
        int totalLetters = solutionString.length();

        for (char letter : LanguageConstants.LOWERCASE_LETTERS) {
            letterCounts.put(letter, 0);
        }

        for (int i = 0; i < totalLetters; i++) {
            char nextLetter = solutionString.charAt(i);
            letterCounts.put(nextLetter, letterCounts.get(nextLetter) + 1);
        }

        int numerator = 0;
        for (Integer count : letterCounts.values()) {
            numerator += (count * (count - 1));
        }

        return (double) numerator / denominator;
    }
}