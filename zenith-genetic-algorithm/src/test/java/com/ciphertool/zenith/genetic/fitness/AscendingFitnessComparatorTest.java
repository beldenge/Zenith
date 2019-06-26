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

import com.ciphertool.zenith.genetic.mocks.MockChromosome;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AscendingFitnessComparatorTest {
    @Test
    public void testCompare() {
        MockChromosome higherFitness = new MockChromosome();
        higherFitness.setFitness(2.0d);

        MockChromosome lowerFitness = new MockChromosome();
        lowerFitness.setFitness(1.0d);

        AscendingFitnessComparator ascendingFitnessComparator = new AscendingFitnessComparator();

        int result = ascendingFitnessComparator.compare(higherFitness, lowerFitness);
        assertEquals(1, result);

        result = ascendingFitnessComparator.compare(lowerFitness, higherFitness);
        assertEquals(-1, result);
    }

    @Test
    public void testCompareEqual() {
        MockChromosome mockA = new MockChromosome();
        mockA.setFitness(3.0d);

        MockChromosome mockB = new MockChromosome();
        mockB.setFitness(3.0d);

        AscendingFitnessComparator ascendingFitnessComparator = new AscendingFitnessComparator();
        int result = ascendingFitnessComparator.compare(mockA, mockB);
        assertEquals(0, result);
    }
}
