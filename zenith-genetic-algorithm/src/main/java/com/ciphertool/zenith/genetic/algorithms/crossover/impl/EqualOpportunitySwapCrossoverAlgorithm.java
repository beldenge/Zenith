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

package com.ciphertool.zenith.genetic.algorithms.crossover.impl;

import com.ciphertool.zenith.genetic.algorithms.crossover.CrossoverAlgorithm;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.util.Coin;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EqualOpportunitySwapCrossoverAlgorithm implements CrossoverAlgorithm<Chromosome<Object>> {
    private Coin coin = new Coin();

    @Override
    public List<Chromosome<Object>> crossover(Chromosome<Object> parentA, Chromosome<Object> parentB) {
        return performCrossover(parentA, parentB);
    }

    @SuppressWarnings("unchecked")
    protected List<Chromosome<Object>> performCrossover(Chromosome<Object> parentA, Chromosome<Object> parentB) {
        Chromosome<Object> childA = (Chromosome<Object>) parentA.clone();
        Chromosome<Object> childB = (Chromosome<Object>) parentB.clone();

        for (Object key : parentA.getGenes().keySet()) {
            if (coin.flip()) {
                childA.replaceGene(key, parentB.getGenes().get(key).clone());
                childB.replaceGene(key, parentA.getGenes().get(key).clone());
            }
        }

        List<Chromosome<Object>> children = new ArrayList<>(2);
        children.add(childA);
        children.add(childB);

        return children;
    }

    @Override
    public int numberOfOffspring() {
        return 2;
    }
}
