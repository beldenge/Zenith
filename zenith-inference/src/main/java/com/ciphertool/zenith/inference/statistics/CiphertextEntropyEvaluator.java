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

package com.ciphertool.zenith.inference.statistics;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.inference.util.MathUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CiphertextEntropyEvaluator {
    private static float BASE = 2f; // Assuming we want the unit of entropy to be 'bit'

    public float evaluate(Cipher cipher) {
        Map<String, Integer> ciphertextCounts = new HashMap<>();

        for (Ciphertext ciphertextCharacter : cipher.getCiphertextCharacters()) {
            String value = ciphertextCharacter.getValue();

            if (!ciphertextCounts.containsKey(value)) {
                ciphertextCounts.put(value, 0);
            }

            ciphertextCounts.put(value, ciphertextCounts.get(value) + 1);
        }

        float sum = 0f;

        for (Map.Entry<String, Integer> entry : ciphertextCounts.entrySet()) {
            if (entry.getValue() == 0) {
                continue;
            }

            float probability = (float) entry.getValue() / (float) cipher.length();

            sum += probability * MathUtils.logBase(probability, BASE);
        }

        return sum * -1.0f;
    }
}
