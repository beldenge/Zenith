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

package com.ciphertool.zenith.genetic.mocks;

 import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.KeyedChromosome;
import com.ciphertool.zenith.genetic.population.Population;

import java.util.HashMap;
import java.util.Map;

public class MockKeyedChromosome implements KeyedChromosome<Object> {
	private boolean				needsEvaluation;
	private Double			fitness				= 0d;
	private Map<Object, Gene>	genes				= new HashMap<>();
	private Integer				targetSize			= 0;
	private int					age					= 0;
	private int					numberOfChildren	= 0;
	private Population population;

	@Override
	public Map<Object, Gene> getGenes() {
		return this.genes;
	}

	@Override
	public Double getFitness() {
		return this.fitness;
	}

	@Override
	public void setFitness(Double fitness) {
		this.fitness = fitness;
		this.needsEvaluation = false;
	}

	@Override
	public int getAge() {
		return this.age;
	}

	@Override
	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public void increaseAge() {
		this.age++;
	}

	@Override
	public int getNumberOfChildren() {
		return this.numberOfChildren;
	}

	@Override
	public void setNumberOfChildren(int numberOfChildren) {
		this.numberOfChildren = numberOfChildren;
	}

	@Override
	public void increaseNumberOfChildren() {
		this.numberOfChildren++;
	}

	@Override
	public Integer actualSize() {
		return this.genes.size();
	}

	@Override
	public Integer targetSize() {
		return targetSize;
	}

	/**
	 * Convenience method for unit tests. This will not normally be implemented this way.
	 * 
	 * @param targetSize
	 *            the targetSize to set
	 */
	public void setTargetSize(int targetSize) {
		this.targetSize = targetSize;
	}

	@Override
	public boolean isEvaluationNeeded() {
		return this.needsEvaluation;
	}

	@Override
	public void setEvaluationNeeded(boolean needsEvaluation) {
		this.needsEvaluation = needsEvaluation;
	}

	@Override
	public void putGene(Object key, Gene gene) {
		gene.setChromosome(this);

		this.genes.put(key, gene);
	}

	@Override
	public Gene removeGene(Object key) {
		return this.genes.remove(key);
	}

	@Override
	public void replaceGene(Object key, Gene newGene) {
		newGene.setChromosome(this);

		this.removeGene(key);

		this.putGene(key, newGene);
	}

	@Override
	public MockKeyedChromosome clone() {
		MockKeyedChromosome copyChromosome = new MockKeyedChromosome();

		copyChromosome.genes = new HashMap<>();
		copyChromosome.setAge(0);
		copyChromosome.setNumberOfChildren(0);
		copyChromosome.setEvaluationNeeded(this.needsEvaluation);

		/*
		 * Since we are copying over the fitness value, we don't need to reset the evaluationNeeded flag because the
		 * cloned default is correct.
		 */
		copyChromosome.setFitness(this.fitness);

		/*
		 * We don't need to clone the solutionSetId or cipherId as even though they are objects, they should remain
		 * static.
		 */

		Gene nextGene;
		for (Object key : this.genes.keySet()) {
			nextGene = this.genes.get(key).clone();

			copyChromosome.putGene(key, nextGene);
		}

		return copyChromosome;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MockKeyedChromosome other = (MockKeyedChromosome) obj;
		if (genes == null) {
			if (other.genes != null) {
				return false;
			}
		} else if (!genes.equals(other.genes)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "MockKeyedChromosome [needsEvaluation=" + needsEvaluation + ", fitness=" + fitness + ", genes=" + genes
				+ "]";
	}

	@Override
	public Integer getSolutionSetId() {
		throw new UnsupportedOperationException("Method not yet implemented.");
	}

	@Override
	public void setSolutionSetId(Integer solutionSetId) {
		throw new UnsupportedOperationException("Method not yet implemented.");
	}

	@Override
	public double similarityTo(Chromosome other) {
		return 0;
	}

	/**
	 * @return the population
	 */
	@Override
	public Population getPopulation() {
		return population;
	}

	/**
	 * @param population
	 *            the population to set
	 */
	@Override
	public void setPopulation(Population population) {
		this.population = population;
	}
}
