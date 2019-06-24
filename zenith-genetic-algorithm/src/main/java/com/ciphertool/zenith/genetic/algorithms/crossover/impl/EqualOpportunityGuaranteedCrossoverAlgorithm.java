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

public class EqualOpportunityGuaranteedCrossoverAlgorithm implements
		EvaluatedCrossoverAlgorithm<KeyedChromosome<Object>> {
	private Logger				log	= LoggerFactory.getLogger(getClass());

	private int					maxAttempts;

	private FitnessEvaluator fitnessEvaluator;

	private Coin coin;

	@Override
	public List<KeyedChromosome<Object>> crossover(KeyedChromosome<Object> parentA, KeyedChromosome<Object> parentB) {
		List<KeyedChromosome<Object>> children = new ArrayList<>(1);

		KeyedChromosome<Object> child = performCrossover(parentA, parentB);

		// The Chromosome could be null if it's identical to one of its parents
		if (child != null) {
			children.add(child);

			parentA.increaseNumberOfChildren();
			parentB.increaseNumberOfChildren();
		}

		return children;
	}

	@SuppressWarnings("unchecked")
	protected KeyedChromosome<Object> performCrossover(KeyedChromosome<Object> parentA, KeyedChromosome<Object> parentB) {
		KeyedChromosome<Object> child = (KeyedChromosome<Object>) parentA.clone();
		Map<Object, Gene> replaced = new HashMap<>();
		Double originalFitness = parentA.getFitness();
		Gene originalGene;
		Gene replacement;

		boolean crossedOver;
		int attempts = 0;
		for (; attempts < maxAttempts; attempts++) {
			crossedOver = false;
			replaced.clear();

			for (Object key : child.getGenes().keySet()) {
				if (coin.flip()) {
					originalGene = child.getGenes().get(key);
					replacement = parentB.getGenes().get(key).clone();

					if (!replacement.equals(originalGene)) {
						replaced.put(key, originalGene);

						child.replaceGene(key, replacement);

						crossedOver = true;
					}
				}
			}

			if (crossedOver) {
				Double fitness = fitnessEvaluator.evaluate(child);

				if (fitness > originalFitness) {
					child.setFitness(fitness);

					break;
				} else {
					// revert crossover
					for (Object key : replaced.keySet()) {
						child.replaceGene(key, replaced.get(key));
					}

					// Make sure it doesn't get re-evaluated
					child.setFitness(originalFitness);
				}
			}
		}

		if (attempts >= maxAttempts) {
			log.debug("Unable to find guaranteed better fitness via crossover after " + maxAttempts
					+ " attempts.  Returning clone of first parent.");
		}

		return child;
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
		return "Equal Opportunity Guaranteed";
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
		return 1;
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
