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

package com.ciphertool.zenith.model.markov;

import com.ciphertool.zenith.model.entities.WordNGram;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WordNGramModelTest {
    @Test
    public void testPutAndGet() {
        WordNGramModel model = new WordNGramModel();

        WordNGram word = new WordNGram();
        word.setNGram("hello");
        word.setCount(42L);
        word.setLogProbability(-3.21d);

        model.putWordNGram("hello", word);

        assertTrue(model.contains("hello"));
        assertEquals(42L, model.getCount("hello"));
        assertEquals(-3.21d, model.getLogProbability("hello"), 0.00001d);
    }

    @Test
    public void testGetWordNGramMapIsUnmodifiable() {
        WordNGramModel model = new WordNGramModel();

        Map<String, WordNGram> map = model.getWordNGramMap();

        assertThrows(UnsupportedOperationException.class, () -> map.put("test", new WordNGram()));
    }
}
