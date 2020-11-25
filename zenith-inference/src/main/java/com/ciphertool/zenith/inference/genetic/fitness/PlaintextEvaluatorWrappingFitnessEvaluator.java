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

package com.ciphertool.zenith.inference.genetic.fitness;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.model.SolutionScore;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.util.ChromosomeToCipherSolutionMapper;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationManager;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;

public class PlaintextEvaluatorWrappingFitnessEvaluator implements FitnessEvaluator {
    private PlaintextEvaluator plaintextEvaluator;
    private List<PlaintextTransformationStep> plaintextTransformationSteps;
    private PlaintextTransformationManager plaintextTransformationManager;
    private Map<String, Object> precomputedCounterweightData;

    public PlaintextEvaluatorWrappingFitnessEvaluator(Map<String, Object> precomputedCounterweightData, PlaintextEvaluator plaintextEvaluator, PlaintextTransformationManager plaintextTransformationManager, List<PlaintextTransformationStep> plaintextTransformationSteps) {
        this.precomputedCounterweightData = precomputedCounterweightData;
        this.plaintextEvaluator = plaintextEvaluator;
        this.plaintextTransformationManager = plaintextTransformationManager;
        this.plaintextTransformationSteps = plaintextTransformationSteps;
    }

    @Override
    public Fitness[] evaluate(Genome genome) {
        // There's only one chromosome for this type of Genome
        Chromosome chromosome = genome.getChromosomes().get(0);

        CipherSolution proposal = ChromosomeToCipherSolutionMapper.map(chromosome);

        String solutionString = proposal.asSingleLineString();

        if (CollectionUtils.isNotEmpty(plaintextTransformationSteps)) {
            solutionString = plaintextTransformationManager.transform(solutionString, plaintextTransformationSteps);
        }

        Cipher cipher = ((CipherKeyChromosome) chromosome).getCipher();
        SolutionScore score = plaintextEvaluator.evaluate(precomputedCounterweightData, cipher, proposal, solutionString, null);
        proposal.setScores(score.getScores());

        return score.getScores();
    }
}
