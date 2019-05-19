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

package com.ciphertool.zenith.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelConstants {
	public static final List<Character>	LOWERCASE_VOWELS		= Arrays.asList(new Character[] { 'a', 'e', 'i', 'o', 'u' });
	public static final List<Character>	LOWERCASE_CONSONANTS	= Arrays.asList(new Character[] { 'b', 'c', 'd', 'f',
			'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z' });
	public static final List<Character>	LOWERCASE_LETTERS		= new ArrayList<>();

	static {
		LOWERCASE_LETTERS.addAll(LOWERCASE_VOWELS);
		LOWERCASE_LETTERS.addAll(LOWERCASE_CONSONANTS);
	}
}
