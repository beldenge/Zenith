/**
 * Copyright 2017-2019 George Belden
 * <p>
 * This file is part of Zenith.
 * <p>
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.inference.optimizer;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.algorithms.StandardGeneticAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.crossover.CrossoverAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.mutation.MutationAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.selection.modes.Selector;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.genetic.fitness.PlaintextEvaluatorWrappingFitnessEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value="decipherment.optimizer", havingValue = "GeneticAlgorithmSolutionOptimizer")
public class GeneticAlgorithmSolutionOptimizer extends AbstractSolutionOptimizer implements SolutionOptimizer {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${genetic-algorithm.population.size}")
    private int populationSize;

    @Value("${genetic-algorithm.number-of-generations}")
    private int numberOfGenerations;

    @Value("${genetic-algorithm.elitism}")
    private int elitism;

    @Value("${genetic-algorithm.mutation.rate}")
    private Double mutationRate;

    @Value("${genetic-algorithm.mutation.max-per-individual}")
    private int maxMutationsPerIndividual;

    @Value("${genetic-algorithm.crossover.implementation}")
    private String crossoverAlgorithmName;

    @Value("${genetic-algorithm.mutation.implementation}")
    private String mutationAlgorithmName;

    @Value("${genetic-algorithm.selection.implementation}")
    private String selectorName;

    @Value("${genetic-algorithm.fitness.implementation}")
    private String fitnessEvaluatorName;

    @Autowired
    private StandardGeneticAlgorithm geneticAlgorithm;

    @Autowired
    private List<CrossoverAlgorithm> crossoverAlgorithms;

    @Autowired
    private List<MutationAlgorithm> mutationAlgorithms;

    @Autowired
    private List<Selector> selectors;

    @Autowired
    private List<PlaintextEvaluator> plaintextEvaluators;

    private CrossoverAlgorithm crossoverAlgorithm;

    private MutationAlgorithm mutationAlgorithm;

    private Selector selector;

    private FitnessEvaluator fitnessEvaluator;

    @PostConstruct
    public void init() {
        // Set the proper CrossoverAlgorithm
        List<String> existentCrossoverAlgorithms = crossoverAlgorithms.stream()
                .map(crossoverAlgorithm -> crossoverAlgorithm.getClass().getSimpleName())
                .collect(Collectors.toList());

        for (CrossoverAlgorithm crossoverAlgorithm : crossoverAlgorithms) {
            if (crossoverAlgorithm.getClass().getSimpleName().equals(crossoverAlgorithmName)) {
                this.crossoverAlgorithm = crossoverAlgorithm;
                break;
            }
        }

        if (crossoverAlgorithm == null) {
            log.error("The CrossoverAlgorithm with name {} does not exist.  Please use a name from the following: {}", crossoverAlgorithmName, existentCrossoverAlgorithms);
            throw new IllegalArgumentException("The CrossoverAlgorithm with name " + crossoverAlgorithmName + " does not exist.");
        }

        // Set the proper MutationAlgorithm
        List<String> existentMutationAlgorithms = mutationAlgorithms.stream()
                .map(mutationAlgorithm ->  mutationAlgorithm.getClass().getSimpleName())
                .collect(Collectors.toList());

        for (MutationAlgorithm mutationAlgorithm : mutationAlgorithms) {
            if (mutationAlgorithm.getClass().getSimpleName().equals(mutationAlgorithmName)) {
                this.mutationAlgorithm = mutationAlgorithm;
                break;
            }
        }

        if (mutationAlgorithm == null) {
            log.error("The MutationAlgorithm with name {} does not exist.  Please use a name from the following: {}", mutationAlgorithmName, existentMutationAlgorithms);
            throw new IllegalArgumentException("The MutationAlgorithm with name " + mutationAlgorithmName + " does not exist.");
        }

        // Set the proper Selector
        List<String> existentSelectors = selectors.stream()
                .map(selector ->  selector.getClass().getSimpleName())
                .collect(Collectors.toList());

        for (Selector selector : selectors) {
            if (selector.getClass().getSimpleName().equals(selectorName)) {
                this.selector = selector;
                break;
            }
        }

        if (selector == null) {
            log.error("The Selector with name {} does not exist.  Please use a name from the following: {}", selectorName, existentSelectors);
            throw new IllegalArgumentException("The Selector with name " + selectorName + " does not exist.");
        }

        // Set the proper FitnessEvaluator
        List<String> existentPlaintextEvaluators = plaintextEvaluators.stream()
                .map(evaluator -> evaluator.getClass().getSimpleName())
                .collect(Collectors.toList());

        for (PlaintextEvaluator evaluator : plaintextEvaluators) {
            if (evaluator.getClass().getSimpleName().equals(fitnessEvaluatorName)) {
                fitnessEvaluator = new PlaintextEvaluatorWrappingFitnessEvaluator(evaluator);
                break;
            }
        }

        if (fitnessEvaluator == null) {
            log.error("The PlaintextEvaluator with name {} does not exist.  Please use a name from the following: {}", fitnessEvaluatorName, existentPlaintextEvaluators);
            throw new IllegalArgumentException("The PlaintextEvaluator with name " + fitnessEvaluatorName + " does not exist.");
        }
    }

    @Override
    public void optimize() {
        GeneticAlgorithmStrategy geneticAlgorithmStrategy = GeneticAlgorithmStrategy.builder()
                .crossoverAlgorithm(crossoverAlgorithm)
                .mutationAlgorithm(mutationAlgorithm)
                .selector(selector)
                .fitnessEvaluator(fitnessEvaluator)
                .populationSize(populationSize)
                .maxGenerations(numberOfGenerations)
                .mutationRate(mutationRate)
                .maxMutationsPerIndividual(maxMutationsPerIndividual)
                .build();

        geneticAlgorithm.setStrategy(geneticAlgorithmStrategy);

        long start = System.currentTimeMillis();

        try {
            geneticAlgorithm.evolveAutonomously();
        } catch (Throwable t) {
            log.error("Caught Throwable while running cipher solution service.  Cannot continue.  Performing tear-down tasks.", t);
        } finally {
            // Print out summary information
            log.info("Total running time was {}ms.", (System.currentTimeMillis() - start));

            this.geneticAlgorithm.getPopulation().printAscending();
        }
    }
}
