/**
 * Copyright 2017-2019 George Belden
 * <p>
 * This file is part of Zenith.
 * <p>
 * Zenith is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * Zenith is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.model;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;

import java.util.Comparator;

public class LanguageConstants {
    public static final CharList LOWERCASE_VOWELS = new CharArrayList(new char[]{'a', 'e', 'i', 'o', 'u'});
    public static final CharList LOWERCASE_CONSONANTS = new CharArrayList(new char[]{'b', 'c', 'd', 'f',
            'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'});
    public static final CharList LOWERCASE_LETTERS = new CharArrayList();

    static {
        LOWERCASE_LETTERS.addAll(LOWERCASE_VOWELS);
        LOWERCASE_LETTERS.addAll(LOWERCASE_CONSONANTS);
        LOWERCASE_LETTERS.sort(Comparator.comparing(Character::charValue));
    }

    public static final int LOWERCASE_LETTERS_SIZE = LOWERCASE_LETTERS.size();
}
