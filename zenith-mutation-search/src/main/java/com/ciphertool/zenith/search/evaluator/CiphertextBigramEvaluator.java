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

package com.ciphertool.zenith.search.evaluator;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CiphertextBigramEvaluator {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${decipherment.remove-last-row:true}")
    private boolean removeLastRow;

    public int evaluate(CipherSolution cipherProposal) {
        long startEvaluation = System.currentTimeMillis();

        Cipher cipher = cipherProposal.getCipher();
        int repeatingBigramCount = 0;
        int end = cipher.length();

        if (removeLastRow) {
            end = (cipher.getColumns() * (cipher.getRows() - 1));
        }

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

        double probability = (double) repeatingBigramCount / (double) (end - 1);
        cipherProposal.setProbability(probability);
        cipherProposal.clearLogProbabilities();
        cipherProposal.addLogProbability(Math.log(probability));

        log.debug("Cipher evaluation took {}ms.", (System.currentTimeMillis() - startEvaluation));

        return repeatingBigramCount;
    }
}
