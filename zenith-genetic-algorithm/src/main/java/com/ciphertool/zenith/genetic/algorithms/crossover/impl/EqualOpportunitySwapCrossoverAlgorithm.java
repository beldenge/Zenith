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
import com.ciphertool.zenith.genetic.util.Coin;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EqualOpportunitySwapCrossoverAlgorithm implements CrossoverAlgorithm<KeyedChromosome<Object>> {
    private Coin coin = new Coin();

    @Override
    public List<KeyedChromosome<Object>> crossover(KeyedChromosome<Object> parentA, KeyedChromosome<Object> parentB) {
        return performCrossover(parentA, parentB);
    }

    @SuppressWarnings("unchecked")
    protected List<KeyedChromosome<Object>> performCrossover(KeyedChromosome<Object> parentA, KeyedChromosome<Object> parentB) {
        KeyedChromosome<Object> childA = (KeyedChromosome<Object>) parentA.clone();
        KeyedChromosome<Object> childB = (KeyedChromosome<Object>) parentB.clone();

        for (Object key : parentA.getGenes().keySet()) {
            if (coin.flip()) {
                childA.replaceGene(key, parentB.getGenes().get(key).clone());
                childB.replaceGene(key, parentA.getGenes().get(key).clone());
            }
        }

        List<KeyedChromosome<Object>> children = new ArrayList<>(2);
        children.add(childA);
        children.add(childB);

        return children;
    }

    @Override
    public int numberOfOffspring() {
        return 2;
    }
}
