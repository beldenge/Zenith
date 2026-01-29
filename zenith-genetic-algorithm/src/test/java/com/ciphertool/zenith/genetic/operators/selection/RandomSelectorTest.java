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

package com.ciphertool.zenith.genetic.operators.selection;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class RandomSelectorTest {
    @Test
    public void testGetNextIndexEmptyReturnsMinusOne() {
        RandomSelector selector = new RandomSelector();

        int index = selector.getNextIndex(Collections.emptyList(), mock(GeneticAlgorithmStrategy.class));

        assertEquals(-1, index);
    }

    @Test
    public void testGetNextIndexSingleElementAlwaysZero() {
        RandomSelector selector = new RandomSelector();

        Genome genome = new Genome(false, new com.ciphertool.zenith.genetic.fitness.Fitness[] { new MaximizingFitness(1.0d) }, null);
        List<Genome> individuals = Collections.singletonList(genome);

        int index = selector.getNextIndex(individuals, mock(GeneticAlgorithmStrategy.class));

        assertEquals(0, index);
    }
}
