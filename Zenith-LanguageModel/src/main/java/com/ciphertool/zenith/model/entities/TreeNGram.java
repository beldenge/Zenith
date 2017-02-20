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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.data.annotation.Transient;

public class TreeNGram extends NGram {
	private static final Pattern		LOWERCASE_LETTERS_AND_SPACE	= Pattern.compile("[a-z +\\-\\.]");

	@Transient
	private Map<Character, TreeNGram>	transitions;

	public TreeNGram(String nGramString) {
		this.cumulativeString = nGramString;

		this.order = nGramString.length();
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
				child.setId(nodeToAdd.id);
				child.setCount(nodeToAdd.count);
				child.setConditionalProbability(nodeToAdd.conditionalProbability);
				child.setProbability(nodeToAdd.probability);
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
		if (this.transitions == null) {
			this.transitions = new HashMap<Character, TreeNGram>(1);
		}

		return this.transitions;
	}
}
