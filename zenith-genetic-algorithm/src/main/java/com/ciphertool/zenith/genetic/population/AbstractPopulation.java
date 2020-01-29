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

package com.ciphertool.zenith.genetic.population;

import com.ciphertool.zenith.genetic.Breeder;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public abstract class AbstractPopulation implements Population {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected TaskExecutor taskExecutor;

    private Breeder breeder;
    private FitnessEvaluator fitnessEvaluator;
    protected int targetSize;
    protected Double totalFitness = 0d;
    protected int elitism = 0;

    @Override
    public int breed() {
        List<FutureTask<Chromosome>> futureTasks = new ArrayList<>();
        FutureTask<Chromosome> futureTask;

        int individualsAdded = 0;
        for (int i = 0; i < targetSize; i++) {
            futureTask = new FutureTask<>(new GeneratorTask());
            futureTasks.add(futureTask);

            this.taskExecutor.execute(futureTask);
        }

        for (FutureTask<Chromosome> future : futureTasks) {
            try {
                this.addIndividual(future.get());

                individualsAdded++;
            } catch (InterruptedException ie) {
                log.error("Caught InterruptedException while waiting for GeneratorTask ", ie);
            } catch (ExecutionException ee) {
                log.error("Caught ExecutionException while waiting for GeneratorTask ", ee);
            }
        }

        log.debug("Added {} individuals to the population.", individualsAdded);

        return individualsAdded;
    }

    /**
     * A concurrent task for adding a brand new Chromosome to the population.
     */
    protected class GeneratorTask implements Callable<Chromosome> {
        public GeneratorTask() {
        }

        @Override
        public Chromosome call() {
            return breeder.breed();
        }
    }

    @Override
    public List<Parents> select() {
        reIndexSelector();

        int pairsToCrossover = (this.size() - this.elitism);

        List<FutureTask<Parents>> futureTasks = new ArrayList<>(pairsToCrossover);
        FutureTask<Parents> futureTask;

        /*
         * Execute each selection concurrently. Each should produce two children, but this is not necessarily always
         * guaranteed.
         */
        for (int i = 0; i < Math.max(0, pairsToCrossover); i++) {
            futureTask = new FutureTask<>(newSelectionTask());
            futureTasks.add(futureTask);
            this.taskExecutor.execute(futureTask);
        }

        List<Parents> allParents = new ArrayList<>(this.size());

        // Add the result of each FutureTask to the Lists of Chromosomes selected for subsequent crossover
        for (FutureTask<Parents> future : futureTasks) {
            try {
                Parents nextParents = future.get();

                allParents.add(nextParents);
            } catch (InterruptedException ie) {
                log.error("Caught InterruptedException while waiting for SelectionTask ", ie);
            } catch (ExecutionException ee) {
                log.error("Caught ExecutionException while waiting for SelectionTask ", ee);
            }
        }

        return allParents;
    }

    @Override
    public Chromosome evaluateFitness(GenerationStatistics generationStatistics) {
        generationStatistics.setNumberOfEvaluations(this.doConcurrentFitnessEvaluations(this.fitnessEvaluator));

        this.totalFitness = 0d;

        Chromosome bestFitIndividual = null;

        for (Chromosome individual : getIndividuals()) {
            this.totalFitness += individual.getFitness();

            if (bestFitIndividual == null || individual.getFitness() > bestFitIndividual.getFitness()) {
                bestFitIndividual = individual;
            }
        }

        Double averageFitness = this.totalFitness / getIndividuals().size();

        if (generationStatistics != null) {
            generationStatistics.setAverageFitness(averageFitness);
            generationStatistics.setBestFitness(bestFitIndividual.getFitness());

            if (bestFitIndividual.hasKnownSolution()) {
                generationStatistics.setKnownSolutionProximity(bestFitIndividual.knownSolutionProximity() * 100.0d);
            }
        }

        return bestFitIndividual;
    }

    /**
     * A concurrent task for evaluating the fitness of a Chromosome.
     */
    protected class EvaluationTask implements Callable<Void> {
        private Chromosome chromosome;
        private FitnessEvaluator fitnessEvaluator;

        public EvaluationTask(Chromosome chromosome, FitnessEvaluator fitnessEvaluator) {
            this.chromosome = chromosome;
            this.fitnessEvaluator = fitnessEvaluator;
        }

        @Override
        public Void call() {
            this.chromosome.setFitness(this.fitnessEvaluator.evaluate(this.chromosome));

            return null;
        }
    }

    /**
     * This method executes all the fitness evaluations concurrently.
     *
     * @throws InterruptedException if stop is requested
     */
    protected int doConcurrentFitnessEvaluations(FitnessEvaluator fitnessEvaluator) {
        List<FutureTask<Void>> futureTasks = new ArrayList<>();
        FutureTask<Void> futureTask;

        int evaluationCount = 0;

        Chromosome individual;

        List<Chromosome> individuals = this.getIndividuals();

        for (int i = individuals.size() - 1; i >= 0; i--) {
            individual = individuals.get(i);

            /*
             * Only evaluate individuals that have changed since the last evaluation.
             */
            if (individual.isEvaluationNeeded()) {
                evaluationCount++;
                futureTask = new FutureTask<>(new EvaluationTask(individual, fitnessEvaluator));
                futureTasks.add(futureTask);
                this.taskExecutor.execute(futureTask);
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
    public void setBreeder(Breeder breeder) {
        this.breeder = breeder;
    }

    @Override
    public void setFitnessEvaluator(FitnessEvaluator fitnessEvaluator) {
        this.fitnessEvaluator = fitnessEvaluator;
    }

    @Override
    public void setTargetSize(int targetSize) {
        this.targetSize = targetSize;
    }

    @Override
    public void setElitism(int elitism) {
        this.elitism = elitism;
    }

    abstract void reIndexSelector();

    abstract Callable newSelectionTask();
}
