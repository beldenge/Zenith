/**
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

package com.ciphertool.zenith.inference.genetic.fitness;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.SolutionScorer;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.util.ChromosomeToCipherSolutionMapper;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationManager;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import com.ciphertool.zenith.inference.util.IndexOfCoincidenceEvaluator;

import java.util.List;

public class PlaintextEvaluatorWrappingFitnessEvaluator implements FitnessEvaluator {
    private PlaintextEvaluator plaintextEvaluator;
    private List<PlaintextTransformationStep> plaintextTransformationSteps;
    private IndexOfCoincidenceEvaluator indexOfCoincidenceEvaluator;
    private SolutionScorer solutionScorer;
    private PlaintextTransformationManager plaintextTransformationManager;

    public PlaintextEvaluatorWrappingFitnessEvaluator(PlaintextEvaluator plaintextEvaluator, PlaintextTransformationManager plaintextTransformationManager, List<PlaintextTransformationStep> plaintextTransformationSteps, IndexOfCoincidenceEvaluator indexOfCoincidenceEvaluator, SolutionScorer solutionScorer) {
        this.plaintextEvaluator = plaintextEvaluator;
        this.plaintextTransformationManager = plaintextTransformationManager;
        this.plaintextTransformationSteps = plaintextTransformationSteps;
        this.indexOfCoincidenceEvaluator = indexOfCoincidenceEvaluator;
        this.solutionScorer = solutionScorer;
    }

    @Override
    public Double evaluate(Chromosome chromosome) {
        CipherSolution proposal = ChromosomeToCipherSolutionMapper.map(chromosome);

        String solutionString = proposal.asSingleLineString();

        if (plaintextTransformationSteps != null && !plaintextTransformationSteps.isEmpty()) {
            solutionString = plaintextTransformationManager.transform(solutionString, plaintextTransformationSteps);
        }

        Cipher cipher = ((CipherKeyChromosome) chromosome).getCipher();
        plaintextEvaluator.evaluate(cipher, proposal, solutionString, null);
        proposal.setIndexOfCoincidence(indexOfCoincidenceEvaluator.evaluate(cipher, solutionString));
        proposal.setScore(solutionScorer.score(proposal));

        return Double.valueOf(proposal.getScore());
    }
}
