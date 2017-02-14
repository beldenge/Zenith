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

package com.ciphertool.zenith.model.entities;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class NGramIndexNode {
	private static final Pattern			LOWERCASE_LETTERS_AND_SPACE	= Pattern.compile("[a-z +\\-\\.]");

	@Id
	private ObjectId						id;

	@Transient
	private Map<Character, NGramIndexNode>	transitions;

	private long							count						= 0L;

	private BigDecimal						probability;

	private BigDecimal						conditionalProbability;

	private String							cumulativeString;

	public NGramIndexNode() {
	}

	public NGramIndexNode(String nGramString) {
		this.cumulativeString = nGramString;
	}

	/**
	 * @return the id
	 */
	public ObjectId getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(ObjectId id) {
		this.id = id;
	}

	public boolean containsChild(Character c) {
		return this.getTransitions().containsKey(c);
	}

	public NGramIndexNode getChild(Character c) {
		return this.getTransitions().get(c);
	}

	public void increment() {
		this.count += 1L;
	}

	/**
	 * @return the count
	 */
	public long getCount() {
		return this.count;
	}

	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(long count) {
		this.count = count;
	}

	/**
	 * @return the probability
	 */
	public BigDecimal getProbability() {
		return this.probability;
	}

	/**
	 * All current usages of this method are thread-safe, but since it's used in a multi-threaded way, this is a
	 * defensive measure in case future usage changes are not thread-safe.
	 * 
	 * @param probability
	 *            the probability to set
	 */
	public synchronized void setProbability(BigDecimal probability) {
		this.probability = probability;
	}

	/**
	 * @return the conditionalProbability
	 */
	public BigDecimal getConditionalProbability() {
		return conditionalProbability;
	}

	/**
	 * All current usages of this method are thread-safe, but since it's used in a multi-threaded way, this is a
	 * defensive measure in case future usage changes are not thread-safe.
	 * 
	 * @param conditionalProbability
	 *            the conditionalProbability to set
	 */
	public synchronized void setConditionalProbability(BigDecimal conditionalProbability) {
		this.conditionalProbability = conditionalProbability;
	}

	public synchronized boolean addOrIncrementChildAsync(String nGramString, int order) {
		Character firstLetter = nGramString.charAt(order - 1);

		NGramIndexNode child = this.getChild(firstLetter);

		boolean isNew = false;

		if (child == null) {
			this.putChild(firstLetter, new NGramIndexNode(nGramString.substring(0, order)));

			child = this.getChild(firstLetter);

			isNew = true;
		}

		child.increment();

		return isNew;
	}

	public synchronized NGramIndexNode addExistingNodeAsync(NGramIndexNode nodeToAdd, int order) {
		Character firstLetter = nodeToAdd.cumulativeString.charAt(order - 1);

		NGramIndexNode child = this.getChild(firstLetter);

		if (order == nodeToAdd.cumulativeString.length()) {
			if (child == null) {
				this.putChild(firstLetter, nodeToAdd);
			} else {
				child.setId(nodeToAdd.id);
				child.setCount(nodeToAdd.count);
				child.setConditionalProbability(nodeToAdd.conditionalProbability);
				child.setProbability(nodeToAdd.probability);
			}

			return null;
		} else if (child == null) {
			this.putChild(firstLetter, new NGramIndexNode(nodeToAdd.cumulativeString.substring(0, order)));
		}

		return this.getChild(firstLetter);
	}

	public NGramIndexNode putChild(Character c, NGramIndexNode child) {
		if (!LOWERCASE_LETTERS_AND_SPACE.matcher(c.toString()).matches()) {
			throw new IllegalArgumentException(
					"Attempted to add a character to the Markov Model which is outside the range of "
							+ LOWERCASE_LETTERS_AND_SPACE);
		}

		return this.getTransitions().put(c, child);
	}

	/**
	 * @return the transitions array
	 */
	public Map<Character, NGramIndexNode> getTransitions() {
		if (this.transitions == null) {
			this.transitions = new HashMap<Character, NGramIndexNode>(1);
		}

		return this.transitions;
	}

	/**
	 * @return the cumulativeString
	 */
	public String getCumulativeString() {
		return cumulativeString;
	}

	/**
	 * @param cumulativeString
	 *            the cumulativeString to set
	 */
	public void setCumulativeString(String cumulativeString) {
		this.cumulativeString = cumulativeString;
	}
}
