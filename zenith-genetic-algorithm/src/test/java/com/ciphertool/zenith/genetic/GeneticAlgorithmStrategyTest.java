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

package com.ciphertool.zenith.genetic;

import com.ciphertool.zenith.genetic.operators.crossover.CrossoverOperator;
import com.ciphertool.zenith.genetic.operators.mutation.MutationOperator;
import com.ciphertool.zenith.genetic.operators.selection.Selector;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.population.Population;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class GeneticAlgorithmStrategyTest {
    @Test
    public void given_builderValues_when_buildingStrategy_then_returnsExpected() {
        TaskExecutor taskExecutor = mock(TaskExecutor.class);
        Population population = mock(Population.class);
        CrossoverOperator crossoverOperator = mock(CrossoverOperator.class);
        MutationOperator mutationOperator = mock(MutationOperator.class);
        FitnessEvaluator fitnessEvaluator = mock(FitnessEvaluator.class);
        Breeder breeder = mock(Breeder.class);
        Selector selector = mock(Selector.class);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .taskExecutor(taskExecutor)
                .populationSize(10)
                .numberOfGenerations(5)
                .elitism(1)
                .population(population)
                .latticeRows(3)
                .latticeColumns(4)
                .latticeWrapAround(true)
                .latticeRadius(2)
                .crossoverOperator(crossoverOperator)
                .fitnessEvaluator(fitnessEvaluator)
                .mutationOperator(mutationOperator)
                .mutationRate(0.1d)
                .maxMutationsPerIndividual(2)
                .breeder(breeder)
                .selector(selector)
                .tournamentSelectorAccuracy(0.9d)
                .tournamentSize(3)
                .minPopulations(1)
                .speciationEvents(2)
                .speciationFactor(3)
                .extinctionCycles(4)
                .build();

        assertEquals(taskExecutor, strategy.getTaskExecutor());
        assertEquals(10, strategy.getPopulationSize());
        assertEquals(5, strategy.getNumberOfGenerations());
        assertEquals(1, strategy.getElitism());
        assertEquals(population, strategy.getPopulation());
        assertEquals(3, strategy.getLatticeRows());
        assertEquals(4, strategy.getLatticeColumns());
        assertEquals(true, strategy.getLatticeWrapAround());
        assertEquals(2, strategy.getLatticeRadius());
        assertEquals(crossoverOperator, strategy.getCrossoverOperator());
        assertEquals(fitnessEvaluator, strategy.getFitnessEvaluator());
        assertEquals(mutationOperator, strategy.getMutationOperator());
        assertEquals(0.1d, strategy.getMutationRate());
        assertEquals(2, strategy.getMaxMutationsPerIndividual());
        assertEquals(breeder, strategy.getBreeder());
        assertEquals(selector, strategy.getSelector());
        assertEquals(0.9d, strategy.getTournamentSelectorAccuracy());
        assertEquals(3, strategy.getTournamentSize());
        assertEquals(1, strategy.getMinPopulations());
        assertEquals(2, strategy.getSpeciationEvents());
        assertEquals(3, strategy.getSpeciationFactor());
        assertEquals(4, strategy.getExtinctionCycles());

        Population newPopulation = mock(Population.class);
        strategy.setPopulation(newPopulation);
        assertEquals(newPopulation, strategy.getPopulation());
    }
}