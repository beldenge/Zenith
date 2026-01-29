/*
 * Copyright 2017-2026 George Belden
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

package com.ciphertool.zenith.genetic.operators.algorithm;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.operators.sort.ParetoSorter;
import com.ciphertool.zenith.genetic.operators.speciation.FitnessSpeciationOperator;
import com.ciphertool.zenith.genetic.operators.speciation.ProximitySpeciationOperator;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.genetic.population.StandardPopulation;
import com.ciphertool.zenith.genetic.statistics.ExecutionStatistics;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import com.ciphertool.zenith.genetic.statistics.PerformanceStatistics;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

@Component
public class DivergentGeneticAlgorithm {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${genetic-algorithm.calculate-entropy:false}")
    private boolean calculateEntropy;

    @Autowired
    private FitnessSpeciationOperator fitnessSpeciationOperator;

    @Autowired
    private ProximitySpeciationOperator proximitySpeciationOperator;

    public void spawnInitialPopulation(GeneticAlgorithmStrategy strategy) {
        GenerationStatistics generationStatistics = new GenerationStatistics(0);

        long start = System.currentTimeMillis();

        Population population = strategy.getPopulation();

        population.clearIndividuals();
        List<Genome> initialPopulation = population.breed(strategy.getPopulationSize());
        initialPopulation.stream().forEach(population::addIndividual);

        if (calculateEntropy) {
            long startEntropyCalculation = System.currentTimeMillis();
            generationStatistics.setEntropy(population.calculateEntropy());
            generationStatistics.getPerformanceStatistics().setEntropyMillis(System.currentTimeMillis() - startEntropyCalculation);
        }

        long startEvaluation = System.currentTimeMillis();
        population.evaluateFitness(generationStatistics);
        generationStatistics.getPerformanceStatistics().setEvaluationMillis(System.currentTimeMillis() - startEvaluation);

        long executionTime = System.currentTimeMillis() - start;
        generationStatistics.getPerformanceStatistics().setTotalMillis(executionTime);

        log.info("Took {}ms to spawn initial population of size {}", executionTime, population.size());
        log.info(generationStatistics.toString());
    }

    public void evolve(GeneticAlgorithmStrategy strategy) {
        List<Population> populations = new ArrayList<>(strategy.getMinPopulations());

        for (int i = 0; i < strategy.getMinPopulations(); i ++) {
            strategy.setPopulation(strategy.getPopulation().getInstance());
            strategy.getPopulation().init(strategy);
            evolvePopulation(strategy, true);
            populations.add(strategy.getPopulation());
        }

        for (int i = 0; i < strategy.getExtinctionCycles(); i ++) {
            if (populations.size() > strategy.getMinPopulations()) {
                populations.stream().forEach(Population::sortIndividuals);

                List<Genome> bestIndividuals = populations.stream()
                        .map(pop -> pop.getIndividuals().get(pop.getIndividuals().size() - 1))
                        .collect(Collectors.toList());

                ParetoSorter.sort(bestIndividuals);
                final List<Genome> finalBestIndividuals = bestIndividuals.subList(bestIndividuals.size() - strategy.getMinPopulations(), bestIndividuals.size());

                // Pick the "best" populations and reset to min populations
                populations = populations.stream()
                        .filter(pop -> finalBestIndividuals.contains(pop.getIndividuals().get(pop.getIndividuals().size() - 1)))
                        .collect(Collectors.toList());
            }

            for (int j = 0; j < strategy.getSpeciationEvents(); j ++) {
                List<Population> newPopulations = new ArrayList<>(populations.size() * 2);

                for (Population population : populations) {
                    List<Population> divergentPopulations;

                    if (population instanceof StandardPopulation) {
                        divergentPopulations = fitnessSpeciationOperator.diverge(strategy, population);
                    } else {
                        divergentPopulations = proximitySpeciationOperator.diverge(strategy, population);
                    }

                    for (Population divergentPopulation : divergentPopulations) {
                        strategy.setPopulation(divergentPopulation);
                        // Use setStrategy instead of init to preserve individuals added by speciation
                        strategy.getPopulation().setStrategy(strategy);
                        evolvePopulation(strategy, false);
                        newPopulations.add(strategy.getPopulation());
                    }
                }

                populations.clear();
                populations.addAll(newPopulations);
            }
        }

        populations.stream().forEach(Population::sortIndividuals);

        // Select the best population
        List<Genome> bestIndividuals = populations.stream()
                .map(pop -> pop.getIndividuals().get(pop.getIndividuals().size() - 1))
                .collect(Collectors.toList());

        ParetoSorter.sort(bestIndividuals);

        Population bestPopulation = populations.stream()
                .filter(pop -> bestIndividuals.contains(pop.getIndividuals().get(pop.getIndividuals().size() - 1)))
                .findFirst()
                .orElse(null);

        strategy.setPopulation(bestPopulation);
    }

    private void evolvePopulation(GeneticAlgorithmStrategy strategy, boolean isNew) {
        int generationCount = 1;
        ExecutionStatistics executionStatistics = new ExecutionStatistics(LocalDateTime.now(), strategy);

        if (isNew) {
            spawnInitialPopulation(strategy);
        }

        do {
            proceedWithNextGeneration(strategy, executionStatistics, generationCount);
            generationCount++;
        } while (generationCount <= strategy.getNumberOfGenerations());

        finish(executionStatistics, generationCount);
    }

    public void proceedWithNextGeneration(GeneticAlgorithmStrategy strategy, ExecutionStatistics executionStatistics, int generationCount) {
        GenerationStatistics generationStatistics = new GenerationStatistics(generationCount);

        long generationStart = System.currentTimeMillis();

        PerformanceStatistics performanceStats = new PerformanceStatistics();
        generationStatistics.setPerformanceStatistics(performanceStats);

        Population population = strategy.getPopulation();

        long startSelection = System.currentTimeMillis();
        List<Parents> allParents = population.select();
        performanceStats.setSelectionMillis(System.currentTimeMillis() - startSelection);

        long startCrossover = System.currentTimeMillis();
        List<Genome> children = crossover(strategy, allParents);
        generationStatistics.setNumberOfCrossovers(children.size());
        performanceStats.setCrossoverMillis(System.currentTimeMillis() - startCrossover);

        long startMutation = System.currentTimeMillis();
        generationStatistics.setNumberOfMutations(mutate(strategy, children));
        performanceStats.setMutationMillis(System.currentTimeMillis() - startMutation);

        replacePopulation(strategy, children);

        if (calculateEntropy) {
            long startEntropyCalculation = System.currentTimeMillis();
            generationStatistics.setEntropy(population.calculateEntropy());
            performanceStats.setEntropyMillis(System.currentTimeMillis() - startEntropyCalculation);
        }

        long startEvaluation = System.currentTimeMillis();
        population.evaluateFitness(generationStatistics);
        performanceStats.setEvaluationMillis(System.currentTimeMillis() - startEvaluation);
        performanceStats.setTotalMillis(System.currentTimeMillis() - generationStart);

        log.info(generationStatistics.toString());

        executionStatistics.addGenerationStatistics(generationStatistics);
    }

    public List<Genome> crossover(GeneticAlgorithmStrategy strategy, List<Parents> allParents) {
        if (strategy.getPopulation().size() < 2) {
            log.info("Unable to perform crossover because there is only 1 individual in the population. Returning.");

            return Collections.emptyList();
        }

        log.debug("Pairs to crossover: {}", allParents.size());

        List<Genome> crossoverResults = doConcurrentCrossovers(strategy, allParents);
        List<Genome> childrenToAdd = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(crossoverResults)) {
            childrenToAdd.addAll(crossoverResults);
        }

        if (childrenToAdd.size() + strategy.getElitism() < strategy.getPopulationSize()) {
            throw new IllegalStateException(childrenToAdd.size() +
                    " children produced from concurrent crossover execution.  Expected " + (strategy.getPopulationSize() - strategy.getElitism()) + " children.");
        }

        return childrenToAdd;
    }

    protected List<Genome> doConcurrentCrossovers(GeneticAlgorithmStrategy strategy, List<Parents> allParents) {
        List<FutureTask<Genome>> futureTasks = new ArrayList<>();
        FutureTask<Genome> futureTask;

        /*
         * Execute each crossover concurrently. Parents should produce two children, but this is not necessarily always
         * guaranteed.
         */
        for (Parents nextParents : allParents) {
            futureTask = new FutureTask<>(new DivergentGeneticAlgorithm.CrossoverTask(strategy, nextParents));
            futureTasks.add(futureTask);
            strategy.getTaskExecutor().execute(futureTask);
        }

        List<Genome> childrenToAdd = new ArrayList<>();

        // Add the result of each FutureTask to the population since it represents a new child Chromosome.
        for (FutureTask<Genome> future : futureTasks) {
            try {
                childrenToAdd.add(future.get());
            } catch (InterruptedException ie) {
                log.error("Caught InterruptedException while waiting for CrossoverTask ", ie);
            } catch (ExecutionException ee) {
                log.error("Caught ExecutionException while waiting for CrossoverTask ", ee);
            }
        }

        return childrenToAdd;
    }

    public int mutate(GeneticAlgorithmStrategy strategy, List<Genome> children) {
        int mutations = 0;
        List<FutureTask<Integer>> futureTasks = new ArrayList<>();
        FutureTask<Integer> futureTask;

        // TODO: this probably is not necessary
        strategy.getPopulation().sortIndividuals();

        /*
         * Execute each mutation concurrently.
         */
        for (Genome child : children) {
            futureTask = new FutureTask<>(new DivergentGeneticAlgorithm.MutationTask(strategy, child));
            futureTasks.add(futureTask);
            strategy.getTaskExecutor().execute(futureTask);
        }

        for (FutureTask<Integer> future : futureTasks) {
            try {
                mutations += future.get();
            } catch (InterruptedException ie) {
                log.error("Caught InterruptedException while waiting for MutationTask ", ie);
            } catch (ExecutionException ee) {
                log.error("Caught ExecutionException while waiting for MutationTask ", ee);
            }
        }

        return mutations;
    }

    protected void replacePopulation(GeneticAlgorithmStrategy strategy, List<Genome> children) {
        List<Genome> eliteIndividuals = new ArrayList<>();

        Population population = strategy.getPopulation();

        if (strategy.getElitism() > 0 && population.size() > 0) {
            population.sortIndividuals();

            int numberToKeep = Math.min(strategy.getElitism(), population.size());

            for (int i = population.size() - 1; i >= population.size() - numberToKeep; i--) {
                eliteIndividuals.add(population.getIndividuals().get(i));
            }
        }

        population.clearIndividuals();

        eliteIndividuals.stream().forEach(population::addIndividual);
        children.stream().forEach(population::addIndividual);
    }

    public void finish(ExecutionStatistics executionStatistics, int generationCount) {
        long totalExecutionTime = 0;

        for (GenerationStatistics generationStatistics : executionStatistics.getGenerationStatisticsList()) {
            if (generationStatistics.getGeneration() == 0) {
                // This is the initial spawning of the population, which will potentially skew the average
                continue;
            }

            totalExecutionTime += generationStatistics.getPerformanceStatistics().getTotalMillis();
        }

        long averageExecutionTime = 0;

        if (generationCount > 1) {
            /*
             * We subtract 1 from the generation count because the zeroth generation is just the initial spawning of the
             * population. And, we add one to the result because the remainder from division is truncated due to use of
             * primitive type long, and we want to round up.
             */
            averageExecutionTime = (totalExecutionTime / (generationCount - 1)) + 1;
        } else {
            averageExecutionTime = totalExecutionTime;
        }

        log.info("Average generation time is {}ms.", averageExecutionTime);

        executionStatistics.setEndDateTime(LocalDateTime.now());
    }

    /**
     * A concurrent task for performing a crossover of two parent Chromosomes, producing one child Chromosome.
     */
    protected class CrossoverTask implements Callable<Genome> {
        private GeneticAlgorithmStrategy strategy;
        private Parents parents;

        public CrossoverTask(GeneticAlgorithmStrategy strategy, Parents parents) {
            this.strategy = strategy;
            this.parents = parents;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Genome call() {
            return strategy.getCrossoverOperator().crossover(parents.getMom(), parents.getDad());
        }
    }

    /**
     * A concurrent task for performing a crossover of two parent Chromosomes, producing one child Chromosome.
     */
    protected class MutationTask implements Callable<Integer> {
        private GeneticAlgorithmStrategy strategy;
        private Genome genome;

        public MutationTask(GeneticAlgorithmStrategy strategy, Genome genome) {
            this.strategy = strategy;
            this.genome = genome;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Integer call() {
            /*
             * Mutate a gene within the Chromosome. The original Chromosome has been cloned.
             */
            if (strategy.getMutationOperator().mutateChromosomes(genome, strategy)) {
                return 1;
            }

            return 0;
        }
    }
}
