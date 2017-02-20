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
import java.util.HashMap;
import java.util.Map;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.model.entities.ListNGram;

public class ListMarkovModel implements MarkovModel {
	private Map<String, ListNGram>	nodes	= new HashMap<String, ListNGram>();
	private Integer					order;
	private BigDecimal				unknownLetterNGramProbability;

	public ListMarkovModel(int order, BigDecimal unknownLetterNGramProbability) {
		this.order = order;
		this.unknownLetterNGramProbability = unknownLetterNGramProbability;
	}

	public void addNode(ListNGram nodeToAdd) {
		if (nodeToAdd.getCumulativeString() == null || nodeToAdd.getCumulativeString().length() != this.order) {
			return;
		}

		ListNGram existingNode = nodes.get(nodeToAdd.getCumulativeString());

		if (existingNode == null) {
			nodes.put(nodeToAdd.getCumulativeString(), nodeToAdd);
		} else {
			existingNode.increment();
		}
	}

	public synchronized boolean addLetterTransition(String nGramString) {
		if (nodes.containsKey(nGramString)) {
			nodes.get(nGramString).increment();

			return false;
		}

		nodes.put(nGramString, new ListNGram(nGramString));

		return true;
	}

	/**
	 * @param nGram
	 *            the N-Gram String to search by
	 * @return the exact matching NGramIndexNode
	 */
	public ListNGram find(String nGram) {
		return nodes.get(nGram);
	}

	/**
	 * @return the rootNode
	 */
	public Map<String, ListNGram> getNodeMap() {
		return nodes;
	}

	/**
	 * @return the order
	 */
	public Integer getOrder() {
		return order;
	}

	/**
	 * @return the unknownLetterNGramProbability
	 */
	public BigDecimal getUnknownLetterNGramProbability() {
		return unknownLetterNGramProbability;
	}

	public void normalize(long orderTotal) {
		nodes.values().parallelStream().forEach(n -> {
			n.setProbability(BigDecimal.valueOf(n.getCount()).divide(BigDecimal.valueOf(orderTotal), MathConstants.PREC_10_HALF_UP));
		});
	}

	@Override
	public long size() {
		return this.nodes.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, ListNGram> entry : nodes.entrySet()) {
			sb.append("\n[" + entry.getKey() + "] ->" + entry.getValue().getCount());
		}

		return sb.toString();
	}
}
