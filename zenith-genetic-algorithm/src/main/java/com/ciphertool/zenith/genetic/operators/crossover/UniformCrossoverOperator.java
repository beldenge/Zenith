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
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.util.Coin;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UniformCrossoverOperator implements CrossoverOperator {
    private Coin coin = new Coin();

    @Override
    public Genome crossover(Genome firstGenome, Genome secondGenome) {
        Genome childGenome = new Genome(firstGenome.isEvaluationNeeded(), firstGenome.getFitnesses(), firstGenome.getPopulation());

        for (int i = 0; i < firstGenome.getChromosomes().size(); i ++) {
            Chromosome<Object> childChromosome = (Chromosome<Object>) firstGenome.getChromosomes().get(i).clone();
            childChromosome.setGenome(childGenome);
            Chromosome<Object> parentB = secondGenome.getChromosomes().get(i);

            Gene next;

            for (Map.Entry<Object, Gene> entry : childChromosome.getGenes().entrySet()) {
                if (coin.flip()) {
                    next = parentB.getGenes().get(entry.getKey());

                    if (!entry.getValue().equals(next)) {
                        childChromosome.replaceGene(entry.getKey(), next.clone());
                    }
                } else {
                    // TODO: clone from parentA -- this could potentially save 50% of the cloning overhead vs. cloning the entire chromosome upfront
                }
            }

            childGenome.addChromosome(childChromosome);
        }

        return childGenome;
    }
}
