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

package com.ciphertool.zenith.inference.statistics;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CiphertextIndexOfCoincidenceEvaluator {
    public float evaluate(Cipher cipher) {
        float denominator = cipher.length() * (cipher.length() - 1);

        Map<String, Integer> ciphertextCounts = new HashMap<>();

        for (Ciphertext ciphertextCharacter : cipher.getCiphertextCharacters()) {
            String value = ciphertextCharacter.getValue();

            if (!ciphertextCounts.containsKey(value)) {
                ciphertextCounts.put(value, 0);
            }

            ciphertextCounts.put(value, ciphertextCounts.get(value) + 1);
        }

        float numerator = 0f;

        for (Map.Entry<String, Integer> entry : ciphertextCounts.entrySet()) {
            numerator += entry.getValue() * (entry.getValue() - 1);
        }

        return numerator / denominator;
    }
}
