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
import com.ciphertool.zenith.genetic.population.Population;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TruncationSelectorTest {
    @Test
    public void testGetNextIndexEmptyListReturnsMinusOne() {
        RandomSelector randomSelector = mock(RandomSelector.class);
        TruncationSelector selector = new TruncationSelector(randomSelector);

        int result = selector.getNextIndex(Collections.emptyList(), mock(GeneticAlgorithmStrategy.class));

        assertEquals(-1, result);
        verifyNoInteractions(randomSelector);
    }

    @Test
    public void testGetNextIndexDelegatesToRandomSelector() {
        RandomSelector randomSelector = mock(RandomSelector.class);
        TruncationSelector selector = new TruncationSelector(randomSelector);

        Population population = mock(Population.class);
        List<Genome> individuals = List.of(new Genome(false, new com.ciphertool.zenith.genetic.fitness.Fitness[] { new MaximizingFitness(1.0d) }, population));
        GeneticAlgorithmStrategy strategy = mock(GeneticAlgorithmStrategy.class);

        when(randomSelector.getNextIndex(individuals, strategy)).thenReturn(0);

        int result = selector.getNextIndex(individuals, strategy);

        assertEquals(0, result);
        verify(randomSelector).getNextIndex(individuals, strategy);
        verifyNoMoreInteractions(randomSelector);
    }
}
