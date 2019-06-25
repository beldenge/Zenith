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

package com.ciphertool.zenith.genetic.algorithms;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.algorithms.crossover.CrossoverAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.mutation.MutationAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.selection.modes.Selector;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.mocks.MockKeyedChromosome;
import com.ciphertool.zenith.genetic.population.StandardPopulation;
import com.ciphertool.zenith.genetic.statistics.ExecutionStatistics;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

public class StandardGeneticAlgorithmTest {
    @Test
    public void testSetPopulation() {
        StandardPopulation populationToSet = mock(StandardPopulation.class);

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();
        standardGeneticAlgorithm.setPopulation(populationToSet);

        StandardPopulation populationFromObject = (StandardPopulation) standardGeneticAlgorithm.getPopulation();

        assertSame(populationToSet, populationFromObject);
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    public void testSetStrategy() {
        StandardPopulation populationMock = mock(StandardPopulation.class);
        CrossoverAlgorithm crossoverAlgorithmMock = mock(CrossoverAlgorithm.class);
        MutationAlgorithm mutationAlgorithmMock = mock(MutationAlgorithm.class);
        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        FitnessEvaluator knownSolutionFitnessEvaluatorMock = mock(FitnessEvaluator.class);
        Selector selectorMock = mock(Selector.class);
        Object geneticStructure = new Object();
        boolean compareToKnownSolution = true;
        int maxMutationsPerIndividual = 5;
        int populationSizeToSet = 100;

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .geneticStructure(geneticStructure)
                .fitnessEvaluator(fitnessEvaluatorMock)
                .knownSolutionFitnessEvaluator(knownSolutionFitnessEvaluatorMock)
                .compareToKnownSolution(compareToKnownSolution)
                .crossoverAlgorithm(crossoverAlgorithmMock)
                .mutationAlgorithm(mutationAlgorithmMock)
                .maxMutationsPerIndividual(maxMutationsPerIndividual)
                .populationSize(populationSizeToSet)
                .selector(selectorMock)
                .build();

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();
        standardGeneticAlgorithm.setPopulation(populationMock);
        standardGeneticAlgorithm.setStrategy(strategyToSet);

        Field strategyField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "strategy");
        ReflectionUtils.makeAccessible(strategyField);
        GeneticAlgorithmStrategy strategyFromObject = (GeneticAlgorithmStrategy) ReflectionUtils.getField(strategyField, standardGeneticAlgorithm);

        assertSame(strategyToSet, strategyFromObject);
        verify(populationMock, times(1)).setGeneticStructure(same(geneticStructure));
        verify(populationMock, times(1)).setFitnessEvaluator(same(fitnessEvaluatorMock));
        verify(populationMock, times(1)).setKnownSolutionFitnessEvaluator(same(knownSolutionFitnessEvaluatorMock));
        verify(populationMock, times(1)).setCompareToKnownSolution(eq(compareToKnownSolution));
        verify(populationMock, times(1)).setTargetSize(eq(populationSizeToSet));
        verify(populationMock, times(1)).setSelector(eq(selectorMock));
        verifyNoMoreInteractions(populationMock);

        verifyNoMoreInteractions(crossoverAlgorithmMock);

        verifyNoMoreInteractions(mutationAlgorithmMock);

        Field crossoverAlgorithmField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "crossoverAlgorithm");
        ReflectionUtils.makeAccessible(crossoverAlgorithmField);
        CrossoverAlgorithm crossoverAlgorithmFromObject = (CrossoverAlgorithm) ReflectionUtils.getField(crossoverAlgorithmField, standardGeneticAlgorithm);

        assertSame(crossoverAlgorithmMock, crossoverAlgorithmFromObject);

        Field mutationAlgorithmField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "mutationAlgorithm");
        ReflectionUtils.makeAccessible(mutationAlgorithmField);
        MutationAlgorithm mutationAlgorithmFromObject = (MutationAlgorithm) ReflectionUtils.getField(mutationAlgorithmField, standardGeneticAlgorithm);

        assertSame(mutationAlgorithmMock, mutationAlgorithmFromObject);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testInitialize() throws InterruptedException {
        Date beforeInitialize = new Date();

        int populationSize = 100;
        double mutationRate = 0.0;
        CrossoverAlgorithm crossoverAlgorithmMock = mock(CrossoverAlgorithm.class);
        FitnessEvaluator fitnessEvaluatorMock = mock(FitnessEvaluator.class);
        MutationAlgorithm mutationAlgorithmMock = mock(MutationAlgorithm.class);

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .geneticStructure(new Object())
                .fitnessEvaluator(fitnessEvaluatorMock)
                .crossoverAlgorithm(crossoverAlgorithmMock)
                .mutationAlgorithm(mutationAlgorithmMock)
                .maxMutationsPerIndividual(0)
                .populationSize(populationSize)
                .selector(mock(Selector.class))
                .mutationRate(mutationRate)
                .maxGenerations(-1)
                .build();

        StandardPopulation populationMock = mock(StandardPopulation.class);

        // Setting the individuals to something non-empty so the calculateEntropy() method won't fail
        List<Chromosome> individuals = new ArrayList<>();
        individuals.add(new MockKeyedChromosome());
        when(populationMock.getIndividuals()).thenReturn(individuals);

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();
        standardGeneticAlgorithm.setPopulation(populationMock);

        Field strategyField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "strategy");
        ReflectionUtils.makeAccessible(strategyField);
        ReflectionUtils.setField(strategyField, standardGeneticAlgorithm, strategyToSet);

        Field generationCountField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "generationCount");
        ReflectionUtils.makeAccessible(generationCountField);
        ReflectionUtils.setField(generationCountField, standardGeneticAlgorithm, 1);

        Field executionStatisticsField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "executionStatistics");
        ReflectionUtils.makeAccessible(executionStatisticsField);
        ExecutionStatistics executionStatisticsFromObject = (ExecutionStatistics) ReflectionUtils.getField(executionStatisticsField, standardGeneticAlgorithm);
        assertNull(executionStatisticsFromObject);

        standardGeneticAlgorithm.initialize();

        int generationCountFromObject = (int) ReflectionUtils.getField(generationCountField, standardGeneticAlgorithm);
        executionStatisticsFromObject = (ExecutionStatistics) ReflectionUtils.getField(executionStatisticsField, standardGeneticAlgorithm);

        assertEquals(0, generationCountFromObject);
        assertNotNull(executionStatisticsFromObject);
        assertTrue(executionStatisticsFromObject.getStartDateTime().getTime() >= beforeInitialize.getTime());
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
        Date beforeFinish = new Date();

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        Field executionStatisticsField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "executionStatistics");
        ReflectionUtils.makeAccessible(executionStatisticsField);
        ReflectionUtils.setField(executionStatisticsField, standardGeneticAlgorithm, executionStatistics);

        Field generationCountField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "generationCount");
        ReflectionUtils.makeAccessible(generationCountField);
        ReflectionUtils.setField(generationCountField, standardGeneticAlgorithm, 1);

        standardGeneticAlgorithm.finish();

        assertTrue(executionStatistics.getEndDateTime().getTime() >= beforeFinish.getTime());

        ExecutionStatistics executionStatisticsFromObject = (ExecutionStatistics) ReflectionUtils.getField(executionStatisticsField, standardGeneticAlgorithm);
        assertNull(executionStatisticsFromObject);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testProceedWithNextGeneration() throws InterruptedException {
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        int initialPopulationSize = 100;
        int populationSize = 100;
        int index = 0;
        double mutationRate = 0.1;

        StandardPopulation populationMock = mock(StandardPopulation.class);

        List<Chromosome> individuals = new ArrayList<Chromosome>();
        for (int i = 0; i < initialPopulationSize; i++) {
            individuals.add(new MockKeyedChromosome());
        }

        when(populationMock.selectIndex()).thenReturn(index);
        when(populationMock.getIndividuals()).thenReturn(individuals);
        when(populationMock.removeIndividual(anyInt())).thenReturn(new MockKeyedChromosome());
        when(populationMock.size()).thenReturn(initialPopulationSize);
        when(populationMock.selectIndex()).thenReturn(0);
        standardGeneticAlgorithm.setPopulation(populationMock);

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .populationSize(populationSize)
                .mutationRate(mutationRate)
                .build();

        TaskExecutor taskExecutorMock = mock(TaskExecutor.class);
        doAnswer(invocation -> {
            ((FutureTask) invocation.getArguments()[0]).run();

            return null;
        }).when(taskExecutorMock).execute(any(FutureTask.class));

        Field taskExecutorField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, standardGeneticAlgorithm, taskExecutorMock);

        Field strategyField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "strategy");
        ReflectionUtils.makeAccessible(strategyField);
        ReflectionUtils.setField(strategyField, standardGeneticAlgorithm, strategyToSet);

        Field generationCountField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "generationCount");
        ReflectionUtils.makeAccessible(generationCountField);
        ReflectionUtils.setField(generationCountField, standardGeneticAlgorithm, 0);

        MutationAlgorithm mutationAlgorithmMock = mock(MutationAlgorithm.class);
        Field mutationAlgorithmField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "mutationAlgorithm");
        ReflectionUtils.makeAccessible(mutationAlgorithmField);
        ReflectionUtils.setField(mutationAlgorithmField, standardGeneticAlgorithm, mutationAlgorithmMock);

        CrossoverAlgorithm crossoverAlgorithmMock = mock(CrossoverAlgorithm.class);
        when(crossoverAlgorithmMock.numberOfOffspring()).thenReturn(1);

        Field crossoverAlgorithmField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "crossoverAlgorithm");
        ReflectionUtils.makeAccessible(crossoverAlgorithmField);
        ReflectionUtils.setField(crossoverAlgorithmField, standardGeneticAlgorithm, crossoverAlgorithmMock);

        Chromosome chromosomeToReturn = new MockKeyedChromosome();
        when(crossoverAlgorithmMock.crossover(any(Chromosome.class), any(Chromosome.class))).thenReturn(Arrays.asList(chromosomeToReturn));

        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        Field executionStatisticsField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "executionStatistics");
        ReflectionUtils.makeAccessible(executionStatisticsField);
        ReflectionUtils.setField(executionStatisticsField, standardGeneticAlgorithm, executionStatistics);

        standardGeneticAlgorithm.setMajorEvaluationPercentage(0.1d);
        standardGeneticAlgorithm.setMajorEvaluationStepSize(1);

        assertEquals(0, executionStatistics.getGenerationStatisticsList().size());

        standardGeneticAlgorithm.proceedWithNextGeneration();

        assertEquals(1, executionStatistics.getGenerationStatisticsList().size());

        /*
         * The population size should be reduced by the number of parents used during crossover.
         */
        assertEquals(100, populationMock.size());

        int generationCountFromObject = (int) ReflectionUtils.getField(generationCountField, standardGeneticAlgorithm);
        assertEquals(1, generationCountFromObject);

        verify(populationMock, times(1)).backupIndividuals();
        verify(populationMock, times(200)).selectIndex();
        verify(populationMock, times(300)).getIndividuals();
        verify(populationMock, times(4)).size();
        verify(populationMock, never()).breed();
        verify(populationMock, times(1)).evaluateFitness(any(GenerationStatistics.class));
        verify(populationMock, times(100)).addIndividual(any(Chromosome.class));
        verify(populationMock, times(1)).sortIndividuals();
        verify(populationMock, times(1)).clearIndividuals();
        verify(populationMock, times(1)).reIndexSelector();
        verify(populationMock, times(1)).calculateEntropy();
        verifyNoMoreInteractions(populationMock);

        verify(mutationAlgorithmMock, times(100)).mutateChromosome(any(Chromosome.class));
        verifyNoMoreInteractions(mutationAlgorithmMock);

        verify(crossoverAlgorithmMock, times(100)).crossover(any(Chromosome.class), any(Chromosome.class));
        verify(crossoverAlgorithmMock, times(1)).numberOfOffspring();
        verifyNoMoreInteractions(crossoverAlgorithmMock);
    }

    @Test
    public void testValidateParameters_NoErrors() {
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .geneticStructure(new Object())
                .fitnessEvaluator(mock(FitnessEvaluator.class))
                .crossoverAlgorithm(mock(CrossoverAlgorithm.class))
                .mutationAlgorithm(mock(MutationAlgorithm.class))
                .maxMutationsPerIndividual(0)
                .populationSize(1)
                .selector(mock(Selector.class))
                .mutationRate(0.0)
                .maxGenerations(-1)
                .build();

        Field strategyField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "strategy");
        ReflectionUtils.makeAccessible(strategyField);
        ReflectionUtils.setField(strategyField, standardGeneticAlgorithm, strategyToSet);
    }

    @Test
    public void testValidateParameters_AllErrors() {
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .geneticStructure(null)
                .fitnessEvaluator(null)
                .crossoverAlgorithm(null)
                .mutationAlgorithm(null)
                .maxMutationsPerIndividual(-1)
                .populationSize(0)
                .selector(null)
                .maxGenerations(0)
                .build();

        /*
         * This must be set via reflection because the setter method does its own validation
         */
        Field mutationRateField = ReflectionUtils.findField(GeneticAlgorithmStrategy.class, "mutationRate");
        ReflectionUtils.makeAccessible(mutationRateField);
        ReflectionUtils.setField(mutationRateField, strategyToSet, -0.1);

        Field strategyField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "strategy");
        ReflectionUtils.makeAccessible(strategyField);
        ReflectionUtils.setField(strategyField, standardGeneticAlgorithm, strategyToSet);

        boolean exceptionCaught = false;

        try {
            standardGeneticAlgorithm.validateParameters();
        } catch (IllegalStateException ise) {
            String expectedMessage = "Unable to execute genetic algorithm because one or more of the required parameters are missing.  The validation errors are:";
            expectedMessage += "\n\t-Parameter 'geneticStructure' cannot be null.";
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
    public void testCrossover() throws InterruptedException {
        int index = 0;
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        int initialPopulationSize = 50;

        List<Chromosome> individuals = new ArrayList<Chromosome>();
        for (int i = 0; i < initialPopulationSize; i++) {
            individuals.add(new MockKeyedChromosome());
        }

        StandardPopulation populationMock = mock(StandardPopulation.class);
        when(populationMock.selectIndex()).thenReturn(index);
        when(populationMock.getIndividuals()).thenReturn(individuals);
        when(populationMock.size()).thenReturn(initialPopulationSize);
        when(populationMock.removeIndividual(anyInt())).thenReturn(new MockKeyedChromosome());
        when(populationMock.selectIndex()).thenReturn(0);

        standardGeneticAlgorithm.setPopulation(populationMock);

        TaskExecutor taskExecutorMock = mock(TaskExecutor.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((FutureTask) invocation.getArguments()[0]).run();

                return null;
            }
        }).when(taskExecutorMock).execute(any(FutureTask.class));

        Field taskExecutorField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, standardGeneticAlgorithm, taskExecutorMock);

        CrossoverAlgorithm crossoverAlgorithmMock = mock(CrossoverAlgorithm.class);
        when(crossoverAlgorithmMock.numberOfOffspring()).thenReturn(1);

        Field crossoverAlgorithmField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "crossoverAlgorithm");
        ReflectionUtils.makeAccessible(crossoverAlgorithmField);
        ReflectionUtils.setField(crossoverAlgorithmField, standardGeneticAlgorithm, crossoverAlgorithmMock);

        Chromosome chromosomeToReturn = new MockKeyedChromosome();
        when(crossoverAlgorithmMock.crossover(any(Chromosome.class), any(Chromosome.class))).thenReturn(Arrays.asList(chromosomeToReturn));

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder().build();

        Field strategyField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "strategy");
        ReflectionUtils.makeAccessible(strategyField);
        ReflectionUtils.setField(strategyField, standardGeneticAlgorithm, strategy);

        List<Chromosome> moms = new ArrayList<Chromosome>();
        for (int i = 0; i < initialPopulationSize; i++) {
            moms.add(individuals.get(i));
        }

        List<Chromosome> dads = new ArrayList<Chromosome>();
        for (int i = 0; i < initialPopulationSize; i++) {
            dads.add(individuals.get(i));
        }

        int childrenProduced = standardGeneticAlgorithm.crossover(initialPopulationSize, moms, dads);

        assertEquals(50, childrenProduced);

        verify(populationMock, times(50)).addIndividual(any(Chromosome.class));
        verify(populationMock, times(1)).size();
        verify(populationMock, times(1)).clearIndividuals();
        verifyNoMoreInteractions(populationMock);

        verify(crossoverAlgorithmMock, times(50)).crossover(any(Chromosome.class), any(Chromosome.class));
        verifyNoMoreInteractions(crossoverAlgorithmMock);
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    public void testCrossover_SmallPopulation() throws InterruptedException {
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        StandardPopulation population = new StandardPopulation();

        int initialPopulationSize = 10;

        Chromosome chromosome = new MockKeyedChromosome();
        population.addIndividual(chromosome);
        standardGeneticAlgorithm.setPopulation(population);

        CrossoverAlgorithm crossoverAlgorithmMock = mock(CrossoverAlgorithm.class);

        Field crossoverAlgorithmField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "crossoverAlgorithm");
        ReflectionUtils.makeAccessible(crossoverAlgorithmField);
        ReflectionUtils.setField(crossoverAlgorithmField, standardGeneticAlgorithm, crossoverAlgorithmMock);

        List<Chromosome> moms = new ArrayList<Chromosome>();
        for (int i = 0; i < initialPopulationSize / 2; i++) {
            moms.add(new MockKeyedChromosome());
        }

        List<Chromosome> dads = new ArrayList<Chromosome>();
        for (int i = initialPopulationSize / 2; i < initialPopulationSize; i++) {
            dads.add(new MockKeyedChromosome());
        }

        int childrenProduced = standardGeneticAlgorithm.crossover(initialPopulationSize, moms, dads);

        assertEquals(1, population.size());

        assertEquals(0, childrenProduced);

        verifyZeroInteractions(crossoverAlgorithmMock);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testMutate() throws InterruptedException {
        int initialPopulationSize = 100;
        int index = 0;

        List<Chromosome> individuals = new ArrayList<Chromosome>();
        for (int i = 0; i < initialPopulationSize; i++) {
            individuals.add(new MockKeyedChromosome());
        }

        StandardPopulation populationMock = mock(StandardPopulation.class);
        when(populationMock.selectIndex()).thenReturn(index);
        when(populationMock.getIndividuals()).thenReturn(individuals);
        when(populationMock.size()).thenReturn(initialPopulationSize);

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();
        standardGeneticAlgorithm.setPopulation(populationMock);

        TaskExecutor taskExecutorMock = mock(TaskExecutor.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((FutureTask) invocation.getArguments()[0]).run();

                return null;
            }
        }).when(taskExecutorMock).execute(any(FutureTask.class));

        Field taskExecutorField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, standardGeneticAlgorithm, taskExecutorMock);

        double mutationRate = 0.5;

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .mutationRate(mutationRate)
                .build();

        Field strategyField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "strategy");
        ReflectionUtils.makeAccessible(strategyField);
        ReflectionUtils.setField(strategyField, standardGeneticAlgorithm, strategyToSet);

        MutationAlgorithm mutationAlgorithmMock = mock(MutationAlgorithm.class);
        Field mutationAlgorithmField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "mutationAlgorithm");
        ReflectionUtils.makeAccessible(mutationAlgorithmField);
        ReflectionUtils.setField(mutationAlgorithmField, standardGeneticAlgorithm, mutationAlgorithmMock);

        standardGeneticAlgorithm.mutate(initialPopulationSize);

        verify(populationMock, times(100)).getIndividuals();
        verify(populationMock, times(1)).size();
        verify(populationMock, times(1)).sortIndividuals();
        verifyNoMoreInteractions(populationMock);

        verify(mutationAlgorithmMock, times(100)).mutateChromosome(any(Chromosome.class));
        verifyNoMoreInteractions(mutationAlgorithmMock);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testMutate_SmallPopulation() throws InterruptedException {
        int initialPopulationSize = 100;
        int actualPopulationSize = 25;
        int index = 0;

        List<Chromosome> individuals = new ArrayList<Chromosome>();
        for (int i = 0; i < initialPopulationSize; i++) {
            individuals.add(new MockKeyedChromosome());
        }

        StandardPopulation populationMock = mock(StandardPopulation.class);
        when(populationMock.selectIndex()).thenReturn(index);
        when(populationMock.getIndividuals()).thenReturn(individuals);
        when(populationMock.size()).thenReturn(actualPopulationSize);

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();
        standardGeneticAlgorithm.setPopulation(populationMock);

        TaskExecutor taskExecutorMock = mock(TaskExecutor.class);
        doAnswer(invocation -> {
            ((FutureTask) invocation.getArguments()[0]).run();

            return null;
        }).when(taskExecutorMock).execute(any(FutureTask.class));

        Field taskExecutorField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, standardGeneticAlgorithm, taskExecutorMock);

        double mutationRate = 0.5;

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .mutationRate(mutationRate)
                .build();

        Field strategyField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "strategy");
        ReflectionUtils.makeAccessible(strategyField);
        ReflectionUtils.setField(strategyField, standardGeneticAlgorithm, strategyToSet);

        MutationAlgorithm mutationAlgorithmMock = mock(MutationAlgorithm.class);
        Field mutationAlgorithmField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "mutationAlgorithm");
        ReflectionUtils.makeAccessible(mutationAlgorithmField);
        ReflectionUtils.setField(mutationAlgorithmField, standardGeneticAlgorithm, mutationAlgorithmMock);

        standardGeneticAlgorithm.mutate(initialPopulationSize);

        verify(populationMock, times(actualPopulationSize)).getIndividuals();
        verify(populationMock, times(1)).size();
        verify(populationMock, times(1)).sortIndividuals();
        verifyNoMoreInteractions(populationMock);

        verify(mutationAlgorithmMock, times(actualPopulationSize)).mutateChromosome(any(Chromosome.class));
        verifyNoMoreInteractions(mutationAlgorithmMock);
    }

    @Test
    public void testSpawnInitialPopulation() throws InterruptedException {
        int populationSize = 100;

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .populationSize(populationSize)
                .build();

        StandardPopulation populationMock = mock(StandardPopulation.class);

        // Setting the individuals to something non-empty so the calculateEntropy() method won't fail
        List<Chromosome> individuals = new ArrayList<Chromosome>();
        individuals.add(new MockKeyedChromosome());
        when(populationMock.getIndividuals()).thenReturn(individuals);

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();
        standardGeneticAlgorithm.setPopulation(populationMock);

        Field executionStatisticsField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "executionStatistics");
        ReflectionUtils.makeAccessible(executionStatisticsField);
        ReflectionUtils.setField(executionStatisticsField, standardGeneticAlgorithm, new ExecutionStatistics());

        Field strategyField = ReflectionUtils.findField(StandardGeneticAlgorithm.class, "strategy");
        ReflectionUtils.makeAccessible(strategyField);
        ReflectionUtils.setField(strategyField, standardGeneticAlgorithm, strategyToSet);

        standardGeneticAlgorithm.spawnInitialPopulation();

        verify(populationMock, times(1)).clearIndividuals();
        verify(populationMock, times(1)).breed();
        verify(populationMock, times(1)).evaluateFitness(any(GenerationStatistics.class));
        verify(populationMock, times(1)).size();
        verify(populationMock, times(1)).calculateEntropy();
        verifyNoMoreInteractions(populationMock);
    }
}
