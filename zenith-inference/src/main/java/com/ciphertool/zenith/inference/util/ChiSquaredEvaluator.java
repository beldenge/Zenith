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

package com.ciphertool.zenith.inference.util;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.model.LanguageConstants;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChiSquaredEvaluator implements CounterweightEvaluator {
    private static final String PRECOMPUTED_CHI_SQUAREDS_KEY = "precomputedChiSquareds";

    @Autowired
    private ArrayMarkovModel letterMarkovModel;

    @Override
    public Map<String, Object> precompute(Cipher cipher) {
        Map<String, Object> precomputedData = new HashMap<>(3);

        // Since we are using only ASCII letters as array indices, we're guaranteed to stay within 256
        int[] englishLetterCounts = new int[256];

        for (TreeNGram node : letterMarkovModel.getFirstOrderNodes()) {
            float letterProbability = (float) node.getCount() / (float) letterMarkovModel.getTotalNGramCount();
            englishLetterCounts[node.getCumulativeString().charAt(0)] = Math.round(letterProbability * cipher.length());
        }

        float[][] precomputedChiSquareds = new float[256][cipher.length() + 1];

        for (int i = 0; i < LanguageConstants.LOWERCASE_LETTERS.length; i ++) {
            for (int j = 0; j <= cipher.length(); j ++) {
                long actualCount = j;
                long expectedCount = englishLetterCounts[LanguageConstants.LOWERCASE_LETTERS[i]];
                float numerator = (float) Math.pow((float) (actualCount - expectedCount), 2.0f);
                float denominator = Math.max(1f, expectedCount); // Prevent division by zero
                precomputedChiSquareds[LanguageConstants.LOWERCASE_LETTERS[i]][j] = numerator / denominator;
            }
        }

        precomputedData.put(PRECOMPUTED_CHI_SQUAREDS_KEY, precomputedChiSquareds);

        return precomputedData;
    }

    @Override
    public float evaluate(Map<String, Object> precomputedData, Cipher cipher, String solutionString) {
        if (precomputedData == null) {
            precomputedData = precompute(cipher);
        }

        float[][] precomputedChiSquareds = (float[][]) precomputedData.get(PRECOMPUTED_CHI_SQUAREDS_KEY);

        int[] actualLetterCounts = new int[256];

        resetLetterCounts(actualLetterCounts);

        for (int i = 0; i < solutionString.length(); i++) {
            actualLetterCounts[solutionString.charAt(i)] ++;
        }

        return computeSum(precomputedChiSquareds, actualLetterCounts);
    }

    private void resetLetterCounts(int[] actualLetterCounts) {
        actualLetterCounts['a'] = 0;
        actualLetterCounts['b'] = 0;
        actualLetterCounts['c'] = 0;
        actualLetterCounts['d'] = 0;
        actualLetterCounts['e'] = 0;
        actualLetterCounts['f'] = 0;
        actualLetterCounts['g'] = 0;
        actualLetterCounts['h'] = 0;
        actualLetterCounts['i'] = 0;
        actualLetterCounts['j'] = 0;
        actualLetterCounts['k'] = 0;
        actualLetterCounts['l'] = 0;
        actualLetterCounts['m'] = 0;
        actualLetterCounts['n'] = 0;
        actualLetterCounts['o'] = 0;
        actualLetterCounts['p'] = 0;
        actualLetterCounts['q'] = 0;
        actualLetterCounts['r'] = 0;
        actualLetterCounts['s'] = 0;
        actualLetterCounts['t'] = 0;
        actualLetterCounts['u'] = 0;
        actualLetterCounts['v'] = 0;
        actualLetterCounts['w'] = 0;
        actualLetterCounts['x'] = 0;
        actualLetterCounts['y'] = 0;
        actualLetterCounts['z'] = 0;
    }

    private float computeSum(float[][] precomputedChiSquareds, int[] actualLetterCounts) {
        float sum = 0f;
        sum += precomputedChiSquareds['a'][actualLetterCounts['a']];
        sum += precomputedChiSquareds['b'][actualLetterCounts['b']];
        sum += precomputedChiSquareds['c'][actualLetterCounts['c']];
        sum += precomputedChiSquareds['d'][actualLetterCounts['d']];
        sum += precomputedChiSquareds['e'][actualLetterCounts['e']];
        sum += precomputedChiSquareds['f'][actualLetterCounts['f']];
        sum += precomputedChiSquareds['g'][actualLetterCounts['g']];
        sum += precomputedChiSquareds['h'][actualLetterCounts['h']];
        sum += precomputedChiSquareds['i'][actualLetterCounts['i']];
        sum += precomputedChiSquareds['j'][actualLetterCounts['j']];
        sum += precomputedChiSquareds['k'][actualLetterCounts['k']];
        sum += precomputedChiSquareds['l'][actualLetterCounts['l']];
        sum += precomputedChiSquareds['m'][actualLetterCounts['m']];
        sum += precomputedChiSquareds['n'][actualLetterCounts['n']];
        sum += precomputedChiSquareds['o'][actualLetterCounts['o']];
        sum += precomputedChiSquareds['p'][actualLetterCounts['p']];
        sum += precomputedChiSquareds['q'][actualLetterCounts['q']];
        sum += precomputedChiSquareds['r'][actualLetterCounts['r']];
        sum += precomputedChiSquareds['s'][actualLetterCounts['s']];
        sum += precomputedChiSquareds['t'][actualLetterCounts['t']];
        sum += precomputedChiSquareds['u'][actualLetterCounts['u']];
        sum += precomputedChiSquareds['v'][actualLetterCounts['v']];
        sum += precomputedChiSquareds['w'][actualLetterCounts['w']];
        sum += precomputedChiSquareds['x'][actualLetterCounts['x']];
        sum += precomputedChiSquareds['y'][actualLetterCounts['y']];
        sum += precomputedChiSquareds['z'][actualLetterCounts['z']];

        return sum;
    }
}
