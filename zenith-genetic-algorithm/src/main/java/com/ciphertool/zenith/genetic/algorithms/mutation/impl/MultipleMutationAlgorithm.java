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

package com.ciphertool.zenith.genetic.algorithms.mutation.impl;

import com.ciphertool.zenith.genetic.algorithms.mutation.MutationAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.mutation.MutationHelper;
import com.ciphertool.zenith.genetic.dao.GeneDao;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.KeyedChromosome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MultipleMutationAlgorithm implements MutationAlgorithm<KeyedChromosome<Object>> {
    @Autowired
    private MutationHelper mutationHelper;

    @Autowired
    private GeneDao geneDao;

    @Override
    public boolean mutateChromosome(KeyedChromosome<Object> chromosome) {
        Chromosome original = chromosome.clone();
        int numMutations;

        /*
         * Choose a random number of mutations constrained by the configurable max and the total number of genes
         */
        numMutations = mutationHelper.getNumMutations(chromosome.getGenes().size());

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
            // Replace that map value with a randomly generated Gene
            chromosome.replaceGene(key, geneDao.findRandomGene(chromosome));
        }

        return !original.equals(chromosome);
    }
}
