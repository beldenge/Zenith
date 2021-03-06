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
package com.ciphertool.zenith.genetic.operators.selection;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

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

        SortedMap<Integer, Genome> competitors = new TreeMap<>(Comparator.reverseOrder());

        for (int i = 0; i < Math.min(tournamentSize, individuals.size()); i ++) {
            int chosenIndex = randomSelector.getNextIndex(individuals, strategy);

            // TODO: How to handle whether we've chosen the same individual more than once? -- since it's a map, the tournament size just suffers
            // Even though we don't use the map value, it is necessary since we use a SortedMap to order by fitness
            competitors.put(chosenIndex, individuals.get(chosenIndex));
        }

        for (Integer index : competitors.keySet()) {
            if (ThreadLocalRandom.current().nextDouble() <= selectionAccuracy) {
                return index;
            }
        }

        // return the least fit individual since it won the tournament
        return competitors.lastKey();
    }
}
