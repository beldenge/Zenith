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
 import com.ciphertool.zenith.genetic.algorithms.crossover.EvaluatedCrossoverAlgorithm;
 import com.ciphertool.zenith.genetic.algorithms.mutation.EvaluatedMutationAlgorithm;
 import com.ciphertool.zenith.genetic.algorithms.mutation.MutationAlgorithm;
 import com.ciphertool.zenith.genetic.algorithms.selection.modes.Selector;
 import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;

 public class GeneticAlgorithmStrategy {
	private Object				geneticStructure;
	private Integer				populationSize;
	private Double				mutationRate;
	private Integer				maxMutationsPerIndividual;
	private Integer				maxGenerations;
	@SuppressWarnings("rawtypes")
	private CrossoverAlgorithm	crossoverAlgorithm;
	private FitnessEvaluator	fitnessEvaluator;
	@SuppressWarnings("rawtypes")
	private MutationAlgorithm	mutationAlgorithm;
	private Selector			selector;
	private FitnessEvaluator	knownSolutionFitnessEvaluator;
	private Boolean				compareToKnownSolution;

	/**
	 * Default no-args constructor
	 */
	public GeneticAlgorithmStrategy() {
	}

	/**
	 * Full-args constructor
	 * 
	 * @param geneticStructure
	 *            the geneticStructure to set
	 * @param populationSize
	 *            the populationSize to set
	 * @param maxGenerations
	 *            the maxGenerations to set
	 * @param mutationRate
	 *            the mutationRate to set
	 * @param maxMutationsPerIndividual
	 *            the maxMutationsPerIndividual to set
	 * @param fitnessEvaluator
	 *            the fitnessEvaluator to set
	 * @param crossoverAlgorithm
	 *            the crossoverAlgorithm to set
	 * @param mutationAlgorithm
	 *            the mutationAlgorithm to set
	 * @param selector
	 *            the selector to set
	 * @param knownSolutionFitnessEvaluator
	 *            the knownSolutionFitnessEvaluator to set
	 * @param compareToKnownSolution
	 *            the compareToKnownSolution to set
	 */
	@SuppressWarnings("rawtypes")
	public GeneticAlgorithmStrategy(Object geneticStructure, Integer populationSize, Integer maxGenerations,
			Double mutationRate, Integer maxMutationsPerIndividual, FitnessEvaluator fitnessEvaluator,
			CrossoverAlgorithm crossoverAlgorithm, MutationAlgorithm mutationAlgorithm, Selector selector,
			FitnessEvaluator knownSolutionFitnessEvaluator, Boolean compareToKnownSolution) {
		this.geneticStructure = geneticStructure;
		this.populationSize = populationSize;
		this.maxGenerations = maxGenerations;

		this.setMutationRate(mutationRate);
		this.maxMutationsPerIndividual = maxMutationsPerIndividual;

		this.fitnessEvaluator = fitnessEvaluator;
		this.fitnessEvaluator.setGeneticStructure(geneticStructure);

		this.crossoverAlgorithm = crossoverAlgorithm;

		if (crossoverAlgorithm instanceof EvaluatedCrossoverAlgorithm) {
			((EvaluatedCrossoverAlgorithm) this.crossoverAlgorithm).setFitnessEvaluator(this.fitnessEvaluator);
		}

		this.mutationAlgorithm = mutationAlgorithm;

		if (mutationAlgorithm instanceof EvaluatedMutationAlgorithm) {
			((EvaluatedMutationAlgorithm) this.mutationAlgorithm).setFitnessEvaluator(this.fitnessEvaluator);
		}

		this.selector = selector;

		this.knownSolutionFitnessEvaluator = knownSolutionFitnessEvaluator;
		if (knownSolutionFitnessEvaluator != null) {
			this.knownSolutionFitnessEvaluator.setGeneticStructure(geneticStructure);
		}
		this.compareToKnownSolution = compareToKnownSolution;
	}

	/**
	 * @return the geneticStructure
	 */
	public Object getGeneticStructure() {
		return geneticStructure;
	}

	/**
	 * @param geneticStructure
	 *            the geneticStructure to set
	 */
	public void setGeneticStructure(Object geneticStructure) {
		this.geneticStructure = geneticStructure;
	}

	/**
	 * @return the populationSize
	 */
	public Integer getPopulationSize() {
		return populationSize;
	}

	/**
	 * @param populationSize
	 *            the populationSize to set
	 */
	public void setPopulationSize(Integer populationSize) {
		this.populationSize = populationSize;
	}

	/**
	 * @return the mutationRate
	 */
	public Double getMutationRate() {
		return mutationRate;
	}

	/**
	 * @param mutationRate
	 *            the mutationRate to set
	 */
	public void setMutationRate(Double mutationRate) {
		if (mutationRate == null || mutationRate < 0.0 || mutationRate > 1.0) {
			throw new IllegalArgumentException("Tried to set a mutationRate of " + mutationRate
					+ ", but GeneicAlgorithmStrategy requires a mutationRate between 0.0 and 1.0 inclusive.");
		}

		this.mutationRate = mutationRate;
	}

	/**
	 * @return the maxMutationsPerIndividual
	 */
	public Integer getMaxMutationsPerIndividual() {
		return maxMutationsPerIndividual;
	}

	/**
	 * @param maxMutationsPerIndividual
	 *            the maxMutationsPerIndividual to set
	 */
	public void setMaxMutationsPerIndividual(Integer maxMutationsPerIndividual) {
		this.maxMutationsPerIndividual = maxMutationsPerIndividual;
	}

	/**
	 * @return the maxGenerations
	 */
	public Integer getMaxGenerations() {
		return maxGenerations;
	}

	/**
	 * @param maxGenerations
	 *            the maxGenerations to set
	 */
	public void setMaxGenerations(Integer maxGenerations) {
		this.maxGenerations = maxGenerations;
	}

	/**
	 * @return the crossoverAlgorithm
	 */
	@SuppressWarnings("rawtypes")
	public CrossoverAlgorithm getCrossoverAlgorithm() {
		return crossoverAlgorithm;
	}

	/**
	 * @param crossoverAlgorithm
	 *            the crossoverAlgorithm to set
	 */
	@SuppressWarnings("rawtypes")
	public void setCrossoverAlgorithm(CrossoverAlgorithm crossoverAlgorithm) {
		this.crossoverAlgorithm = crossoverAlgorithm;
	}

	/**
	 * @return the fitnessEvaluator
	 */
	public FitnessEvaluator getFitnessEvaluator() {
		return fitnessEvaluator;
	}

	/**
	 * @param fitnessEvaluator
	 *            the fitnessEvaluator to set
	 */
	public void setFitnessEvaluator(FitnessEvaluator fitnessEvaluator) {
		this.fitnessEvaluator = fitnessEvaluator;
	}

	/**
	 * @return the mutationAlgorithm
	 */
	@SuppressWarnings("rawtypes")
	public MutationAlgorithm getMutationAlgorithm() {
		return mutationAlgorithm;
	}

	/**
	 * @param mutationAlgorithm
	 *            the mutationAlgorithm to set
	 */
	@SuppressWarnings("rawtypes")
	public void setMutationAlgorithm(MutationAlgorithm mutationAlgorithm) {
		this.mutationAlgorithm = mutationAlgorithm;
	}

	/**
	 * @return the selector
	 */
	public Selector getSelector() {
		return selector;
	}

	/**
	 * @param selector
	 *            the selector to set
	 */
	public void setSelector(Selector selector) {
		this.selector = selector;
	}

	/**
	 * @return the knownSolutionFitnessEvaluator
	 */
	public FitnessEvaluator getKnownSolutionFitnessEvaluator() {
		return knownSolutionFitnessEvaluator;
	}

	/**
	 * @param knownSolutionFitnessEvaluator
	 *            the knownSolutionFitnessEvaluator to set
	 */
	public void setKnownSolutionFitnessEvaluator(FitnessEvaluator knownSolutionFitnessEvaluator) {
		this.knownSolutionFitnessEvaluator = knownSolutionFitnessEvaluator;
	}

	/**
	 * @return the compareToKnownSolution
	 */
	public Boolean getCompareToKnownSolution() {
		return compareToKnownSolution;
	}

	/**
	 * @param compareToKnownSolution
	 *            the compareToKnownSolution to set
	 */
	public void setCompareToKnownSolution(Boolean compareToKnownSolution) {
		this.compareToKnownSolution = compareToKnownSolution;
	}
}
