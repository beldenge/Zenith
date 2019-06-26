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

package com.ciphertool.zenith.genetic.population;

import com.ciphertool.zenith.genetic.Breeder;
import com.ciphertool.zenith.genetic.algorithms.selection.Selector;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.mocks.MockBreeder;
import com.ciphertool.zenith.genetic.mocks.MockChromosome;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

public class StandardPopulationTest {
    private static ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    private static final Double DEFAULT_FITNESS_VALUE = 1.0d;

    @BeforeClass
    public static void setUp() {
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setKeepAliveSeconds(1);
        taskExecutor.setAllowCoreThreadTimeOut(true);
        taskExecutor.initialize();
    }

    @Test
    public void testSetBreeder() {
        StandardPopulation population = new StandardPopulation();

        MockBreeder mockBreeder = new MockBreeder();
        population.setBreeder(mockBreeder);

        Field breederField = ReflectionUtils.findField(StandardPopulation.class, "breeder");
        ReflectionUtils.makeAccessible(breederField);
        MockBreeder breederFromObject = (MockBreeder) ReflectionUtils.getField(breederField, population);

        assertSame(mockBreeder, breederFromObject);
    }

    @Test
    public void testSetFitnessEvaluator() {
        StandardPopulation population = new StandardPopulation();

        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(fitnessEvaluatorMock.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
        population.setFitnessEvaluator(fitnessEvaluatorMock);

        Field fitnessEvaluatorField = ReflectionUtils.findField(StandardPopulation.class, "fitnessEvaluator");
        ReflectionUtils.makeAccessible(fitnessEvaluatorField);
        FitnessEvaluator fitnessEvaluatorFromObject = (FitnessEvaluator) ReflectionUtils.getField(fitnessEvaluatorField, population);

        assertSame(fitnessEvaluatorMock, fitnessEvaluatorFromObject);
    }

    @Test
    public void testSetSelector() {
        StandardPopulation population = new StandardPopulation();

        Selector selector = mock(Selector.class);
        population.setSelector(selector);

        Field selectorField = ReflectionUtils.findField(StandardPopulation.class, "selector");
        ReflectionUtils.makeAccessible(selectorField);
        Selector selectorFromObject = (Selector) ReflectionUtils.getField(selectorField, population);

        assertSame(selector, selectorFromObject);
    }

    @Test
    public void testSetKnownSolutionFitnessEvaluator() {
        StandardPopulation population = new StandardPopulation();

        FitnessEvaluator knownSolutionFitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(knownSolutionFitnessEvaluatorMock.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
        population.setKnownSolutionFitnessEvaluator(knownSolutionFitnessEvaluatorMock);

        Field knownSolutionFitnessEvaluatorField = ReflectionUtils.findField(StandardPopulation.class, "knownSolutionFitnessEvaluator");
        ReflectionUtils.makeAccessible(knownSolutionFitnessEvaluatorField);
        FitnessEvaluator knownSolutionFitnessEvaluatorFromObject = (FitnessEvaluator) ReflectionUtils.getField(knownSolutionFitnessEvaluatorField, population);

        assertSame(knownSolutionFitnessEvaluatorMock, knownSolutionFitnessEvaluatorFromObject);
    }

    @Test
    public void testSetCompareToKnownSolution() {
        StandardPopulation population = new StandardPopulation();

        Boolean compareToKnownSolution = true;
        population.setCompareToKnownSolution(compareToKnownSolution);

        Field compareToKnownSolutionField = ReflectionUtils.findField(StandardPopulation.class, "compareToKnownSolution");
        ReflectionUtils.makeAccessible(compareToKnownSolutionField);
        Boolean compareToKnownSolutionFromObject = (Boolean) ReflectionUtils.getField(compareToKnownSolutionField, population);

        assertSame(compareToKnownSolution, compareToKnownSolutionFromObject);
    }

    @Test
    public void testSetCompareToKnownSolutionDefault() {
        StandardPopulation population = new StandardPopulation();

        Field compareToKnownSolutionField = ReflectionUtils.findField(StandardPopulation.class, "compareToKnownSolution");
        ReflectionUtils.makeAccessible(compareToKnownSolutionField);
        Boolean compareToKnownSolutionFromObject = (Boolean) ReflectionUtils.getField(compareToKnownSolutionField, population);

        assertEquals(false, compareToKnownSolutionFromObject);
    }

    @Test
    public void testGeneratorTask() {
        StandardPopulation population = new StandardPopulation();
        StandardPopulation.GeneratorTask generatorTask = population.new GeneratorTask();

        MockChromosome chromosomeToReturn = new MockChromosome();
        Breeder mockBreeder = mock(Breeder.class);
        when(mockBreeder.breed()).thenReturn(chromosomeToReturn);
        population.setBreeder(mockBreeder);

        Chromosome chromosomeReturned = null;
        try {
            chromosomeReturned = generatorTask.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertSame(chromosomeToReturn, chromosomeReturned);
    }

    @Test
    public void testBreed() {
        StandardPopulation population = new StandardPopulation();

        Field taskExecutorField = ReflectionUtils.findField(StandardPopulation.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, population, taskExecutor);

        int expectedPopulationSize = 10;
        population.setTargetSize(expectedPopulationSize);

        Breeder breederMock = mock(Breeder.class);
        MockChromosome mockChromosome = new MockChromosome();
        mockChromosome.setFitness(5.0d);
        when(breederMock.breed()).thenReturn(mockChromosome.clone());
        population.setBreeder(breederMock);

        assertEquals(0, population.size());
        assertEquals(Double.valueOf(0d), population.getTotalFitness());

        population.breed();

        assertEquals(expectedPopulationSize, population.size());
        assertEquals(Double.valueOf(50.0d), population.getTotalFitness());
    }

    @Test
    public void testEvaluatorTask() {
        StandardPopulation population = new StandardPopulation();
        MockChromosome chromosomeToEvaluate = new MockChromosome();

        FitnessEvaluator mockEvaluator = mock(FitnessEvaluator.class);
        Double fitnessToReturn = 101.0d;
        when(mockEvaluator.evaluate(same(chromosomeToEvaluate))).thenReturn(fitnessToReturn);

        StandardPopulation.EvaluationTask evaluationTask = population.new EvaluationTask(chromosomeToEvaluate,
                mock(FitnessEvaluator.class));
        population.setFitnessEvaluator(mockEvaluator);

        Void fitnessReturned = null;
        try {
            fitnessReturned = evaluationTask.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNull(fitnessReturned);
    }

    @Test
    public void testDoConcurrentFitnessEvaluations() throws InterruptedException {
        StandardPopulation population = new StandardPopulation();

        Field taskExecutorField = ReflectionUtils.findField(StandardPopulation.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, population, taskExecutor);

        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(fitnessEvaluatorMock.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
        population.setFitnessEvaluator(fitnessEvaluatorMock);

        MockChromosome chromosomeEvaluationNeeded1 = new MockChromosome();
        chromosomeEvaluationNeeded1.setFitness(1.0d);
        population.addIndividual(chromosomeEvaluationNeeded1);
        chromosomeEvaluationNeeded1.setEvaluationNeeded(true);

        MockChromosome chromosomeEvaluationNeeded2 = new MockChromosome();
        chromosomeEvaluationNeeded2.setFitness(1.0d);
        population.addIndividual(chromosomeEvaluationNeeded2);
        chromosomeEvaluationNeeded2.setEvaluationNeeded(true);

        MockChromosome chromosomeEvaluationNotNeeded1 = new MockChromosome();
        chromosomeEvaluationNotNeeded1.setFitness(1.0d);
        population.addIndividual(chromosomeEvaluationNotNeeded1);

        MockChromosome chromosomeEvaluationNotNeeded2 = new MockChromosome();
        chromosomeEvaluationNotNeeded2.setFitness(1.0d);
        population.addIndividual(chromosomeEvaluationNotNeeded2);

        assertTrue(chromosomeEvaluationNeeded1.isEvaluationNeeded());
        assertTrue(chromosomeEvaluationNeeded2.isEvaluationNeeded());
        assertFalse(chromosomeEvaluationNotNeeded1.isEvaluationNeeded());
        assertFalse(chromosomeEvaluationNotNeeded2.isEvaluationNeeded());

        population.doConcurrentFitnessEvaluations(fitnessEvaluatorMock);

        for (Chromosome individual : population.getIndividuals()) {
            assertFalse(individual.isEvaluationNeeded());
        }

        // Only two of the individuals needed to be evaluated
        verify(fitnessEvaluatorMock, times(2)).evaluate(any(Chromosome.class));
    }

    @Test
    public void testEvaluateFitness() throws InterruptedException {
        GenerationStatistics generationStatistics = new GenerationStatistics();

        StandardPopulation population = new StandardPopulation();

        Field taskExecutorField = ReflectionUtils.findField(StandardPopulation.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, population, taskExecutor);

        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(fitnessEvaluatorMock.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
        population.setFitnessEvaluator(fitnessEvaluatorMock);

        MockChromosome chromosomeEvaluationNeeded1 = new MockChromosome();
        chromosomeEvaluationNeeded1.setFitness(5.0d);
        population.addIndividual(chromosomeEvaluationNeeded1);
        chromosomeEvaluationNeeded1.setEvaluationNeeded(true);

        MockChromosome chromosomeEvaluationNeeded2 = new MockChromosome();
        chromosomeEvaluationNeeded2.setFitness(5.0d);
        population.addIndividual(chromosomeEvaluationNeeded2);
        chromosomeEvaluationNeeded2.setEvaluationNeeded(true);

        MockChromosome chromosomeEvaluationNotNeeded1 = new MockChromosome();
        chromosomeEvaluationNotNeeded1.setFitness(5.0d);
        population.addIndividual(chromosomeEvaluationNotNeeded1);

        MockChromosome chromosomeEvaluationNotNeeded2 = new MockChromosome();
        chromosomeEvaluationNotNeeded2.setFitness(100.1d);
        population.addIndividual(chromosomeEvaluationNotNeeded2);

        assertTrue(chromosomeEvaluationNeeded1.isEvaluationNeeded());
        assertTrue(chromosomeEvaluationNeeded2.isEvaluationNeeded());
        assertFalse(chromosomeEvaluationNotNeeded1.isEvaluationNeeded());
        assertFalse(chromosomeEvaluationNotNeeded2.isEvaluationNeeded());

        population.evaluateFitness(generationStatistics);

        for (Chromosome individual : population.getIndividuals()) {
            assertFalse(individual.isEvaluationNeeded());
        }

        // Only two of the individuals needed to be evaluated
        verify(fitnessEvaluatorMock, times(2)).evaluate(any(Chromosome.class));

        /*
         * The fitnessEvaluatorMock always returns 1.0, so the total is (1.0 x 2) + 5.0 + 100.1, since two individuals
         * are re-evaluated
         */
        Double expectedTotalFitness = 107.1d;

        assertEquals(expectedTotalFitness, population.getTotalFitness());
        assertEquals(Double.valueOf(expectedTotalFitness / population.size()), generationStatistics.getAverageFitness());
        assertEquals(Double.valueOf(100.1d), generationStatistics.getBestFitness());
    }

    @Test
    public void testEvaluateFitnessCompareToKnownSolution() throws InterruptedException {
        GenerationStatistics generationStatistics = new GenerationStatistics();

        StandardPopulation population = new StandardPopulation();

        Field taskExecutorField = ReflectionUtils.findField(StandardPopulation.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, population, taskExecutor);

        population.setCompareToKnownSolution(true);

        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(fitnessEvaluatorMock.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
        population.setFitnessEvaluator(fitnessEvaluatorMock);

        FitnessEvaluator knownSolutionFitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(knownSolutionFitnessEvaluatorMock.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
        population.setKnownSolutionFitnessEvaluator(knownSolutionFitnessEvaluatorMock);

        MockChromosome chromosomeEvaluationNeeded1 = new MockChromosome();
        chromosomeEvaluationNeeded1.setFitness(5.0d);
        population.addIndividual(chromosomeEvaluationNeeded1);
        chromosomeEvaluationNeeded1.setEvaluationNeeded(true);

        MockChromosome chromosomeEvaluationNeeded2 = new MockChromosome();
        chromosomeEvaluationNeeded2.setFitness(5.0d);
        population.addIndividual(chromosomeEvaluationNeeded2);
        chromosomeEvaluationNeeded2.setEvaluationNeeded(true);

        MockChromosome chromosomeEvaluationNotNeeded1 = new MockChromosome();
        chromosomeEvaluationNotNeeded1.setFitness(5.0d);
        population.addIndividual(chromosomeEvaluationNotNeeded1);

        MockChromosome chromosomeEvaluationNotNeeded2 = new MockChromosome();
        chromosomeEvaluationNotNeeded2.setFitness(100.1d);
        population.addIndividual(chromosomeEvaluationNotNeeded2);

        assertTrue(chromosomeEvaluationNeeded1.isEvaluationNeeded());
        assertTrue(chromosomeEvaluationNeeded2.isEvaluationNeeded());
        assertFalse(chromosomeEvaluationNotNeeded1.isEvaluationNeeded());
        assertFalse(chromosomeEvaluationNotNeeded2.isEvaluationNeeded());

        population.evaluateFitness(generationStatistics);

        for (Chromosome individual : population.getIndividuals()) {
            assertFalse(individual.isEvaluationNeeded());
        }

        // Only two of the individuals needed to be evaluated
        verify(fitnessEvaluatorMock, times(2)).evaluate(any(Chromosome.class));

        /*
         * The fitnessEvaluatorMock always returns 1.0, so the total is (1.0 x 2) + 5.0 + 100.1, since two individuals
         * are re-evaluated
         */
        Double expectedTotalFitness = 107.1d;

        assertEquals(expectedTotalFitness, population.getTotalFitness());
        assertEquals(Double.valueOf(expectedTotalFitness / population.size()), generationStatistics.getAverageFitness());
        assertEquals(Double.valueOf(100.1d), generationStatistics.getBestFitness());
        assertEquals(Double.valueOf(100.0d), generationStatistics.getKnownSolutionProximity());
    }

    @Test
    public void testSelectIndex() {
        StandardPopulation population = new StandardPopulation();

        int indexToReturn = 7;

        Selector selector = mock(Selector.class);
        when(selector.getNextIndex(anyListOf(Chromosome.class), any(Double.class))).thenReturn(indexToReturn);
        population.setSelector(selector);

        assertEquals(indexToReturn, population.selectIndex());
        verify(selector, times(1)).getNextIndex(anyListOf(Chromosome.class), any(Double.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIndividualsUnmodifiable() {
        StandardPopulation population = new StandardPopulation();
        population.addIndividual(mock(Chromosome.class));
        population.addIndividual(mock(Chromosome.class));
        population.addIndividual(mock(Chromosome.class));

        List<Chromosome> individuals = population.getIndividuals();
        individuals.remove(0); // should throw exception
    }

    @Test
    public void getNullIndividuals() {
        StandardPopulation population = new StandardPopulation();
        assertNotNull(population.getIndividuals());
    }

    @Test
    public void testAddIndividual() {
        StandardPopulation population = new StandardPopulation();

        Field taskExecutorField = ReflectionUtils.findField(StandardPopulation.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, population, taskExecutor);

        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(fitnessEvaluatorMock.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
        population.setFitnessEvaluator(fitnessEvaluatorMock);

        Double fitnessSum = 0d;
        assertEquals(fitnessSum, population.getTotalFitness());
        assertEquals(0, population.size());

        // Add a chromosome that needs evaluation
        MockChromosome chromosomeEvaluationNeeded = new MockChromosome();
        chromosomeEvaluationNeeded.setFitness(5.0d);
        chromosomeEvaluationNeeded.setEvaluationNeeded(true);
        population.addIndividual(chromosomeEvaluationNeeded);

        // Validate
        fitnessSum += chromosomeEvaluationNeeded.getFitness();
        assertEquals(fitnessSum, population.getTotalFitness());
        assertEquals(1, population.size());
        assertSame(chromosomeEvaluationNeeded, population.getIndividuals().get(0));

        // Add a chromosome that doesn't need evaluation
        MockChromosome chromosomeEvaluationNotNeeded = new MockChromosome();
        chromosomeEvaluationNotNeeded.setFitness(5.0d);
        population.addIndividual(chromosomeEvaluationNotNeeded);

        // Validate
        fitnessSum += chromosomeEvaluationNotNeeded.getFitness();
        assertEquals(fitnessSum, population.getTotalFitness());
        verifyNoMoreInteractions(fitnessEvaluatorMock);
        assertEquals(2, population.size());
        assertSame(chromosomeEvaluationNotNeeded, population.getIndividuals().get(1));
    }

    @Test
    public void testRemoveIndividual() {
        StandardPopulation population = new StandardPopulation();

        MockChromosome chromosome1 = new MockChromosome();
        chromosome1.setFitness(5.0d);
        population.addIndividual(chromosome1);
        chromosome1.setEvaluationNeeded(true);

        MockChromosome chromosome2 = new MockChromosome();
        chromosome2.setFitness(5.0d);
        population.addIndividual(chromosome2);

        Double fitnessSum = 10.0d;
        assertEquals(fitnessSum, population.getTotalFitness());
        assertEquals(2, population.size());

        fitnessSum -= population.removeIndividual(1).getFitness();
        assertEquals(fitnessSum, population.getTotalFitness());
        assertEquals(1, population.size());
        assertSame(chromosome1, population.getIndividuals().get(0));

        fitnessSum -= population.removeIndividual(0).getFitness();
        assertEquals(fitnessSum, population.getTotalFitness());
        assertEquals(0, population.size());

        // Try to remove an individual that doesn't exist
        assertNull(population.removeIndividual(0));
    }

    @Test
    public void testClearIndividuals() {
        StandardPopulation population = new StandardPopulation();

        MockChromosome chromosome1 = new MockChromosome();
        chromosome1.setFitness(5.0d);
        population.addIndividual(chromosome1);
        chromosome1.setEvaluationNeeded(true);

        MockChromosome chromosome2 = new MockChromosome();
        chromosome2.setFitness(5.0d);
        population.addIndividual(chromosome2);

        assertEquals(Double.valueOf(10.0d), population.getTotalFitness());
        assertEquals(2, population.size());

        population.clearIndividuals();

        assertEquals(Double.valueOf(0d), population.getTotalFitness());
        assertEquals(0, population.size());
    }

    @Test
    public void testSize() {
        StandardPopulation population = new StandardPopulation();

        Field taskExecutorField = ReflectionUtils.findField(StandardPopulation.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, population, taskExecutor);

        // This is needed to avoid a NullPointerException on fitnessEvaluator
        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(fitnessEvaluatorMock.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
        population.setFitnessEvaluator(fitnessEvaluatorMock);

        assertEquals(0, population.size());

        population.addIndividual(new MockChromosome());

        assertEquals(1, population.size());

        population.addIndividual(new MockChromosome());

        assertEquals(2, population.size());
    }

    @Test
    public void testSortIndividuals() {
        StandardPopulation population = new StandardPopulation();

        MockChromosome chromosome1 = new MockChromosome();
        chromosome1.setFitness(3.0d);
        population.addIndividual(chromosome1);

        MockChromosome chromosome2 = new MockChromosome();
        chromosome2.setFitness(2.0d);
        population.addIndividual(chromosome2);

        MockChromosome chromosome3 = new MockChromosome();
        chromosome3.setFitness(1.0d);
        population.addIndividual(chromosome3);

        assertSame(chromosome1, population.getIndividuals().get(0));
        assertSame(chromosome2, population.getIndividuals().get(1));
        assertSame(chromosome3, population.getIndividuals().get(2));

        population.sortIndividuals();

        assertSame(chromosome3, population.getIndividuals().get(0));
        assertSame(chromosome2, population.getIndividuals().get(1));
        assertSame(chromosome1, population.getIndividuals().get(2));
    }
}
