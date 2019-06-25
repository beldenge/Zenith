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
}
