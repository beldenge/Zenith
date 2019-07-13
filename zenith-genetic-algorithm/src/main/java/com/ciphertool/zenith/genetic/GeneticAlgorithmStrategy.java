/**
 * Copyright 2017-2019 George Belden
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

import com.ciphertool.zenith.genetic.algorithms.crossover.CrossoverAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.mutation.MutationAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.selection.Selector;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeneticAlgorithmStrategy {
    private Integer populationSize;
    private Double mutationRate;
    private Integer maxMutationsPerIndividual;
    private Integer maxGenerations;
    private Integer elitism;
    private CrossoverAlgorithm crossoverAlgorithm;
    private FitnessEvaluator fitnessEvaluator;
    private MutationAlgorithm mutationAlgorithm;
    private Breeder breeder;
    private Selector selector;
}
