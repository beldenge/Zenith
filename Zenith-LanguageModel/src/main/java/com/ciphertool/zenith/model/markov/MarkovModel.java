/**
 * Copyright 2017 George Belden
 * 
 * This file is part of Zenith.
 * 
 * Zenith is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Zenith is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.model.markov;

import java.math.BigDecimal;

public interface MarkovModel {
	public void addNode(NGramIndexNode nodeToAdd);

	public boolean addNGram(String nGramString);

	/**
	 * @param nGram
	 *            the N-Gram String to search by
	 * @return the exact matching NGramIndexNode
	 */
	public NGramIndexNode find(String nGram);

	/**
	 * @return the rootNode
	 */
	public NGramIndexNode getRootNode();

	/**
	 * @return the numWithInsufficientCounts
	 */
	public Long getNumWithInsufficientCounts();

	/**
	 * @return the order
	 */
	public Integer getOrder();

	/**
	 * @return the unknownLetterNGramProbability
	 */
	public BigDecimal getUnknownLetterNGramProbability();

	/**
	 * @return the indexOfCoincidence
	 */
	public BigDecimal getIndexOfCoincidence();

	@Override
	public String toString();
}
