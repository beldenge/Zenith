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

package com.ciphertool.zenith.genetic.population;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public abstract class AbstractPopulation implements Population {
    private Logger log = LoggerFactory.getLogger(getClass());

    protected GeneticAlgorithmStrategy strategy;
    protected Double totalFitness = 0d;
    protected Double totalProbability = 0d;

    @Override
    public void init(GeneticAlgorithmStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void setStrategy(GeneticAlgorithmStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public List<Genome> breed(int numberToBreed) {
        List<FutureTask<Genome>> futureTasks = new ArrayList<>();
        FutureTask<Genome> futureTask;

        List<Genome> individualsAdded = new ArrayList<>(numberToBreed);
        for (int i = 0; i < numberToBreed; i++) {
            futureTask = new FutureTask<>(new GeneratorTask());
            futureTasks.add(futureTask);

            strategy.getTaskExecutor().execute(futureTask);
        }

        for (FutureTask<Genome> future : futureTasks) {
            try {
                individualsAdded.add(future.get());
            } catch (InterruptedException ie) {
                log.error("Caught InterruptedException while waiting for GeneratorTask ", ie);
            } catch (ExecutionException ee) {
                log.error("Caught ExecutionException while waiting for GeneratorTask ", ee);
            }
        }

        log.debug("Added {} individuals to the population.", individualsAdded.size());

        return individualsAdded;
    }

    /**
     * A concurrent task for adding a brand new Chromosome to the population.
     */
    protected class GeneratorTask implements Callable<Genome> {
        public GeneratorTask() {
        }

        @Override
        public Genome call() {
            return strategy.getBreeder().breed(strategy.getPopulation());
        }
    }

    @Override
    public List<Parents> select() {
        reIndexSelector();

        int pairsToCrossover = (this.strategy.getPopulationSize() - this.strategy.getElitism());

        List<FutureTask<Parents>> futureTasks = new ArrayList<>(pairsToCrossover);
        FutureTask<Parents> futureTask;

        /*
         * Execute each selection concurrently. Each should produce two children, but this is not necessarily always
         * guaranteed.
         */
        for (int i = 0; i < Math.max(0, pairsToCrossover); i++) {
            futureTask = new FutureTask<>(newSelectionTask());
            futureTasks.add(futureTask);
            strategy.getTaskExecutor().execute(futureTask);
        }

        List<Parents> allParents = new ArrayList<>(this.size());

        // Add the result of each FutureTask to the Lists of Chromosomes selected for subsequent crossover
        for (FutureTask<Parents> future : futureTasks) {
            try {
                allParents.add(future.get());
            } catch (InterruptedException ie) {
                log.error("Caught InterruptedException while waiting for SelectionTask ", ie);
            } catch (ExecutionException ee) {
                log.error("Caught ExecutionException while waiting for SelectionTask ", ee);
            }
        }

        return allParents;
    }

    @Override
    public Genome evaluateFitness(GenerationStatistics generationStatistics) {
        generationStatistics.setNumberOfEvaluations(this.doConcurrentFitnessEvaluations(this.strategy.getFitnessEvaluator(), getIndividuals()));

        this.totalFitness = 0d;
        this.totalProbability = 0d;

        Genome bestFitIndividual = null;
        boolean singleObjective = true;

        for (Genome individual : getIndividuals()) {
            // Probability calculation and average fitness is only supported for single-objective fitness functions.
            if (individual.getFitnesses().length == 1) {
                this.totalFitness += individual.getFitnesses()[0].getValue();
                this.totalProbability += individual.getProbability();
            } else {
                singleObjective = false;
            }

            if (bestFitIndividual == null || individual.compareTo(bestFitIndividual) > 0) {
                bestFitIndividual = individual;
            }
        }

        if (singleObjective) {
            generationStatistics.setAverageFitness(this.totalFitness / size());
            generationStatistics.setBestFitness(bestFitIndividual.getFitnesses()[0].getValue());
        }

        return bestFitIndividual;
    }

    /**
     * A concurrent task for evaluating the fitness of a Chromosome.
     */
    protected class EvaluationTask implements Callable<Void> {
        private Genome genome;
        private FitnessEvaluator fitnessEvaluator;

        public EvaluationTask(Genome genome, FitnessEvaluator fitnessEvaluator) {
            this.genome = genome;
            this.fitnessEvaluator = fitnessEvaluator;
        }

        @Override
        public Void call() {
            this.genome.setFitnesses(this.fitnessEvaluator.evaluate(this.genome));

            return null;
        }
    }

    /**
     * This method executes all the fitness evaluations concurrently.
     *
     * @throws InterruptedException if stop is requested
     */
    protected int doConcurrentFitnessEvaluations(FitnessEvaluator fitnessEvaluator, List<Genome> individuals) {
        List<FutureTask<Void>> futureTasks = new ArrayList<>();
        FutureTask<Void> futureTask;

        int evaluationCount = 0;

        for (Genome individual : individuals) {
            /*
             * Only evaluate individuals that have changed since the last evaluation.
             */
            if (individual.isEvaluationNeeded()) {
                evaluationCount++;
                futureTask = new FutureTask<>(new EvaluationTask(individual, fitnessEvaluator));
                futureTasks.add(futureTask);
                strategy.getTaskExecutor().execute(futureTask);
            }
        }

        for (FutureTask<Void> future : futureTasks) {
            try {
                future.get();
            } catch (InterruptedException ie) {
                log.error("Caught InterruptedException while waiting for EvaluationTask ", ie);
            } catch (ExecutionException ee) {
                log.error("Caught ExecutionException while waiting for EvaluationTask ", ee);
            }
        }

        return evaluationCount;
    }

    @Override
    public Double getTotalFitness() {
        return totalFitness;
    }

    @Override
    public Double getTotalProbability() {
        return totalProbability;
    }

    abstract void reIndexSelector();

    abstract Callable newSelectionTask();

    public static Double convertFromLogProbability(Double logProbability) {
        if (logProbability < 0) {
            return Math.exp(logProbability);
        }

        return logProbability;
    }
}
