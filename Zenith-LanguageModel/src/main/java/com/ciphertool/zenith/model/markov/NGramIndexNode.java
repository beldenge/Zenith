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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class NGramIndexNode {
	private static final Pattern			LOWERCASE_LETTERS	= Pattern.compile("[a-z]");
	private Map<Character, NGramIndexNode>	transitions;
	private TerminalInfo					terminalInfo;
	private NGramIndexNode					parent;

	public NGramIndexNode(NGramIndexNode parent) {
		this.parent = parent;
	}

	public NGramIndexNode(NGramIndexNode parent, TerminalInfo terminalInfo) {
		this.parent = parent;
		this.terminalInfo = terminalInfo;
	}

	public boolean containsChild(Character c) {
		return this.getTransitions().containsKey(c);
	}

	public NGramIndexNode getChild(Character c) {
		return this.getTransitions().get(c);
	}

	public synchronized boolean addOrIncrementChildAsync(Character firstLetter, int level, boolean isTerminal) {
		NGramIndexNode child = this.getChild(firstLetter);

		boolean isNew = false;

		if (child == null) {
			this.putChild(firstLetter, new NGramIndexNode(this));

			child = this.getChild(firstLetter);
		}

		if (isTerminal) {
			if (child.getTerminalInfo() == null) {
				child.setTerminalInfo(new TerminalInfo(level));

				isNew = true;
			}

			child.getTerminalInfo().increment();
		}

		return isNew;
	}

	public void putChild(Character c, NGramIndexNode child) {
		if (!LOWERCASE_LETTERS.matcher(c.toString()).matches()) {
			throw new IllegalArgumentException(
					"Attempted to add a character to the Markov Model which is outside the range of "
							+ LOWERCASE_LETTERS);
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
	 * @return the terminalInfo
	 */
	public TerminalInfo getTerminalInfo() {
		return terminalInfo;
	}

	/**
	 * @param terminalInfo
	 *            the terminalInfo to set
	 */
	public void setTerminalInfo(TerminalInfo terminalInfo) {
		this.terminalInfo = terminalInfo;
	}

	public String getCumulativeStringValue() {
		if (this.parent != null) {
			for (Map.Entry<Character, NGramIndexNode> entry : this.parent.transitions.entrySet()) {
				if (entry.getValue() == this) {
					return this.parent.getCumulativeStringValue() + "" + entry.getKey().toString();
				}
			}
		}

		return "";
	}
}
