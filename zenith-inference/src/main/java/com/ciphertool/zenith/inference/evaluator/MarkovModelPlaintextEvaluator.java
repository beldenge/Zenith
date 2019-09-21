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
import com.ciphertool.zenith.inference.util.LetterUtils;
import com.ciphertool.zenith.model.LanguageConstants;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.MapMarkovModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(value = "decipherment.evaluator.plaintext", havingValue = "MarkovModelPlaintextEvaluator")
public class MarkovModelPlaintextEvaluator implements PlaintextEvaluator {
    private Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, Long> englishLetterCounts = new HashMap<>(LetterUtils.NUMBER_OF_LETTERS);

    @Autowired
    private Cipher cipher;

    @Autowired
    private MapMarkovModel letterMarkovModel;

    private double denominator;
    private Map<Character, Integer> letterCounts = new HashMap<>(LanguageConstants.LOWERCASE_LETTERS.size());

    @PostConstruct
    public void init() {
        List<TreeNGram> firstOrderNodes = new ArrayList<>(letterMarkovModel.getFirstOrderNodes());

        for (TreeNGram node : firstOrderNodes) {
            double letterProbability = (double) node.getCount() / (double) letterMarkovModel.getTotalNumberOfNgrams();
            englishLetterCounts.put(node.getCumulativeString(), Math.round(letterProbability * cipher.length()));
        }

        denominator = cipher.length() * (cipher.length() - 1);
    }

    @Override
    public void evaluate(CipherSolution solution, String solutionString, String ciphertextKey) {
        long startLetter = System.currentTimeMillis();

        evaluateLetterNGrams(solution, solutionString, ciphertextKey);

        solution.setIndexOfCoincidence(computeIndexOfCoincidence(solutionString));

        log.debug("Letter N-Grams took {}ms.", (System.currentTimeMillis() - startLetter));
    }

    protected void evaluateLetterNGrams(CipherSolution solution, String solutionString, String ciphertextKey) {
        int order = letterMarkovModel.getOrder();

        Double logProbability;

        if (ciphertextKey != null) {
            List<Integer> ciphertextIndices = new ArrayList<>();
            for (int i = 0; i < solution.getCipher().getCiphertextCharacters().size(); i++) {
                if (ciphertextKey.equals(solution.getCipher().getCiphertextCharacters().get(i).getValue())) {
                    ciphertextIndices.add(i);
                }
            }

            Integer lastIndex = null;
            for (Integer ciphertextIndex : ciphertextIndices) {
                int start = Math.max(0, ciphertextIndex - (order - 1));
                int end = Math.min(solutionString.length() - order, ciphertextIndex + 1);

                if (lastIndex != null) {
                    start = Math.max(start, lastIndex);
                }

                for (int i = start; i < end; i++) {
                    logProbability = computeNGramLogProbability(solutionString.substring(i, i + order));

                    solution.replaceLogProbability(i, logProbability);
                }

                lastIndex = end;
            }
        } else {
            solution.clearLogProbabilities();

            for (int i = 0; i < solutionString.length() - order; i++) {
                logProbability = computeNGramLogProbability(solutionString.substring(i, i + order));

                solution.addLogProbability(logProbability);
            }
        }
    }

    protected Double computeNGramLogProbability(String ngram) {
        Double logProbability;
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

    protected Double computeIndexOfCoincidence(String solutionString) {
        int totalLetters = solutionString.length();

        for (Character letter : LanguageConstants.LOWERCASE_LETTERS) {
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