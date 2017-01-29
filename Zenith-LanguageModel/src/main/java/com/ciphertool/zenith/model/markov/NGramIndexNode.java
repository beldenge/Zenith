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
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class NGramIndexNode {
	private static final Pattern			LOWERCASE_LETTERS_AND_SPACE	= Pattern.compile("[a-z ]");

	@Id
	private ObjectId						id;

	@Transient
	private Map<Character, NGramIndexNode>	transitions;

	private int								level						= -1;

	private long							count						= 0L;

	private BigDecimal						probability;

	private BigDecimal						conditionalProbability;

	@Transient
	private NGramIndexNode					parent;

	private String							cumulativeString;

	public NGramIndexNode(NGramIndexNode parent, String nGramString, int level) {
		this.parent = parent;
		this.cumulativeString = nGramString;
		this.level = level;
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
	 * @return the level
	 */
	public int getLevel() {
		return this.level;
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

	public synchronized boolean addOrIncrementChildAsync(String nGramString, int level) {
		Character firstLetter = nGramString.charAt(level - 1);

		NGramIndexNode child = this.getChild(firstLetter);

		boolean isNew = false;

		if (child == null) {
			this.putChild(firstLetter, new NGramIndexNode(this, nGramString, level));

			child = this.getChild(firstLetter);

			isNew = true;
		}

		child.increment();

		return isNew;
	}

	public void putChild(Character c, NGramIndexNode child) {
		if (!LOWERCASE_LETTERS_AND_SPACE.matcher(c.toString()).matches()) {
			throw new IllegalArgumentException(
					"Attempted to add a character to the Markov Model which is outside the range of "
							+ LOWERCASE_LETTERS_AND_SPACE);
		}

		this.getTransitions().put(c, child);
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
