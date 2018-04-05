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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.entities.TreeNGram;

public class TreeMarkovModel {
	private Logger		log			= LoggerFactory.getLogger(getClass());

	private TreeNGram	rootNode	= new TreeNGram("");
	private Integer		order;
	private Double	unknownLetterNGramProbability;
	private Double	indexOfCoincidence;

	public TreeMarkovModel(int order) {
		this.order = order;
	}

	public void addNode(TreeNGram nodeToAdd) {
		if (nodeToAdd.getCumulativeString() == null || nodeToAdd.getCumulativeString().length() == 0) {
			// This is the root node

			rootNode.setId(nodeToAdd.getId());
			rootNode.setCount(nodeToAdd.getCount());
			rootNode.setConditionalProbability(nodeToAdd.getConditionalProbability());
			rootNode.setProbability(nodeToAdd.getProbability());
			rootNode.setChainedProbability(nodeToAdd.getChainedProbability());

			return;
		}

		boolean succeeded = populateExistingNode(rootNode, nodeToAdd, 1);

		if (!succeeded) {
			throw new IllegalStateException("Could not add node to Markov Model: " + nodeToAdd);
		}
	}

	protected boolean populateExistingNode(TreeNGram parentNode, TreeNGram nodeToAdd, int order) {
		TreeNGram newChild = parentNode.addExistingNodeAsync(nodeToAdd, order);

		if (order < nodeToAdd.getCumulativeString().length()) {
			return populateExistingNode(newChild, nodeToAdd, order + 1);
		} else if (newChild != null) {
			return false;
		}

		return true;
	}

	public boolean addLetterTransition(String nGramString, Double amountToIncrement) {
		return populateLetterNode(rootNode, nGramString, 1, amountToIncrement);
	}

	protected boolean populateLetterNode(TreeNGram currentNode, String nGramString, Integer order, Double amountToIncrement) {
		boolean isNew = currentNode.addOrIncrementChildAsync(nGramString, order, amountToIncrement);

		if (order < nGramString.length()) {
			return populateLetterNode(currentNode.getChild(nGramString.charAt(order - 1)), nGramString, order + 1, amountToIncrement);
		}

		return isNew && order == this.order;
	}

	/**
	 * @param nGram
	 *            the N-Gram String to search by
	 * @return the exact matching NGramIndexNode
	 */
	public TreeNGram findExact(String nGram) {
		return findExactMatch(rootNode, nGram);
	}

	protected static TreeNGram findExactMatch(TreeNGram node, String nGramString) {
		TreeNGram nextNode = node.getChild(nGramString.charAt(0));

		if (nextNode == null) {
			return null;
		}

		if (nGramString.length() == 1) {
			return nextNode;
		}

		return findExactMatch(nextNode, nGramString.substring(1));
	}

	/**
	 * @param nGram
	 *            the N-Gram String to search by
	 * @return the longest matching NGramIndexNode
	 */
	public TreeNGram findLongest(String nGram) {
		return findLongestMatch(rootNode, nGram, null);
	}

	protected static TreeNGram findLongestMatch(TreeNGram node, String nGramString, TreeNGram longestMatch) {
		TreeNGram nextNode = node.getChild(nGramString.charAt(0));

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
	public TreeNGram getRootNode() {
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
	public Double getUnknownLetterNGramProbability() {
		return unknownLetterNGramProbability;
	}

	/**
	 * @param unknownLetterNGramProbability
	 *            the unknownLetterNGramProbability to set
	 */
	public void setUnknownLetterNGramProbability(Double unknownLetterNGramProbability) {
		this.unknownLetterNGramProbability = unknownLetterNGramProbability;
	}

	/**
	 * @return the indexOfCoincidence
	 */
	public Double getIndexOfCoincidence() {
		if (this.indexOfCoincidence == null) {
			this.indexOfCoincidence = 0.0;

			Double occurences;
			for (Map.Entry<Character, TreeNGram> entry : this.rootNode.getTransitions().entrySet()) {
				occurences = entry.getValue().getCount();
				this.indexOfCoincidence = this.indexOfCoincidence + (occurences * (occurences - 1.0));
			}

			occurences = this.rootNode.getCount();
			this.indexOfCoincidence = this.indexOfCoincidence / (occurences * (occurences - 1.0));
		}

		return indexOfCoincidence;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Map<Character, TreeNGram> transitions = rootNode.getTransitions();

		for (Map.Entry<Character, TreeNGram> entry : transitions.entrySet()) {
			if (entry.getValue() != null) {
				appendTransitions("", entry.getKey(), entry.getValue(), sb);
			}
		}

		return sb.toString();
	}

	protected void appendTransitions(String parent, Character symbol, TreeNGram node, StringBuilder sb) {
		sb.append("\n[" + parent + "] ->" + symbol + " | " + node.getCount());

		Map<Character, TreeNGram> transitions = node.getTransitions();

		if (transitions == null || transitions.isEmpty()) {
			return;
		}

		for (Map.Entry<Character, TreeNGram> entry : transitions.entrySet()) {
			if (entry.getValue() != null) {
				appendTransitions(parent + entry.getKey(), entry.getKey(), entry.getValue(), sb);
			}
		}
	}

	public void linkChildren(boolean includeWordBoundaries, TaskExecutor taskExecutor) {
		List<FutureTask<Void>> futures = new ArrayList<FutureTask<Void>>(26);
		FutureTask<Void> task;

		futures = new ArrayList<FutureTask<Void>>(26);

		for (Map.Entry<Character, TreeNGram> entry : this.rootNode.getTransitions().entrySet()) {
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

	protected void linkChild(TreeNGram node, String nGram, boolean includeWordBoundaries) {
		Map<Character, TreeNGram> transitions = node.getTransitions();

		if (nGram.length() == order) {
			for (Character letter : (includeWordBoundaries ? ModelConstants.LOWERCASE_LETTERS_AND_SPACE : ModelConstants.LOWERCASE_LETTERS)) {
				TreeNGram match = this.findExact(nGram.substring(1) + letter.toString());

				if (match != null) {
					node.putChild(letter, match);
				}
			}

			return;
		}

		for (Map.Entry<Character, TreeNGram> entry : transitions.entrySet()) {
			TreeNGram nextNode = entry.getValue();

			if (nextNode != null) {
				linkChild(nextNode, nGram + String.valueOf(entry.getKey()), includeWordBoundaries);
			}
		}
	}

	/**
	 * A concurrent task for linking leaf nodes in a Markov model.
	 */
	protected class LinkChildTask implements Callable<Void> {
		private Character	key;
		private TreeNGram	node;
		private boolean		includeWordBoundaries;

		/**
		 * @param key
		 *            the Character key to set
		 * @param node
		 *            the NGramIndexNode to set
		 * @param includeWordBoundaries
		 *            whether to include word boundaries
		 */
		public LinkChildTask(Character key, TreeNGram node, boolean includeWordBoundaries) {
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

	public void normalize(int order, long orderTotal, TaskExecutor taskExecutor) {
		List<FutureTask<Void>> futures = new ArrayList<FutureTask<Void>>(26);
		FutureTask<Void> task;

		for (Map.Entry<Character, TreeNGram> entry : this.getRootNode().getTransitions().entrySet()) {
			if (entry.getValue() != null) {
				task = new FutureTask<Void>(new NormalizeTask(entry.getValue(), order, orderTotal));
				futures.add(task);
				taskExecutor.execute(task);
			}
		}

		for (FutureTask<Void> future : futures) {
			try {
				future.get();
			} catch (InterruptedException ie) {
				log.error("Caught InterruptedException while waiting for NormalizeTask ", ie);
			} catch (ExecutionException ee) {
				log.error("Caught ExecutionException while waiting for NormalizeTask ", ee);
			}
		}
	}

	/**
	 * A concurrent task for normalizing a Markov model node.
	 */
	protected class NormalizeTask implements Callable<Void> {
		private TreeNGram	node;
		private int			order;
		private long		total;

		/**
		 * @param node
		 *            the NGramIndexNode to set
		 * @param order
		 *            the order to set
		 * @param total
		 *            the order to set
		 */
		public NormalizeTask(TreeNGram node, int order, long total) {
			this.node = node;
			this.order = order;
			this.total = total;
		}

		@Override
		public Void call() throws Exception {
			normalizeTerminal(this.node, this.order, this.total);

			return null;
		}
	}

	protected void normalizeTerminal(TreeNGram node, int order, long total) {
		if (node.getCumulativeString().length() == order) {
			node.setProbability(node.getCount() / (double) total);

			return;
		}

		Map<Character, TreeNGram> transitions = node.getTransitions();

		if (transitions == null || transitions.isEmpty()) {
			return;
		}

		for (Map.Entry<Character, TreeNGram> entry : transitions.entrySet()) {
			normalizeTerminal(entry.getValue(), order, total);
		}
	}

	public long size() {
		return countAll(this.getRootNode());
	}

	protected long countAll(TreeNGram node) {
		long sum = 1L;

		for (Map.Entry<Character, TreeNGram> entry : node.getTransitions().entrySet()) {
			sum += countAll(entry.getValue());
		}

		return sum;
	}
}
