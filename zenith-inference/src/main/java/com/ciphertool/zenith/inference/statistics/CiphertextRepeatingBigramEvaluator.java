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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CiphertextRepeatingBigramEvaluator {
    private Logger log = LoggerFactory.getLogger(getClass());

    public int evaluate(Cipher cipher) {
        long startEvaluation = System.currentTimeMillis();

        int repeatingBigramCount = 0;
        int end = cipher.length();

        Map<String, Integer> repeatingBigramCounts = new HashMap<>();

        for (int i = 0; i < end - 1; i++) {
            Ciphertext first = cipher.getCiphertextCharacters().get(i);
            Ciphertext second = cipher.getCiphertextCharacters().get(i + 1);

            String bigram = first.getValue() + second.getValue();

            if (!repeatingBigramCounts.containsKey(bigram)) {
                repeatingBigramCounts.put(bigram, 0);
            }

            repeatingBigramCounts.put(bigram, repeatingBigramCounts.get(bigram) + 1);
        }

        for (Integer count : repeatingBigramCounts.values()) {
            repeatingBigramCount += count - 1;
        }

        log.debug("Cipher evaluation took {}ms.", (System.currentTimeMillis() - startEvaluation));

        return repeatingBigramCount;
    }
}
