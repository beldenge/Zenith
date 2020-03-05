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

package com.ciphertool.zenith.genetic.algorithms.crossover;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.util.Coin;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Set;

@Component
public class RandomSinglePointCrossoverAlgorithm implements CrossoverAlgorithm<Chromosome<Object>> {
    private Coin coin = new Coin();

    @Override
    public Chromosome<Object> crossover(Chromosome<Object> parentA, Chromosome<Object> parentB) {
        Random generator = new Random();
        Set<Object> availableKeys = parentA.getGenes().keySet();
        Object[] keys = availableKeys.toArray();

        // Get a random map key
        int randomIndex = generator.nextInt(keys.length);

        // Replace all the Genes from the map key to the end of the array
        Chromosome<Object> dad = coin.flip() ? parentA : parentB;
        Chromosome<Object> mom = (dad == parentA) ? parentB : parentA;

        Chromosome<Object> child = dad.clone();
        for (int i = 0; i <= randomIndex; i++) {
            Object nextKey = keys[i];

            if (null == mom.getGenes().get(nextKey)) {
                throw new IllegalStateException("Expected second parent to have a Gene with key " + nextKey
                        + ", but no such key was found.  Cannot continue.");
            }

            child.replaceGene(nextKey, mom.getGenes().get(nextKey).clone());
        }

        return child;
    }
}
