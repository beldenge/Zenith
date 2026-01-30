/*
 * Copyright 2017-2026 George Belden
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

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.operators.crossover.CrossoverOperator;
import com.ciphertool.zenith.genetic.operators.mutation.MutationOperator;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ExecutionStatisticsTest {
    @Test
    public void given_validInput_when_constructing_then_returnsSameInstance() {
        LocalDateTime startDateToSet = LocalDateTime.now();
        GeneticAlgorithmStrategy strategy = createGeneticAlgorithmStrategy();

        ExecutionStatistics executionStatistics = new ExecutionStatistics(startDateToSet, strategy);

        assertSame(startDateToSet, executionStatistics.getStartDateTime());
        assertSame(strategy.getPopulationSize(), executionStatistics.getPopulationSize());
        assertSame(strategy.getMutationRate(), executionStatistics.getMutationRate());
        assertEquals(strategy.getCrossoverOperator().getClass().getSimpleName(), executionStatistics.getCrossoverOperator());
        assertEquals(strategy.getFitnessEvaluator().getClass().getSimpleName(), executionStatistics.getFitnessEvaluator());
        assertEquals(strategy.getMutationOperator().getClass().getSimpleName(), executionStatistics.getMutationOperator());
    }

    @Test
    public void given_validInput_when_settingStartDate_then_returnsSameInstance() {
        LocalDateTime startDateToSet = LocalDateTime.now();
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        executionStatistics.setStartDateTime(startDateToSet);

        assertSame(startDateToSet, executionStatistics.getStartDateTime());
    }

    @Test
    public void given_validInput_when_settingEndDate_then_returnsSameInstance() {
        LocalDateTime endDateToSet = LocalDateTime.now();
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        executionStatistics.setEndDateTime(endDateToSet);

        assertSame(endDateToSet, executionStatistics.getEndDateTime());
    }

    @Test
    public void given_validInput_when_settingPopulationSize_then_returnsSameInstance() {
        Integer populationSizeToSet = 1000;
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        executionStatistics.setPopulationSize(populationSizeToSet);

        assertSame(populationSizeToSet, executionStatistics.getPopulationSize());
    }

    @Test
    public void given_validInput_when_settingMutationRate_then_returnsSameInstance() {
        Double mutationRateToSet = 0.05;
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        executionStatistics.setMutationRate(mutationRateToSet);

        assertSame(mutationRateToSet, executionStatistics.getMutationRate());
    }

    @Test
    public void given_validInput_when_settingCrossoverOperator_then_returnsSameInstance() {
        String crossoverOperatorToSet = mock(CrossoverOperator.class).getClass().getSimpleName();
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        executionStatistics.setCrossoverOperator(crossoverOperatorToSet);

        assertSame(crossoverOperatorToSet, executionStatistics.getCrossoverOperator());
    }

    @Test
    public void given_validInput_when_settingFitnessEvaluator_then_returnsSameInstance() {
        String fitnessEvaluatorToSet = mock(FitnessEvaluator.class).getClass().getSimpleName();
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        executionStatistics.setFitnessEvaluator(fitnessEvaluatorToSet);

        assertSame(fitnessEvaluatorToSet, executionStatistics.getFitnessEvaluator());
    }

    @Test
    public void given_validInput_when_settingMutationOperator_then_returnsSameInstance() {
        String mutationOperatorToSet = mock(MutationOperator.class).getClass().getSimpleName();
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        executionStatistics.setMutationOperator(mutationOperatorToSet);

        assertSame(mutationOperatorToSet, executionStatistics.getMutationOperator());
    }

    @Test
    public void given_validInput_when_generationStatisticsListUnmodifiable_then_throwsUnsupportedOperationException() {
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        executionStatistics.addGenerationStatistics(new GenerationStatistics());
        executionStatistics.addGenerationStatistics(new GenerationStatistics());
        executionStatistics.addGenerationStatistics(new GenerationStatistics());

        List<GenerationStatistics> generationStatisticsList = executionStatistics.getGenerationStatisticsList();

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            generationStatisticsList.remove(0); // should throw exception
        });
    }

    @Test
    public void given_nullInput_when_gettingNullGenerationStatisticsList_then_returnsNotNull() {
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        assertNotNull(executionStatistics.getGenerationStatisticsList());
    }

    @Test
    public void given_validInput_when_addingGenerationStatistics_then_returnsSameInstance() {
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        assertEquals(0, executionStatistics.getGenerationStatisticsList().size());

        GenerationStatistics generationStatistics1 = new GenerationStatistics();
        executionStatistics.addGenerationStatistics(generationStatistics1);
        GenerationStatistics generationStatistics2 = new GenerationStatistics();
        executionStatistics.addGenerationStatistics(generationStatistics2);
        GenerationStatistics generationStatistics3 = new GenerationStatistics();
        executionStatistics.addGenerationStatistics(generationStatistics3);

        assertEquals(3, executionStatistics.getGenerationStatisticsList().size());
        assertSame(generationStatistics1, executionStatistics.getGenerationStatisticsList().get(0));
        assertSame(generationStatistics2, executionStatistics.getGenerationStatisticsList().get(1));
        assertSame(generationStatistics3, executionStatistics.getGenerationStatisticsList().get(2));
    }

    @Test
    public void given_validInput_when_removingGenerationStatistics_then_returnsSameInstance() {
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        assertEquals(0, executionStatistics.getGenerationStatisticsList().size());

        GenerationStatistics generationStatistics1 = new GenerationStatistics(0);
        executionStatistics.addGenerationStatistics(generationStatistics1);
        GenerationStatistics generationStatistics2 = new GenerationStatistics(1);
        executionStatistics.addGenerationStatistics(generationStatistics2);
        GenerationStatistics generationStatistics3 = new GenerationStatistics(2);
        executionStatistics.addGenerationStatistics(generationStatistics3);

        assertEquals(3, executionStatistics.getGenerationStatisticsList().size());

        executionStatistics.removeGenerationStatistics(generationStatistics2);

        assertEquals(2, executionStatistics.getGenerationStatisticsList().size());
        assertSame(generationStatistics1, executionStatistics.getGenerationStatisticsList().get(0));
        assertSame(generationStatistics3, executionStatistics.getGenerationStatisticsList().get(1));
    }

    @Test
    public void given_validInput_when_equals_then_comparesAsExpected() {
        LocalDateTime baseStartDate = LocalDateTime.now();
        GeneticAlgorithmStrategy baseStrategy = createGeneticAlgorithmStrategy();

        ExecutionStatistics base = new ExecutionStatistics(baseStartDate, baseStrategy);

        ExecutionStatistics executionStatisticsEqualToBase = new ExecutionStatistics(baseStartDate, baseStrategy);
        assertEquals(base, executionStatisticsEqualToBase);

        LocalDateTime differentStartDate = LocalDateTime.now().minusYears(1);
        ExecutionStatistics executionStatisticsWithDifferentStartDate = new ExecutionStatistics(differentStartDate,
                baseStrategy);
        assertFalse(base.equals(executionStatisticsWithDifferentStartDate));

        ExecutionStatistics executionStatisticsWithDifferentPopulationSize = new ExecutionStatistics(baseStartDate,
                baseStrategy);
        executionStatisticsWithDifferentPopulationSize.setPopulationSize(999);
        assertFalse(base.equals(executionStatisticsWithDifferentPopulationSize));

        ExecutionStatistics executionStatisticsWithDifferentMutationRate = new ExecutionStatistics(baseStartDate,
                baseStrategy);
        executionStatisticsWithDifferentMutationRate.setMutationRate(1.0);
        assertFalse(base.equals(executionStatisticsWithDifferentMutationRate));

        ExecutionStatistics executionStatisticsWithDifferentCrossoverOperator = new ExecutionStatistics(baseStartDate,
                baseStrategy);
        executionStatisticsWithDifferentCrossoverOperator.setCrossoverOperator("differentCrossoverOperator");
        assertFalse(base.equals(executionStatisticsWithDifferentCrossoverOperator));

        ExecutionStatistics executionStatisticsWithDifferentFitnessEvaluator = new ExecutionStatistics(baseStartDate,
                baseStrategy);
        executionStatisticsWithDifferentFitnessEvaluator.setFitnessEvaluator("differentFitnessEvaluator");
        assertFalse(base.equals(executionStatisticsWithDifferentFitnessEvaluator));

        ExecutionStatistics executionStatisticsWithDifferentMutationOperator = new ExecutionStatistics(baseStartDate,
                baseStrategy);
        executionStatisticsWithDifferentMutationOperator.setMutationOperator("differentMutationOperator");
        assertFalse(base.equals(executionStatisticsWithDifferentMutationOperator));

        ExecutionStatistics executionStatisticsWithNullPropertiesA = new ExecutionStatistics();
        ExecutionStatistics executionStatisticsWithNullPropertiesB = new ExecutionStatistics();
        assertEquals(executionStatisticsWithNullPropertiesA, executionStatisticsWithNullPropertiesB);
    }

    @SuppressWarnings("rawtypes")
    private static GeneticAlgorithmStrategy createGeneticAlgorithmStrategy() {
        Integer populationSizeToSet = 1000;
        Double mutationRateToSet = 0.05;
        CrossoverOperator crossoverOperatorToSet = mock(CrossoverOperator.class);
        FitnessEvaluator fitnessEvaluatorToSet = mock(FitnessEvaluator.class);
        MutationOperator mutationOperatorToSet = mock(MutationOperator.class);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .populationSize(populationSizeToSet)
                .mutationRate(mutationRateToSet)
                .crossoverOperator(crossoverOperatorToSet)
                .fitnessEvaluator(fitnessEvaluatorToSet)
                .mutationOperator(mutationOperatorToSet)
                .build();

        return strategy;
    }
}
