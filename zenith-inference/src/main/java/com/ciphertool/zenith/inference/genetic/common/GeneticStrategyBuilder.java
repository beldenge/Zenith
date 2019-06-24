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

package com.ciphertool.zenith.inference.genetic.common;

 import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
 import com.ciphertool.zenith.genetic.algorithms.crossover.CrossoverAlgorithm;
 import com.ciphertool.zenith.genetic.algorithms.mutation.MutationAlgorithm;
 import com.ciphertool.zenith.genetic.algorithms.selection.modes.Selector;
 import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
 import com.ciphertool.zenith.inference.dao.CipherDao;
 import com.ciphertool.zenith.inference.entities.Cipher;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;

 import java.util.Map;

public class GeneticStrategyBuilder implements StrategyBuilder {
	private Logger				log	= LoggerFactory.getLogger(getClass());

	private CipherDao cipherDao;
	private FitnessEvaluator knownSolutionFitnessEvaluator;

	@SuppressWarnings("rawtypes")
	@Override
	public GeneticAlgorithmStrategy buildStrategy(Map<String, Object> parameters) {
		Cipher cipher = getCipher(parameters);
		log.info("Cipher: " + cipher.getName());

		FitnessEvaluator fitnessEvaluator = getFitnessEvaluator(parameters);
		log.info("FitnessEvaluator implementation: " + fitnessEvaluator.getClass());

		CrossoverAlgorithm crossoverAlgorithm = getCrossoverAlgorithm(parameters);
		log.info("CrossoverAlgorithm implementation: " + crossoverAlgorithm.getClass());

		MutationAlgorithm mutationAlgorithm = getMutationAlgorithm(parameters);
		log.info("MutationAlgorithm implementation: " + mutationAlgorithm.getClass());

		Selector selector = getSelector(parameters);
		log.info("Selector implementation: " + selector.getClass());

		Integer populationSize = getPopulationSize(parameters);
		log.info("Population size: " + populationSize);

		Integer numGenerations = getNumGenerations(parameters);
		log.info("Number of generations: " + numGenerations);

		Double mutationRate = getMutationRate(parameters);
		log.info("Mutation rate: " + mutationRate);

		Integer maxMutationsPerIndividual = getMaxMutationsPerIndividual(parameters);
		log.info("Max mutations per individual: " + maxMutationsPerIndividual);

		Boolean compareToKnownSolution = getCompareToKnown(parameters);
		log.info("Compare to known solution: " + compareToKnownSolution);

		if (compareToKnownSolution) {
			if (!cipher.hasKnownSolution()) {
				throw new IllegalStateException(
						"Cannot compare to known solution because this cipher does not have a known solution.  "
								+ cipher);
			}

			if (knownSolutionFitnessEvaluator == null) {
				throw new IllegalStateException(
						"Cannot compare to known solution because no respective FitnessEvaluator implementation has been set.  "
								+ cipher);
			}
		}

		return new GeneticAlgorithmStrategy(cipher, populationSize, numGenerations, mutationRate,
				maxMutationsPerIndividual, fitnessEvaluator, crossoverAlgorithm, mutationAlgorithm, selector,
				knownSolutionFitnessEvaluator, compareToKnownSolution);
	}

	protected Cipher getCipher(Map<String, Object> parameters) {
		Object cipherName = parameters.get(ParameterConstants.CIPHER_NAME);

		if (cipherName == null) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.CIPHER_NAME + " cannot be null.");
		}

		if (!(cipherName instanceof String)) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.CIPHER_NAME
					+ " must be of type String.");
		}

		Cipher cipher = cipherDao.findByCipherName((String) cipherName);

		if (cipher == null) {
			throw new IllegalStateException("Unable to find the cipher with name: " + (String) cipherName
					+ ".  Unable to build GeneticAlgorithmStrategy.");
		}

		return cipher;
	}

	protected FitnessEvaluator getFitnessEvaluator(Map<String, Object> parameters) {
		Object selectedFitnessEvaluator = parameters.get(ParameterConstants.FITNESS_EVALUATOR);

		if (selectedFitnessEvaluator == null) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.FITNESS_EVALUATOR
					+ " cannot be null.");
		}

		if (!(selectedFitnessEvaluator instanceof FitnessEvaluator)) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.FITNESS_EVALUATOR
					+ " must be of type FitnessEvaluator.");
		}

		return (FitnessEvaluator) selectedFitnessEvaluator;
	}

	@SuppressWarnings("rawtypes")
	protected CrossoverAlgorithm getCrossoverAlgorithm(Map<String, Object> parameters) {
		Object selectedCrossoverAlgorithm = parameters.get(ParameterConstants.CROSSOVER_ALGORITHM);

		if (selectedCrossoverAlgorithm == null) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.CROSSOVER_ALGORITHM
					+ " cannot be null.");
		}

		if (!(selectedCrossoverAlgorithm instanceof CrossoverAlgorithm)) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.CROSSOVER_ALGORITHM
					+ " must be of type CrossoverAlgorithm.");
		}

		return (CrossoverAlgorithm) selectedCrossoverAlgorithm;
	}

	@SuppressWarnings("rawtypes")
	protected MutationAlgorithm getMutationAlgorithm(Map<String, Object> parameters) {
		Object selectedMutationAlgorithm = parameters.get(ParameterConstants.MUTATION_ALGORITHM);

		if (selectedMutationAlgorithm == null) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.MUTATION_ALGORITHM
					+ " cannot be null.");
		}

		if (!(selectedMutationAlgorithm instanceof MutationAlgorithm)) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.MUTATION_ALGORITHM
					+ " must be of type MutationAlgorithm.");
		}

		return (MutationAlgorithm) selectedMutationAlgorithm;
	}

	protected Selector getSelector(Map<String, Object> parameters) {
		Object selectedSelector = parameters.get(ParameterConstants.SELECTOR_METHOD);

		if (selectedSelector == null) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.SELECTOR_METHOD
					+ " cannot be null.");
		}

		if (!(selectedSelector instanceof Selector)) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.SELECTOR_METHOD
					+ " must be of type Selector.");
		}

		return (Selector) selectedSelector;
	}

	protected Integer getPopulationSize(Map<String, Object> parameters) {
		Object populationSize = parameters.get(ParameterConstants.POPULATION_SIZE);

		if (populationSize == null) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.POPULATION_SIZE
					+ " cannot be null.");
		}

		if (!(populationSize instanceof Integer)) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.POPULATION_SIZE
					+ " must be of type Integer.");
		}

		return (Integer) populationSize;
	}

	protected Integer getNumGenerations(Map<String, Object> parameters) {
		Object numGenerations = parameters.get(ParameterConstants.NUMBER_OF_GENERATIONS);

		if (numGenerations == null) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.NUMBER_OF_GENERATIONS
					+ " cannot be null.");
		}

		if (!(numGenerations instanceof Integer)) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.NUMBER_OF_GENERATIONS
					+ " must be of type Integer.");
		}

		return (Integer) numGenerations;
	}

	protected Double getMutationRate(Map<String, Object> parameters) {
		Object mutationRate = parameters.get(ParameterConstants.MUTATION_RATE);

		if (mutationRate == null) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.MUTATION_RATE
					+ " cannot be null.");
		}

		if (!(mutationRate instanceof Double)) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.MUTATION_RATE
					+ " must be of type Double.");
		}

		return (Double) mutationRate;
	}

	protected Integer getMaxMutationsPerIndividual(Map<String, Object> parameters) {
		Object maxMutationsPerIndividual = parameters.get(ParameterConstants.MAX_MUTATIONS_PER_INDIVIDUAL);

		if (maxMutationsPerIndividual == null) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.MAX_MUTATIONS_PER_INDIVIDUAL
					+ " cannot be null.");
		}

		if (!(maxMutationsPerIndividual instanceof Integer)) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.MAX_MUTATIONS_PER_INDIVIDUAL
					+ " must be of type Integer.");
		}

		return (Integer) maxMutationsPerIndividual;
	}

	protected Boolean getCompareToKnown(Map<String, Object> parameters) {
		Object compareToKnown = parameters.get(ParameterConstants.COMPARE_TO_KNOWN_SOLUTION);

		if (compareToKnown == null) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.COMPARE_TO_KNOWN_SOLUTION
					+ " cannot be null.");
		}

		if (!(compareToKnown instanceof Boolean)) {
			throw new IllegalArgumentException("The parameter " + ParameterConstants.COMPARE_TO_KNOWN_SOLUTION
					+ " must be of type Boolean.");
		}

		return (Boolean) compareToKnown;
	}

	/**
	 * @param cipherDao
	 *            the cipherDao to set
	 */
	@Required
	public void setCipherDao(CipherDao cipherDao) {
		this.cipherDao = cipherDao;
	}

	/**
	 * This is NOT required. We will not always know the solution. In fact, that should be the rare case.
	 * 
	 * @param knownSolutionFitnessEvaluator
	 *            the knownSolutionFitnessEvaluator to set
	 */
	public void setKnownSolutionFitnessEvaluator(FitnessEvaluator knownSolutionFitnessEvaluator) {
		this.knownSolutionFitnessEvaluator = knownSolutionFitnessEvaluator;
	}
}
