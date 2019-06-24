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

package com.ciphertool.zenith.genetic.statistics;

 import org.junit.Test;

import static org.junit.Assert.*;

 public class GenerationStatisticsTest {
	@Test
	public void testConstructor() {
		ExecutionStatistics executionStatisticsToSet = new ExecutionStatistics();
		int generationToSet = 1;

		GenerationStatistics generationStatistics = new GenerationStatistics(executionStatisticsToSet, generationToSet);

		assertSame(executionStatisticsToSet, generationStatistics.getExecutionStatistics());
		assertEquals(generationToSet, generationStatistics.getGeneration());
	}

	@Test
	public void testSetExecutionStatistics() {
		ExecutionStatistics executionStatisticsToSet = new ExecutionStatistics();
		GenerationStatistics generationStatistics = new GenerationStatistics();
		generationStatistics.setExecutionStatistics(executionStatisticsToSet);

		assertSame(executionStatisticsToSet, generationStatistics.getExecutionStatistics());
	}

	@Test
	public void testSetGeneration() {
		int generationToSet = 1;
		GenerationStatistics generationStatistics = new GenerationStatistics();
		generationStatistics.setGeneration(generationToSet);

		assertEquals(generationToSet, generationStatistics.getGeneration());
	}

	@Test
	public void testSetBestFitness() {
		Double bestFitnessToSet = 99.9d;
		GenerationStatistics generationStatistics = new GenerationStatistics();
		generationStatistics.setBestFitness(bestFitnessToSet);

		assertEquals(bestFitnessToSet, generationStatistics.getBestFitness());
	}

	@Test
	public void testSetAverageFitness() {
		Double averageFitnessToSet = 49.9d;
		GenerationStatistics generationStatistics = new GenerationStatistics();
		generationStatistics.setAverageFitness(averageFitnessToSet);

		assertEquals(averageFitnessToSet, generationStatistics.getAverageFitness());
	}

	@Test
	public void testSetKnownSolutionProximity() {
		Double knownSolutionProximityToSet = 9.9d;
		GenerationStatistics generationStatistics = new GenerationStatistics();
		generationStatistics.setKnownSolutionProximity(knownSolutionProximityToSet);

		assertSame(knownSolutionProximityToSet, generationStatistics.getKnownSolutionProximity());
	}

	@Test
	public void testSetNumberOfMutations() {
		int numberOfMutationsToSet = 5;
		GenerationStatistics generationStatistics = new GenerationStatistics();
		generationStatistics.setNumberOfMutations(numberOfMutationsToSet);

		assertEquals(numberOfMutationsToSet, generationStatistics.getNumberOfMutations());
	}

	@Test
	public void testSetNumberOfCrossovers() {
		int numberOfCrossoversToSet = 10;
		GenerationStatistics generationStatistics = new GenerationStatistics();
		generationStatistics.setNumberOfCrossovers(numberOfCrossoversToSet);

		assertEquals(numberOfCrossoversToSet, generationStatistics.getNumberOfCrossovers());
	}

	@Test
	public void testSetNumberRandomlyGenerated() {
		int numberRandomlyGeneratedToSet = 15;
		GenerationStatistics generationStatistics = new GenerationStatistics();
		generationStatistics.setNumberRandomlyGenerated(numberRandomlyGeneratedToSet);

		assertEquals(numberRandomlyGeneratedToSet, generationStatistics.getNumberRandomlyGenerated());
	}

	@Test
	public void testSetNumberSelectedOut() {
		int numberSelectedOutToSet = 20;
		GenerationStatistics generationStatistics = new GenerationStatistics();
		generationStatistics.setNumberSelectedOut(numberSelectedOutToSet);

		assertEquals(numberSelectedOutToSet, generationStatistics.getNumberSelectedOut());
	}

	@Test
	public void testEquals() {
		ExecutionStatistics baseExecutionStatistics = new ExecutionStatistics();
		baseExecutionStatistics.setMutationRate(1.0);

		int baseGeneration = 1;
		Double baseBestFitness = 99.9d;
		Double baseAverageFitness = 49.9d;
		Double baseKnownSolutionProximity = 9.9d;

		GenerationStatistics base = new GenerationStatistics();
		base.setExecutionStatistics(baseExecutionStatistics);
		base.setGeneration(baseGeneration);
		base.setBestFitness(baseBestFitness);
		base.setAverageFitness(baseAverageFitness);
		base.setKnownSolutionProximity(baseKnownSolutionProximity);

		GenerationStatistics generationStatisticsEqualToBase = new GenerationStatistics();
		generationStatisticsEqualToBase.setExecutionStatistics(baseExecutionStatistics);
		generationStatisticsEqualToBase.setGeneration(baseGeneration);
		generationStatisticsEqualToBase.setBestFitness(baseBestFitness);
		generationStatisticsEqualToBase.setAverageFitness(baseAverageFitness);
		generationStatisticsEqualToBase.setKnownSolutionProximity(baseKnownSolutionProximity);
		assertEquals(base, generationStatisticsEqualToBase);

		GenerationStatistics generationStatisticsWithDifferentExecutionStatistics = new GenerationStatistics();
		ExecutionStatistics differentExecutionStatistics = new ExecutionStatistics();
		differentExecutionStatistics.setMutationRate(0.0);
		generationStatisticsWithDifferentExecutionStatistics.setExecutionStatistics(differentExecutionStatistics);
		generationStatisticsWithDifferentExecutionStatistics.setGeneration(baseGeneration);
		generationStatisticsWithDifferentExecutionStatistics.setBestFitness(baseBestFitness);
		generationStatisticsWithDifferentExecutionStatistics.setAverageFitness(baseAverageFitness);
		generationStatisticsWithDifferentExecutionStatistics.setKnownSolutionProximity(baseKnownSolutionProximity);
		assertFalse(base.equals(generationStatisticsWithDifferentExecutionStatistics));

		GenerationStatistics generationStatisticsWithDifferentGeneration = new GenerationStatistics();
		generationStatisticsWithDifferentGeneration.setExecutionStatistics(baseExecutionStatistics);
		generationStatisticsWithDifferentGeneration.setGeneration(2);
		generationStatisticsWithDifferentGeneration.setBestFitness(baseBestFitness);
		generationStatisticsWithDifferentGeneration.setAverageFitness(baseAverageFitness);
		generationStatisticsWithDifferentGeneration.setKnownSolutionProximity(baseKnownSolutionProximity);
		assertFalse(base.equals(generationStatisticsWithDifferentGeneration));

		GenerationStatistics generationStatisticsWithDifferentBestFitness = new GenerationStatistics();
		generationStatisticsWithDifferentBestFitness.setExecutionStatistics(baseExecutionStatistics);
		generationStatisticsWithDifferentBestFitness.setGeneration(baseGeneration);
		generationStatisticsWithDifferentBestFitness.setBestFitness(199.9d);
		generationStatisticsWithDifferentBestFitness.setAverageFitness(baseAverageFitness);
		generationStatisticsWithDifferentBestFitness.setKnownSolutionProximity(baseKnownSolutionProximity);
		assertFalse(base.equals(generationStatisticsWithDifferentBestFitness));

		GenerationStatistics generationStatisticsWithDifferentAverageFitness = new GenerationStatistics();
		generationStatisticsWithDifferentAverageFitness.setExecutionStatistics(baseExecutionStatistics);
		generationStatisticsWithDifferentAverageFitness.setGeneration(baseGeneration);
		generationStatisticsWithDifferentAverageFitness.setBestFitness(baseBestFitness);
		generationStatisticsWithDifferentAverageFitness.setAverageFitness(149.9d);
		generationStatisticsWithDifferentAverageFitness.setKnownSolutionProximity(baseKnownSolutionProximity);
		assertFalse(base.equals(generationStatisticsWithDifferentAverageFitness));

		GenerationStatistics generationStatisticsWithDifferentKnownSolutionProximity = new GenerationStatistics();
		generationStatisticsWithDifferentKnownSolutionProximity.setExecutionStatistics(baseExecutionStatistics);
		generationStatisticsWithDifferentKnownSolutionProximity.setGeneration(baseGeneration);
		generationStatisticsWithDifferentKnownSolutionProximity.setBestFitness(baseBestFitness);
		generationStatisticsWithDifferentKnownSolutionProximity.setAverageFitness(baseAverageFitness);
		generationStatisticsWithDifferentKnownSolutionProximity.setKnownSolutionProximity(109.9d);
		assertFalse(base.equals(generationStatisticsWithDifferentKnownSolutionProximity));

		GenerationStatistics generationStatisticsWithNullPropertiesA = new GenerationStatistics();
		GenerationStatistics generationStatisticsWithNullPropertiesB = new GenerationStatistics();
		assertEquals(generationStatisticsWithNullPropertiesA, generationStatisticsWithNullPropertiesB);
	}
}
