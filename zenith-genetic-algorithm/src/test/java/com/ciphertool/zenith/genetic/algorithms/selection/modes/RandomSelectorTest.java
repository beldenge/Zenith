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

package com.ciphertool.zenith.genetic.algorithms.selection.modes;

 import com.ciphertool.zenith.genetic.entities.Chromosome;
 import com.ciphertool.zenith.genetic.mocks.MockKeyedChromosome;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.springframework.util.ReflectionUtils;

 import java.lang.reflect.Field;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;

 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.*;

public class RandomSelectorTest {
	private static RandomSelector	randomSelector;
	private static Logger			logMock;

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
		List<Chromosome> individuals = new ArrayList<>();

		MockKeyedChromosome chromosome1 = new MockKeyedChromosome();
		chromosome1.setFitness(2.0d);
		individuals.add(chromosome1);

		Double bestFitness = 3.0d;
		MockKeyedChromosome chromosome2 = new MockKeyedChromosome();
		chromosome2.setFitness(bestFitness);
		individuals.add(chromosome2);

		MockKeyedChromosome chromosome3 = new MockKeyedChromosome();
		chromosome3.setFitness(1.0d);
		individuals.add(chromosome3);

		int selectedIndex = randomSelector.getNextIndex(individuals, null);

		assertTrue(selectedIndex > -1);
		verifyZeroInteractions(logMock);
	}

	@Test
	public void testGetNextIndexWithNullPopulation() {
		int selectedIndex = randomSelector.getNextIndex(null, 6.0d);

		assertEquals(-1, selectedIndex);
		verify(logMock, times(1)).warn(anyString());
	}

	@Test
	public void testGetNextIndexWithEmptyPopulation() {
		int selectedIndex = randomSelector.getNextIndex(new ArrayList<>(), 6.0d);

		assertEquals(-1, selectedIndex);
		verify(logMock, times(1)).warn(anyString());
	}
}
