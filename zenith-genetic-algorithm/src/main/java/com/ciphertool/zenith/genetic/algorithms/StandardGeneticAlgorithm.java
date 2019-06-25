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

package com.ciphertool.zenith.genetic.algorithms;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.population.StandardPopulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Component
public class StandardGeneticAlgorithm extends AbstractGeneticAlgorithm {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${genetic-algorithm.elitism}")
    private int elitism;

    /**
     * A concurrent task for performing a crossover of two parent Chromosomes, producing one child Chromosome.
     */
    protected class CrossoverTask implements Callable<List<Chromosome>> {
        private Chromosome mom;
        private Chromosome dad;

        public CrossoverTask(Chromosome mom, Chromosome dad) {
            this.mom = mom;
            this.dad = dad;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<Chromosome> call() {
            return crossoverAlgorithm.crossover(mom, dad);
        }
    }

    protected class SelectionTask implements Callable<SelectionResult> {
        public SelectionTask() {
        }

        @Override
        public SelectionResult call() {
            StandardPopulation standardPopulation = (StandardPopulation) population;

            int momIndex = standardPopulation.selectIndex();
            Chromosome mom = standardPopulation.getIndividuals().get(momIndex);

            int dadIndex = standardPopulation.selectIndex();
            // Ensure that dadIndex is different from momIndex
            dadIndex += (dadIndex == momIndex) ? ((dadIndex == 0) ? 1 : -1) : 0;
            Chromosome dad = standardPopulation.getIndividuals().get(dadIndex);

            return new SelectionResult(mom, dad);
        }
    }

    public void select(int initialPopulationSize, List<Chromosome> moms, List<Chromosome> dads) {
        long pairsToCrossover = (initialPopulationSize - elitism) / this.crossoverAlgorithm.numberOfOffspring();

        List<FutureTask<SelectionResult>> futureTasks = new ArrayList<>();
        FutureTask<SelectionResult> futureTask;

        /*
         * Execute each selection concurrently. Each should produce two children, but this is not necessarily always
         * guaranteed.
         */
        for (int i = 0; i < Math.max(0, pairsToCrossover); i++) {
            futureTask = new FutureTask<>(new SelectionTask());
            futureTasks.add(futureTask);
            this.taskExecutor.execute(futureTask);
        }

        // Add the result of each FutureTask to the Lists of Chromosomes selected for subsequent crossover
        for (FutureTask<SelectionResult> future : futureTasks) {
            try {
                SelectionResult result = future.get();
                moms.add(result.getMom());
                dads.add(result.getDad());
            } catch (InterruptedException ie) {
                log.error("Caught InterruptedException while waiting for SelectionTask ", ie);
            } catch (ExecutionException ee) {
                log.error("Caught ExecutionException while waiting for SelectionTask ", ee);
            }
        }
    }

    public int crossover(int pairsToCrossover, List<Chromosome> moms, List<Chromosome> dads) {
        StandardPopulation standardPopulation = (StandardPopulation) this.population;

        if (this.population.size() < 2) {
            log.info("Unable to perform crossover because there is only 1 individual in the population. Returning.");

            return 0;
        }

        log.debug("Pairs to crossover: {}", pairsToCrossover);

        List<Chromosome> crossoverResults = doConcurrentCrossovers(moms, dads);
        List<Chromosome> childrenToAdd = new ArrayList<>();

        if (crossoverResults != null && !crossoverResults.isEmpty()) {
            childrenToAdd.addAll(crossoverResults);
        }

        if (childrenToAdd == null || (childrenToAdd.size() + elitism) < pairsToCrossover) {
            log.error("{} children produced from concurrent crossover execution.  Expected {} children.", ((null == childrenToAdd) ? "No" : childrenToAdd.size()), pairsToCrossover);

            return ((null == childrenToAdd) ? 0 : childrenToAdd.size());
        }

        List<Chromosome> eliteIndividuals = new ArrayList<>();

        if (elitism > 0) {
            standardPopulation.sortIndividuals();

            for (int i = this.population.size() - 1; i >= this.population.size() - elitism; i--) {
                eliteIndividuals.add(this.population.getIndividuals().get(i));
            }
        }

        this.population.clearIndividuals();

        for (Chromosome elite : eliteIndividuals) {
            standardPopulation.addIndividual(elite);
        }

        for (Chromosome child : childrenToAdd) {
            standardPopulation.addIndividual(child);
        }

        return childrenToAdd.size();
    }

    protected List<Chromosome> doConcurrentCrossovers(List<Chromosome> moms, List<Chromosome> dads) {
        if (moms.size() != dads.size()) {
            throw new IllegalStateException(
                    "Attempted to perform crossover on the population, but there are not an equal number of moms and dads.  Something is wrong.  Moms: "
                            + moms.size() + ", Dads:  " + dads.size());
        }

        List<FutureTask<List<Chromosome>>> futureTasks = new ArrayList<>();
        FutureTask<List<Chromosome>> futureTask;

        Chromosome mom;
        Chromosome dad;

        /*
         * Execute each crossover concurrently. Parents should produce two children, but this is not necessarily always
         * guaranteed.
         */
        for (int i = 0; i < moms.size(); i++) {
            mom = moms.get(i);
            dad = dads.get(i);

            futureTask = new FutureTask<>(new CrossoverTask(mom, dad));
            futureTasks.add(futureTask);
            this.taskExecutor.execute(futureTask);
        }

        List<Chromosome> childrenToAdd = new ArrayList<>();

        // Add the result of each FutureTask to the population since it represents a new child Chromosome.
        for (FutureTask<List<Chromosome>> future : futureTasks) {
            try {
                /*
                 * Add children after all crossover operations are completed so that children are not inadvertently
                 * breeding immediately after birth.
                 */
                childrenToAdd.addAll(future.get());
            } catch (InterruptedException ie) {
                log.error("Caught InterruptedException while waiting for CrossoverTask ", ie);
            } catch (ExecutionException ee) {
                log.error("Caught ExecutionException while waiting for CrossoverTask ", ee);
            }
        }

        return childrenToAdd;
    }

    public int mutate(int initialPopulationSize) {
        StandardPopulation standardPopulation = (StandardPopulation) this.population;

        List<FutureTask<Void>> futureTasks = new ArrayList<>();
        FutureTask<Void> futureTask;

        mutations.set(0);

        standardPopulation.sortIndividuals();

        /*
         * Execute each mutation concurrently.
         */
        for (int i = this.population.size() - elitism - 1; i >= 0; i--) {
            futureTask = new FutureTask<>(new MutationTask(this.population.getIndividuals().get(i)));
            futureTasks.add(futureTask);
            this.taskExecutor.execute(futureTask);
        }

        for (FutureTask<Void> future : futureTasks) {
            try {
                future.get();
            } catch (InterruptedException ie) {
                log.error("Caught InterruptedException while waiting for MutationTask ", ie);
            } catch (ExecutionException ee) {
                log.error("Caught ExecutionException while waiting for MutationTask ", ee);
            }
        }

        return mutations.get();
    }
}
