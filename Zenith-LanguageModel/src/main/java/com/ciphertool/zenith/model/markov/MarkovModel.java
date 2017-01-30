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
import java.util.Map;

import com.ciphertool.zenith.math.MathConstants;

public class MarkovModel {
	private NGramIndexNode	rootNode	= new NGramIndexNode(null, "", 0);
	private Integer			order;
	private Long			numWithInsufficientCounts;
	private BigDecimal		unknownLetterNGramProbability;
	private BigDecimal		indexOfCoincidence;

	public MarkovModel(int order) {
		this.order = order;
	}

	public void addNode(NGramIndexNode nodeToAdd) {
		if (nodeToAdd.getLevel() == 0) {
			// This is the root node

			rootNode.setId(nodeToAdd.getId());
			rootNode.setCount(nodeToAdd.getCount());
			rootNode.setConditionalProbability(nodeToAdd.getConditionalProbability());
			rootNode.setProbability(nodeToAdd.getProbability());

			return;
		}

		boolean succeeded = populateExistingNode(rootNode, nodeToAdd, 1);

		if (!succeeded) {
			throw new IllegalStateException("Could not add node to Markov Model: " + nodeToAdd);
		}
	}

	protected boolean populateExistingNode(NGramIndexNode parentNode, NGramIndexNode nodeToAdd, int level) {
		NGramIndexNode newChild = parentNode.addExistingNodeAsync(nodeToAdd, level);

		if (level < nodeToAdd.getCumulativeString().length()) {
			return populateExistingNode(newChild, nodeToAdd, level + 1);
		} else if (newChild != null) {
			return false;
		}

		return true;
	}

	public boolean addLetterTransition(String nGramString) {
		return populateLetterNode(rootNode, nGramString, 1);
	}

	protected boolean populateLetterNode(NGramIndexNode currentNode, String nGramString, Integer level) {
		boolean isNew = currentNode.addOrIncrementChildAsync(nGramString, level);

		if (level < nGramString.length()) {
			return populateLetterNode(currentNode.getChild(nGramString.charAt(level - 1)), nGramString, level + 1);
		}

		return isNew && level == this.order;
	}

	/**
	 * @param nGram
	 *            the N-Gram String to search by
	 * @return the exact matching NGramIndexNode
	 */
	public NGramIndexNode find(String nGram) {
		return findMatch(rootNode, nGram);
	}

	protected static NGramIndexNode findMatch(NGramIndexNode node, String nGramString) {
		NGramIndexNode nextNode = node.getChild(nGramString.charAt(0));

		if (nextNode == null) {
			return null;
		}

		if (nGramString.length() == 1) {
			return nextNode;
		}

		return findMatch(nextNode, nGramString.substring(1));
	}

	/**
	 * @param nGram
	 *            the N-Gram String to search by
	 * @return the longest matching NGramIndexNode
	 */
	public NGramIndexNode findLongest(String nGram) {
		return findLongestMatch(rootNode, nGram, null);
	}

	protected static NGramIndexNode findLongestMatch(NGramIndexNode node, String nGramString, NGramIndexNode longestMatch) {
		NGramIndexNode nextNode = node.getChild(nGramString.charAt(0));

		if (nextNode == null) {
			return longestMatch;
		}

		longestMatch = nextNode;

		if (nGramString.length() == 1) {
			return longestMatch;
		}

		return findLongestMatch(nextNode, nGramString.substring(1), longestMatch);
	}

	/**
	 * @return the rootNode
	 */
	public NGramIndexNode getRootNode() {
		return rootNode;
	}

	/**
	 * @return the numWithInsufficientCounts
	 */
	public Long getNumWithInsufficientCounts() {
		return numWithInsufficientCounts;
	}

	/**
	 * @param numWithInsufficientCounts
	 *            the numWithInsufficientCounts to set
	 */
	public void setNumWithInsufficientCounts(Long numWithInsufficientCounts) {
		this.numWithInsufficientCounts = numWithInsufficientCounts;
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
		if (this.unknownLetterNGramProbability == null) {
			this.unknownLetterNGramProbability = BigDecimal.ONE.divide(BigDecimal.valueOf(this.rootNode.getCount()
					+ 1), MathConstants.PREC_10_HALF_UP);
		}

		return unknownLetterNGramProbability;
	}

	/**
	 * @return the indexOfCoincidence
	 */
	public BigDecimal getIndexOfCoincidence() {
		if (this.indexOfCoincidence == null) {
			this.indexOfCoincidence = BigDecimal.ZERO;

			BigDecimal occurences = null;
			for (Map.Entry<Character, NGramIndexNode> entry : this.rootNode.getTransitions().entrySet()) {
				occurences = BigDecimal.valueOf(entry.getValue().getCount());
				this.indexOfCoincidence = this.indexOfCoincidence.add(occurences.multiply(occurences.subtract(BigDecimal.ONE), MathConstants.PREC_10_HALF_UP));
			}

			occurences = BigDecimal.valueOf(this.rootNode.getCount());
			this.indexOfCoincidence = this.indexOfCoincidence.divide(occurences.multiply(occurences.subtract(BigDecimal.ONE), MathConstants.PREC_10_HALF_UP), MathConstants.PREC_10_HALF_UP);
		}

		return indexOfCoincidence;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Map<Character, NGramIndexNode> transitions = rootNode.getTransitions();

		for (Map.Entry<Character, NGramIndexNode> entry : transitions.entrySet()) {
			if (entry.getValue() != null) {
				appendTransitions("", entry.getKey(), entry.getValue(), sb);
			}
		}

		return sb.toString();
	}

	protected void appendTransitions(String parent, Character symbol, NGramIndexNode node, StringBuilder sb) {
		sb.append("\n[" + parent + "] ->" + symbol + " | " + node.getCount());

		Map<Character, NGramIndexNode> transitions = node.getTransitions();

		if (transitions == null || transitions.isEmpty()) {
			return;
		}

		for (Map.Entry<Character, NGramIndexNode> entry : transitions.entrySet()) {
			if (entry.getValue() != null) {
				appendTransitions(parent + entry.getKey(), entry.getKey(), entry.getValue(), sb);
			}
		}
	}
}
