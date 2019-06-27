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

public class TournamentSelectorTest {
    private static TournamentSelector tournamentSelector;
    private static Logger logMock;

    @BeforeClass
    public static void setUp() {
        tournamentSelector = new TournamentSelector();

        Field selectionAccuracyField = ReflectionUtils.findField(TournamentSelector.class, "selectionAccuracy");
        ReflectionUtils.makeAccessible(selectionAccuracyField);
        ReflectionUtils.setField(selectionAccuracyField, tournamentSelector, 0.9);

        logMock = mock(Logger.class);
        Field logField = ReflectionUtils.findField(TournamentSelector.class, "log");
        ReflectionUtils.makeAccessible(logField);
        ReflectionUtils.setField(logField, tournamentSelector, logMock);
    }

    @Before
    public void resetMocks() {
        reset(logMock);
    }

    @Test
    public void testGetNextIndex() {
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

        int selectedIndex = tournamentSelector.getNextIndex(individuals, 6.0d);

        assertTrue(selectedIndex > -1);
        verifyZeroInteractions(logMock);
    }

    @Test
    public void testGetNextIndexWithNullPopulation() {
        int selectedIndex = tournamentSelector.getNextIndex(null, 6.0d);

        assertEquals(-1, selectedIndex);
        verify(logMock, times(1)).warn(anyString());
    }

    @Test
    public void testGetNextIndexWithEmptyPopulation() {
        int selectedIndex = tournamentSelector.getNextIndex(new ArrayList<>(), 6.0d);

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

        int selectedIndex = tournamentSelector.getNextIndex(individuals, null);

        assertTrue(selectedIndex > -1);
        verifyZeroInteractions(logMock);
    }
}
