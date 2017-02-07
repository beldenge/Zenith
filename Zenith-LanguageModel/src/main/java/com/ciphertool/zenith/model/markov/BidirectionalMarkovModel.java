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

public class BidirectionalMarkovModel implements MarkovModel {
	private NGramIndexNode	frontRootNode	= new BidirectionalNGramIndexNode(null, null, "");
	private NGramIndexNode	backRootNode	= new BidirectionalNGramIndexNode(null, null, "");
	private Integer			order;
	private Long			numWithInsufficientCounts;
	private BigDecimal		unknownLetterNGramProbability;
	private BigDecimal		indexOfCoincidence;

	public BidirectionalMarkovModel(int order) {
		this.order = order;
	}

	public BidirectionalMarkovModel(int order, long numWithInsufficientCounts) {
		this.order = order;
		this.numWithInsufficientCounts = numWithInsufficientCounts;
	}

	@Override
	public void addNode(NGramIndexNode nodeToAdd) {
		boolean succeeded = populateExistingNode(frontRootNode, backRootNode, nodeToAdd, 0);

		if (!succeeded) {
			throw new IllegalStateException("Could not add node to Markov Model: " + nodeToAdd);
		}
	}

	protected boolean populateExistingNode(NGramIndexNode frontParentNode, NGramIndexNode backParentNode, NGramIndexNode nodeToAdd, int frontLetterIndex) {
		boolean isLast = (frontLetterIndex == (nodeToAdd.getCumulativeString().length() / 2));
		NGramIndexNode frontChild = ((BidirectionalNGramIndexNode) frontParentNode).addExistingNodeAsync(nodeToAdd, frontLetterIndex, true, isLast);
		NGramIndexNode backChild = ((BidirectionalNGramIndexNode) backParentNode).addExistingNodeAsync(nodeToAdd, nodeToAdd.getCumulativeString().length()
				- frontLetterIndex - 1, false, isLast);

		if (frontLetterIndex < nodeToAdd.getCumulativeString().length() / 2) {
			return populateExistingNode(frontChild, backChild, nodeToAdd, frontLetterIndex + 1);
		} else if (frontChild != null || backChild != null) {
			return false;
		}

		return true;
	}

	@Override
	public boolean addNGram(String nGramString) {
		return populateLetterNode(frontRootNode, nGramString, true, 0)
				&& populateLetterNode(backRootNode, nGramString, false, nGramString.length() - 1);
	}

	protected boolean populateLetterNode(NGramIndexNode currentNode, String nGramString, boolean isFront, Integer letterIndex) {
		boolean isNew = ((BidirectionalNGramIndexNode) currentNode).addOrIncrementChildAsync(nGramString, isFront, letterIndex);

		if (letterIndex != nGramString.length() / 2) {
			return populateLetterNode(currentNode.getChild(nGramString.charAt(isFront ? letterIndex + 1 : letterIndex
					- 1)), nGramString, isFront, isFront ? letterIndex + 1 : letterIndex - 1);
		}

		return isNew && letterIndex == nGramString.length() / 2;
	}

	/**
	 * @param nGram
	 *            the N-Gram String to search by
	 * @return the exact matching NGramIndexNode
	 */
	@Override
	public NGramIndexNode find(String nGram) {
		return findMatch(frontRootNode, backRootNode, nGram.substring(0, (nGram.length() / 2)
				+ 1), nGram.substring(nGram.length() - (nGram.length() / 2) - 1, nGram.length()));
	}

	protected static NGramIndexNode findMatch(NGramIndexNode frontNode, NGramIndexNode backNode, String frontSubstring, String backSubstring) {
		NGramIndexNode nextFront = frontNode.getChild(frontSubstring.charAt(0));
		NGramIndexNode nextBack = frontNode.getChild(backSubstring.charAt(backSubstring.length() - 1));

		if (nextFront == null || nextBack == null) {
			return null;
		}

		if (frontSubstring.length() == 1 || backSubstring.length() == 1) {
			if (nextFront == nextBack) {
				return nextFront;
			} else {
				return null;
			}
		}

		return findMatch(nextFront, nextBack, frontSubstring.substring(1), backSubstring.substring(0, backSubstring.length()
				- 1));
	}

	/**
	 * @return the rootNode
	 */
	@Override
	public NGramIndexNode getRootNode() {
		return frontRootNode;
	}

	/**
	 * @return the numWithInsufficientCounts
	 */
	@Override
	public Long getNumWithInsufficientCounts() {
		return numWithInsufficientCounts;
	}

	/**
	 * @return the order
	 */
	@Override
	public Integer getOrder() {
		return order;
	}

	/**
	 * @return the unknownLetterNGramProbability
	 */
	@Override
	public BigDecimal getUnknownLetterNGramProbability() {
		if (this.unknownLetterNGramProbability == null) {
			// FIXME: the root node count is the count of n-grams of all order, not just the highest order
			this.unknownLetterNGramProbability = BigDecimal.ONE.divide(BigDecimal.valueOf(this.frontRootNode.getCount()), MathConstants.PREC_10_HALF_UP);
		}

		return unknownLetterNGramProbability;
	}

	/**
	 * @return the indexOfCoincidence
	 */
	@Override
	public BigDecimal getIndexOfCoincidence() {
		if (this.indexOfCoincidence == null) {
			this.indexOfCoincidence = BigDecimal.ZERO;

			BigDecimal occurences = null;
			for (Map.Entry<Character, NGramIndexNode> entry : this.frontRootNode.getTransitions().entrySet()) {
				occurences = BigDecimal.valueOf(entry.getValue().getCount());
				this.indexOfCoincidence = this.indexOfCoincidence.add(occurences.multiply(occurences.subtract(BigDecimal.ONE), MathConstants.PREC_10_HALF_UP));
			}

			occurences = BigDecimal.valueOf(this.frontRootNode.getCount());
			this.indexOfCoincidence = this.indexOfCoincidence.divide(occurences.multiply(occurences.subtract(BigDecimal.ONE), MathConstants.PREC_10_HALF_UP), MathConstants.PREC_10_HALF_UP);
		}

		return indexOfCoincidence;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Map<Character, NGramIndexNode> transitions = frontRootNode.getTransitions();

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
