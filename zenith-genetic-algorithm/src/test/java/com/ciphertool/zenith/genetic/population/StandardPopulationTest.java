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
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

public class StandardPopulationTest {
    private static ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    private static final Double DEFAULT_FITNESS_VALUE = 1.0d;

    @BeforeAll
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

        Genome genomeToReturn = new Genome(true, new Fitness[] { new MaximizingFitness(0d) }, population);
        Breeder breederMock = mock(Breeder.class);
        when(breederMock.breed(same(population))).thenReturn(genomeToReturn);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .breeder(breederMock)
                .population(population)
                .build();

        population.init(strategy);

        Genome genomeReturned = null;
        try {
            genomeReturned = generatorTask.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertSame(genomeToReturn, genomeReturned);
    }

    @Test
    public void testBreed() {
        StandardPopulation population = new StandardPopulation();

        int expectedPopulationSize = 10;

        Genome genomeToReturn = new Genome(true, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        Breeder breederMock = mock(Breeder.class);

        when(breederMock.breed(same(population))).thenReturn(genomeToReturn);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .breeder(breederMock)
                .population(population)
                .build();

        population.init(strategy);

        assertEquals(0, population.size());
        assertEquals(Double.valueOf(0d), population.getTotalFitness());

        List<Genome> individuals = population.breed(expectedPopulationSize);

        assertEquals(expectedPopulationSize, individuals.size());
        individuals.stream().forEach(population::addIndividual);
        assertEquals(Double.valueOf(50.0d), population.getTotalFitness());
    }

    @Test
    public void testEvaluatorTask() {
        StandardPopulation population = new StandardPopulation();
        Genome genomeToEvaluate = new Genome(true, new Fitness[] { new MaximizingFitness(0d) }, population);

        FitnessEvaluator mockEvaluator = mock(FitnessEvaluator.class);
        Double fitnessToReturn = 101.0d;
        when(mockEvaluator.evaluate(same(genomeToEvaluate))).thenReturn(new Fitness[] { new MaximizingFitness(fitnessToReturn) });

        StandardPopulation.EvaluationTask evaluationTask = population.new EvaluationTask(genomeToEvaluate, mockEvaluator);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .fitnessEvaluator(mockEvaluator)
                .build();

        population.init(strategy);

        Double fitnessReturned = null;
        try {
            evaluationTask.call();
            fitnessReturned = genomeToEvaluate.getFitnesses()[0].getValue();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertEquals(fitnessToReturn, fitnessReturned);
        verify(mockEvaluator, times(1)).evaluate(same(genomeToEvaluate));
        verifyNoMoreInteractions(mockEvaluator);
    }

    @Test
    public void testDoConcurrentFitnessEvaluations() {
        StandardPopulation population = new StandardPopulation();

        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(fitnessEvaluatorMock.evaluate(any(Genome.class))).thenReturn(new Fitness[] { new MaximizingFitness(DEFAULT_FITNESS_VALUE) });

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .fitnessEvaluator(fitnessEvaluatorMock)
                .build();

        population.init(strategy);

        Genome genomeEvaluationNeeded1 = new Genome(true, new Fitness[] { new MaximizingFitness(1.0d) }, population);
        population.addIndividual(genomeEvaluationNeeded1);

        Genome genomeEvaluationNeeded2 = new Genome(true, new Fitness[] { new MaximizingFitness(1.0d) }, population);
        population.addIndividual(genomeEvaluationNeeded2);

        Genome genomeEvaluationNotNeeded1 = new Genome(false, new Fitness[] { new MaximizingFitness(1.0d) }, population);
        population.addIndividual(genomeEvaluationNotNeeded1);

        Genome genomeEvaluationNotNeeded2 = new Genome(false, new Fitness[] { new MaximizingFitness(1.0d) }, population);
        population.addIndividual(genomeEvaluationNotNeeded2);

        assertTrue(genomeEvaluationNeeded1.isEvaluationNeeded());
        assertTrue(genomeEvaluationNeeded2.isEvaluationNeeded());
        assertFalse(genomeEvaluationNotNeeded1.isEvaluationNeeded());
        assertFalse(genomeEvaluationNotNeeded2.isEvaluationNeeded());

        population.doConcurrentFitnessEvaluations(fitnessEvaluatorMock, population.getIndividuals());

        for (Genome individual : population.getIndividuals()) {
            assertFalse(individual.isEvaluationNeeded());
        }

        // Only two of the individuals needed to be evaluated
        verify(fitnessEvaluatorMock, times(2)).evaluate(any(Genome.class));
    }

    @Test
    public void testEvaluateFitness() {
        GenerationStatistics generationStatistics = new GenerationStatistics();

        StandardPopulation population = new StandardPopulation();

        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(fitnessEvaluatorMock.evaluate(any(Genome.class))).thenReturn(new Fitness[] { new MaximizingFitness(DEFAULT_FITNESS_VALUE) });

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .fitnessEvaluator(fitnessEvaluatorMock)
                .build();

        population.init(strategy);

        Genome genomeEvaluationNeeded1 = new Genome(true, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        population.addIndividual(genomeEvaluationNeeded1);

        Genome genomeEvaluationNeeded2 = new Genome(true, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        population.addIndividual(genomeEvaluationNeeded2);

        Genome genomeEvaluationNotNeeded1 = new Genome(false, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        population.addIndividual(genomeEvaluationNotNeeded1);

        Genome genomeEvaluationNotNeeded2 = new Genome(false, new Fitness[] { new MaximizingFitness(100.1d) }, population);
        population.addIndividual(genomeEvaluationNotNeeded2);

        assertTrue(genomeEvaluationNeeded1.isEvaluationNeeded());
        assertTrue(genomeEvaluationNeeded2.isEvaluationNeeded());
        assertFalse(genomeEvaluationNotNeeded1.isEvaluationNeeded());
        assertFalse(genomeEvaluationNotNeeded2.isEvaluationNeeded());

        population.evaluateFitness(generationStatistics);

        for (Genome individual : population.getIndividuals()) {
            assertFalse(individual.isEvaluationNeeded());
        }

        // Only two of the individuals needed to be evaluated
        verify(fitnessEvaluatorMock, times(2)).evaluate(any(Genome.class));

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
    public void testEvaluateFitnessCompareToKnownSolution() {
        GenerationStatistics generationStatistics = new GenerationStatistics();

        StandardPopulation population = new StandardPopulation();

        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        when(fitnessEvaluatorMock.evaluate(any(Genome.class))).thenReturn(new Fitness[] { new MaximizingFitness(DEFAULT_FITNESS_VALUE) });

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .fitnessEvaluator(fitnessEvaluatorMock)
                .build();

        population.init(strategy);

        Genome genomeEvaluationNeeded1 = new Genome(true, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        population.addIndividual(genomeEvaluationNeeded1);

        Genome genomeEvaluationNeeded2 = new Genome(true, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        population.addIndividual(genomeEvaluationNeeded2);

        Genome genomeEvaluationNotNeeded1 = new Genome(false, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        population.addIndividual(genomeEvaluationNotNeeded1);

        Genome genomeEvaluationNotNeeded2 = new Genome(false, new Fitness[] { new MaximizingFitness(100.1d) }, population);
        population.addIndividual(genomeEvaluationNotNeeded2);

        assertTrue(genomeEvaluationNeeded1.isEvaluationNeeded());
        assertTrue(genomeEvaluationNeeded2.isEvaluationNeeded());
        assertFalse(genomeEvaluationNotNeeded1.isEvaluationNeeded());
        assertFalse(genomeEvaluationNotNeeded2.isEvaluationNeeded());

        population.evaluateFitness(generationStatistics);

        for (Genome individual : population.getIndividuals()) {
            assertFalse(individual.isEvaluationNeeded());
        }

        // Only two of the individuals needed to be evaluated
        verify(fitnessEvaluatorMock, times(2)).evaluate(any(Genome.class));

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
    public void testIndividualsUnmodifiable() {
        StandardPopulation population = new StandardPopulation();
        population.addIndividual(mock(Genome.class));
        population.addIndividual(mock(Genome.class));
        population.addIndividual(mock(Genome.class));

        List<Genome> individuals = population.getIndividuals();

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            individuals.remove(0); // should throw exception
        });
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

        // Add a genome that needs evaluation
        Genome genomeEvaluationNeeded = new Genome(true, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        population.addIndividual(genomeEvaluationNeeded);

        // Validate
        fitnessSum += genomeEvaluationNeeded.getFitnesses()[0].getValue();
        assertEquals(fitnessSum, population.getTotalFitness());
        assertEquals(1, population.size());
        assertSame(genomeEvaluationNeeded, population.getIndividuals().get(0));

        // Add a genome that doesn't need evaluation
        Genome genomeEvaluationNotNeeded = new Genome(false, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        population.addIndividual(genomeEvaluationNotNeeded);

        // Validate
        fitnessSum += genomeEvaluationNotNeeded.getFitnesses()[0].getValue();
        assertEquals(fitnessSum, population.getTotalFitness());
        assertEquals(2, population.size());
        assertSame(genomeEvaluationNotNeeded, population.getIndividuals().get(1));
    }

    @Test
    public void testRemoveIndividual() {
        StandardPopulation population = new StandardPopulation();

        Genome genome1 = new Genome(true, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        population.addIndividual(genome1);

        Genome genome2 = new Genome(false, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        population.addIndividual(genome2);

        Double fitnessSum = 10.0d;
        assertEquals(fitnessSum, population.getTotalFitness());
        assertEquals(2, population.size());

        fitnessSum -= population.removeIndividual(1).getFitnesses()[0].getValue();
        assertEquals(fitnessSum, population.getTotalFitness());
        assertEquals(1, population.size());
        assertSame(genome1, population.getIndividuals().get(0));

        fitnessSum -= population.removeIndividual(0).getFitnesses()[0].getValue();
        assertEquals(fitnessSum, population.getTotalFitness());
        assertEquals(0, population.size());

        // Try to remove an individual that doesn't exist
        assertNull(population.removeIndividual(0));
    }

    @Test
    public void testClearIndividuals() {
        StandardPopulation population = new StandardPopulation();

        Genome genome1 = new Genome(true, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        population.addIndividual(genome1);

        Genome genome2 = new Genome(false, new Fitness[] { new MaximizingFitness(5.0d) }, population);
        population.addIndividual(genome2);

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

        population.addIndividual(new Genome(true, new Fitness[] { new MaximizingFitness(0d) }, population));

        assertEquals(1, population.size());

        population.addIndividual(new Genome(true, new Fitness[] { new MaximizingFitness(0d) }, population));

        assertEquals(2, population.size());
    }

    @Test
    public void testSortIndividuals() {
        StandardPopulation population = new StandardPopulation();

        Genome genome1 = new Genome(false, new Fitness[] { new MaximizingFitness(3.0d) }, population);
        population.addIndividual(genome1);

        Genome genome2 = new Genome(false, new Fitness[] { new MaximizingFitness(2.0d) }, population);
        population.addIndividual(genome2);

        Genome genome3 = new Genome(false, new Fitness[] { new MaximizingFitness(1.0d) }, population);
        population.addIndividual(genome3);

        assertSame(genome1, population.getIndividuals().get(0));
        assertSame(genome2, population.getIndividuals().get(1));
        assertSame(genome3, population.getIndividuals().get(2));

        population.sortIndividuals();

        assertSame(genome3, population.getIndividuals().get(0));
        assertSame(genome2, population.getIndividuals().get(1));
        assertSame(genome1, population.getIndividuals().get(2));
    }
}
