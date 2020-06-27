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

package com.ciphertool.zenith.genetic.population;

import com.ciphertool.zenith.genetic.Breeder;
import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.mocks.MockChromosome;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
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
    public void testGeneratorTask() {
        StandardPopulation population = new StandardPopulation();
        StandardPopulation.GeneratorTask generatorTask = population.new GeneratorTask();

        MockChromosome chromosomeToReturn = new MockChromosome();
        Breeder breederMock = mock(Breeder.class);
        when(breederMock.breed()).thenReturn(chromosomeToReturn);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .breeder(breederMock)
                .build();

        population.init(strategy);

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

        int expectedPopulationSize = 10;

        Breeder breederMock = mock(Breeder.class);
        MockChromosome mockChromosome = new MockChromosome();
        mockChromosome.setFitness(5.0d);
        when(breederMock.breed()).thenReturn(mockChromosome.clone());

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .breeder(breederMock)
                .build();

        population.init(strategy);

        assertEquals(0, population.size());
        assertEquals(Double.valueOf(0d), population.getTotalFitness());

        List<Chromosome> individuals = population.breed(expectedPopulationSize);

        assertEquals(expectedPopulationSize, individuals.size());
        individuals.stream().forEach(population::addIndividual);
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

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .fitnessEvaluator(mockEvaluator)
                .build();

        population.init(strategy);

        Void fitnessReturned = null;
        try {
            fitnessReturned = evaluationTask.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNull(fitnessReturned);
    }

    @Test
    public void testDoConcurrentFitnessEvaluations() {
        StandardPopulation population = new StandardPopulation();

        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(fitnessEvaluatorMock.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .fitnessEvaluator(fitnessEvaluatorMock)
                .build();

        population.init(strategy);

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

        population.doConcurrentFitnessEvaluations(fitnessEvaluatorMock, population.getIndividuals());

        for (Chromosome individual : population.getIndividuals()) {
            assertFalse(individual.isEvaluationNeeded());
        }

        // Only two of the individuals needed to be evaluated
        verify(fitnessEvaluatorMock, times(2)).evaluate(any(Chromosome.class));
    }

    @Ignore
    @Test
    public void testEvaluateFitness() {
        GenerationStatistics generationStatistics = new GenerationStatistics();

        StandardPopulation population = new StandardPopulation();

        Field taskExecutorField = ReflectionUtils.findField(StandardPopulation.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, population, taskExecutor);

        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(fitnessEvaluatorMock.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .fitnessEvaluator(fitnessEvaluatorMock)
                .build();

        population.init(strategy);

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

    @Ignore
    @Test
    public void testEvaluateFitnessCompareToKnownSolution() {
        GenerationStatistics generationStatistics = new GenerationStatistics();

        StandardPopulation population = new StandardPopulation();

        Field taskExecutorField = ReflectionUtils.findField(StandardPopulation.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, population, taskExecutor);

        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(fitnessEvaluatorMock.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .fitnessEvaluator(fitnessEvaluatorMock)
                .build();

        population.init(strategy);

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
