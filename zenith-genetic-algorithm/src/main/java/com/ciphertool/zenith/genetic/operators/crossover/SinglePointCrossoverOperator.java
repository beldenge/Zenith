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

package com.ciphertool.zenith.genetic.operators.crossover;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.util.Coin;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Set;

@Component
public class SinglePointCrossoverOperator implements CrossoverOperator {
    private Coin coin = new Coin();

    @Override
    public Genome crossover(Genome firstGenome, Genome secondGenome) {
        // TODO: consider using ThreadLocalRandom
        Random generator = new Random();

        Genome childGenome = new Genome(false, 0d, firstGenome.getPopulation());

        for (int i = 0; i < firstGenome.getChromosomes().size(); i ++) {
            Chromosome parentA = firstGenome.getChromosomes().get(i);
            Chromosome parentB = secondGenome.getChromosomes().get(i);

            Set<Object> availableKeys = parentA.getGenes().keySet();
            Object[] keys = availableKeys.toArray();

            // Get a random map key
            int randomIndex = generator.nextInt(keys.length);

            // Replace all the Genes from the map key to the end of the array
            Chromosome<Object> dad = coin.flip() ? parentA : parentB;
            Chromosome<Object> mom = (dad == parentA) ? parentB : parentA;

            Chromosome<Object> childChromosome = dad.clone();
            childChromosome.setGenome(childGenome);

            for (int j = 0; j <= randomIndex; i++) {
                Object nextKey = keys[j];

                if (null == mom.getGenes().get(nextKey)) {
                    throw new IllegalStateException("Expected second parent to have a Gene with key " + nextKey
                            + ", but no such key was found.  Cannot continue.");
                }

                childChromosome.replaceGene(nextKey, mom.getGenes().get(nextKey).clone());
            }

            childGenome.addChromosome(childChromosome);
        }

        return childGenome;
    }
}
