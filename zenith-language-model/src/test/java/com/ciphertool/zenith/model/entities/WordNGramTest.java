/*
 * Copyright 2017-2026 George Belden
 *
 * This file is part of Zenith.
 *
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.model.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WordNGramTest {
    @Test
    public void given_validInput_when_accessors_then_returnsExpectedValue() {
        WordNGram ngram = new WordNGram();
        ngram.setOrder(2);
        ngram.setNGram("hello");
        ngram.setCount(42L);
        ngram.setLogProbability(-1.5d);

        assertEquals(2, ngram.getOrder());
        assertEquals("hello", ngram.getNGram());
        assertEquals(42L, ngram.getCount());
        assertEquals(-1.5d, ngram.getLogProbability(), 0.000001d);
    }
}
