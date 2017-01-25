/**
 * Copyright 2017 George Belden
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

package com.ciphertool.zenith.inference;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PartialDerivation {
	private BigDecimal						productOfProbabilities	= BigDecimal.ONE;
	private BigDecimal						sumOfProbabilities		= BigDecimal.ZERO;
	private Map<String, Integer>			unigramCounts			= new HashMap<>();
	private Map<String, Integer>			bigramCounts			= new HashMap<>();
	private Map<CiphertextMapping, Integer>	ciphertextMappingCounts	= new HashMap<>();
	private String							lastCharacter;

	/**
	 * @param productOfProbabilities
	 *            the product of probabilities
	 * @param sumOfProbabilities
	 *            the sum of probabilities
	 */
	public PartialDerivation(BigDecimal productOfProbabilities, BigDecimal sumOfProbabilities) {
		this.productOfProbabilities = productOfProbabilities;
		this.sumOfProbabilities = sumOfProbabilities;
	}

	/**
	 * @param productOfProbabilities
	 *            the product of probabilities
	 * @param sumOfProbabilities
	 *            the sum of probabilities
	 * @param unigramCounts
	 *            the unigram counts
	 * @param bigramCounts
	 *            the bigram counts
	 * @param ciphertextMappingCounts
	 *            the ciphertext mapping counts
	 * @param lastCharacter
	 *            the last character
	 */
	public PartialDerivation(BigDecimal productOfProbabilities, BigDecimal sumOfProbabilities,
			Map<String, Integer> unigramCounts, Map<String, Integer> bigramCounts,
			Map<CiphertextMapping, Integer> ciphertextMappingCounts, String lastCharacter) {
		this.productOfProbabilities = productOfProbabilities;
		this.sumOfProbabilities = sumOfProbabilities;
		this.unigramCounts = unigramCounts;
		this.bigramCounts = bigramCounts;
		this.ciphertextMappingCounts = ciphertextMappingCounts;
		this.lastCharacter = lastCharacter;
	}

	/**
	 * @return the productOfProbabilities
	 */
	public BigDecimal getProductOfProbabilities() {
		return productOfProbabilities;
	}

	/**
	 * @return the sumOfProbabilities
	 */
	public BigDecimal getSumOfProbabilities() {
		return sumOfProbabilities;
	}

	/**
	 * @return the unigramCounts
	 */
	public Map<String, Integer> getUnigramCounts() {
		return unigramCounts;
	}

	/**
	 * @return the bigramCounts
	 */
	public Map<String, Integer> getBigramCounts() {
		return bigramCounts;
	}

	/**
	 * @return the ciphertextMappingCounts
	 */
	public Map<CiphertextMapping, Integer> getCiphertextMappingCounts() {
		return ciphertextMappingCounts;
	}

	/**
	 * @return the lastCharacter
	 */
	public String getLastCharacter() {
		return lastCharacter;
	}
}
