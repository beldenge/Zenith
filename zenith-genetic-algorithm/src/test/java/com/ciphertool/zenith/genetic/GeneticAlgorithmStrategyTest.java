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
import com.ciphertool.zenith.genetic.algorithms.crossover.impl.EqualOpportunityGeneCrossoverAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.mutation.MutationAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.mutation.impl.MultipleMutationAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.selection.modes.RouletteSelector;
import com.ciphertool.zenith.genetic.algorithms.selection.modes.Selector;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeneticAlgorithmStrategyTest {
	private static final Double DEFAULT_FITNESS_VALUE = 100.0d;

	@SuppressWarnings("rawtypes")
	@Test
	public void testConstructorWithoutComparisonToKnownSolution() {
		Object geneticStructureToSet = new Object();
		Integer populationSizeToSet = new Integer(500);
		Integer maxGenerationsToSet = new Integer(1000);
		Double mutationRateToSet = new Double(0.05);
		Integer maxMutationsPerIndividualToSet = new Integer(5);
		FitnessEvaluator fitnessEvaluatorToSet = mock(FitnessEvaluator.class);
		when(fitnessEvaluatorToSet.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
		CrossoverAlgorithm crossoverAlgorithmToSet = new EqualOpportunityGeneCrossoverAlgorithm();
		MutationAlgorithm mutationAlgorithmToSet = new MultipleMutationAlgorithm();
		Selector selectorToSet = new RouletteSelector();

		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy(geneticStructureToSet,
				populationSizeToSet, maxGenerationsToSet, mutationRateToSet, maxMutationsPerIndividualToSet,
				fitnessEvaluatorToSet, crossoverAlgorithmToSet, mutationAlgorithmToSet, selectorToSet, null, false);

		assertSame(geneticStructureToSet, geneticAlgorithmStrategy.getGeneticStructure());
		assertSame(populationSizeToSet, geneticAlgorithmStrategy.getPopulationSize());
		assertSame(maxGenerationsToSet, geneticAlgorithmStrategy.getMaxGenerations());
		assertSame(mutationRateToSet, geneticAlgorithmStrategy.getMutationRate());
		assertSame(maxMutationsPerIndividualToSet, geneticAlgorithmStrategy.getMaxMutationsPerIndividual());
		assertSame(fitnessEvaluatorToSet, geneticAlgorithmStrategy.getFitnessEvaluator());
		assertSame(crossoverAlgorithmToSet, geneticAlgorithmStrategy.getCrossoverAlgorithm());
		assertSame(mutationAlgorithmToSet, geneticAlgorithmStrategy.getMutationAlgorithm());
		assertSame(selectorToSet, geneticAlgorithmStrategy.getSelector());
		assertNull(geneticAlgorithmStrategy.getKnownSolutionFitnessEvaluator());
		assertFalse(geneticAlgorithmStrategy.getCompareToKnownSolution());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testConstructorWithComparisonToKnownSolution() {
		Object geneticStructureToSet = new Object();
		Integer populationSizeToSet = new Integer(500);
		Integer maxGenerationsToSet = new Integer(1000);
		Double mutationRateToSet = new Double(0.05);
		Integer maxMutationsPerIndividualToSet = new Integer(5);
		FitnessEvaluator fitnessEvaluatorToSet = mock(FitnessEvaluator.class);
		when(fitnessEvaluatorToSet.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
		CrossoverAlgorithm crossoverAlgorithmToSet = new EqualOpportunityGeneCrossoverAlgorithm();
		MutationAlgorithm mutationAlgorithmToSet = new MultipleMutationAlgorithm();
		Selector selectorToSet = new RouletteSelector();
		FitnessEvaluator knownSolutionFitnessEvaluatorToSet = mock(FitnessEvaluator.class);
		when(knownSolutionFitnessEvaluatorToSet.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
		Boolean compareToKnownSolutionToSet = new Boolean(true);

		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy(geneticStructureToSet,
				populationSizeToSet, maxGenerationsToSet, mutationRateToSet, maxMutationsPerIndividualToSet,
				fitnessEvaluatorToSet, crossoverAlgorithmToSet, mutationAlgorithmToSet, selectorToSet,
				knownSolutionFitnessEvaluatorToSet, compareToKnownSolutionToSet);

		assertSame(geneticStructureToSet, geneticAlgorithmStrategy.getGeneticStructure());
		assertSame(populationSizeToSet, geneticAlgorithmStrategy.getPopulationSize());
		assertSame(maxGenerationsToSet, geneticAlgorithmStrategy.getMaxGenerations());
		assertSame(mutationRateToSet, geneticAlgorithmStrategy.getMutationRate());
		assertSame(maxMutationsPerIndividualToSet, geneticAlgorithmStrategy.getMaxMutationsPerIndividual());
		assertSame(fitnessEvaluatorToSet, geneticAlgorithmStrategy.getFitnessEvaluator());
		assertSame(crossoverAlgorithmToSet, geneticAlgorithmStrategy.getCrossoverAlgorithm());
		assertSame(mutationAlgorithmToSet, geneticAlgorithmStrategy.getMutationAlgorithm());
		assertSame(selectorToSet, geneticAlgorithmStrategy.getSelector());
		assertSame(knownSolutionFitnessEvaluatorToSet, geneticAlgorithmStrategy.getKnownSolutionFitnessEvaluator());
		assertSame(compareToKnownSolutionToSet, geneticAlgorithmStrategy.getCompareToKnownSolution());
	}

	@Test
	public void testSetGeneticStructure() {
		Object geneticStructureToSet = new Object();
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setGeneticStructure(geneticStructureToSet);

		assertSame(geneticStructureToSet, geneticAlgorithmStrategy.getGeneticStructure());
	}

	@Test
	public void testSetPopulationSize() {
		Integer populationSizeToSet = new Integer(500);
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setPopulationSize(populationSizeToSet);

		assertSame(populationSizeToSet, geneticAlgorithmStrategy.getPopulationSize());
	}

	@Test
	public void testSetMutationRate() {
		Double mutationRateToSet = new Double(0.05);
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setMutationRate(mutationRateToSet);

		assertSame(mutationRateToSet, geneticAlgorithmStrategy.getMutationRate());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetMutationRateInvalid() {
		Double mutationRateToSet = new Double(1.05);
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setMutationRate(mutationRateToSet);

		assertSame(mutationRateToSet, geneticAlgorithmStrategy.getMutationRate());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetMutationRateNegative() {
		Double mutationRateToSet = new Double(-0.05);
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setMutationRate(mutationRateToSet);

		assertSame(mutationRateToSet, geneticAlgorithmStrategy.getMutationRate());
	}

	@Test
	public void testSetMaxMutationsPerIndividual() {
		Integer maxMutationsPerIndividualToSet = new Integer(5);
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setMaxMutationsPerIndividual(maxMutationsPerIndividualToSet);

		assertSame(maxMutationsPerIndividualToSet, geneticAlgorithmStrategy.getMaxMutationsPerIndividual());
	}

	@Test
	public void testSetMaxGenerations() {
		Integer maxGenerationsToSet = new Integer(1000);
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setMaxGenerations(maxGenerationsToSet);

		assertSame(maxGenerationsToSet, geneticAlgorithmStrategy.getMaxGenerations());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testSetCrossoverAlgorithm() {
		CrossoverAlgorithm crossoverAlgorithmToSet = new EqualOpportunityGeneCrossoverAlgorithm();
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setCrossoverAlgorithm(crossoverAlgorithmToSet);

		assertSame(crossoverAlgorithmToSet, geneticAlgorithmStrategy.getCrossoverAlgorithm());
	}

	@Test
	public void testSetFitnessEvaluator() {
		FitnessEvaluator fitnessEvaluatorToSet = mock(FitnessEvaluator.class);
		when(fitnessEvaluatorToSet.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setFitnessEvaluator(fitnessEvaluatorToSet);

		assertSame(fitnessEvaluatorToSet, geneticAlgorithmStrategy.getFitnessEvaluator());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testSetMutationAlgorithm() {
		MutationAlgorithm mutationAlgorithmToSet = new MultipleMutationAlgorithm();
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setMutationAlgorithm(mutationAlgorithmToSet);

		assertSame(mutationAlgorithmToSet, geneticAlgorithmStrategy.getMutationAlgorithm());
	}

	@Test
	public void testSetSelector() {
		Selector selectorToSet = new RouletteSelector();
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setSelector(selectorToSet);

		assertSame(selectorToSet, geneticAlgorithmStrategy.getSelector());
	}

	@Test
	public void testSetKnownSolutionFitnessEvaluator() {
		FitnessEvaluator knownSolutionFitnessEvaluatorToSet = mock(FitnessEvaluator.class);
		when(knownSolutionFitnessEvaluatorToSet.evaluate(any(Chromosome.class))).thenReturn(DEFAULT_FITNESS_VALUE);
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setKnownSolutionFitnessEvaluator(knownSolutionFitnessEvaluatorToSet);

		assertSame(knownSolutionFitnessEvaluatorToSet, geneticAlgorithmStrategy.getKnownSolutionFitnessEvaluator());
	}

	@Test
	public void testSetCompareToKnownSolution() {
		Boolean compareToKnownSolutionToSet = new Boolean(true);
		GeneticAlgorithmStrategy geneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticAlgorithmStrategy.setCompareToKnownSolution(compareToKnownSolutionToSet);

		assertSame(compareToKnownSolutionToSet, geneticAlgorithmStrategy.getCompareToKnownSolution());
	}
}
