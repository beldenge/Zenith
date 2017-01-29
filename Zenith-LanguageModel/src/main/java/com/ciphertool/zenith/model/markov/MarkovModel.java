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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.task.TaskExecutor;

import com.ciphertool.zenith.math.MathConstants;

public class MarkovModel {
	private static Logger					log							= LoggerFactory.getLogger(MarkovModel.class);

	private static final List<Character>	LOWERCASE_LETTERS			= Arrays.asList(new Character[] { 'a', 'b', 'c',
			'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
			'y', 'z' });
	private static final List<Character>	LOWERCASE_LETTERS_AND_SPACE	= new ArrayList<>(LOWERCASE_LETTERS);

	static {
		LOWERCASE_LETTERS_AND_SPACE.add(' ');
	}

	private Boolean			includeWordBoundaries;
	private NGramIndexNode	rootNode		= new NGramIndexNode(null, "", 0);
	private boolean			postProcessed	= false;
	private Integer			order;
	private TaskExecutor	taskExecutor;
	private Long			numWithCountOfOne;

	/**
	 * A concurrent task for normalizing a Markov model node.
	 */
	protected class NormalizeTask implements Callable<Void> {
		private NGramIndexNode	node;
		private long			parentCount;

		/**
		 * @param node
		 *            the NGramIndexNode to set
		 * @param parentCount
		 *            the parentCount to set
		 */
		public NormalizeTask(NGramIndexNode node, long parentCount) {
			this.node = node;
			this.parentCount = parentCount;
		}

		@Override
		public Void call() throws Exception {
			normalize(this.node, this.parentCount);

			return null;
		}
	}

	/**
	 * A concurrent task for linking leaf nodes in a Markov model.
	 */
	protected class LinkChildTask implements Callable<Void> {
		private Character		key;
		private NGramIndexNode	node;

		/**
		 * @param key
		 *            the Character key to set
		 * @param node
		 *            the NGramIndexNode to set
		 */
		public LinkChildTask(Character key, NGramIndexNode node) {
			this.key = key;
			this.node = node;
		}

		@Override
		public Void call() throws Exception {
			linkChild(this.node, String.valueOf(this.key));

			return null;
		}
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

	public void postProcess(boolean normalize, boolean linkChildren) {
		if (postProcessed) {
			return;
		}

		long start = System.currentTimeMillis();

		log.info("Starting Markov model post-processing...");

		Map<Character, NGramIndexNode> initialTransitions = this.rootNode.getTransitions();

		List<FutureTask<Void>> futures = new ArrayList<FutureTask<Void>>(26);
		FutureTask<Void> task;

		if (normalize) {
			for (Map.Entry<Character, NGramIndexNode> entry : initialTransitions.entrySet()) {
				if (entry.getValue() != null) {
					task = new FutureTask<Void>(new NormalizeTask(entry.getValue(), rootNode.getCount()));
					futures.add(task);
					this.taskExecutor.execute(task);
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

		if (linkChildren) {
			futures = new ArrayList<FutureTask<Void>>(26);

			for (Map.Entry<Character, NGramIndexNode> entry : initialTransitions.entrySet()) {
				if (entry.getValue() != null) {
					task = new FutureTask<Void>(new LinkChildTask(entry.getKey(), entry.getValue()));
					futures.add(task);
					this.taskExecutor.execute(task);
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

		postProcessed = true;

		log.info("Time elapsed: " + (System.currentTimeMillis() - start) + "ms");
	}

	protected void normalize(NGramIndexNode node, long parentCount) {
		node.setConditionalProbability(BigDecimal.valueOf(node.getCount()).divide(BigDecimal.valueOf(parentCount), MathConstants.PREC_10_HALF_UP));

		Map<Character, NGramIndexNode> transitions = node.getTransitions();

		if (transitions == null || transitions.isEmpty()) {
			return;
		}

		for (Map.Entry<Character, NGramIndexNode> entry : transitions.entrySet()) {
			normalize(entry.getValue(), node.getCount());
		}
	}

	protected void linkChild(NGramIndexNode node, String nGram) {
		Map<Character, NGramIndexNode> transitions = node.getTransitions();

		if (nGram.length() == order) {
			for (Character letter : (includeWordBoundaries ? LOWERCASE_LETTERS_AND_SPACE : LOWERCASE_LETTERS)) {
				NGramIndexNode match = this.findLongest(nGram.substring(1) + letter.toString());

				if (match != null) {
					node.putChild(letter, match);
				}
			}

			return;
		}

		for (Map.Entry<Character, NGramIndexNode> entry : transitions.entrySet()) {
			NGramIndexNode nextNode = entry.getValue();

			if (nextNode != null) {
				linkChild(nextNode, nGram + String.valueOf(entry.getKey()));
			}
		}
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

	/**
	 * @return the numWithCountOfOne
	 */
	public Long getNumWithCountOfOne() {
		return numWithCountOfOne;
	}

	/**
	 * @param numWithCountOfOne
	 *            the numWithCountOfOne to set
	 */
	public void setNumWithCountOfOne(Long numWithCountOfOne) {
		this.numWithCountOfOne = numWithCountOfOne;
	}

	/**
	 * @return the order
	 */
	public Integer getOrder() {
		return order;
	}

	/**
	 * @param order
	 *            the order to set
	 */
	@Required
	public void setOrder(Integer order) {
		this.order = order;
	}

	/**
	 * @param taskExecutor
	 *            the taskExecutor to set
	 */
	@Required
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * @param includeWordBoundaries
	 *            the includeWordBoundaries to set
	 */
	@Required
	public void setIncludeWordBoundaries(Boolean includeWordBoundaries) {
		this.includeWordBoundaries = includeWordBoundaries;
	}
}
