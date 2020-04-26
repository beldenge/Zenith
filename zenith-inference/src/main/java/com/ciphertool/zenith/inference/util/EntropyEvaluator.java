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
public class EntropyEvaluator {
    private static float BASE = 2f; // Assuming we want the unit of entropy to be 'bit'

    private double[] precomputedEntropies;

    private Cipher initialized = null;

    private void init(Cipher cipher) {
        precomputedEntropies = new double[cipher.length()];

        for (int i = 0; i < cipher.length(); i ++) {
            float probability = ((float) i / 1f);
            precomputedEntropies[i] = Math.abs(MathUtils.logBase(probability, BASE) * probability);
        }

        initialized = cipher;
    }

    public float evaluate(Cipher cipher, String solutionString) {
        if (initialized == null || initialized != cipher) {
            init(cipher);
        }

        Map<Character, Integer> plaintextCounts = new HashMap<>(LetterUtils.NUMBER_OF_LETTERS);

        for (int i = 0; i < solutionString.length(); i ++) {
            char value = solutionString.charAt(i);

            if (!plaintextCounts.containsKey(value)) {
                plaintextCounts.put(value, 0);
            }

            plaintextCounts.put(value, plaintextCounts.get(value) + 1);
        }

        float sum = 0f;

        for (Map.Entry<Character, Integer> entry : plaintextCounts.entrySet()) {
            if (entry.getValue() == 0) {
                continue;
            }

            sum += precomputedEntropies[entry.getValue()];
        }

        return sum;
    }
}
