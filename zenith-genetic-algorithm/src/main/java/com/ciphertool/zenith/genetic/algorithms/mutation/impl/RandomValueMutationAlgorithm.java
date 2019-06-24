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

 import com.ciphertool.zenith.genetic.algorithms.mutation.NonUniformMutationAlgorithm;
 import com.ciphertool.zenith.genetic.dao.GeneDao;
 import com.ciphertool.zenith.genetic.entities.Chromosome;
 import com.ciphertool.zenith.genetic.entities.KeyedChromosome;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;

 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.ThreadLocalRandom;

public class RandomValueMutationAlgorithm implements NonUniformMutationAlgorithm<KeyedChromosome<Object>> {
	private static Logger	log	= LoggerFactory.getLogger(RandomValueMutationAlgorithm.class);

	private GeneDao geneDao;
	private Integer			maxMutationsPerChromosome;

	@Override
	public boolean mutateChromosome(KeyedChromosome<Object> chromosome) {
		if (maxMutationsPerChromosome == null) {
			throw new IllegalStateException("The maxMutationsPerChromosome cannot be null.");
		}

		Chromosome original = chromosome.clone();

		/*
		 * Choose a random number of mutations constrained by the configurable max and the total number of genes
		 */
		int numMutations = (int) (ThreadLocalRandom.current().nextDouble()
				* Math.min(maxMutationsPerChromosome, chromosome.getGenes().size())) + 1;

		Set<Object> availableKeys = chromosome.getGenes().keySet();

		Set<Object> modifiableKeys = new HashSet<>();
		for (Object key : availableKeys) {
			modifiableKeys.add(key);
		}

		for (int i = 0; i < numMutations; i++) {
			// Keep track of the mutated keys
			mutateRandomGene(chromosome, modifiableKeys);
		}

		return !original.equals(chromosome);
	}

	/**
	 * Performs a genetic mutation of a random Gene of the supplied Chromosome
	 * 
	 * @param chromosome
	 *            the Chromosome to mutate
	 * @param availableIndices
	 *            the Set of available indices to mutate
	 */
	protected void mutateRandomGene(KeyedChromosome<Object> chromosome, Set<Object> availableIndices) {
		if (availableIndices == null || availableIndices.isEmpty()) {
			log.warn("List of available indices is null or empty.  Unable to find a Gene to mutate.  Returning null.");

			return;
		}

		Random generator = new Random();
		Object[] keys = availableIndices.toArray();

		// Get a random map key
		Object randomKey = keys[generator.nextInt(keys.length)];

		// Replace that map value with a randomly generated Gene
		chromosome.replaceGene(randomKey, geneDao.findRandomGene(chromosome));

		// Remove the key so that it is not used for mutation again
		availableIndices.remove(randomKey);
	}

	/**
	 * @param geneDao
	 *            the geneDao to set
	 */
	@Required
	public void setGeneDao(GeneDao geneDao) {
		this.geneDao = geneDao;
	}

	@Override
	public void setMaxMutationsPerChromosome(Integer maxMutationsPerChromosome) {
		this.maxMutationsPerChromosome = maxMutationsPerChromosome;
	}

	@Override
	public String getDisplayName() {
		return "Random Value";
	}
}
