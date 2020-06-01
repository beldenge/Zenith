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
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CiphertextNgramEvaluator {
    public Map<String, Integer> evaluate(Cipher cipher, int ngramSize) {
        Map<String, Integer> ngramCounts = new HashMap<>();

        int ciphertextSize = cipher.getCiphertextCharacters().size();

        if (ciphertextSize < ngramSize) {
            return ngramCounts;
        }

        Ciphertext[] ciphertextArray = new Ciphertext[ciphertextSize];

        for (int i = 0; i < ciphertextSize; i ++) {
            ciphertextArray[i] = cipher.getCiphertextCharacters().get(i);
        }

        for (int i = ngramSize - 1; i < ciphertextArray.length; i ++) {
            String ngram = "";

            boolean first = true;
            for (int j = ngramSize - 1; j >= 0; j --) {
                if (!first) {
                    ngram += " ";
                }

                first = false;

                ngram += ciphertextArray[i - j].getValue();
            }

            if (!ngramCounts.containsKey(ngram)) {
                ngramCounts.put(ngram, 0);
            }

            ngramCounts.put(ngram, ngramCounts.get(ngram) + 1);
        }

        return ngramCounts;
    }
}
