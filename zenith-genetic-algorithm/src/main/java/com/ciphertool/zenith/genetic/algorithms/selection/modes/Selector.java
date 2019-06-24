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
package com.ciphertool.zenith.genetic.algorithms.selection.modes;

 import com.ciphertool.zenith.genetic.Selectable;
import com.ciphertool.zenith.genetic.entities.Chromosome;

import java.util.List;

/**
 * This class serves as the mode of selecting a fit individual from the population. It is essentially a helper class
 * used by other algorithms, most notably any SelectionAlgorithm implementation.
 * 
 * @author george
 */
public interface Selector extends Selectable {

	/**
	 * @param individuals
	 *            the individuals to index
	 */
	void reIndex(List<Chromosome> individuals);

	/**
	 * @param individuals
	 *            the List of individuals to select from
	 * @param totalFitness
	 *            the total fitness of the population of individuals
	 * @return the indice of the chosen individual within the population
	 */
	int getNextIndex(List<Chromosome> individuals, Double totalFitness);
}
