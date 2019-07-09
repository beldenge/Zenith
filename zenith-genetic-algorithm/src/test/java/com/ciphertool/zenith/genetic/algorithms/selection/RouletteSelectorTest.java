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

package com.ciphertool.zenith.genetic.algorithms.selection;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.mocks.MockChromosome;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.math.selection.BinaryRouletteNode;
import com.ciphertool.zenith.math.selection.BinaryRouletteTree;
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
        BinaryRouletteTree binaryRouletteTree = new BinaryRouletteTree();
        binaryRouletteTree.insert(new BinaryRouletteNode(0, 7.0d));

        List<Chromosome> individuals = new ArrayList<>();

        MockChromosome chromosome1 = new MockChromosome();
        chromosome1.setFitness(0.2d);
        individuals.add(chromosome1);
        chromosome1.setPopulation(populationMock);

        Double bestFitness = 0.3d;
        MockChromosome chromosome2 = new MockChromosome();
        chromosome2.setFitness(bestFitness);
        individuals.add(chromosome2);
        chromosome2.setPopulation(populationMock);

        MockChromosome chromosome3 = new MockChromosome();
        chromosome3.setFitness(0.5d);
        individuals.add(chromosome3);
        chromosome3.setPopulation(populationMock);

        when(populationMock.getTotalFitness()).thenReturn(individuals.stream().mapToDouble(individual -> individual.getFitness()).sum());

        rouletteSelector.reIndex(individuals);

        int selectedIndex = rouletteSelector.getNextIndex(individuals);

        assertTrue(selectedIndex > -1);
    }
}
