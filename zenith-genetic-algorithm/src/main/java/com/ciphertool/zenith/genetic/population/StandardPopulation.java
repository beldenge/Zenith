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

package com.ciphertool.zenith.genetic.population;

import com.ciphertool.zenith.genetic.Breeder;
import com.ciphertool.zenith.genetic.algorithms.selection.Selector;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.fitness.AscendingFitnessComparator;
import com.ciphertool.zenith.genetic.fitness.FitnessComparator;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Component
public class StandardPopulation implements Population {
    private Logger log = LoggerFactory.getLogger(getClass());
    private Breeder breeder;
    private List<Chromosome> individuals = new ArrayList<>();
    private FitnessEvaluator fitnessEvaluator;
    private FitnessComparator fitnessComparator = new AscendingFitnessComparator();
    private Selector selector;
    private Double totalFitness = 0d;
    private FitnessEvaluator knownSolutionFitnessEvaluator;
    private static final boolean COMPARE_TO_KNOWN_SOLUTION_DEFAULT = false;
    private Boolean compareToKnownSolution = COMPARE_TO_KNOWN_SOLUTION_DEFAULT;
    private int targetSize;

    @Autowired
    private TaskExecutor taskExecutor;

    public StandardPopulation() {
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

    public int breed() {
        List<FutureTask<Chromosome>> futureTasks = new ArrayList<>();
        FutureTask<Chromosome> futureTask;

        int individualsAdded = 0;
        for (int i = this.individuals.size(); i < targetSize; i++) {
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

        for (int i = this.individuals.size() - 1; i >= 0; i--) {
            individual = this.individuals.get(i);

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
    public Chromosome evaluateFitness(GenerationStatistics generationStatistics) {
        generationStatistics.setNumberOfEvaluations(this.doConcurrentFitnessEvaluations(this.fitnessEvaluator));

        return updateFitness(generationStatistics);
    }

    protected Chromosome updateFitness(GenerationStatistics generationStatistics) {
        this.totalFitness = 0d;

        Chromosome bestFitIndividual = null;

        for (Chromosome individual : individuals) {
            this.totalFitness += individual.getFitness();

            if (bestFitIndividual == null || individual.getFitness() > bestFitIndividual.getFitness()) {
                bestFitIndividual = individual;
            }
        }

        Double averageFitness = this.totalFitness / individuals.size();

        if (generationStatistics != null) {
            generationStatistics.setAverageFitness(averageFitness);
            generationStatistics.setBestFitness(bestFitIndividual.getFitness());

            if (this.compareToKnownSolution) {
                /*
                 * We have to clone the best fit individual since the knownSolutionFitnessEvaluator sets properties on
                 * the Chromosome, and we want it to do that in all other cases.
                 */
                Chromosome bestFitClone = bestFitIndividual.clone();
                generationStatistics.setKnownSolutionProximity(this.knownSolutionFitnessEvaluator.evaluate(bestFitClone) * 100.0d);
            }
        }

        return bestFitIndividual;
    }

    /*
     * This method depends on the totalFitness and individuals' fitness being accurately maintained. Returns the index
     * of the Chromosome chosen.
     */
    public int selectIndex() {
        return this.selector.getNextIndex(individuals);
    }

    /**
     * @return the individuals
     */
    @Override
    public List<Chromosome> getIndividuals() {
        return Collections.unmodifiableList(individuals);
    }

    /**
     * Removes an individual from the population based on its index. This is much more efficient than removing by
     * equality.
     */
    public Chromosome removeIndividual(int indexToRemove) {
        if (indexToRemove < 0 || indexToRemove > this.individuals.size() - 1) {
            log.error("Tried to remove individual by invalid index {} from population of size {}.  Returning.", indexToRemove, this.size());

            return null;
        }

        this.totalFitness -= this.individuals.get(indexToRemove).getFitness();

        return this.individuals.remove(indexToRemove);
    }

    @Override
    public void clearIndividuals() {
        this.individuals.clear();

        this.totalFitness = 0d;
    }

    /**
     * @param individual
     */
    public boolean addIndividual(Chromosome individual) {
        this.individuals.add(individual);

        individual.setPopulation(this);

        this.totalFitness += individual.getFitness() == null ? 0d : individual.getFitness();

        return individual.isEvaluationNeeded();
    }

    public int size() {
        return this.individuals.size();
    }

    public void sortIndividuals() {
        Collections.sort(individuals, this.fitnessComparator);
    }

    /**
     * Prints every Chromosome in this population in ascending order by fitness. Note that a lower fitness value can be
     * a better value depending on the strategy.
     */
    @Override
    public void printAscending() {
        this.sortIndividuals();

        int size = this.individuals.size();

        for (int i = 0; i < size; i++) {
            log.info("Chromosome {}: {}", (i + 1), this.individuals.get(i));
        }
    }

    @Override
    public void reIndexSelector() {
        Collections.sort(this.individuals);
        this.selector.reIndex(this.individuals);
    }

    /**
     * @return the totalFitness
     */
    @Override
    public Double getTotalFitness() {
        return totalFitness;
    }

    /**
     * @param breeder the breeder to set
     */
    @Override
    public void setBreeder(Breeder breeder) {
        this.breeder = breeder;
    }

    @Override
    public void setFitnessEvaluator(FitnessEvaluator fitnessEvaluator) {
        this.fitnessEvaluator = fitnessEvaluator;
    }

    @Override
    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void setKnownSolutionFitnessEvaluator(FitnessEvaluator knownSolutionFitnessEvaluator) {
        this.knownSolutionFitnessEvaluator = knownSolutionFitnessEvaluator;
    }

    @Override
    public void setCompareToKnownSolution(Boolean compareToKnownSolution) {
        this.compareToKnownSolution = compareToKnownSolution;
    }

    @Override
    public void setTargetSize(int targetSize) {
        this.targetSize = targetSize;
    }
}
