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

package com.ciphertool.zenith.genetic.algorithms.crossover.impl;

import com.ciphertool.zenith.genetic.algorithms.crossover.CrossoverAlgorithm;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.KeyedChromosome;
import com.ciphertool.zenith.genetic.util.Coin;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EqualOpportunityGeneCrossoverAlgorithm implements CrossoverAlgorithm<KeyedChromosome<Object>> {
    private Coin coin = new Coin();

    @Override
    public List<KeyedChromosome<Object>> crossover(KeyedChromosome<Object> parentA, KeyedChromosome<Object> parentB) {
        List<KeyedChromosome<Object>> children = new ArrayList<>(1);

        KeyedChromosome<Object> child = performCrossover(parentA, parentB);

        // The Chromosome could be null if it's identical to one of its parents
        if (child != null) {
            children.add(child);
        }

        return children;
    }

    @SuppressWarnings("unchecked")
    protected KeyedChromosome<Object> performCrossover(KeyedChromosome<Object> parentA, KeyedChromosome<Object> parentB) {
        KeyedChromosome<Object> child = (KeyedChromosome<Object>) parentA.clone();

        Object key;

        for (Map.Entry<Object, Gene> entry : parentA.getGenes().entrySet()) {
            key = entry.getKey();

            if (coin.flip()) {
                if (!child.getGenes().get(key).equals(parentB.getGenes().get(key))) {
                    child.replaceGene(key, parentB.getGenes().get(key).clone());
                }
            }
        }

        return child;
    }

    @Override
    public int numberOfOffspring() {
        return 1;
    }
}
