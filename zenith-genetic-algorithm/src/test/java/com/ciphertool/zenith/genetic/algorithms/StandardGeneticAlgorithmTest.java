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

package com.ciphertool.zenith.genetic.algorithms;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.algorithms.crossover.CrossoverAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.mutation.MutationAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.selection.Selector;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.mocks.MockChromosome;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.genetic.population.StandardPopulation;
import com.ciphertool.zenith.genetic.statistics.ExecutionStatistics;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import org.junit.Test;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StandardGeneticAlgorithmTest {
    @SuppressWarnings("rawtypes")
    @Test
    public void testInitialize() {
        LocalDateTime beforeInitialize = LocalDateTime.now();

        int populationSize = 100;
        double mutationRate = 0.0;
        CrossoverAlgorithm crossoverAlgorithmMock = mock(CrossoverAlgorithm.class);
        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        MutationAlgorithm mutationAlgorithmMock = mock(MutationAlgorithm.class);
        StandardPopulation populationMock = mock(StandardPopulation.class);

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .fitnessEvaluator(fitnessEvaluatorMock)
                .crossoverAlgorithm(crossoverAlgorithmMock)
                .mutationAlgorithm(mutationAlgorithmMock)
                .maxMutationsPerIndividual(0)
                .population(populationMock)
                .populationSize(populationSize)
                .selector(mock(Selector.class))
                .mutationRate(mutationRate)
                .numberOfGenerations(-1)
                .build();

        // Setting the individuals to something non-empty so the calculateEntropy() method won't fail
        List<Chromosome> individuals = new ArrayList<>();
        individuals.add(new MockChromosome());
        when(populationMock.getIndividuals()).thenReturn(individuals);

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        Field generationCountField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "generationCount");
        ReflectionUtils.makeAccessible(generationCountField);
        ReflectionUtils.setField(generationCountField, standardGeneticAlgorithm, 1);

        Field executionStatisticsField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "executionStatistics");
        ReflectionUtils.makeAccessible(executionStatisticsField);
        ExecutionStatistics executionStatisticsFromObject = (ExecutionStatistics) ReflectionUtils.getField(executionStatisticsField, standardGeneticAlgorithm);
        assertNull(executionStatisticsFromObject);

        standardGeneticAlgorithm.initialize(strategyToSet);

        int generationCountFromObject = (int) ReflectionUtils.getField(generationCountField, standardGeneticAlgorithm);
        executionStatisticsFromObject = (ExecutionStatistics) ReflectionUtils.getField(executionStatisticsField, standardGeneticAlgorithm);

        assertEquals(0, generationCountFromObject);
        assertNotNull(executionStatisticsFromObject);
        assertTrue(executionStatisticsFromObject.getStartDateTime().compareTo(beforeInitialize) >= 0);
        assertEquals(populationSize, executionStatisticsFromObject.getPopulationSize().intValue());
        assertEquals(new Double(mutationRate), executionStatisticsFromObject.getMutationRate());
        assertEquals(crossoverAlgorithmMock.getClass().getSimpleName(), executionStatisticsFromObject.getCrossoverAlgorithm());
        assertEquals(fitnessEvaluatorMock.getClass().getSimpleName(), executionStatisticsFromObject.getFitnessEvaluator());
        assertEquals(mutationAlgorithmMock.getClass().getSimpleName(), executionStatisticsFromObject.getMutationAlgorithm());

        verify(populationMock, times(1)).clearIndividuals();
        verify(populationMock, times(1)).breed();
        verify(populationMock, times(1)).evaluateFitness(any(GenerationStatistics.class));
        verify(populationMock, times(1)).size();
        verify(populationMock, times(1)).calculateEntropy();
        verifyNoMoreInteractions(populationMock);
    }

    @Test
    public void testFinish() {
        LocalDateTime beforeFinish = LocalDateTime.now();

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        Field executionStatisticsField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "executionStatistics");
        ReflectionUtils.makeAccessible(executionStatisticsField);
        ReflectionUtils.setField(executionStatisticsField, standardGeneticAlgorithm, executionStatistics);

        Field generationCountField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "generationCount");
        ReflectionUtils.makeAccessible(generationCountField);
        ReflectionUtils.setField(generationCountField, standardGeneticAlgorithm, 1);

        standardGeneticAlgorithm.finish();

        assertTrue(executionStatistics.getEndDateTime().compareTo(beforeFinish) >= 0);

        ExecutionStatistics executionStatisticsFromObject = (ExecutionStatistics) ReflectionUtils.getField(executionStatisticsField, standardGeneticAlgorithm);
        assertNull(executionStatisticsFromObject);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testProceedWithNextGeneration() {
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        int initialPopulationSize = 100;
        int populationSize = 100;
        double mutationRate = 0.1;

        StandardPopulation populationMock = mock(StandardPopulation.class);

        List<Chromosome> individuals = new ArrayList<>();
        for (int i = 0; i < initialPopulationSize; i++) {
            individuals.add(new MockChromosome());
        }

        List<Parents> allParents = new ArrayList<>(initialPopulationSize);

        for (int i = 0; i < initialPopulationSize; i ++) {
            allParents.add(new Parents(new MockChromosome(), new MockChromosome()));
        }

        when(populationMock.select()).thenReturn(allParents);
        when(populationMock.getIndividuals()).thenReturn(individuals);
        when(populationMock.removeIndividual(anyInt())).thenReturn(new MockChromosome());
        when(populationMock.size()).thenReturn(initialPopulationSize);

        MutationAlgorithm mutationAlgorithmMock = mock(MutationAlgorithm.class);
        CrossoverAlgorithm crossoverAlgorithmMock = mock(CrossoverAlgorithm.class);

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .population(populationMock)
                .populationSize(populationSize)
                .mutationRate(mutationRate)
                .mutationAlgorithm(mutationAlgorithmMock)
                .crossoverAlgorithm(crossoverAlgorithmMock)
                .elitism(0)
                .build();

        TaskExecutor taskExecutorMock = mock(TaskExecutor.class);
        doAnswer(invocation -> {
            ((FutureTask) invocation.getArguments()[0]).run();

            return null;
        }).when(taskExecutorMock).execute(any(FutureTask.class));

        Field taskExecutorField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, standardGeneticAlgorithm, taskExecutorMock);

        Field generationCountField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "generationCount");
        ReflectionUtils.makeAccessible(generationCountField);
        ReflectionUtils.setField(generationCountField, standardGeneticAlgorithm, 0);

        Chromosome chromosomeToReturn = new MockChromosome();
        when(crossoverAlgorithmMock.crossover(any(Chromosome.class), any(Chromosome.class))).thenReturn(chromosomeToReturn);

        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        Field executionStatisticsField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "executionStatistics");
        ReflectionUtils.makeAccessible(executionStatisticsField);
        ReflectionUtils.setField(executionStatisticsField, standardGeneticAlgorithm, executionStatistics);

        assertEquals(0, executionStatistics.getGenerationStatisticsList().size());

        standardGeneticAlgorithm.proceedWithNextGeneration(strategyToSet);

        assertEquals(1, executionStatistics.getGenerationStatisticsList().size());

        /*
         * The population size should be reduced by the number of parents used during crossover.
         */
        assertEquals(100, populationMock.size());

        int generationCountFromObject = (int) ReflectionUtils.getField(generationCountField, standardGeneticAlgorithm);
        assertEquals(1, generationCountFromObject);

        verify(populationMock, times(1)).select();
        verify(populationMock, times(2)).size();
        verify(populationMock, never()).breed();
        verify(populationMock, times(1)).evaluateFitness(any(GenerationStatistics.class));
        verify(populationMock, times(100)).addIndividual(any(Chromosome.class));
        verify(populationMock, times(1)).sortIndividuals();
        verify(populationMock, times(1)).clearIndividuals();
        verify(populationMock, times(1)).calculateEntropy();
        verifyNoMoreInteractions(populationMock);

        verify(mutationAlgorithmMock, times(100)).mutateChromosome(any(Chromosome.class), same(strategyToSet));
        verifyNoMoreInteractions(mutationAlgorithmMock);

        verify(crossoverAlgorithmMock, times(100)).crossover(any(Chromosome.class), any(Chromosome.class));
        verifyNoMoreInteractions(crossoverAlgorithmMock);
    }

    @Test
    public void testValidateParameters_NoErrors() {
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .fitnessEvaluator(mock(FitnessEvaluator.class))
                .crossoverAlgorithm(mock(CrossoverAlgorithm.class))
                .mutationAlgorithm(mock(MutationAlgorithm.class))
                .maxMutationsPerIndividual(0)
                .population(mock(Population.class))
                .populationSize(1)
                .selector(mock(Selector.class))
                .mutationRate(0.0)
                .numberOfGenerations(-1)
                .build();

        standardGeneticAlgorithm.validateParameters(strategyToSet);
    }

    @Test
    public void testValidateParameters_AllErrors() {
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .fitnessEvaluator(null)
                .crossoverAlgorithm(null)
                .mutationAlgorithm(null)
                .maxMutationsPerIndividual(-1)
                .populationSize(0)
                .selector(null)
                .numberOfGenerations(0)
                .build();

        /*
         * This must be set via reflection because the setter method does its own validation
         */
        Field mutationRateField = ReflectionUtils.findField(GeneticAlgorithmStrategy.class, "mutationRate");
        ReflectionUtils.makeAccessible(mutationRateField);
        ReflectionUtils.setField(mutationRateField, strategyToSet, -0.1);

        boolean exceptionCaught = false;

        try {
            standardGeneticAlgorithm.validateParameters(strategyToSet);
        } catch (IllegalStateException ise) {
            String expectedMessage = "Unable to execute genetic algorithm because one or more of the required parameters are missing.  The validation errors are:";
            expectedMessage += "\n\t-Parameter 'populationSize' must be greater than zero.";
            expectedMessage += "\n\t-Parameter 'mutationRate' must be greater than or equal to zero.";
            expectedMessage += "\n\t-Parameter 'maxMutationsPerIndividual' must be greater than or equal to zero.";
            expectedMessage += "\n\t-Parameter 'maxGenerations' cannot be null and must not equal zero.";
            expectedMessage += "\n\t-Parameter 'crossoverAlgorithm' cannot be null.";
            expectedMessage += "\n\t-Parameter 'fitnessEvaluator' cannot be null.";
            expectedMessage += "\n\t-Parameter 'mutationAlgorithm' cannot be null.";
            expectedMessage += "\n\t-Parameter 'selectorMethod' cannot be null.";

            assertEquals(expectedMessage, ise.getMessage());

            exceptionCaught = true;
        }

        assertTrue(exceptionCaught);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testCrossover() {
        int index = 0;
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        int initialPopulationSize = 50;

        List<Chromosome> individuals = new ArrayList<>();
        for (int i = 0; i < initialPopulationSize; i++) {
            individuals.add(new MockChromosome());
        }

        StandardPopulation populationMock = mock(StandardPopulation.class);
        when(populationMock.size()).thenReturn(initialPopulationSize);

        TaskExecutor taskExecutorMock = mock(TaskExecutor.class);
        doAnswer(invocation -> {
            ((FutureTask) invocation.getArguments()[0]).run();

            return null;
        }).when(taskExecutorMock).execute(any(FutureTask.class));

        Field taskExecutorField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, standardGeneticAlgorithm, taskExecutorMock);

        CrossoverAlgorithm crossoverAlgorithmMock = mock(CrossoverAlgorithm.class);

        Chromosome chromosomeToReturn = new MockChromosome();
        when(crossoverAlgorithmMock.crossover(any(Chromosome.class), any(Chromosome.class))).thenReturn(chromosomeToReturn);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .population(populationMock)
                .crossoverAlgorithm(crossoverAlgorithmMock)
                .elitism(0)
                .populationSize(initialPopulationSize)
                .build();

        List<Parents> allParents = new ArrayList<>();

        for (int i = 0; i < initialPopulationSize; i++) {
            allParents.add(new Parents(individuals.get(i), individuals.get(i)));
        }

        List<Chromosome> children = standardGeneticAlgorithm.crossover(strategy, allParents);

        assertEquals(50, children.size());

        verify(populationMock, times(1)).size();
        verifyNoMoreInteractions(populationMock);

        verify(crossoverAlgorithmMock, times(50)).crossover(any(Chromosome.class), any(Chromosome.class));
        verifyNoMoreInteractions(crossoverAlgorithmMock);
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    public void testCrossover_SmallPopulation() {
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        StandardPopulation population = new StandardPopulation();

        int initialPopulationSize = 10;

        Chromosome chromosome = new MockChromosome();
        population.addIndividual(chromosome);

        CrossoverAlgorithm crossoverAlgorithmMock = mock(CrossoverAlgorithm.class);

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .crossoverAlgorithm(crossoverAlgorithmMock)
                .population(population)
                .build();

        List<Parents> allParents = new ArrayList<>();

        for (int i = initialPopulationSize / 2; i < initialPopulationSize; i++) {
            allParents.add(new Parents(new MockChromosome(), new MockChromosome()));
        }

        List<Chromosome> children = standardGeneticAlgorithm.crossover(strategyToSet, allParents);

        assertEquals(1, population.size());

        assertEquals(0, children.size());

        verifyNoInteractions(crossoverAlgorithmMock);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testMutate() {
        int initialPopulationSize = 100;
        int index = 0;

        List<Chromosome> individuals = new ArrayList<>();
        for (int i = 0; i < initialPopulationSize; i++) {
            individuals.add(new MockChromosome());
        }

        StandardPopulation populationMock = mock(StandardPopulation.class);

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        TaskExecutor taskExecutorMock = mock(TaskExecutor.class);
        doAnswer(invocation -> {
            ((FutureTask) invocation.getArguments()[0]).run();

            return null;
        }).when(taskExecutorMock).execute(any(FutureTask.class));

        Field taskExecutorField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, standardGeneticAlgorithm, taskExecutorMock);

        double mutationRate = 0.5;

        MutationAlgorithm mutationAlgorithmMock = mock(MutationAlgorithm.class);

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .population(populationMock)
                .mutationRate(mutationRate)
                .mutationAlgorithm(mutationAlgorithmMock)
                .elitism(0)
                .build();

        standardGeneticAlgorithm.mutate(strategyToSet, individuals);

        verify(populationMock, times(1)).sortIndividuals();
        verifyNoMoreInteractions(populationMock);

        verify(mutationAlgorithmMock, times(100)).mutateChromosome(any(Chromosome.class), same(strategyToSet));
        verifyNoMoreInteractions(mutationAlgorithmMock);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testMutate_SmallPopulation() {
        int initialPopulationSize = 100;

        List<Chromosome> individuals = new ArrayList<>();
        for (int i = 0; i < initialPopulationSize; i++) {
            individuals.add(new MockChromosome());
        }

        StandardPopulation populationMock = mock(StandardPopulation.class);

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        TaskExecutor taskExecutorMock = mock(TaskExecutor.class);
        doAnswer(invocation -> {
            ((FutureTask) invocation.getArguments()[0]).run();

            return null;
        }).when(taskExecutorMock).execute(any(FutureTask.class));

        Field taskExecutorField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, standardGeneticAlgorithm, taskExecutorMock);

        double mutationRate = 0.5;

        MutationAlgorithm mutationAlgorithmMock = mock(MutationAlgorithm.class);

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .population(populationMock)
                .mutationRate(mutationRate)
                .mutationAlgorithm(mutationAlgorithmMock)
                .elitism(0)
                .build();

        standardGeneticAlgorithm.mutate(strategyToSet, individuals);

        verify(populationMock, times(1)).sortIndividuals();
        verifyNoMoreInteractions(populationMock);

        verify(mutationAlgorithmMock, times(initialPopulationSize)).mutateChromosome(any(Chromosome.class), same(strategyToSet));
        verifyNoMoreInteractions(mutationAlgorithmMock);
    }

    @Test
    public void testSpawnInitialPopulation() {
        StandardPopulation populationMock = mock(StandardPopulation.class);
        int populationSize = 100;

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .population(populationMock)
                .populationSize(populationSize)
                .build();

        // Setting the individuals to something non-empty so the calculateEntropy() method won't fail
        List<Chromosome> individuals = new ArrayList<>();
        individuals.add(new MockChromosome());
        when(populationMock.getIndividuals()).thenReturn(individuals);

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        Field executionStatisticsField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "executionStatistics");
        ReflectionUtils.makeAccessible(executionStatisticsField);
        ReflectionUtils.setField(executionStatisticsField, standardGeneticAlgorithm, new ExecutionStatistics());

        standardGeneticAlgorithm.spawnInitialPopulation(strategyToSet);

        verify(populationMock, times(1)).clearIndividuals();
        verify(populationMock, times(1)).breed();
        verify(populationMock, times(1)).evaluateFitness(any(GenerationStatistics.class));
        verify(populationMock, times(1)).size();
        verify(populationMock, times(1)).calculateEntropy();
        verifyNoMoreInteractions(populationMock);
    }
}
