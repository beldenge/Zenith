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
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.operators.sort.ParetoSorter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@NoArgsConstructor
@Component
public class StandardPopulation extends AbstractPopulation {
    private Logger log = LoggerFactory.getLogger(getClass());

    private List<Genome> individuals = new ArrayList<>();

    @Override
    public StandardPopulation getInstance() {
        return new StandardPopulation();
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
            Genome mom = getIndividuals().get(momIndex);

            int dadIndex = strategy.getSelector().getNextIndex(individuals, strategy);
            Genome dad = getIndividuals().get(dadIndex);

            return new Parents(mom, dad);
        }
    }

    @Override
    public List<Genome> getIndividuals() {
        return Collections.unmodifiableList(individuals);
    }

    /**
     * Removes an individual from the population based on its index. This is much more efficient than removing by
     * equality.
     */
    public synchronized Genome removeIndividual(int indexToRemove) {
        if (indexToRemove < 0 || indexToRemove > this.individuals.size() - 1) {
            log.error("Tried to remove individual by invalid index {} from population of size {}.  Returning.", indexToRemove, this.size());

            return null;
        }

        Genome individualToRemove = this.individuals.get(indexToRemove);

        if (individualToRemove.getFitnesses() != null && individualToRemove.getFitnesses().length == 1) {
            this.totalFitness -= individualToRemove.getFitnesses()[0].getValue();
            this.totalProbability -= convertFromLogProbability(individualToRemove.getFitnesses()[0].getValue());
        }

        return this.individuals.remove(indexToRemove);
    }

    @Override
    public void clearIndividuals() {
        this.individuals.clear();

        this.totalFitness = 0d;
        this.totalProbability = 0d;
    }

    @Override
    public synchronized boolean addIndividual(Genome individual) {
        this.individuals.add(individual);

        individual.setPopulation(this);

        if (individual.getFitnesses() != null && individual.getFitnesses().length == 1) {
            this.totalFitness += individual.getFitnesses()[0].getValue();
            this.totalProbability += convertFromLogProbability(individual.getFitnesses()[0].getValue());
        }

        return individual.isEvaluationNeeded();
    }

    public int size() {
        return this.individuals.size();
    }

    @Override
    public void sortIndividuals() {
        ParetoSorter.sort(individuals);
    }

    @Override
    public void reIndexSelector() {
        // This sort is necessary since we rely on the List index for selection
        sortIndividuals();
        this.strategy.getSelector().reIndex(this.individuals);
    }
}
