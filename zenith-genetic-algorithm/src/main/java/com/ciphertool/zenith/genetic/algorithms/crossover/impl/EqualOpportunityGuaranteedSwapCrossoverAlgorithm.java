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

package com.ciphertool.zenith.genetic.algorithms.crossover.impl;

 import com.ciphertool.zenith.genetic.algorithms.crossover.EvaluatedCrossoverAlgorithm;
 import com.ciphertool.zenith.genetic.entities.Gene;
 import com.ciphertool.zenith.genetic.entities.KeyedChromosome;
 import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
 import com.ciphertool.zenith.genetic.util.Coin;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;

 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;

public class EqualOpportunityGuaranteedSwapCrossoverAlgorithm implements EvaluatedCrossoverAlgorithm<KeyedChromosome<Object>> {
	private Logger				log	= LoggerFactory.getLogger(getClass());

	private int					maxAttempts;

	private FitnessEvaluator fitnessEvaluator;
	private int					maxGenerations;

	private Coin coin;

	@Override
	public List<KeyedChromosome<Object>> crossover(KeyedChromosome<Object> parentA, KeyedChromosome<Object> parentB) {
		List<KeyedChromosome<Object>> children = performCrossover(parentA, parentB);

		// The Chromosome could be null if it's identical to one of its parents
		for (KeyedChromosome<Object> child : children) {
			parentA.increaseNumberOfChildren();
			parentB.increaseNumberOfChildren();
		}

		return children;
	}

	@SuppressWarnings("unchecked")
	protected List<KeyedChromosome<Object>> performCrossover(KeyedChromosome<Object> parentA, KeyedChromosome<Object> parentB) {
		KeyedChromosome<Object> childA = (KeyedChromosome<Object>) parentA.clone();
		KeyedChromosome<Object> childB = (KeyedChromosome<Object>) parentB.clone();
		Map<Object, Gene> replacedChildA = new HashMap<>();
		Map<Object, Gene> replacedChildB = new HashMap<>();
		Double originalFitnessA = parentA.getFitness();
		Double originalFitnessB = parentB.getFitness();
		Gene originalGeneChildA;
		Gene originalGeneChildB;
		Gene replacementChildA;
		Gene replacementChildB;

		boolean crossedOver;
		int attempts = 0;
		for (; attempts < maxAttempts; attempts++) {
			crossedOver = false;
			replacedChildA.clear();
			replacedChildB.clear();

			for (Object key : parentA.getGenes().keySet()) {
				originalGeneChildA = childA.getGenes().get(key);
				originalGeneChildB = childB.getGenes().get(key);
				replacementChildA = parentB.getGenes().get(key).clone();
				replacementChildB = parentA.getGenes().get(key).clone();

				if (coin.flip()) {
					if (!originalGeneChildA.equals(originalGeneChildB)) {
						replacedChildA.put(key, originalGeneChildA);
						replacedChildB.put(key, originalGeneChildB);

						childA.replaceGene(key, replacementChildA);
						childB.replaceGene(key, replacementChildB);

						crossedOver = true;
					}
				}
			}

			if (crossedOver) {
				Double fitnessChildA = fitnessEvaluator.evaluate(childA);
				Double fitnessChildB = fitnessEvaluator.evaluate(childB);

				if (fitnessChildA > originalFitnessA && fitnessChildB > originalFitnessB) {
					childA.setFitness(fitnessChildA);
					childB.setFitness(fitnessChildB);

					break;
				} else {
					// revert the crossovers
					for (Object key : replacedChildA.keySet()) {
						childA.replaceGene(key, replacedChildA.get(key));
					}

					for (Object key : replacedChildB.keySet()) {
						childB.replaceGene(key, replacedChildB.get(key));
					}

					// Make sure they don't get re-evaluated
					childA.setFitness(originalFitnessA);
					childB.setFitness(originalFitnessB);
				}
			}
		}

		if (attempts >= maxAttempts) {
			log.debug("Unable to find guaranteed better fitness via crossover after " + maxAttempts
					+ " attempts.  Returning clones of parents.");
		}

		List<KeyedChromosome<Object>> children = new ArrayList<>(2);
		children.add(childA);
		children.add(childB);

		return children;
	}

	/**
	 * @param coin
	 *            the coin to set
	 */
	@Required
	public void setCoin(Coin coin) {
		this.coin = coin;
	}

	@Override
	public String getDisplayName() {
		return "Equal Opportunity Guaranteed Swap";
	}

	/**
	 * @param maxGenerations
	 *            the maxGenerations to set
	 */
	@Required
	public void setMaxGenerations(int maxGenerations) {
		this.maxGenerations = maxGenerations;
	}

	/**
	 * @param fitnessEvaluator
	 *            the fitnessEvaluator to set
	 */
	@Override
	public void setFitnessEvaluator(FitnessEvaluator fitnessEvaluator) {
		this.fitnessEvaluator = fitnessEvaluator;
	}

	@Override
	public int numberOfOffspring() {
		return 2;
	}

	/**
	 * @param maxAttempts
	 *            the maxAttempts to set
	 */
	@Required
	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}
}
