/*
 * Copyright 2017-2020 George Belden
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
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IndexOfCoincidenceEvaluator implements CounterweightEvaluator {
    private static final String LETTER_COUNTS_KEY = "letterCounts";
    private static final String PRECOMPUTED_NOMINATOR_VALUES_KEY = "precomputedNominatorValues";
    private static final String DENOMINATOR_KEY = "denominator";

    @Override
    public Map<String, Object> precompute(Cipher cipher) {
        Map<String, Object> precomputedData = new HashMap<>(3);

        int[] precomputedNominatorValues = new int[cipher.length() + 1];

        for (int i = 0; i <= cipher.length(); i ++) {
            precomputedNominatorValues[i] = i * (i - 1);
        }

        // Since we are using only ASCII letters as array indices, we're guaranteed to stay within 256
        precomputedData.put(LETTER_COUNTS_KEY, new int[256]);
        precomputedData.put(DENOMINATOR_KEY, (float) cipher.length() * (cipher.length() - 1));
        precomputedData.put(PRECOMPUTED_NOMINATOR_VALUES_KEY, precomputedNominatorValues);

        return precomputedData;
    }

    @Override
    public float evaluate(Map<String, Object> precomputedData, Cipher cipher, String solutionString) {
        if (precomputedData == null) {
            precomputedData = precompute(cipher);
        }

        int[] letterCounts = (int[]) precomputedData.get(LETTER_COUNTS_KEY);
        int[] precomputedNominatorValues = (int[]) precomputedData.get(PRECOMPUTED_NOMINATOR_VALUES_KEY);
        float denominator = (float) precomputedData.get(DENOMINATOR_KEY);

        resetLetterCounts(letterCounts);

        for (int i = 0; i < solutionString.length(); i++) {
            letterCounts[solutionString.charAt(i)] ++;
        }

        return (float) buildNumerator(precomputedNominatorValues, letterCounts) / denominator;
    }

    private void resetLetterCounts(int[] letterCounts) {
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

    private int buildNumerator(int[] precomputedNominatorValues, int[] letterCounts) {
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
