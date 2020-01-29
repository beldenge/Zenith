/**
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

import com.ciphertool.zenith.genetic.dao.GeneDao;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MandatorySingleMutationAlgorithm implements MutationAlgorithm<Chromosome<Object>> {
    @Autowired
    private GeneDao geneDao;

    @Override
    public boolean mutateChromosome(Chromosome<Object> chromosome) {
        Chromosome original = chromosome.clone();

        List<Object> availableKeys = new ArrayList<>(chromosome.getGenes().keySet());

        int randomIndex = (int) (ThreadLocalRandom.current().nextDouble() * availableKeys.size());
        Object randomKey = availableKeys.get(randomIndex);

        // Replace that map value with a randomly generated Gene
        chromosome.replaceGene(randomKey, geneDao.findRandomGene(chromosome));

        return !original.equals(chromosome);
    }
}
