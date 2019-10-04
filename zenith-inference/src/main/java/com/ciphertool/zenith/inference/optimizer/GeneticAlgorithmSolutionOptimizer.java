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
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyGene;
import com.ciphertool.zenith.inference.genetic.fitness.PlaintextEvaluatorWrappingFitnessEvaluator;
import com.ciphertool.zenith.inference.genetic.util.ChromosomeToCipherSolutionMapper;
import com.ciphertool.zenith.inference.printer.CipherSolutionPrinter;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value = "decipherment.optimizer", havingValue = "GeneticAlgorithmSolutionOptimizer")
public class GeneticAlgorithmSolutionOptimizer implements SolutionOptimizer {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${decipherment.epochs:1}")
    private int epochs;

    @Value("${decipherment.known-solution.correctness-threshold:0.9}")
    private double knownSolutionCorrectnessThreshold;

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

    @Value("${genetic-algorithm.population.type}")
    private String populationName;

    @Value("${genetic-algorithm.breeder.implementation}")
    private String breederName;

    @Value("${genetic-algorithm.crossover.implementation}")
    private String crossoverAlgorithmName;

    @Value("${genetic-algorithm.mutation.implementation}")
    private String mutationAlgorithmName;

    @Value("${genetic-algorithm.selection.implementation}")
    private String selectorName;

    @Value("${genetic-algorithm.fitness.implementation}")
    private String fitnessEvaluatorName;

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

    @Autowired(required = false)
    @Qualifier("activePlaintextTransformers")
    private List<PlaintextTransformer> plaintextTransformers;

    @Autowired
    private PlaintextEvaluator plaintextEvaluator;

    @Autowired
    private CipherSolutionPrinter cipherSolutionPrinter;

    private Population population;

    private Breeder breeder;

    private CrossoverAlgorithm crossoverAlgorithm;

    private MutationAlgorithm mutationAlgorithm;

    private Selector selector;

    private FitnessEvaluator fitnessEvaluator;

    @PostConstruct
    public void init() {
        // Set the proper Population
        List<String> existentPopulations = populations.stream()
                .map(population -> population.getClass().getSimpleName())
                .collect(Collectors.toList());

        for (Population population : populations) {
            if (population.getClass().getSimpleName().equals(populationName)) {
                this.population = population;
                break;
            }
        }

        if (population == null) {
            log.error("The Population with name {} does not exist.  Please use a name from the following: {}", populationName, existentPopulations);
            throw new IllegalArgumentException("The Population with name " + populationName + " does not exist.");
        }

        // Set the proper Breeder
        List<String> existentBreeders = breeders.stream()
                .map(breeder -> breeder.getClass().getSimpleName())
                .collect(Collectors.toList());

        for (Breeder breeder : breeders) {
            if (breeder.getClass().getSimpleName().equals(breederName)) {
                this.breeder = breeder;
                break;
            }
        }

        if (breeder == null) {
            log.error("The Breeder with name {} does not exist.  Please use a name from the following: {}", breederName, existentBreeders);
            throw new IllegalArgumentException("The Breeder with name " + breederName + " does not exist.");
        }

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
                .map(mutationAlgorithm -> mutationAlgorithm.getClass().getSimpleName())
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
                .map(selector -> selector.getClass().getSimpleName())
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

        fitnessEvaluator = new PlaintextEvaluatorWrappingFitnessEvaluator(plaintextEvaluator, plaintextTransformers);
    }

    @Override
    public CipherSolution optimize() {
        GeneticAlgorithmStrategy geneticAlgorithmStrategy = GeneticAlgorithmStrategy.builder()
                .crossoverAlgorithm(crossoverAlgorithm)
                .mutationAlgorithm(mutationAlgorithm)
                .selector(selector)
                .population(population)
                .fitnessEvaluator(fitnessEvaluator)
                .breeder(breeder)
                .populationSize(populationSize)
                .maxGenerations(numberOfGenerations)
                .mutationRate(mutationRate)
                .maxMutationsPerIndividual(maxMutationsPerIndividual)
                .elitism(elitism)
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
                        cipherSolutionPrinter.print(ChromosomeToCipherSolutionMapper.map(next));
                    }
                }
            }

            last = (CipherKeyChromosome) this.geneticAlgorithm.getPopulation().getIndividuals().get(this.geneticAlgorithm.getPopulation().getIndividuals().size() - 1);

            log.info("Best probability solution:");
            CipherSolution bestSolution = ChromosomeToCipherSolutionMapper.map(last);
            if (log.isInfoEnabled()) {
                cipherSolutionPrinter.print(bestSolution);
            }
            log.info("Mappings for best probability:");

            for (Map.Entry<String, Gene> entry : last.getGenes().entrySet()) {
                log.info("{}: {}", entry.getKey(), ((CipherKeyGene) entry.getValue()).getValue());
            }

            if (last.getCipher().hasKnownSolution() && knownSolutionCorrectnessThreshold <= bestSolution.evaluateKnownSolution()) {
                correctSolutions ++;
            }

            overallBest = (overallBest == null) ? bestSolution : (bestSolution.getScore() > overallBest.getScore() ? bestSolution : overallBest);
        }

        if (last != null && last.getCipher().hasKnownSolution()) {
            log.info("{} out of {} epochs ({}%) produced the correct solution.", correctSolutions, epochs, String.format("%1$,.2f", (correctSolutions / (double) epochs) * 100.0));
        }

        log.info("Average epoch time={}ms", ((float) totalElapsed / (float) epoch));

        return overallBest;
    }
}
