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

package com.ciphertool.zenith.genetic.population;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Parents;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@NoArgsConstructor
@Component
public class StandardPopulation extends AbstractPopulation {
    private Logger log = LoggerFactory.getLogger(getClass());

    private List<Chromosome> individuals = new ArrayList<>();

    public StandardPopulation(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public StandardPopulation getInstance() {
        return new StandardPopulation(taskExecutor);
    }

    @Override
    public void init(GeneticAlgorithmStrategy strategy) {
        super.init(strategy);
    }

    @Override
    public Callable newSelectionTask(){
        return new SelectionTask();
    }

    private class SelectionTask implements Callable<Parents> {
        public SelectionTask() {}

        @Override
        public Parents call() {
            int momIndex = strategy.getSelector().getNextIndex(individuals, strategy);
            Chromosome mom = getIndividuals().get(momIndex);

            int dadIndex = strategy.getSelector().getNextIndex(individuals, strategy);
            Chromosome dad = getIndividuals().get(dadIndex);

            return new Parents(mom, dad);
        }
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
    public synchronized Chromosome removeIndividual(int indexToRemove) {
        if (indexToRemove < 0 || indexToRemove > this.individuals.size() - 1) {
            log.error("Tried to remove individual by invalid index {} from population of size {}.  Returning.", indexToRemove, this.size());

            return null;
        }

        this.totalFitness -= this.individuals.get(indexToRemove).getFitness();
        this.totalProbability -= convertFromLogProbability(this.individuals.get(indexToRemove).getFitness());

        return this.individuals.remove(indexToRemove);
    }

    @Override
    public void clearIndividuals() {
        this.individuals.clear();

        this.totalFitness = 0d;
        this.totalProbability = 0d;
    }

    @Override
    public synchronized boolean addIndividual(Chromosome individual) {
        this.individuals.add(individual);

        individual.setPopulation(this);

        this.totalFitness += individual.getFitness() == null ? 0d : individual.getFitness();
        this.totalProbability += convertFromLogProbability(individual.getFitness() == null ? 0d : individual.getFitness());

        return individual.isEvaluationNeeded();
    }

    public int size() {
        return this.individuals.size();
    }

    @Override
    public void sortIndividuals() {
        Collections.sort(individuals);
    }

    @Override
    public void reIndexSelector() {
        // This sort is necessary since we rely on the List index for selection
        Collections.sort(this.individuals);
        this.strategy.getSelector().reIndex(this.individuals);
    }
}
