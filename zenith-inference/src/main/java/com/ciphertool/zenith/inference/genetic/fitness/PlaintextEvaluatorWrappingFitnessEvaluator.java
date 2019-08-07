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
import com.ciphertool.zenith.inference.genetic.util.ChromosomeToCipherSolutionMapper;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;

import java.util.List;

public class PlaintextEvaluatorWrappingFitnessEvaluator implements FitnessEvaluator {
    private PlaintextEvaluator plaintextEvaluator;
    private List<PlaintextTransformer> plaintextTransformers;

    public PlaintextEvaluatorWrappingFitnessEvaluator(PlaintextEvaluator plaintextEvaluator, List<PlaintextTransformer> plaintextTransformers) {
        this.plaintextEvaluator = plaintextEvaluator;
        this.plaintextTransformers = plaintextTransformers;
    }

    @Override
    public Double evaluate(Chromosome chromosome) {
        CipherSolution proposal = ChromosomeToCipherSolutionMapper.map(chromosome);

        String solutionString = proposal.asSingleLineString();
        for (PlaintextTransformer plaintextTransformer : plaintextTransformers) {
            solutionString = plaintextTransformer.transform(solutionString);
        }

        plaintextEvaluator.evaluate(proposal, solutionString, null);

        // TODO: make the decision whether to do the conversion based on the selector method?
        // In other words, negative log probabilities work just fine with the TournamentSelector, but we must use
        // positive numbers for the RouletteSelector
        return convertNegativeLogProbabilityToPositiveScore(proposal.getScore());
    }

    /*
     * We are applying the function: (x - 1000000) / (x / 100)
     * Model it in a graphing calculator (i.e. paste it into Google) to see what the curve looks like
     */
    private static Double convertNegativeLogProbabilityToPositiveScore(Double negativeLogProbability) {
        return negativeLogProbability; // (negativeLogProbability - 1000000d) / (negativeLogProbability / 100d);
    }
}
