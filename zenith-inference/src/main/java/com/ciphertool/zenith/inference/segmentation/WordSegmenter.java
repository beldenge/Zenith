/*
 * Copyright 2017-2026 George Belden
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

package com.ciphertool.zenith.inference.segmentation;

import com.ciphertool.zenith.model.markov.WordNGramModel;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @link http://practicalcryptography.com/cryptanalysis/text-characterisation/word-statistics-fitness-measure/
 */
@Component
public class WordSegmenter {
    private static final int MAX_WORD_LENGTH = 20;

    @Value("${language-model.word-ngram.total-token-count}")
    private long wordGramTotalTokenCount;

    @Autowired
    private WordNGramModel wordUnigramModel;

    @Autowired
    private WordNGramModel wordBigramModel;

    private double[] unseenNGramProbabilities = new double[MAX_WORD_LENGTH + 1];

    @PostConstruct
    public void init() {
        for (int i = 0; i < MAX_WORD_LENGTH + 1; i ++) {
            unseenNGramProbabilities[i] = Math.log10(10D / ((double) wordGramTotalTokenCount * Math.pow(10, i)));
        }
    }

    public Map.Entry<Double, String[]> score(String text) {
        text = text.toLowerCase();

        double[][] probabilities = new double[text.length()][MAX_WORD_LENGTH];
        String[][][] strings = new String[text.length()][MAX_WORD_LENGTH][];

        for (int i = 0; i < text.length(); i ++) {
            for (int j = 0; j < MAX_WORD_LENGTH; j ++) {
                probabilities[i][j] = Double.NEGATIVE_INFINITY;
                strings[i][j] = new String[] { "" };
            }
        }

        for (int i = 0; i < MAX_WORD_LENGTH; i ++) {
            String partial = text.substring(0, i + 1);
            probabilities[0][i] = getConditionalWordProbability(partial, null);
            strings[0][i] = new String[] { partial };
        }

        for (int i = 1; i < text.length(); i ++) {
            for (int j = 0; j < MAX_WORD_LENGTH; j ++) {
                if (i + j + 1 > text.length()) {
                    break;
                }

                Map<Double, String[]> candidates = new HashMap<>();
                String candidateString = text.substring(i, i + j + 1);
                for (int k = 0; k < Math.min(i, MAX_WORD_LENGTH); k ++) {
                    candidates.put(probabilities[i - k - 1][k] + getConditionalWordProbability(candidateString, strings[i - k - 1][k][strings[i - k - 1][k].length - 1]), ArrayUtils.add(strings[i - k - 1][k], candidateString));
                }

                Map.Entry<Double, String[]> bestCandidate = null;
                double bestProbability = Double.NEGATIVE_INFINITY;
                for (Map.Entry<Double, String[]> entry : candidates.entrySet()) {
                    if (entry.getKey() > bestProbability) {
                        bestProbability = entry.getKey();
                        bestCandidate = entry;
                    }
                }

                probabilities[i][j] = bestCandidate.getKey();
                strings[i][j] = bestCandidate.getValue();
            }
        }

        Map.Entry<Double, String[]> bestEnd = null;
        for (int i = 0; i < Math.min(text.length(), MAX_WORD_LENGTH); i ++) {
            if (bestEnd == null || probabilities[probabilities.length - i - 1][i] > bestEnd.getKey()) {
                bestEnd = new AbstractMap.SimpleEntry<>(probabilities[probabilities.length - i - 1][i], strings[strings.length - i - 1][i]);
            }
        }

        return bestEnd;
    }

    private double getConditionalWordProbability(String word, String previous) {
        if (!wordUnigramModel.contains(word)) {
            return unseenNGramProbabilities[word.length()];
        }

        if (previous == null || !wordBigramModel.contains(previous + " " + word)) {
            return wordUnigramModel.getLogProbability(word);
        }

        return wordBigramModel.getLogProbability(previous + " " + word);
    }
}
