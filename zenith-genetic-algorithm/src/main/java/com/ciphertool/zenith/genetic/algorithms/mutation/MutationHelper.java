/**
 * Copyright 2017-2019 George Belden
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class MutationHelper {
    @Value("${genetic-algorithm.mutation.max-attempts}")
    private int maxMutations;

    @Value("${genetic-algorithm.mutation.count.factor}")
    private double mutationCountFactor;

    public int getNumMutations(int numGenes) {
        return ThreadLocalRandom.current().nextInt(Math.min(maxMutations, numGenes)) + 1;
    }

    public int getNumMutationsDistributed(int numGenes) {
        int max = Math.min(maxMutations, numGenes);

        for (int i = 1; i <= max; i++) {
            if (ThreadLocalRandom.current().nextDouble() < mutationCountFactor) {
                return i;
            }
        }

        return 1;
    }
}
