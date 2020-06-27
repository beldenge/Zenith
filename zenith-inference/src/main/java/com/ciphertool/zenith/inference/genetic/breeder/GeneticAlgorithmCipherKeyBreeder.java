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

package com.ciphertool.zenith.inference.genetic.breeder;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.algorithms.StandardGeneticAlgorithm;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.inference.configuration.GeneticAlgorithmInitialization;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.optimizer.GeneticAlgorithmSolutionOptimizer;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeneticAlgorithmCipherKeyBreeder extends AbstractCipherKeyBreeder {
    private Logger log = LoggerFactory.getLogger(getClass());

    private List<PlaintextTransformationStep> plaintextTransformationSteps;
    private PlaintextEvaluator plaintextEvaluator;

    @Autowired
    private GeneticAlgorithmSolutionOptimizer optimizer;

    @Autowired
    private StandardGeneticAlgorithm geneticAlgorithm;

    @Autowired
    @Qualifier("nestedGeneticAlgorithmTaskExecutor")
    private TaskExecutor nestedGeneticAlgorithmTaskExecutor;

    @Override
    public void init(Cipher cipher, List<PlaintextTransformationStep> plaintextTransformationSteps, PlaintextEvaluator plaintextEvaluator) {
        super.init(cipher, plaintextTransformationSteps, plaintextEvaluator);

        this.plaintextTransformationSteps = plaintextTransformationSteps;
        this.plaintextEvaluator = plaintextEvaluator;
    }

    @Override
    public Chromosome breed() {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put(GeneticAlgorithmSolutionOptimizer.POPULATION_NAME, "StandardPopulation");
        configuration.put(GeneticAlgorithmSolutionOptimizer.BREEDER_NAME, "ProbabilisticCipherKeyBreeder");
        configuration.put(GeneticAlgorithmSolutionOptimizer.CROSSOVER_ALGORITHM_NAME, "GeneWiseCrossoverAlgorithm");
        configuration.put(GeneticAlgorithmSolutionOptimizer.MUTATION_ALGORITHM_NAME, "StandardMutationAlgorithm");
        configuration.put(GeneticAlgorithmSolutionOptimizer.SELECTOR_NAME, "TournamentSelector");

        GeneticAlgorithmInitialization initialization = optimizer.init(cipher, configuration, plaintextTransformationSteps, plaintextEvaluator);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(nestedGeneticAlgorithmTaskExecutor)
                .populationSize(100)
                .numberOfGenerations(500)
                .elitism(0)
                .population(initialization.getPopulation())
                .fitnessEvaluator(initialization.getFitnessEvaluator())
                .breeder(initialization.getBreeder())
                .crossoverAlgorithm(initialization.getCrossoverAlgorithm())
                .mutationAlgorithm(initialization.getMutationAlgorithm())
                .mutationRate(0.01)
                .selector(initialization.getSelector())
                .tournamentSelectorAccuracy(0.9)
                .tournamentSize(5)
                .shareFitness(false)
                .invasiveSpeciesCount(0)
                .build();

        strategy.getPopulation().init(strategy);

        log.info("Gene Production Started");

        geneticAlgorithm.evolve(strategy);

        log.info("Gene Produced");

        return initialization.getPopulation().getIndividuals().get(initialization.getPopulation().getIndividuals().size() - 1);
    }
}