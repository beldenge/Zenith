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

package com.ciphertool.zenith.genetic.algorithms.crossover;

 import com.ciphertool.zenith.genetic.Selectable;
 import com.ciphertool.zenith.genetic.entities.Chromosome;

 import java.util.List;

public interface CrossoverAlgorithm<T extends Chromosome> extends Selectable {

	/**
	 * Performs crossover to a List of children by cloning one or both of the parents and then selectively replacing
	 * Genes from the other parent.
	 * 
	 * @param parentA
	 *            the first parent
	 * @param parentB
	 *            the second parent
	 * @return the List of children Chromosomes produced from the crossover
	 */
	List<T> crossover(T parentA, T parentB);

	/**
	 * @return the number of offspring this CrossoverAlgorithm will generate
	 */
	int numberOfOffspring();
}
