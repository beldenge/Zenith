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

package com.ciphertool.zenith.genetic.operators.sort;

import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParetoSorterTest {
    @Test
    public void given_validInput_when_sortSingleObjective_then_returnsExpectedValue() {
        Genome first = new Genome(false, new Fitness[] { new MaximizingFitness(3.0d) }, null);
        Genome second = new Genome(false, new Fitness[] { new MaximizingFitness(1.0d) }, null);
        Genome third = new Genome(false, new Fitness[] { new MaximizingFitness(2.0d) }, null);

        List<Genome> individuals = new ArrayList<>(Arrays.asList(first, second, third));

        ParetoSorter.sort(individuals);

        assertEquals(1.0d, individuals.get(0).getFitnesses()[0].getValue(), 0.000001d);
        assertEquals(2.0d, individuals.get(1).getFitnesses()[0].getValue(), 0.000001d);
        assertEquals(3.0d, individuals.get(2).getFitnesses()[0].getValue(), 0.000001d);
    }

    @Test
    public void given_validInput_when_sortMultiObjectiveBuckets_then_returnsTrue() {
        Genome dominated = new Genome(false, new Fitness[] { new MaximizingFitness(0.0d), new MaximizingFitness(0.0d) }, null);
        Genome first = new Genome(false, new Fitness[] { new MaximizingFitness(2.0d), new MaximizingFitness(1.0d) }, null);
        Genome second = new Genome(false, new Fitness[] { new MaximizingFitness(1.0d), new MaximizingFitness(2.0d) }, null);

        List<Genome> individuals = new ArrayList<>(Arrays.asList(dominated, first, second));

        ParetoSorter.sort(individuals);

        assertEquals(dominated, individuals.get(0));

        Set<Genome> topTwo = new HashSet<>(individuals.subList(1, 3));
        assertTrue(topTwo.contains(first));
        assertTrue(topTwo.contains(second));
    }
}
