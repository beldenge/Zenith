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
import com.ciphertool.zenith.genetic.entities.KeyedChromosome;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
public class RandomSinglePointCrossoverAlgorithm implements CrossoverAlgorithm<KeyedChromosome<Object>> {
    @Override
    public List<KeyedChromosome<Object>> crossover(KeyedChromosome<Object> parentA, KeyedChromosome<Object> parentB) {
        List<KeyedChromosome<Object>> children = new ArrayList<KeyedChromosome<Object>>(1);

        KeyedChromosome<Object> child = performCrossover(parentA, parentB);

        // The Chromosome could be null if it's identical to one of its parents
        if (child != null) {
            children.add(child);
        }

        return children;
    }

    @SuppressWarnings("unchecked")
    protected KeyedChromosome<Object> performCrossover(KeyedChromosome<Object> parentA, KeyedChromosome<Object> parentB) {
        Random generator = new Random();
        Set<Object> availableKeys = parentA.getGenes().keySet();
        Object[] keys = availableKeys.toArray();

        // Get a random map key
        int randomIndex = generator.nextInt(keys.length);

        // Replace all the Genes from the map key to the end of the array
        KeyedChromosome<Object> child = (KeyedChromosome<Object>) parentA.clone();
        for (int i = 0; i <= randomIndex; i++) {
            Object nextKey = keys[i];

            if (null == parentB.getGenes().get(nextKey)) {
                throw new IllegalStateException("Expected second parent to have a Gene with key " + nextKey
                        + ", but no such key was found.  Cannot continue.");
            }

            child.replaceGene(nextKey, parentB.getGenes().get(nextKey).clone());
        }

        return child;
    }

    @Override
    public int numberOfOffspring() {
        return 1;
    }
}
