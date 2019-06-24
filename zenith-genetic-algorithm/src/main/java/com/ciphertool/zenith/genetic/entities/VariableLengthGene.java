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

import java.util.List;

public interface VariableLengthGene extends Gene {
	/**
	 * @return an unmodifiable List of this Gene's Sequences
	 */
	List<Sequence> getSequences();

	/**
	 * Adds a Sequence to the end of the Gene.
	 * 
	 * @param sequence
	 */
	void addSequence(Sequence sequence);

	/**
	 * Inserts a Sequence at the specified index. Care must be taken to update any Sequence indexes which follow the
	 * inserted Sequence.
	 * 
	 * @param index
	 * @param sequence
	 */
	void insertSequence(int index, Sequence sequence);

	/**
	 * Removes a Sequence at the specified index. Care must be taken to update any Sequence indexes which follow the
	 * removed Sequence.
	 * 
	 * @param sequence
	 */
	void removeSequence(Sequence sequence);

	/**
	 * Replaces a Sequence at the specified index.
	 */
	void replaceSequence(int index, Sequence newSequence);

	/**
	 * Return the size of this Gene, measured as the number of sequences making up this Gene.
	 */
	int size();
}
