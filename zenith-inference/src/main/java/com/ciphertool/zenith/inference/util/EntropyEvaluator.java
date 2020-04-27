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

@Component
public class EntropyEvaluator {
    private static float BASE = 2f; // Assuming we want the unit of entropy to be 'bit'

    // Since we are using only ASCII letters as array indices, we're guaranteed to stay within 256
    private int[] letterCounts = new int[256];
    private double[] precomputedEntropies;

    private Cipher initialized = null;

    private void init(Cipher cipher) {
        precomputedEntropies = new double[cipher.length() + 1];
        precomputedEntropies[0] = 0f;

        for (int i = 1; i <= cipher.length(); i ++) {
            float probability = ((float) i / (float) cipher.length());
            precomputedEntropies[i] = Math.abs(MathUtils.logBase(probability, BASE) * probability);
        }

        initialized = cipher;
    }

    public float evaluate(Cipher cipher, String solutionString) {
        if (initialized == null || initialized != cipher) {
            init(cipher);
        }

        resetLetterCounts();

        for (int i = 0; i < solutionString.length(); i++) {
            letterCounts[solutionString.charAt(i)] ++;
        }

        return computeSum();
    }

    private void resetLetterCounts() {
        // TODO: see if Arrays.fill is any faster/slower
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

    private float computeSum() {
        float sum = 0f;
        sum += precomputedEntropies[letterCounts['a']];
        sum += precomputedEntropies[letterCounts['b']];
        sum += precomputedEntropies[letterCounts['c']];
        sum += precomputedEntropies[letterCounts['d']];
        sum += precomputedEntropies[letterCounts['e']];
        sum += precomputedEntropies[letterCounts['f']];
        sum += precomputedEntropies[letterCounts['g']];
        sum += precomputedEntropies[letterCounts['h']];
        sum += precomputedEntropies[letterCounts['i']];
        sum += precomputedEntropies[letterCounts['j']];
        sum += precomputedEntropies[letterCounts['k']];
        sum += precomputedEntropies[letterCounts['l']];
        sum += precomputedEntropies[letterCounts['m']];
        sum += precomputedEntropies[letterCounts['n']];
        sum += precomputedEntropies[letterCounts['o']];
        sum += precomputedEntropies[letterCounts['p']];
        sum += precomputedEntropies[letterCounts['q']];
        sum += precomputedEntropies[letterCounts['r']];
        sum += precomputedEntropies[letterCounts['s']];
        sum += precomputedEntropies[letterCounts['t']];
        sum += precomputedEntropies[letterCounts['u']];
        sum += precomputedEntropies[letterCounts['v']];
        sum += precomputedEntropies[letterCounts['w']];
        sum += precomputedEntropies[letterCounts['x']];
        sum += precomputedEntropies[letterCounts['y']];
        sum += precomputedEntropies[letterCounts['z']];

        return sum;
    }
}
