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
        if (plaintextTransformers != null) {
            for (PlaintextTransformer plaintextTransformer : plaintextTransformers) {
                solutionString = plaintextTransformer.transform(solutionString);
            }
        }

        plaintextEvaluator.evaluate(proposal, solutionString, null);

        return Double.valueOf(proposal.getScore());
    }
}
