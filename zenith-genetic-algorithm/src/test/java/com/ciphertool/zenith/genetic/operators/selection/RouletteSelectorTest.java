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

package com.ciphertool.zenith.genetic.operators.selection;

import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import com.ciphertool.zenith.genetic.population.Population;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RouletteSelectorTest {
    private static RouletteSelector rouletteSelector;

    @BeforeClass
    public static void setUp() {
        rouletteSelector = new RouletteSelector();
    }

    @Test
    public void testGetNextIndex() {
        Population populationMock = mock(Population.class);

        List<Genome> individuals = new ArrayList<>();

        Genome genome1 = new Genome(false, new Fitness[] { new MaximizingFitness(0.2d) }, populationMock);
        individuals.add(genome1);

        Genome genome2 = new Genome(false, new Fitness[] { new MaximizingFitness(0.3d) }, populationMock);
        individuals.add(genome2);

        Genome genome3 = new Genome(false, new Fitness[] { new MaximizingFitness(0.5d) }, populationMock);
        individuals.add(genome3);

        when(populationMock.getTotalProbability()).thenReturn(individuals.stream().mapToDouble(individual -> individual.getFitnesses()[0].getValue()).sum());

        rouletteSelector.reIndex(individuals);

        int selectedIndex = rouletteSelector.getNextIndex(individuals, null);

        assertTrue(selectedIndex > -1);
    }
}
