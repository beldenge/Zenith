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

package com.ciphertool.zenith.inference.evaluator;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;

public class AbstractNgramEvaluator {
    private int order;
    private int stepSize;
    private int doubleStepSize;

    @Autowired
    protected ArrayMarkovModel letterMarkovModel;

    @PostConstruct
    public void init() {
        order = letterMarkovModel.getOrder();
        stepSize = order / 2;
        doubleStepSize = stepSize * 2;
    }

    protected float[][] evaluateLetterNGrams(Cipher cipher, CipherSolution solution, String solutionString, String ciphertextKey) {
        int stringLengthMinusOrder = solutionString.length() - order;

        float[][] logProbabilitiesUpdated;
        int lastIndex = -1;

        if (ciphertextKey != null) {
            int[] cipherSymbolIndices = cipher.getCipherSymbolIndicesMap().get(ciphertextKey);
            logProbabilitiesUpdated = new float[2][cipherSymbolIndices.length * (stepSize + 1)];

            int k = 0;
            for (int i = 0; i < cipherSymbolIndices.length; i ++) {
                int ciphertextIndex = cipherSymbolIndices[i];

                int wayBack = ciphertextIndex - (ciphertextIndex % stepSize) - doubleStepSize;
                if (wayBack + order <= ciphertextIndex) {
                    wayBack += stepSize;
                }

                int start = Math.max(0, wayBack);
                int end = Math.min(stringLengthMinusOrder, ciphertextIndex + 1);

                if (lastIndex >= 0 && start < lastIndex) {
                    continue;
                }

                int index;
                for (int j = start; j < end; j += stepSize) {
                    index = j / stepSize;
                    logProbabilitiesUpdated[0][k] = index;
                    logProbabilitiesUpdated[1][k] = solution.getLogProbability(index);

                    solution.replaceLogProbability(index, letterMarkovModel.findExact(solutionString.substring(j, j + order)));
                    k++;
                }

                lastIndex = end;
            }
        } else {
            float[] logProbabilities = solution.getLogProbabilities();

            logProbabilitiesUpdated = new float[2][logProbabilities.length];

            for (int i = 0; i < logProbabilities.length; i ++) {
                logProbabilitiesUpdated[0][i] = i;
                logProbabilitiesUpdated[1][i] = logProbabilities[i];
            }

            solution.clearLogProbabilities();

            int k = 0;
            for (int i = 0; i < solutionString.length() - order; i += stepSize) {
                solution.addLogProbability(k, letterMarkovModel.findExact(solutionString.substring(i, i + order)));
                k ++;
            }
        }

        return logProbabilitiesUpdated;
    }
}
