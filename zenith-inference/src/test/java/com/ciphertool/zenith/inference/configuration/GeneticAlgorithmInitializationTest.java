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

package com.ciphertool.zenith.inference.configuration;

import com.ciphertool.zenith.genetic.Breeder;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.operators.crossover.CrossoverOperator;
import com.ciphertool.zenith.genetic.operators.mutation.MutationOperator;
import com.ciphertool.zenith.genetic.operators.selection.Selector;
import com.ciphertool.zenith.genetic.population.Population;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class GeneticAlgorithmInitializationTest {
    @Test
    public void given_validInput_when_builder_then_returnsExpectedValue() {
        Population population = mock(Population.class);
        Breeder breeder = mock(Breeder.class);
        CrossoverOperator crossoverOperator = mock(CrossoverOperator.class);
        MutationOperator mutationOperator = mock(MutationOperator.class);
        Selector selector = mock(Selector.class);
        FitnessEvaluator fitnessEvaluator = mock(FitnessEvaluator.class);

        GeneticAlgorithmInitialization initialization = GeneticAlgorithmInitialization.builder()
                .population(population)
                .breeder(breeder)
                .crossoverOperator(crossoverOperator)
                .mutationOperator(mutationOperator)
                .selector(selector)
                .fitnessEvaluator(fitnessEvaluator)
                .build();

        assertEquals(population, initialization.getPopulation());
        assertEquals(breeder, initialization.getBreeder());
        assertEquals(crossoverOperator, initialization.getCrossoverOperator());
        assertEquals(mutationOperator, initialization.getMutationOperator());
        assertEquals(selector, initialization.getSelector());
        assertEquals(fitnessEvaluator, initialization.getFitnessEvaluator());
    }
}