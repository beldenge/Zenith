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
import com.ciphertool.zenith.model.markov.TreeMarkovModel;
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
    private TreeMarkovModel letterMarkovModel;

    @PostConstruct
    public void init() {
        List<TreeNGram> firstOrderNodes = new ArrayList<>(letterMarkovModel.getRootNode().getTransitions().values());

        for (TreeNGram node : firstOrderNodes) {
            double letterProbability = (double) node.getCount() / (double) letterMarkovModel.getRootNode().getCount();
            englishLetterCounts.put(node.getCumulativeString(), Math.round(letterProbability * cipher.length()));
        }
    }

    @Override
    public void evaluate(CipherSolution solution, String solutionString, String ciphertextKey) {
        long startLetter = System.currentTimeMillis();

        evaluateLetterNGrams(solution, solutionString, ciphertextKey);

        solution.setChiSquared(computeChiSquared(solutionString));

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

    protected double computeChiSquared(String solutionString) {
        Map<String, Long> solutionLetterCounts = new HashMap<>(LetterUtils.NUMBER_OF_LETTERS);

        for (Character c : LanguageConstants.LOWERCASE_LETTERS) {
            solutionLetterCounts.put(String.valueOf(c), 0L);
        }

        for (int i = 0; i < solutionString.length(); i ++) {
            String letter = String.valueOf(solutionString.charAt(i));

            solutionLetterCounts.put(letter, solutionLetterCounts.get(letter) + 1L);
        }

        List<Double> chiSquaredPerLetter = new ArrayList<>(LetterUtils.NUMBER_OF_LETTERS);

        for (String letter : englishLetterCounts.keySet()) {
            long actualCount = solutionLetterCounts.get(letter);
            long expectedCount = englishLetterCounts.get(letter);
            double numerator = Math.pow((double) (actualCount - expectedCount), 2.0);
            double denominator = Math.max(1d, expectedCount); // Prevent division by zero
            chiSquaredPerLetter.add(numerator / denominator);
        }

        return chiSquaredPerLetter.stream()
                .mapToDouble(perLetter -> perLetter.doubleValue())
                .sum();
    }

    protected Double computeIndexOfCoincidence(String solutionString) {
        int totalLetters = solutionString.length();

        List<Integer> counts = new ArrayList<>(LanguageConstants.LOWERCASE_LETTERS.size());

        int denominator = totalLetters * (totalLetters - 1);

        for (Character c : LanguageConstants.LOWERCASE_LETTERS) {
            int count = 0;

            for (int i = 0; i < totalLetters; i++) {
                if (c.equals(solutionString.charAt(i))) {
                    count++;
                }
            }

            counts.add(count);
        }

        int numerator = 0;
        for (Integer count : counts) {
            numerator += (count * (count - 1));
        }

        return (double) numerator / (double) denominator;
    }
}