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

public class UnidirectionalNGramIndexNode implements NGramIndexNode {
	private static final Pattern			LOWERCASE_LETTERS_AND_SPACE	= Pattern.compile("[a-z +\\-]");

	@Id
	private ObjectId						id;

	@Transient
	private Map<Character, NGramIndexNode>	transitions;

	private int								level						= -1;

	private long							count						= 0L;

	private BigDecimal						probability;

	private BigDecimal						conditionalProbability;

	@Transient
	private UnidirectionalNGramIndexNode	parent;

	private String							cumulativeString;

	public UnidirectionalNGramIndexNode(UnidirectionalNGramIndexNode parent, String nGramString, int level) {
		this.parent = parent;
		this.cumulativeString = nGramString;
		this.level = level;
	}

	/**
	 * @return the id
	 */
	@Override
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

	@Override
	public boolean containsChild(Character c) {
		return this.getTransitions().containsKey(c);
	}

	@Override
	public UnidirectionalNGramIndexNode getChild(Character c) {
		return (UnidirectionalNGramIndexNode) this.getTransitions().get(c);
	}

	@Override
	public void increment() {
		this.count += 1L;
	}

	/**
	 * @return the count
	 */
	@Override
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
	@Override
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

		UnidirectionalNGramIndexNode child = this.getChild(firstLetter);

		boolean isNew = false;

		if (child == null) {
			this.putChild(firstLetter, new UnidirectionalNGramIndexNode(this, nGramString.substring(0, level), level));

			child = this.getChild(firstLetter);

			isNew = true;
		}

		child.increment();

		return isNew;
	}

	public synchronized UnidirectionalNGramIndexNode addExistingNodeAsync(NGramIndexNode nodeToAdd, int level) {
		UnidirectionalNGramIndexNode node = (UnidirectionalNGramIndexNode) nodeToAdd;

		Character firstLetter = node.cumulativeString.charAt(level - 1);

		UnidirectionalNGramIndexNode child = this.getChild(firstLetter);

		if (level == node.level) {
			if (child == null) {
				node.setParent(this);

				this.putChild(firstLetter, node);
			} else {
				child.setId(node.id);
				child.setCount(node.count);
				child.setConditionalProbability(node.conditionalProbability);
				child.setProbability(node.probability);
			}

			return null;
		} else if (child == null) {
			this.putChild(firstLetter, new UnidirectionalNGramIndexNode(this, node.cumulativeString.substring(0, level),
					level));
		}

		return this.getChild(firstLetter);
	}

	@Override
	public UnidirectionalNGramIndexNode putChild(Character c, NGramIndexNode child) {
		UnidirectionalNGramIndexNode node = (UnidirectionalNGramIndexNode) child;

		if (!LOWERCASE_LETTERS_AND_SPACE.matcher(c.toString()).matches()) {
			throw new IllegalArgumentException(
					"Attempted to add a character to the Markov Model which is outside the range of "
							+ LOWERCASE_LETTERS_AND_SPACE);
		}

		return (UnidirectionalNGramIndexNode) this.getTransitions().put(c, node);
	}

	/**
	 * @return the transitions array
	 */
	@Override
	public Map<Character, NGramIndexNode> getTransitions() {
		if (this.transitions == null) {
			this.transitions = new HashMap<Character, NGramIndexNode>(1);
		}

		return this.transitions;
	}

	/**
	 * @return the cumulativeString
	 */
	@Override
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

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(UnidirectionalNGramIndexNode parent) {
		this.parent = parent;
	}
}
