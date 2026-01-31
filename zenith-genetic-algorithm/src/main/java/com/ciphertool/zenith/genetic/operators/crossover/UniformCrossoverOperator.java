/*
 * Copyright 2017-2026 George Belden
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
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.util.Coin;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UniformCrossoverOperator implements CrossoverOperator {
    private Coin coin = new Coin();

    @Override
    public Genome crossover(Genome firstGenome, Genome secondGenome) {
        // BUG FIX: Always set evaluationNeeded to true for children. A child created by crossover
        // has potentially different genes than either parent and must be evaluated. Previously,
        // inheriting the parent's state could cause children to skip fitness evaluation entirely
        // if the parent was already evaluated.
        Genome childGenome = new Genome(true, firstGenome.getFitnesses(), firstGenome.getPopulation());

        for (int i = 0; i < firstGenome.getChromosomes().size(); i ++) {
            Chromosome<Object> childChromosome = (Chromosome<Object>) firstGenome.getChromosomes().get(i).clone();
            childChromosome.setGenome(childGenome);
            Chromosome<Object> parentB = secondGenome.getChromosomes().get(i);

            Gene next;

            // Iterate over a snapshot of keys because replaceGene mutates the backing map.
            for (Object key : new ArrayList<>(childChromosome.getGenes().keySet())) {
                if (coin.flip()) {
                    next = parentB.getGenes().get(key);

                    if (next == null) {
                        throw new IllegalStateException("Expected second parent to have a Gene with key " + key
                                + ", but no such key was found.  Cannot continue.");
                    }

                    if (!childChromosome.getGenes().get(key).equals(next)) {
                        childChromosome.replaceGene(key, next.clone());
                    }
                }
                // Else, keep the gene from parentA (already cloned). Future optimization: clone genes individually instead of cloning the entire chromosome upfront to save ~33% cloning overhead.
            }

            childGenome.addChromosome(childChromosome);
        }

        return childGenome;
    }
}
