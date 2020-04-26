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

package com.ciphertool.zenith.inference.optimizer;

import com.ciphertool.zenith.genetic.Breeder;
import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.algorithms.StandardGeneticAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.crossover.CrossoverAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.mutation.MutationAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.selection.Selector;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.genetic.breeder.AbstractCipherKeyBreeder;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyGene;
import com.ciphertool.zenith.inference.genetic.fitness.PlaintextEvaluatorWrappingFitnessEvaluator;
import com.ciphertool.zenith.inference.genetic.util.ChromosomeToCipherSolutionMapper;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GeneticAlgorithmSolutionOptimizer extends AbstractSolutionOptimizer {
    private Logger log = LoggerFactory.getLogger(getClass());

    public static final String POPULATION_SIZE = "populationSize";
    public static final String NUMBER_OF_GENERATIONS = "numberOfGenerations";
    public static final String ELITISM = "elitism";
    public static final String POPULATION_NAME = "populationName";
    public static final String LATTICE_ROWS = "latticeRows";
    public static final String LATTICE_COLUMNS = "latticeColumns";
    public static final String LATTICE_WRAP_AROUND = "latticeWrapAround";
    public static final String LATTICE_RADIUS = "latticeRadius";
    public static final String BREEDER_NAME = "breederName";
    public static final String CROSSOVER_ALGORITHM_NAME = "crossoverAlgorithmName";
    public static final String MUTATION_ALGORITHM_NAME = "mutationAlgorithmName";
    public static final String MUTATION_RATE = "mutationRate";
    public static final String MAX_MUTATIONS_PER_INDIVIDUAL = "maxMutationsPerIndividual";
    public static final String SELECTOR_NAME = "selectorName";
    public static final String TOURNAMENT_SELECTOR_ACCURACY = "tournamentSelectorAccuracy";
    public static final String TOURNAMENT_SIZE = "tournamentSize";

    @Autowired
    private List<Population> populations;

    @Autowired
    private StandardGeneticAlgorithm geneticAlgorithm;

    @Autowired
    private List<Breeder> breeders;

    @Autowired
    private List<CrossoverAlgorithm> crossoverAlgorithms;

    @Autowired
    private List<MutationAlgorithm> mutationAlgorithms;

    @Autowired
    private List<Selector> selectors;

    @Autowired
    private PlaintextEvaluator plaintextEvaluator;

    private Population population;

    private Breeder breeder;

    private CrossoverAlgorithm crossoverAlgorithm;

    private MutationAlgorithm mutationAlgorithm;

    private Selector selector;

    private FitnessEvaluator fitnessEvaluator;

    public void init(Cipher cipher, Map<String, Object> configuration, List<PlaintextTransformationStep> plaintextTransformationSteps) {
        String populationName = (String) configuration.get(POPULATION_NAME);
        String breederName = (String) configuration.get(BREEDER_NAME);
        String crossoverAlgorithmName = (String) configuration.get(CROSSOVER_ALGORITHM_NAME);
        String mutationAlgorithmName = (String) configuration.get(MUTATION_ALGORITHM_NAME);
        String selectorName = (String) configuration.get(SELECTOR_NAME);

        // Set the proper Population
        for (Population population : populations) {
            if (population.getClass().getSimpleName().equals(populationName)) {
                this.population = population;
                break;
            }
        }

        if (population == null) {
            List<String> existentPopulations = populations.stream()
                    .map(population -> population.getClass().getSimpleName())
                    .collect(Collectors.toList());

            log.error("The Population with name {} does not exist.  Please use a name from the following: {}", populationName, existentPopulations);
            throw new IllegalArgumentException("The Population with name " + populationName + " does not exist.");
        }

        // Set the proper Breeder
        for (Breeder breeder : breeders) {
            if (breeder.getClass().getSimpleName().equals(breederName)) {
                this.breeder = breeder;
                ((AbstractCipherKeyBreeder) breeder).init(cipher);
                break;
            }
        }

        if (breeder == null) {
            List<String> existentBreeders = breeders.stream()
                    .map(breeder -> breeder.getClass().getSimpleName())
                    .collect(Collectors.toList());

            log.error("The Breeder with name {} does not exist.  Please use a name from the following: {}", breederName, existentBreeders);
            throw new IllegalArgumentException("The Breeder with name " + breederName + " does not exist.");
        }

        // Set the proper CrossoverAlgorithm
        for (CrossoverAlgorithm crossoverAlgorithm : crossoverAlgorithms) {
            if (crossoverAlgorithm.getClass().getSimpleName().equals(crossoverAlgorithmName)) {
                this.crossoverAlgorithm = crossoverAlgorithm;
                break;
            }
        }

        if (crossoverAlgorithm == null) {
            List<String> existentCrossoverAlgorithms = crossoverAlgorithms.stream()
                    .map(crossoverAlgorithm -> crossoverAlgorithm.getClass().getSimpleName())
                    .collect(Collectors.toList());

            log.error("The CrossoverAlgorithm with name {} does not exist.  Please use a name from the following: {}", crossoverAlgorithmName, existentCrossoverAlgorithms);
            throw new IllegalArgumentException("The CrossoverAlgorithm with name " + crossoverAlgorithmName + " does not exist.");
        }

        // Set the proper MutationAlgorithm
        for (MutationAlgorithm mutationAlgorithm : mutationAlgorithms) {
            if (mutationAlgorithm.getClass().getSimpleName().equals(mutationAlgorithmName)) {
                this.mutationAlgorithm = mutationAlgorithm;
                break;
            }
        }

        if (mutationAlgorithm == null) {
            List<String> existentMutationAlgorithms = mutationAlgorithms.stream()
                    .map(mutationAlgorithm -> mutationAlgorithm.getClass().getSimpleName())
                    .collect(Collectors.toList());

            log.error("The MutationAlgorithm with name {} does not exist.  Please use a name from the following: {}", mutationAlgorithmName, existentMutationAlgorithms);
            throw new IllegalArgumentException("The MutationAlgorithm with name " + mutationAlgorithmName + " does not exist.");
        }

        // Set the proper Selector
        for (Selector selector : selectors) {
            if (selector.getClass().getSimpleName().equals(selectorName)) {
                this.selector = selector;
                break;
            }
        }

        if (selector == null) {
            List<String> existentSelectors = selectors.stream()
                    .map(selector -> selector.getClass().getSimpleName())
                    .collect(Collectors.toList());

            log.error("The Selector with name {} does not exist.  Please use a name from the following: {}", selectorName, existentSelectors);
            throw new IllegalArgumentException("The Selector with name " + selectorName + " does not exist.");
        }

        fitnessEvaluator = new PlaintextEvaluatorWrappingFitnessEvaluator(plaintextEvaluator, plaintextTransformationManager, plaintextTransformationSteps);
    }

    @Override
    public CipherSolution optimize(Cipher cipher, int epochs, Map<String, Object> configuration, List<PlaintextTransformationStep> plaintextTransformationSteps, OnEpochComplete onEpochComplete) {
        int populationSize = (int) configuration.get(POPULATION_SIZE);
        int numberOfGenerations = (int) configuration.get(NUMBER_OF_GENERATIONS);
        int elitism = (int) configuration.get(ELITISM);
        Integer latticeRows = (Integer) configuration.get(LATTICE_ROWS);
        Integer latticeColumns = (Integer) configuration.get(LATTICE_COLUMNS);
        Boolean latticeWrapAround = (Boolean) configuration.get(LATTICE_WRAP_AROUND);
        Integer latticeRadius = (Integer) configuration.get(LATTICE_RADIUS);
        Double mutationRate = (Double) configuration.get(MUTATION_RATE);
        Integer maxMutationsPerIndividual = (Integer) configuration.get(MAX_MUTATIONS_PER_INDIVIDUAL);
        Double tournamentSelectorAccuracy = (Double) configuration.get(TOURNAMENT_SELECTOR_ACCURACY);
        Integer tournamentSize = (Integer) configuration.get(TOURNAMENT_SIZE);

        init(cipher, configuration, plaintextTransformationSteps);

        GeneticAlgorithmStrategy geneticAlgorithmStrategy = GeneticAlgorithmStrategy.builder()
                .populationSize(populationSize)
                .numberOfGenerations(numberOfGenerations)
                .elitism(elitism)
                .population(population)
                .latticeRows(latticeRows)
                .latticeColumns(latticeColumns)
                .latticeWrapAround(latticeWrapAround)
                .latticeRadius(latticeRadius)
                .fitnessEvaluator(fitnessEvaluator)
                .breeder(breeder)
                .crossoverAlgorithm(crossoverAlgorithm)
                .mutationAlgorithm(mutationAlgorithm)
                .mutationRate(mutationRate)
                .maxMutationsPerIndividual(maxMutationsPerIndividual)
                .selector(selector)
                .tournamentSelectorAccuracy(tournamentSelectorAccuracy)
                .tournamentSize(tournamentSize)
                .build();

        geneticAlgorithm.setStrategy(geneticAlgorithmStrategy);

        CipherSolution overallBest = null;
        CipherKeyChromosome last = null;
        int correctSolutions = 0;
        long totalElapsed = 0;

        int epoch = 0;
        for (; epoch < epochs; epoch++) {
            log.info("Epoch {} of {}.  Evolving for {} generations.", (epoch + 1), epochs, numberOfGenerations);

            long start = System.currentTimeMillis();

            geneticAlgorithm.evolve();

            long elapsed = System.currentTimeMillis() - start;
            totalElapsed += elapsed;
            log.info("Epoch completed in {}ms.", elapsed);

            this.geneticAlgorithm.getPopulation().sortIndividuals();

            if (log.isDebugEnabled()) {
                this.geneticAlgorithm.getPopulation().sortIndividuals();

                List<Chromosome> individuals = this.geneticAlgorithm.getPopulation().getIndividuals();
                int size = individuals.size();

                for (int i = 0; i < size; i++) {
                    Chromosome next = individuals.get(i);
                    log.info("Chromosome {}:", (i + 1), next);
                    if (log.isInfoEnabled()) {
                        cipherSolutionPrinter.print(ChromosomeToCipherSolutionMapper.map(next), plaintextTransformationSteps);
                    }
                }
            }

            last = (CipherKeyChromosome) this.geneticAlgorithm.getPopulation().getIndividuals().get(this.geneticAlgorithm.getPopulation().getIndividuals().size() - 1);

            log.info("Best probability solution:");
            CipherSolution bestSolution = ChromosomeToCipherSolutionMapper.map(last);
            if (log.isInfoEnabled()) {
                cipherSolutionPrinter.print(bestSolution, plaintextTransformationSteps);
            }
            log.info("Mappings for best probability:");

            for (Map.Entry<String, Gene> entry : last.getGenes().entrySet()) {
                log.info("{}: {}", entry.getKey(), ((CipherKeyGene) entry.getValue()).getValue());
            }

            if (last.getCipher().hasKnownSolution() && knownSolutionCorrectnessThreshold <= bestSolution.evaluateKnownSolution()) {
                correctSolutions ++;
            }

            overallBest = (overallBest == null) ? bestSolution : (bestSolution.getScore() > overallBest.getScore() ? bestSolution : overallBest);

            if (onEpochComplete != null) {
                onEpochComplete.fire(epoch + 1);
            }
        }

        if (last != null && last.getCipher().hasKnownSolution()) {
            log.info("{} out of {} epochs ({}%) produced the correct solution.", correctSolutions, epochs, String.format("%1$,.2f", (correctSolutions / (double) epochs) * 100.0));
        }

        log.info("Average epoch time={}ms", ((float) totalElapsed / (float) epoch));

        return overallBest;
    }
}
