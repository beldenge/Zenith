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

package com.ciphertool.zenith.genetic.algorithms.mutation;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.dao.GeneDao;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MultipleMutationAlgorithm implements MutationAlgorithm<Chromosome<Object>> {
    @Autowired
    private GeneDao geneDao;

    @Override
    public boolean mutateChromosome(Chromosome<Object> chromosome, GeneticAlgorithmStrategy strategy) {
        int maxMutations = strategy.getMaxMutationsPerIndividual();
        boolean mutated = false;
        int numMutations;

        /*
         * Choose a random number of mutations constrained by the configurable max and the total number of genes
         */
        numMutations = ThreadLocalRandom.current().nextInt(Math.min(maxMutations, chromosome.getGenes().size())) + 1;

        List<Object> availableKeys = new ArrayList<>(chromosome.getGenes().keySet());
        Map<Object, Gene> originalGenes = new HashMap<>(numMutations);

        for (int i = 0; i < numMutations; i++) {
            /*
             * We don't want to reuse an index, so we get one from the List of indices which are still available
             */
            int randomIndex = (int) (ThreadLocalRandom.current().nextDouble() * availableKeys.size());
            Object randomKey = availableKeys.get(randomIndex);
            originalGenes.put(randomKey, chromosome.getGenes().get(randomKey));
            availableKeys.remove(randomIndex);
        }

        for (Object key : originalGenes.keySet()) {
            Gene next = geneDao.findRandomGene(chromosome);

            if (!next.equals(chromosome.getGenes().get(key))) {
                mutated = true;

                // Replace that map value with a randomly generated Gene
                chromosome.replaceGene(key, next);
            }
        }

        return mutated;
    }
}
