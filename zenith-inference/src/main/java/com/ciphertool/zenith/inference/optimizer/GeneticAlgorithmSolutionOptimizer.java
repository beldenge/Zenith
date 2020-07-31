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
import com.ciphertool.zenith.genetic.operators.DivergentGeneticAlgorithm;
import com.ciphertool.zenith.genetic.operators.crossover.CrossoverOperator;
import com.ciphertool.zenith.genetic.operators.mutation.MutationOperator;
import com.ciphertool.zenith.genetic.operators.selection.Selector;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.inference.configuration.GeneticAlgorithmInitialization;
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
import org.springframework.core.task.TaskExecutor;
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
    public static final String CROSSOVER_OPERATOR_NAME = "crossoverOperatorName";
    public static final String MUTATION_OPERATOR_NAME = "mutationOperatorName";
    public static final String MUTATION_RATE = "mutationRate";
    public static final String MAX_MUTATIONS_PER_INDIVIDUAL = "maxMutationsPerIndividual";
    public static final String SELECTOR_NAME = "selectorName";
    public static final String TOURNAMENT_SELECTOR_ACCURACY = "tournamentSelectorAccuracy";
    public static final String TOURNAMENT_SIZE = "tournamentSize";
    public static final String TRUNCATION_PERCENTAGE = "truncationPercentage";
    public static final String ENABLE_FITNESS_SHARING = "enableFitnessSharing";
    public static final String INVASIVE_SPECIES_COUNT = "invasiveSpeciesCount";
    public static final String MIN_POPULATIONS = "minPopulations";
    public static final String SPECIATION_EVENTS = "speciationEvents";
    public static final String SPECIATION_FACTOR = "speciationFactor";
    public static final String EXTINCTION_CYCLES = "extinctionCycles";

    @Autowired
    protected TaskExecutor taskExecutor;

    @Autowired
    private DivergentGeneticAlgorithm geneticAlgorithm;

    @Autowired
    private List<Population> populations;

    @Autowired
    private List<Breeder> breeders;

    @Autowired
    private List<CrossoverOperator> crossoverOperators;

    @Autowired
    private List<MutationOperator> mutationOperators;

    @Autowired
    private List<Selector> selectors;

    public GeneticAlgorithmInitialization init(Cipher cipher, Map<String, Object> configuration, List<PlaintextTransformationStep> plaintextTransformationSteps, PlaintextEvaluator plaintextEvaluator) {
        Population population = null;
        Breeder breeder = null;
        CrossoverOperator crossoverOperator = null;
        MutationOperator mutationOperator = null;
        Selector selector = null;
        FitnessEvaluator fitnessEvaluator;

        String populationName = (String) configuration.get(POPULATION_NAME);
        String breederName = (String) configuration.get(BREEDER_NAME);
        String crossoverOperatorName = (String) configuration.get(CROSSOVER_OPERATOR_NAME);
        String mutationOperatorName = (String) configuration.get(MUTATION_OPERATOR_NAME);
        String selectorName = (String) configuration.get(SELECTOR_NAME);

        // Set the proper Population
        for (Population nextPopulation : populations) {
            if (nextPopulation.getClass().getSimpleName().equals(populationName)) {
                population = nextPopulation.getInstance();
                break;
            }
        }

        if (population == null) {
            List<String> existentPopulations = populations.stream()
                    .map(nextPopulation -> nextPopulation.getClass().getSimpleName())
                    .collect(Collectors.toList());

            log.error("The Population with name {} does not exist.  Please use a name from the following: {}", populationName, existentPopulations);
            throw new IllegalArgumentException("The Population with name " + populationName + " does not exist.");
        }

        // Set the proper Breeder
        for (Breeder nextBreeder : breeders) {
            if (nextBreeder.getClass().getSimpleName().equals(breederName)) {
                breeder = nextBreeder;
                ((AbstractCipherKeyBreeder) breeder).init(cipher, plaintextTransformationSteps, plaintextEvaluator);
                break;
            }
        }

        if (breeder == null) {
            List<String> existentBreeders = breeders.stream()
                    .map(nextBreeder -> nextBreeder.getClass().getSimpleName())
                    .collect(Collectors.toList());

            log.error("The Breeder with name {} does not exist.  Please use a name from the following: {}", breederName, existentBreeders);
            throw new IllegalArgumentException("The Breeder with name " + breederName + " does not exist.");
        }

        // Set the proper CrossoverOperator
        for (CrossoverOperator nextCrossoverOperator : crossoverOperators) {
            if (nextCrossoverOperator.getClass().getSimpleName().equals(crossoverOperatorName)) {
                crossoverOperator = nextCrossoverOperator;
                break;
            }
        }

        if (crossoverOperator == null) {
            List<String> existentCrossoverOperators = crossoverOperators.stream()
                    .map(nextCrossoverOperator -> nextCrossoverOperator.getClass().getSimpleName())
                    .collect(Collectors.toList());

            log.error("The CrossoverOperator with name {} does not exist.  Please use a name from the following: {}", crossoverOperatorName, existentCrossoverOperators);
            throw new IllegalArgumentException("The CrossoverOperator with name " + crossoverOperatorName + " does not exist.");
        }

        // Set the proper MutationOperator
        for (MutationOperator nextMutationOperator : mutationOperators) {
            if (nextMutationOperator.getClass().getSimpleName().equals(mutationOperatorName)) {
                mutationOperator = nextMutationOperator;
                break;
            }
        }

        if (mutationOperator == null) {
            List<String> existentMutationOperators = mutationOperators.stream()
                    .map(nextMutationOperator -> nextMutationOperator.getClass().getSimpleName())
                    .collect(Collectors.toList());

            log.error("The MutationOperator with name {} does not exist.  Please use a name from the following: {}", mutationOperatorName, existentMutationOperators);
            throw new IllegalArgumentException("The MutationOperator with name " + mutationOperatorName + " does not exist.");
        }

        // Set the proper Selector
        for (Selector nextSelector : selectors) {
            if (nextSelector.getClass().getSimpleName().equals(selectorName)) {
                selector = nextSelector.getInstance();
                break;
            }
        }

        if (selector == null) {
            List<String> existentSelectors = selectors.stream()
                    .map(nextSelector -> nextSelector.getClass().getSimpleName())
                    .collect(Collectors.toList());

            log.error("The Selector with name {} does not exist.  Please use a name from the following: {}", selectorName, existentSelectors);
            throw new IllegalArgumentException("The Selector with name " + selectorName + " does not exist.");
        }

        fitnessEvaluator = new PlaintextEvaluatorWrappingFitnessEvaluator(plaintextEvaluator.getPrecomputedCounterweightData(cipher), plaintextEvaluator, plaintextTransformationManager, plaintextTransformationSteps);

        return GeneticAlgorithmInitialization.builder()
                .population(population)
                .breeder(breeder)
                .crossoverOperator(crossoverOperator)
                .mutationOperator(mutationOperator)
                .selector(selector)
                .fitnessEvaluator(fitnessEvaluator)
                .build();
    }

    @Override
    public CipherSolution optimize(Cipher cipher, int epochs, Map<String, Object> configuration, List<PlaintextTransformationStep> plaintextTransformationSteps, PlaintextEvaluator plaintextEvaluator, OnEpochComplete onEpochComplete) {
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
        Double truncationPercentage = (Double) configuration.get(TRUNCATION_PERCENTAGE);
        boolean enableFitnessSharing = (boolean) configuration.get(ENABLE_FITNESS_SHARING);
        Integer invasiveSpeciesCount = (Integer) configuration.get(INVASIVE_SPECIES_COUNT);
        Integer minPopulations = (Integer) configuration.get(MIN_POPULATIONS);
        Integer speciationEvents = (Integer) configuration.get(SPECIATION_EVENTS);
        Integer speciationFactor = (Integer) configuration.get(SPECIATION_FACTOR);
        Integer extinctionCycles = (Integer) configuration.get(EXTINCTION_CYCLES);

        GeneticAlgorithmInitialization initialization = init(cipher, configuration, plaintextTransformationSteps, plaintextEvaluator);

        GeneticAlgorithmStrategy geneticAlgorithmStrategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .populationSize(populationSize)
                .numberOfGenerations(numberOfGenerations)
                .elitism(elitism)
                .population(initialization.getPopulation())
                .latticeRows(latticeRows)
                .latticeColumns(latticeColumns)
                .latticeWrapAround(latticeWrapAround)
                .latticeRadius(latticeRadius)
                .fitnessEvaluator(initialization.getFitnessEvaluator())
                .breeder(initialization.getBreeder())
                .crossoverOperator(initialization.getCrossoverOperator())
                .mutationOperator(initialization.getMutationOperator())
                .mutationRate(mutationRate)
                .maxMutationsPerIndividual(maxMutationsPerIndividual)
                .selector(initialization.getSelector())
                .tournamentSelectorAccuracy(tournamentSelectorAccuracy)
                .tournamentSize(tournamentSize)
                .truncationPercentage(truncationPercentage)
                .shareFitness(enableFitnessSharing)
                .invasiveSpeciesCount(invasiveSpeciesCount)
                .minPopulations(minPopulations)
                .speciationEvents(speciationEvents)
                .speciationFactor(speciationFactor)
                .extinctionCycles(extinctionCycles)
                .build();

        geneticAlgorithmStrategy.getPopulation().init(geneticAlgorithmStrategy);

        CipherSolution overallBest = null;
        CipherKeyChromosome last = null;
        int correctSolutions = 0;
        long totalElapsed = 0;

        int epoch = 0;
        for (; epoch < epochs; epoch++) {
            log.info("Epoch {} of {}.  Evolving for {} generations.", (epoch + 1), epochs, numberOfGenerations);

            long start = System.currentTimeMillis();

            geneticAlgorithm.evolve(geneticAlgorithmStrategy);

            long elapsed = System.currentTimeMillis() - start;
            totalElapsed += elapsed;
            log.info("Epoch completed in {}ms.", elapsed);

            geneticAlgorithmStrategy.getPopulation().sortIndividuals();

            if (log.isDebugEnabled()) {
                List<Chromosome> individuals = geneticAlgorithmStrategy.getPopulation().getIndividuals();
                int size = individuals.size();

                for (int i = 0; i < size; i++) {
                    Chromosome next = individuals.get(i);
                    log.info("Chromosome {}:", (i + 1), next);
                    if (log.isInfoEnabled()) {
                        cipherSolutionPrinter.print(ChromosomeToCipherSolutionMapper.map(next), plaintextTransformationSteps);
                    }
                }
            }

            last = (CipherKeyChromosome) geneticAlgorithmStrategy.getPopulation().getIndividuals().get(geneticAlgorithmStrategy.getPopulation().getIndividuals().size() - 1);

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
