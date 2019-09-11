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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CiphertextRowLevelEntropyEvaluator {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${decipherment.remove-last-row:true}")
    private boolean removeLastRow;

    public double evaluate(CipherSolution cipherProposal) {
        long startEvaluation = System.currentTimeMillis();

        Cipher cipher = cipherProposal.getCipher();

        int lastRow = cipher.getRows() - (removeLastRow ? 1 : 0);

        Double rowEntropyTotal = 0d;

        for (int i = 0; i < lastRow; i++) {
            Map<String, Integer> ciphertextCounts = new HashMap<>();

            for (int j = 0; j < cipher.getColumns(); j++) {
                String ciphertext = cipher.getCiphertextCharacters().get((i * cipher.getColumns()) + j).getValue();

                if (!ciphertextCounts.containsKey(ciphertext)) {
                    ciphertextCounts.put(ciphertext, 0);
                }

                ciphertextCounts.put(ciphertext, ciphertextCounts.get(ciphertext) + 1);
            }

            Double rowEntropy = 0d;

            for (Map.Entry<String, Integer> entry : ciphertextCounts.entrySet()) {
                Double ciphertextProbability = ((double) entry.getValue()) / (double) cipher.getColumns();

                rowEntropy += (ciphertextProbability * logBase(ciphertextProbability, 2));
            }

            rowEntropyTotal += rowEntropy;
        }

        log.debug("Cipher evaluation took {}ms.", (System.currentTimeMillis() - startEvaluation));

        return rowEntropyTotal * -1d;
    }

    // Use the change of base formula to calculate the logarithm with an arbitrary base
    static double logBase(double num, int base) {
        return (Math.log(num) / Math.log(base));
    }
}
