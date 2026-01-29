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

import com.ciphertool.zenith.model.entities.TreeNGram;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArrayMarkovModelTest {
    @Test
    public void testComputeArrayIndex() {
        ArrayMarkovModel model = new ArrayMarkovModel(5, 0.01f);

        assertEquals(0, model.computeArrayIndex("aaaaa"));
        assertEquals(1, model.computeArrayIndex("aaaab"));
        assertEquals(26 * 26 * 26 * 26, model.computeArrayIndex("baaaa"));
    }

    @Test
    public void testAddNodeAndFindExact() {
        ArrayMarkovModel model = new ArrayMarkovModel(5, 0.01f);

        TreeNGram ngram = new TreeNGram("abcde");
        ngram.setLogProbability(-2.5d);

        model.addNode(ngram);

        assertEquals(1, model.getMapSize());
        assertEquals(-2.5f, model.findExact("abcde"), 0.0001f);
        assertEquals(model.getUnknownLetterNGramLogProbability(), model.findExact("zzzzz"), 0.0001f);
    }

    @Test
    public void testAddDuplicateNodeThrows() {
        ArrayMarkovModel model = new ArrayMarkovModel(5, 0.01f);

        TreeNGram ngram = new TreeNGram("abcde");
        ngram.setLogProbability(-1.0d);

        model.addNode(ngram);

        TreeNGram duplicate = new TreeNGram("abcde");
        duplicate.setLogProbability(-2.0d);

        assertThrows(IllegalStateException.class, () -> model.addNode(duplicate));
    }

    @Test
    public void testGetTotalNGramCount() {
        ArrayMarkovModel model = new ArrayMarkovModel(1, 0.01f);

        TreeNGram a = new TreeNGram("a");
        a.setCount(2L);
        TreeNGram b = new TreeNGram("b");
        b.setCount(3L);

        model.addNode(a);
        model.addNode(b);

        assertEquals(0, model.getMapSize());
        assertEquals(5L, model.getTotalNGramCount());
    }
}
