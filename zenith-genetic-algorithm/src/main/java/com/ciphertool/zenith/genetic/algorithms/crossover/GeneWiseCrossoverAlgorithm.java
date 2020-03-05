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
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.util.Coin;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GeneWiseCrossoverAlgorithm implements CrossoverAlgorithm<Chromosome<Object>> {
    private Coin coin = new Coin();

    @Override
    public Chromosome<Object> crossover(Chromosome<Object> parentA, Chromosome<Object> parentB) {
        Chromosome<Object> child = (Chromosome<Object>) parentA.clone();

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
}
