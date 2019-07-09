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

package com.ciphertool.zenith.inference.genetic.fitness;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;

public class PlaintextEvaluatorWrappingFitnessEvaluator implements FitnessEvaluator {
    private PlaintextEvaluator plaintextEvaluator;

    public PlaintextEvaluatorWrappingFitnessEvaluator(PlaintextEvaluator plaintextEvaluator) {
        this.plaintextEvaluator = plaintextEvaluator;
    }

    @Override
    public Double evaluate(Chromosome chromosome) {
        CipherSolution cipherSolution = ChromosomeToCipherSolutionMapper.map(chromosome);

        plaintextEvaluator.evaluate(cipherSolution, null);

        return convertNegativeLogProbabilityToPositiveScore(cipherSolution.getScore());
    }

    /*
     * We are applying the function: (x - 1000000) / (x / 100)
     * Model it in a graphing calculator (i.e. paste it into Google) to see what the curve looks like
     */
    private static Double convertNegativeLogProbabilityToPositiveScore(Double negativeLogProbability) {
        return (negativeLogProbability - 1000000d) / (negativeLogProbability / 100d);
    }
}
