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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.entities.NGramIndexNode;

public class MarkovModel {
	private Logger			log			= LoggerFactory.getLogger(getClass());

	private NGramIndexNode	rootNode	= new NGramIndexNode("");
	private Integer			order;
	private BigDecimal		unknownLetterNGramProbability;
	private BigDecimal		indexOfCoincidence;

	public MarkovModel(int order, BigDecimal unknownLetterNGramProbability) {
		this.order = order;
		this.unknownLetterNGramProbability = unknownLetterNGramProbability;
	}

	public void addNode(NGramIndexNode nodeToAdd) {
		if (nodeToAdd.getCumulativeString() == null || nodeToAdd.getCumulativeString().length() == 0) {
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

	protected boolean populateExistingNode(NGramIndexNode parentNode, NGramIndexNode nodeToAdd, int order) {
		NGramIndexNode newChild = parentNode.addExistingNodeAsync(nodeToAdd, order);

		if (order < nodeToAdd.getCumulativeString().length()) {
			return populateExistingNode(newChild, nodeToAdd, order + 1);
		} else if (newChild != null) {
			return false;
		}

		return true;
	}

	public boolean addLetterTransition(String nGramString) {
		return populateLetterNode(rootNode, nGramString, 1);
	}

	protected boolean populateLetterNode(NGramIndexNode currentNode, String nGramString, Integer order) {
		boolean isNew = currentNode.addOrIncrementChildAsync(nGramString, order);

		if (order < nGramString.length()) {
			return populateLetterNode(currentNode.getChild(nGramString.charAt(order - 1)), nGramString, order + 1);
		}

		return isNew && order == this.order;
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

	public void linkChildren(boolean includeWordBoundaries, TaskExecutor taskExecutor) {
		List<FutureTask<Void>> futures = new ArrayList<FutureTask<Void>>(26);
		FutureTask<Void> task;

		futures = new ArrayList<FutureTask<Void>>(26);

		for (Map.Entry<Character, NGramIndexNode> entry : this.rootNode.getTransitions().entrySet()) {
			if (entry.getValue() != null) {
				task = new FutureTask<Void>(new LinkChildTask(entry.getKey(), entry.getValue(), includeWordBoundaries));
				futures.add(task);
				taskExecutor.execute(task);
			}
		}

		for (FutureTask<Void> future : futures) {
			try {
				future.get();
			} catch (InterruptedException ie) {
				log.error("Caught InterruptedException while waiting for LinkChildTask ", ie);
			} catch (ExecutionException ee) {
				log.error("Caught ExecutionException while waiting for LinkChildTask ", ee);
			}
		}
	}

	protected void linkChild(NGramIndexNode node, String nGram, boolean includeWordBoundaries) {
		Map<Character, NGramIndexNode> transitions = node.getTransitions();

		if (nGram.length() == order) {
			for (Character letter : (includeWordBoundaries ? ModelConstants.LOWERCASE_LETTERS_AND_SPACE : ModelConstants.LOWERCASE_LETTERS)) {
				NGramIndexNode match = this.find(nGram.substring(1) + letter.toString());

				if (match != null) {
					node.putChild(letter, match);
				}
			}

			return;
		}

		for (Map.Entry<Character, NGramIndexNode> entry : transitions.entrySet()) {
			NGramIndexNode nextNode = entry.getValue();

			if (nextNode != null) {
				linkChild(nextNode, nGram + String.valueOf(entry.getKey()), includeWordBoundaries);
			}
		}
	}

	/**
	 * A concurrent task for linking leaf nodes in a Markov model.
	 */
	protected class LinkChildTask implements Callable<Void> {
		private Character		key;
		private NGramIndexNode	node;
		private boolean			includeWordBoundaries;

		/**
		 * @param key
		 *            the Character key to set
		 * @param node
		 *            the NGramIndexNode to set
		 * @param includeWordBoundaries
		 *            whether to include word boundaries
		 */
		public LinkChildTask(Character key, NGramIndexNode node, boolean includeWordBoundaries) {
			this.key = key;
			this.node = node;
			this.includeWordBoundaries = includeWordBoundaries;
		}

		@Override
		public Void call() throws Exception {
			linkChild(this.node, String.valueOf(this.key), includeWordBoundaries);

			return null;
		}
	}
}
