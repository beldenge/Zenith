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
package com.ciphertool.zenith.genetic.operators.selection;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.operators.sort.ParetoSorter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class TournamentSelector implements Selector {
    private Logger log = LoggerFactory.getLogger(getClass());

    private RandomSelector randomSelector;

    @Autowired
    public TournamentSelector(RandomSelector randomSelector) {
        this.randomSelector = randomSelector;
    }

    @Override
    public Selector getInstance() {
        return new TournamentSelector(randomSelector);
    }

    @Override
    public void reIndex(List<Genome> individuals) {
        // Nothing to do
    }

    @Override
    public int getNextIndex(List<Genome> individuals, GeneticAlgorithmStrategy strategy) {
        if (CollectionUtils.isEmpty(individuals)) {
            log.warn("Attempted to select an individual from a null or empty population.  Unable to continue.");

            return -1;
        }

        double selectionAccuracy = strategy.getTournamentSelectorAccuracy();
        int tournamentSize = strategy.getTournamentSize();

        List<IndexedIndividual> indexedCompetitors = new ArrayList<>(tournamentSize);

        for (int i = 0; i < Math.min(tournamentSize, individuals.size()); i ++) {
            int chosenIndex = randomSelector.getNextIndex(individuals, strategy);
            indexedCompetitors.add(new IndexedIndividual(chosenIndex, individuals.get(chosenIndex)));
        }

        List<Genome> competitors = indexedCompetitors.stream()
                .map(IndexedIndividual::getIndividual)
                .collect(Collectors.toList());
        ParetoSorter.sort(competitors);

        List<IndexedIndividual> sortedIndexedCompetitors = new ArrayList<>(indexedCompetitors.size());
        for (Genome competitor : competitors) {
            for (IndexedIndividual indexedCompetitor : indexedCompetitors) {
                if (competitor == indexedCompetitor.getIndividual()) {
                    sortedIndexedCompetitors.add(indexedCompetitor);
                    break;
                }
            }
        }

        for (int i = sortedIndexedCompetitors.size() - 1; i >= 0; i --) {
            if (ThreadLocalRandom.current().nextDouble() <= selectionAccuracy) {
                return sortedIndexedCompetitors.get(i).getIndex();
            }
        }

        // return the least fit individual since it won the tournament
        return indexedCompetitors.get(indexedCompetitors.size() - 1).getIndex();
    }

    @AllArgsConstructor
    @Getter
    private class IndexedIndividual {
        private int index;
        private Genome individual;
    }
}
