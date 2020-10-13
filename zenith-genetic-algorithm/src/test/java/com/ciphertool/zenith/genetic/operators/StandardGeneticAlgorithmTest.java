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

package com.ciphertool.zenith.genetic.operators;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.operators.algorithm.StandardGeneticAlgorithm;
import com.ciphertool.zenith.genetic.operators.crossover.CrossoverOperator;
import com.ciphertool.zenith.genetic.operators.mutation.MutationOperator;
import com.ciphertool.zenith.genetic.population.StandardPopulation;
import com.ciphertool.zenith.genetic.statistics.ExecutionStatistics;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import org.junit.Test;
import org.springframework.core.task.TaskExecutor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class StandardGeneticAlgorithmTest {
    @Test
    public void testFinish() {
        LocalDateTime beforeFinish = LocalDateTime.now();

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        ExecutionStatistics executionStatistics = new ExecutionStatistics();

        standardGeneticAlgorithm.finish(executionStatistics, 1);

        assertTrue(executionStatistics.getEndDateTime().compareTo(beforeFinish) >= 0);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testProceedWithNextGeneration() {
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        int initialPopulationSize = 100;
        int populationSize = 100;
        double mutationRate = 0.1;

        StandardPopulation populationMock = mock(StandardPopulation.class);

        List<Genome> individuals = new ArrayList<>();
        for (int i = 0; i < initialPopulationSize; i++) {
            individuals.add(new Genome(true, 0d, populationMock));
        }

        List<Parents> allParents = new ArrayList<>(initialPopulationSize);

        for (int i = 0; i < initialPopulationSize; i ++) {
            allParents.add(new Parents(new Genome(true, 0d, populationMock), new Genome(true, 0d, populationMock)));
        }

        when(populationMock.select()).thenReturn(allParents);
        when(populationMock.getIndividuals()).thenReturn(individuals);
        when(populationMock.removeIndividual(anyInt())).thenReturn(new Genome(true, 0d, populationMock));
        when(populationMock.size()).thenReturn(initialPopulationSize);

        MutationOperator mutationOperatorMock = mock(MutationOperator.class);
        CrossoverOperator crossoverOperatorMock = mock(CrossoverOperator.class);

        TaskExecutor taskExecutorMock = mock(TaskExecutor.class);
        doAnswer(invocation -> {
            ((FutureTask) invocation.getArguments()[0]).run();

            return null;
        }).when(taskExecutorMock).execute(any(FutureTask.class));

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutorMock)
                .population(populationMock)
                .populationSize(populationSize)
                .mutationRate(mutationRate)
                .mutationOperator(mutationOperatorMock)
                .crossoverOperator(crossoverOperatorMock)
                .elitism(0)
                .build();

        Genome genomeToReturn = new Genome(true, 0d, populationMock);
        when(crossoverOperatorMock.crossover(any(Genome.class), any(Genome.class))).thenReturn(genomeToReturn);

        ExecutionStatistics executionStatistics = new ExecutionStatistics();

        assertEquals(0, executionStatistics.getGenerationStatisticsList().size());

        standardGeneticAlgorithm.proceedWithNextGeneration(strategyToSet, executionStatistics, 0);

        assertEquals(1, executionStatistics.getGenerationStatisticsList().size());

        /*
         * The population size should be reduced by the number of parents used during crossover.
         */
        assertEquals(100, populationMock.size());

        verify(populationMock, times(1)).select();
        verify(populationMock, times(2)).size();
        verify(populationMock, never()).breed(anyInt());
        verify(populationMock, times(1)).evaluateFitness(any(GenerationStatistics.class));
        verify(populationMock, times(100)).addIndividual(any(Genome.class));
        verify(populationMock, times(1)).sortIndividuals();
        verify(populationMock, times(1)).clearIndividuals();
        verify(populationMock, never()).calculateEntropy();
        verifyNoMoreInteractions(populationMock);

        verify(mutationOperatorMock, times(100)).mutateChromosomes(any(Genome.class), same(strategyToSet));
        verifyNoMoreInteractions(mutationOperatorMock);

        verify(crossoverOperatorMock, times(100)).crossover(any(Genome.class), any(Genome.class));
        verifyNoMoreInteractions(crossoverOperatorMock);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testCrossover() {
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        int initialPopulationSize = 50;

        StandardPopulation populationMock = mock(StandardPopulation.class);
        when(populationMock.size()).thenReturn(initialPopulationSize);

        List<Genome> individuals = new ArrayList<>();
        for (int i = 0; i < initialPopulationSize; i++) {
            individuals.add(new Genome(true, 0d, populationMock));
        }

        TaskExecutor taskExecutorMock = mock(TaskExecutor.class);
        doAnswer(invocation -> {
            ((FutureTask) invocation.getArguments()[0]).run();

            return null;
        }).when(taskExecutorMock).execute(any(FutureTask.class));

        CrossoverOperator crossoverOperatorMock = mock(CrossoverOperator.class);

        Genome genomeToReturn = new Genome(true, 0d, populationMock);
        when(crossoverOperatorMock.crossover(any(Genome.class), any(Genome.class))).thenReturn(genomeToReturn);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutorMock)
                .population(populationMock)
                .crossoverOperator(crossoverOperatorMock)
                .elitism(0)
                .populationSize(initialPopulationSize)
                .build();

        List<Parents> allParents = new ArrayList<>();

        for (int i = 0; i < initialPopulationSize; i++) {
            allParents.add(new Parents(individuals.get(i), individuals.get(i)));
        }

        List<Genome> children = standardGeneticAlgorithm.crossover(strategy, allParents);

        assertEquals(50, children.size());

        verify(populationMock, times(1)).size();
        verifyNoMoreInteractions(populationMock);

        verify(crossoverOperatorMock, times(50)).crossover(any(Genome.class), any(Genome.class));
        verifyNoMoreInteractions(crossoverOperatorMock);
    }

    @SuppressWarnings({"rawtypes"})
    @Test
    public void testCrossover_SmallPopulation() {
        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        StandardPopulation population = new StandardPopulation();

        int initialPopulationSize = 10;

        Genome genome = new Genome(true, 0d, population);
        population.addIndividual(genome);

        CrossoverOperator crossoverOperatorMock = mock(CrossoverOperator.class);

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .crossoverOperator(crossoverOperatorMock)
                .population(population)
                .build();

        List<Parents> allParents = new ArrayList<>();

        for (int i = initialPopulationSize / 2; i < initialPopulationSize; i++) {
            allParents.add(new Parents(new Genome(true, 0d, population), new Genome(true, 0d, population)));
        }

        List<Genome> children = standardGeneticAlgorithm.crossover(strategyToSet, allParents);

        assertEquals(1, population.size());

        assertEquals(0, children.size());

        verifyNoInteractions(crossoverOperatorMock);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testMutate() {
        int initialPopulationSize = 100;

        StandardPopulation populationMock = mock(StandardPopulation.class);

        List<Genome> individuals = new ArrayList<>();
        for (int i = 0; i < initialPopulationSize; i++) {
            individuals.add(new Genome(true, 0d, populationMock));
        }

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        TaskExecutor taskExecutorMock = mock(TaskExecutor.class);
        doAnswer(invocation -> {
            ((FutureTask) invocation.getArguments()[0]).run();

            return null;
        }).when(taskExecutorMock).execute(any(FutureTask.class));

        double mutationRate = 0.5;

        MutationOperator mutationOperatorMock = mock(MutationOperator.class);

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutorMock)
                .population(populationMock)
                .mutationRate(mutationRate)
                .mutationOperator(mutationOperatorMock)
                .elitism(0)
                .build();

        standardGeneticAlgorithm.mutate(strategyToSet, individuals);

        verify(populationMock, times(1)).sortIndividuals();
        verifyNoMoreInteractions(populationMock);

        verify(mutationOperatorMock, times(100)).mutateChromosomes(any(Genome.class), same(strategyToSet));
        verifyNoMoreInteractions(mutationOperatorMock);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testMutate_SmallPopulation() {
        int initialPopulationSize = 100;

        StandardPopulation populationMock = mock(StandardPopulation.class);

        List<Genome> individuals = new ArrayList<>();
        for (int i = 0; i < initialPopulationSize; i++) {
            individuals.add(new Genome(true, 0d, populationMock));
        }

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        TaskExecutor taskExecutorMock = mock(TaskExecutor.class);
        doAnswer(invocation -> {
            ((FutureTask) invocation.getArguments()[0]).run();

            return null;
        }).when(taskExecutorMock).execute(any(FutureTask.class));

        double mutationRate = 0.5;

        MutationOperator mutationOperatorMock = mock(MutationOperator.class);

        GeneticAlgorithmStrategy strategyToSet = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutorMock)
                .population(populationMock)
                .mutationRate(mutationRate)
                .mutationOperator(mutationOperatorMock)
                .elitism(0)
                .build();

        standardGeneticAlgorithm.mutate(strategyToSet, individuals);

        verify(populationMock, times(1)).sortIndividuals();
        verifyNoMoreInteractions(populationMock);

        verify(mutationOperatorMock, times(initialPopulationSize)).mutateChromosomes(any(Genome.class), same(strategyToSet));
        verifyNoMoreInteractions(mutationOperatorMock);
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
        List<Genome> individuals = new ArrayList<>();
        individuals.add(new Genome(true, 0d, populationMock));
        when(populationMock.getIndividuals()).thenReturn(individuals);

        StandardGeneticAlgorithm standardGeneticAlgorithm = new StandardGeneticAlgorithm();

        standardGeneticAlgorithm.spawnInitialPopulation(strategyToSet);

        verify(populationMock, times(1)).clearIndividuals();
        verify(populationMock, times(1)).breed(eq(populationSize));
        verify(populationMock, times(1)).evaluateFitness(any(GenerationStatistics.class));
        verify(populationMock, times(1)).size();
        verify(populationMock, never()).calculateEntropy();
        verifyNoMoreInteractions(populationMock);
    }
}
