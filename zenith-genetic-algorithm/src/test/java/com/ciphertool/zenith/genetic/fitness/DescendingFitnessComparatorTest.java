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

package com.ciphertool.zenith.genetic.fitness;

import com.ciphertool.zenith.genetic.mocks.MockKeyedChromosome;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DescendingFitnessComparatorTest {
    @Test
    public void testCompare() {
        MockKeyedChromosome higherFitness = new MockKeyedChromosome();
        higherFitness.setFitness(2.0d);

        MockKeyedChromosome lowerFitness = new MockKeyedChromosome();
        lowerFitness.setFitness(1.0d);

        DescendingFitnessComparator descendingFitnessComparator = new DescendingFitnessComparator();

        int result = descendingFitnessComparator.compare(higherFitness, lowerFitness);
        assertEquals(-1, result);

        result = descendingFitnessComparator.compare(lowerFitness, higherFitness);
        assertEquals(1, result);
    }

    @Test
    public void testCompareEqual() {
        MockKeyedChromosome mockA = new MockKeyedChromosome();
        mockA.setFitness(3.0d);

        MockKeyedChromosome mockB = new MockKeyedChromosome();
        mockB.setFitness(3.0d);

        DescendingFitnessComparator descendingFitnessComparator = new DescendingFitnessComparator();
        int result = descendingFitnessComparator.compare(mockA, mockB);
        assertEquals(0, result);
    }
}
