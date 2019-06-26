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
package com.ciphertool.zenith.genetic.algorithms.selection;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlphaSelector implements Selector {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public synchronized void reIndex(List<Chromosome> individuals) {
        // Nothing to do
    }

    @Override
    public int getNextIndex(List<Chromosome> individuals, Double totalFitness) {
        if (individuals == null || individuals.isEmpty()) {
            log.warn("Attempted to select an individual from a null or empty population.  Unable to continue.");

            return -1;
        }

        Chromosome bestFitIndividual = null;
        Integer bestFitIndex = null;

        Chromosome currentIndividual;
        for (int i = 0; i < individuals.size(); i++) {
            currentIndividual = individuals.get(i);

            if (bestFitIndex == null || currentIndividual.getFitness().compareTo(bestFitIndividual.getFitness()) > 0) {
                bestFitIndividual = currentIndividual;
                bestFitIndex = i;
            }
        }

        return (bestFitIndex != null) ? bestFitIndex : -1;
    }
}
