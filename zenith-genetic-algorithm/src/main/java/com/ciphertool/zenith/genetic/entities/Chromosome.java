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

package com.ciphertool.zenith.genetic.entities;

 import com.ciphertool.zenith.genetic.population.Population;

public interface Chromosome extends Cloneable {
	Double getFitness();

	/**
	 * @param fitness
	 */
	void setFitness(Double fitness);

	/**
	 * @return the age of this individual Chromosome
	 */
	int getAge();

	/**
	 * @param age
	 *            the age to set
	 */
	void setAge(int age);

	void increaseAge();

	/**
	 * @return the number of children this Chromosome has procreated
	 */
	int getNumberOfChildren();

	/**
	 * @param numberOfChildren
	 *            the numberOfChildren to set
	 */
	void setNumberOfChildren(int numberOfChildren);

	void increaseNumberOfChildren();

	/*
	 * Returns the size as the number of gene sequences
	 */
	Integer actualSize();

	Integer targetSize();

	Chromosome clone();

	/*
	 * Whether this Chromosome has changed since it was last evaluated.
	 */
	boolean isEvaluationNeeded();

	/**
	 * @param evaluationNeeded
	 *            the evaluationNeeded value to set
	 */
	void setEvaluationNeeded(boolean evaluationNeeded);

	/**
	 * @return the solutionSetId
	 */
	Integer getSolutionSetId();

	/**
	 * @param solutionSetId
	 *            the solutionSetId to set
	 */
	void setSolutionSetId(Integer solutionSetId);

	/**
	 * @param other
	 *            the other Chromosome
	 * @return the percentage similarity between this Chromosome and other
	 */
	double similarityTo(Chromosome other);

	/**
	 * @return this Chromosome's Population
	 */
	Population getPopulation();

	/**
	 * @param population
	 *            the population to set
	 */
	void setPopulation(Population population);
}
