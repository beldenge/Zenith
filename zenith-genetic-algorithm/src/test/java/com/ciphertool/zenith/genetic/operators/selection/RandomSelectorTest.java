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
import com.ciphertool.zenith.genetic.population.Population;
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

public class RandomSelectorTest {
    private static RandomSelector randomSelector;
    private static Logger logMock;

    @BeforeClass
    public static void setUp() {
        randomSelector = new RandomSelector();

        logMock = mock(Logger.class);
        Field logField = ReflectionUtils.findField(RandomSelector.class, "log");
        ReflectionUtils.makeAccessible(logField);
        ReflectionUtils.setField(logField, randomSelector, logMock);
    }

    @Before
    public void resetMocks() {
        reset(logMock);
    }

    @Test
    public void testGetNextIndex() {
        Population populationMock = mock(Population.class);
        List<Genome> individuals = new ArrayList<>();

        Genome genome1 = new Genome(true, 0.2d, populationMock);
        individuals.add(genome1);

        Genome genome2 = new Genome(true, 0.3d, populationMock);
        individuals.add(genome2);

        Genome genome3 = new Genome(true, 0.5d, populationMock);
        individuals.add(genome3);

        int selectedIndex = randomSelector.getNextIndex(individuals, null);

        assertTrue(selectedIndex > -1);
        verifyNoInteractions(logMock);
    }

    @Test
    public void testGetNextIndexWithNullPopulation() {
        int selectedIndex = randomSelector.getNextIndex(null, null);

        assertEquals(-1, selectedIndex);
        verify(logMock, times(1)).warn(anyString());
    }

    @Test
    public void testGetNextIndexWithEmptyPopulation() {
        int selectedIndex = randomSelector.getNextIndex(new ArrayList<>(), null);

        assertEquals(-1, selectedIndex);
        verify(logMock, times(1)).warn(anyString());
    }
}
