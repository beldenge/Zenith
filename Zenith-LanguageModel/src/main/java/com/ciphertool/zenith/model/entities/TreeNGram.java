/**
 * Copyright 2017-2019 George Belden
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

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Document
public class TreeNGram {
	private static final Pattern		LOWERCASE_LETTERS_AND_SPACE	= Pattern.compile("[a-z \\.]");

	@Id
	protected ObjectId					id;

	protected long						count						= 0L;

	protected Double probability;
	protected Double conditionalProbability;
	protected Double logProbability;
	protected Double logConditionalProbability;

	protected String					cumulativeString;

	protected Integer					order;

	@Transient
	private Map<Character, TreeNGram>	transitions = new HashMap<>(1);

	// Required by Spring Data
	public TreeNGram() {}

	public TreeNGram(String nGramString) {
		this.cumulativeString = nGramString;

		this.order = nGramString.length();
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
	public Double getProbability() {
		return this.probability;
	}

	/**
	 * All current usages of this method are thread-safe, but since it's used in a multi-threaded way, this is a
	 * defensive measure in case future usage changes are not thread-safe.
	 * 
	 * @param probability
	 *            the probability to set
	 */
	public synchronized void setProbability(Double probability) {
		this.probability = probability;
	}

	/**
	 * @return the conditionalProbability
	 */
	public Double getConditionalProbability() {
		return conditionalProbability;
	}

	public Double getLogProbability() {
		return logProbability;
	}

	public void setLogProbability(Double logProbability) {
		this.logProbability = logProbability;
	}

	public Double getLogConditionalProbability() {
		return logConditionalProbability;
	}

	public void setLogConditionalProbability(Double logConditionalProbability) {
		this.logConditionalProbability = logConditionalProbability;
	}

	/**
	 * All current usages of this method are thread-safe, but since it's used in a multi-threaded way, this is a
	 * defensive measure in case future usage changes are not thread-safe.
	 * 
	 * @param conditionalProbability
	 *            the conditionalProbability to set
	 */
	public synchronized void setConditionalProbability(Double conditionalProbability) {
		this.conditionalProbability = conditionalProbability;
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
	public void setOrder(Integer order) {
		this.order = order;
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

	public boolean containsChild(Character c) {
		return this.getTransitions().containsKey(c);
	}

	public TreeNGram getChild(Character c) {
		return this.getTransitions().get(c);
	}

	public synchronized boolean addOrIncrementChildAsync(String nGramString, int order) {
		Character firstLetter = nGramString.charAt(order - 1);

		TreeNGram child = this.getChild(firstLetter);

		boolean isNew = false;

		if (child == null) {
			this.putChild(firstLetter, new TreeNGram(nGramString.substring(0, order)));

			child = this.getChild(firstLetter);

			isNew = true;
		}

		child.increment();

		return isNew;
	}

	public synchronized TreeNGram addExistingNodeAsync(TreeNGram nodeToAdd, int order) {
		Character firstLetter = nodeToAdd.cumulativeString.charAt(order - 1);

		TreeNGram child = this.getChild(firstLetter);

		if (order == nodeToAdd.cumulativeString.length()) {
			if (child == null) {
				this.putChild(firstLetter, nodeToAdd);
			} else {
				child.id = nodeToAdd.id;
				child.count = nodeToAdd.count;
				child.conditionalProbability = nodeToAdd.conditionalProbability;
				child.probability = nodeToAdd.probability;
				child.logProbability = nodeToAdd.logProbability;
				child.logConditionalProbability = nodeToAdd.logConditionalProbability;
			}

			return null;
		} else if (child == null) {
			this.putChild(firstLetter, new TreeNGram(nodeToAdd.cumulativeString.substring(0, order)));
		}

		return this.getChild(firstLetter);
	}

	public TreeNGram putChild(Character c, TreeNGram child) {
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
	public Map<Character, TreeNGram> getTransitions() {
		return this.transitions;
	}
}
