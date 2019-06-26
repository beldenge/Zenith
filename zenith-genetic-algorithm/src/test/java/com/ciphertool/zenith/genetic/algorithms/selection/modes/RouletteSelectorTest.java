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

package com.ciphertool.zenith.genetic.algorithms.selection.modes;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.mocks.MockChromosome;
import com.ciphertool.zenith.math.selection.BinaryRouletteNode;
import com.ciphertool.zenith.math.selection.BinaryRouletteTree;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RouletteSelectorTest {
    private static RouletteSelector rouletteSelector;
    private static Logger logMock;

    @BeforeClass
    public static void setUp() {
        rouletteSelector = new RouletteSelector();

        logMock = mock(Logger.class);
        Field logField = ReflectionUtils.findField(RouletteSelector.class, "log");
        ReflectionUtils.makeAccessible(logField);
        ReflectionUtils.setField(logField, rouletteSelector, logMock);
    }

    @Before
    public void resetMocks() {
        reset(logMock);
    }

    @Test
    public void testGetNextIndex() {
        BinaryRouletteTree binaryRouletteTree = new BinaryRouletteTree();
        binaryRouletteTree.insert(new BinaryRouletteNode(0, 7.0d));

        Field rouletteWheelField = ReflectionUtils.findField(RouletteSelector.class, "rouletteWheel");
        ReflectionUtils.makeAccessible(rouletteWheelField);
        ReflectionUtils.setField(rouletteWheelField, rouletteSelector, binaryRouletteTree);

        List<Chromosome> individuals = new ArrayList<>();

        MockChromosome chromosome1 = new MockChromosome();
        chromosome1.setFitness(2.0d);
        individuals.add(chromosome1);

        Double bestFitness = 3.0d;
        MockChromosome chromosome2 = new MockChromosome();
        chromosome2.setFitness(bestFitness);
        individuals.add(chromosome2);

        MockChromosome chromosome3 = new MockChromosome();
        chromosome3.setFitness(1.0d);
        individuals.add(chromosome3);

        int selectedIndex = rouletteSelector.getNextIndex(individuals, (6.0));

        assertTrue(selectedIndex > -1);
        verifyZeroInteractions(logMock);
    }

    @Test
    public void testGetNextIndexWithNullPopulation() {
        int selectedIndex = rouletteSelector.getNextIndex(null, 6.0d);

        assertEquals(-1, selectedIndex);
        verify(logMock, times(1)).warn(anyString());
    }

    @Test
    public void testGetNextIndexWithEmptyPopulation() {
        int selectedIndex = rouletteSelector.getNextIndex(new ArrayList<>(), 6.0d);

        assertEquals(-1, selectedIndex);
        verify(logMock, times(1)).warn(anyString());
    }

    @Test
    public void testGetNextIndexWithNullTotalFitness() {
        List<Chromosome> individuals = new ArrayList<>();

        MockChromosome chromosome1 = new MockChromosome();
        chromosome1.setFitness(2.0d);
        individuals.add(chromosome1);

        Double bestFitness = 3.0d;
        MockChromosome chromosome2 = new MockChromosome();
        chromosome2.setFitness(bestFitness);
        individuals.add(chromosome2);

        MockChromosome chromosome3 = new MockChromosome();
        chromosome3.setFitness(1.0d);
        individuals.add(chromosome3);

        int selectedIndex = rouletteSelector.getNextIndex(individuals, null);

        assertEquals(-1, selectedIndex);
        verify(logMock, times(1)).warn(anyString());
    }
}
