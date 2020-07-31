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
import com.ciphertool.zenith.genetic.entities.Chromosome;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TruncationSelector implements Selector {
    private Logger log = LoggerFactory.getLogger(getClass());

    private RandomSelector randomSelector;

    @Autowired
    public TruncationSelector(RandomSelector randomSelector) {
        this.randomSelector = randomSelector;
    }

    @Override
    public Selector getInstance() {
        return new TruncationSelector(randomSelector);
    }

    @Override
    public synchronized void reIndex(List<Chromosome> individuals) {
        // Nothing to do
    }

    @Override
    public int getNextIndex(List<Chromosome> individuals, GeneticAlgorithmStrategy strategy) {
        if (CollectionUtils.isEmpty(individuals)) {
            log.warn("Attempted to select an individual from a null or empty population.  Unable to continue.");

            return -1;
        }

        int truncationPoint = (int) (individuals.size() * strategy.getTruncationPercentage());

        return randomSelector.getNextIndex(individuals.subList(truncationPoint, individuals.size()), strategy) + truncationPoint;
    }
}
