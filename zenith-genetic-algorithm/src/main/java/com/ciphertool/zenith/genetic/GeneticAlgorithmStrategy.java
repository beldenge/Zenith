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

package com.ciphertool.zenith.genetic;

import com.ciphertool.zenith.genetic.operators.crossover.CrossoverOperator;
import com.ciphertool.zenith.genetic.operators.mutation.MutationOperator;
import com.ciphertool.zenith.genetic.operators.selection.Selector;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.population.Population;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.task.TaskExecutor;

@Getter
@Builder
public class GeneticAlgorithmStrategy {
    private TaskExecutor taskExecutor;
    private Integer populationSize;
    private Integer numberOfGenerations;
    private Integer elitism;
    @Setter
    private Population population;
    private Integer latticeRows;
    private Integer latticeColumns;
    private Boolean latticeWrapAround;
    private Integer latticeRadius;
    private CrossoverOperator crossoverOperator;
    private FitnessEvaluator fitnessEvaluator;
    private MutationOperator mutationOperator;
    private Double mutationRate;
    private Integer maxMutationsPerIndividual;
    private Breeder breeder;
    private Selector selector;
    private Double tournamentSelectorAccuracy;
    private Integer tournamentSize;
    private Integer minPopulations;
    private Integer speciationEvents;
    private Integer speciationFactor;
    private Integer extinctionCycles;
}
