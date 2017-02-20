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

import java.util.regex.Pattern;

public class ListNGram extends NGram {
	private static final Pattern LOWERCASE_LETTERS_AND_SPACE = Pattern.compile("[a-z +\\-\\.]+");

	public ListNGram(String nGramString) {
		if (nGramString == null || nGramString.isEmpty()
				|| !LOWERCASE_LETTERS_AND_SPACE.matcher(nGramString).matches()) {
			throw new IllegalArgumentException(
					"Attempted to create a node with an n-gram String which contains characters outside the range of "
							+ LOWERCASE_LETTERS_AND_SPACE + ".  n-gram: " + nGramString);
		}

		this.cumulativeString = nGramString;

		this.order = nGramString.length();

		this.increment();
	}
}
