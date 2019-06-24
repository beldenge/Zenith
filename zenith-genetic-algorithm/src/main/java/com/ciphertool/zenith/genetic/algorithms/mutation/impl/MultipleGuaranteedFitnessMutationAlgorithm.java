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

package com.ciphertool.zenith.genetic.algorithms.mutation.impl;

 import com.ciphertool.zenith.genetic.algorithms.mutation.EvaluatedMutationAlgorithm;
 import com.ciphertool.zenith.genetic.algorithms.mutation.MutationHelper;
 import com.ciphertool.zenith.genetic.algorithms.mutation.UniformMutationAlgorithm;
 import com.ciphertool.zenith.genetic.dao.GeneDao;
 import com.ciphertool.zenith.genetic.entities.Gene;
 import com.ciphertool.zenith.genetic.entities.KeyedChromosome;
 import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;

 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ThreadLocalRandom;

public class MultipleGuaranteedFitnessMutationAlgorithm implements UniformMutationAlgorithm<KeyedChromosome<Object>>,
		EvaluatedMutationAlgorithm<KeyedChromosome<Object>> {
	private Logger				log	= LoggerFactory.getLogger(getClass());

	private int					maxAttempts;

	private GeneDao geneDao;

	private MutationHelper mutationHelper;

	private FitnessEvaluator fitnessEvaluator;

	@Override
	public boolean mutateChromosome(KeyedChromosome<Object> chromosome) {
		Double originalFitness = chromosome.getFitness();

		List<Object> availableKeys;
		Map<Object, Gene> replaced = new HashMap<>();
		List<Object> randomKeys = new ArrayList<>();
		int numMutations;
		Gene originalGene;
		Gene replacement;

		boolean mutated;
		int attempts = 0;
		for (; attempts < maxAttempts; attempts++) {
			mutated = false;

			/*
			 * Choose a random number of mutations constrained by the configurable max and the total number of genes
			 */
			numMutations = mutationHelper.getNumMutations(chromosome.getGenes().size());

			availableKeys = new ArrayList<>(chromosome.getGenes().keySet());
			replaced.clear();
			randomKeys.clear();

			for (int i = 0; i < numMutations; i++) {
				/*
				 * We don't want to reuse an index, so we get one from the List of indices which are still available
				 */
				int randomIndex = (int) (ThreadLocalRandom.current().nextDouble() * availableKeys.size());

				randomKeys.add(availableKeys.get(randomIndex));

				availableKeys.remove(randomIndex);
			}

			for (Object key : randomKeys) {
				originalGene = chromosome.getGenes().get(key);

				// Replace that map value with a randomly generated Gene
				replacement = geneDao.findRandomGene(chromosome);

				if (!replacement.equals(originalGene)) {
					replaced.put(key, chromosome.getGenes().get(key));

					chromosome.replaceGene(key, replacement);

					mutated = true;
				}
			}

			if (mutated) {
				Double fitness = fitnessEvaluator.evaluate(chromosome);

				// Test if the replacement is better, otherwise continue looping
				if (fitness > originalFitness) {
					chromosome.setFitness(fitness);

					break;
				} else {
					// revert the mutations
					for (Object key : replaced.keySet()) {
						chromosome.replaceGene(key, replaced.get(key));
					}

					// Make sure it doesn't get re-evaluated
					chromosome.setFitness(originalFitness);
				}
			}
		}

		if (attempts >= maxAttempts) {
			log.debug("Unable to find guaranteed better fitness via mutation after " + attempts
					+ " attempts.  Returning clone of parent.");

			return false;
		}

		return true;
	}

	@Override
	public void setMutationRate(Double mutationRate) {
		// Not used
	}

	/**
	 * @param geneDao
	 *            the geneDao to set
	 */
	@Required
	public void setGeneDao(GeneDao geneDao) {
		this.geneDao = geneDao;
	}

	/**
	 * @param fitnessEvaluator
	 *            the fitnessEvaluator to set
	 */
	@Override
	public void setFitnessEvaluator(FitnessEvaluator fitnessEvaluator) {
		this.fitnessEvaluator = fitnessEvaluator;
	}

	/**
	 * @param maxAttempts
	 *            the maxAttempts to set
	 */
	@Required
	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	/**
	 * @param mutationHelper
	 *            the mutationHelper to set
	 */
	@Required
	public void setMutationHelper(MutationHelper mutationHelper) {
		this.mutationHelper = mutationHelper;
	}

	@Override
	public String getDisplayName() {
		return "Multiple Guaranteed Fitness";
	}
}
