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

public class BidirectionalNGramIndexNode implements NGramIndexNode {
	private static final Pattern			LOWERCASE_LETTERS_AND_SPACE	= Pattern.compile("[a-z +\\-]");

	@Id
	private ObjectId						id;

	@Transient
	private Map<Character, NGramIndexNode>	transitions;

	private long							count						= 0L;

	private BigDecimal						probability;

	@Transient
	private BidirectionalNGramIndexNode		frontParent;

	@Transient
	private BidirectionalNGramIndexNode		backParent;

	private String							cumulativeString;

	public BidirectionalNGramIndexNode(BidirectionalNGramIndexNode frontParent, BidirectionalNGramIndexNode backParent,
			String nGramString) {
		this.frontParent = frontParent;
		this.backParent = backParent;
		this.cumulativeString = nGramString;
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
	public BidirectionalNGramIndexNode getChild(Character c) {
		return (BidirectionalNGramIndexNode) this.getTransitions().get(c);
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

	public synchronized boolean addOrIncrementChildAsync(String nGramString, boolean isFront, int letterIndex) {
		Character nextLetter = nGramString.charAt(letterIndex);

		BidirectionalNGramIndexNode child = this.getChild(nextLetter);

		boolean isNew = false;

		if (child == null) {
			this.putChild(nextLetter, new BidirectionalNGramIndexNode(isFront ? this : null, isFront ? null : this,
					nGramString.substring(isFront ? 0 : letterIndex, isFront ? letterIndex
							+ 1 : nGramString.length())));

			child = this.getChild(nextLetter);

			isNew = true;
		}

		child.increment();

		return isNew;
	}

	public synchronized BidirectionalNGramIndexNode addExistingNodeAsync(NGramIndexNode nodeToAdd, int letterIndex, boolean isFront, boolean isLast) {
		BidirectionalNGramIndexNode node = (BidirectionalNGramIndexNode) nodeToAdd;

		Character nextLetter = node.cumulativeString.charAt(letterIndex);

		BidirectionalNGramIndexNode child = this.getChild(nextLetter);

		if (isLast) {
			if (child == null) {
				if (isFront) {
					node.setFrontParent(this);
				} else {
					node.setBackParent(this);
				}

				this.putChild(nextLetter, node);
			} else {
				child.setId(node.id);
				child.setCount(node.count);
				child.setProbability(node.probability);
			}

			return null;
		} else if (child == null) {
			this.putChild(nextLetter, new BidirectionalNGramIndexNode(isFront ? this : null, isFront ? null : this,
					node.cumulativeString.substring(isFront ? 0 : letterIndex, isFront ? letterIndex
							+ 1 : node.cumulativeString.length())));
		}

		return this.getChild(nextLetter);
	}

	@Override
	public BidirectionalNGramIndexNode putChild(Character c, NGramIndexNode child) {
		BidirectionalNGramIndexNode node = (BidirectionalNGramIndexNode) child;

		if (!LOWERCASE_LETTERS_AND_SPACE.matcher(c.toString()).matches()) {
			throw new IllegalArgumentException(
					"Attempted to add a character to the Markov Model which is outside the range of "
							+ LOWERCASE_LETTERS_AND_SPACE);
		}

		return (BidirectionalNGramIndexNode) this.getTransitions().put(c, node);
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
	 * @param frontParent
	 *            the frontParent to set
	 */
	public void setFrontParent(BidirectionalNGramIndexNode frontParent) {
		this.frontParent = frontParent;
	}

	/**
	 * @param backParent
	 *            the backParent to set
	 */
	public void setBackParent(BidirectionalNGramIndexNode backParent) {
		this.backParent = backParent;
	}
}
