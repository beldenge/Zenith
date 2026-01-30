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
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ArrayMarkovModelCacheTest {
    @TempDir
    Path tempDir;

    @Test
    public void given_writeAndRead_when_invoked_then_expected() throws IOException {
        ArrayMarkovModel model = new ArrayMarkovModel(5, 0.01f);

        TreeNGram unigram = new TreeNGram("a");
        unigram.setCount(10);
        unigram.setLogProbability(Math.log(0.1d));
        model.addNode(unigram);

        TreeNGram ngram = new TreeNGram("abcde");
        ngram.setCount(3);
        ngram.setLogProbability(-2.5d);
        model.addNode(ngram);

        Path cachePath = tempDir.resolve("zenith-model.array.bin");
        ArrayMarkovModelCache.write(cachePath, model, 123);

        ArrayMarkovModel loaded = ArrayMarkovModelCache.readIfValid(cachePath, 5, 123);

        assertNotNull(loaded);
        assertEquals(model.getOrder(), loaded.getOrder());
        assertEquals(model.getUnknownLetterNGramProbability(), loaded.getUnknownLetterNGramProbability());
        assertEquals(model.getMapSize(), loaded.getMapSize());

        Map<String, Long> expectedCounts = model.getFirstOrderNodes().stream()
                .collect(Collectors.toMap(TreeNGram::getCumulativeString, TreeNGram::getCount));
        Map<String, Long> actualCounts = loaded.getFirstOrderNodes().stream()
                .collect(Collectors.toMap(TreeNGram::getCumulativeString, TreeNGram::getCount));

        assertEquals(expectedCounts, actualCounts);
        assertEquals(model.findExact("abcde"), loaded.findExact("abcde"));
    }

    @Test
    public void given_readInvalidWhenMaxNGramsToKeepChanges_when_invoked_then_expected() throws IOException {
        ArrayMarkovModel model = new ArrayMarkovModel(5, 0.01f);

        TreeNGram unigram = new TreeNGram("a");
        unigram.setCount(1);
        unigram.setLogProbability(Math.log(0.1d));
        model.addNode(unigram);

        Path cachePath = tempDir.resolve("zenith-model.array.bin");
        ArrayMarkovModelCache.write(cachePath, model, 123);

        ArrayMarkovModel loaded = ArrayMarkovModelCache.readIfValid(cachePath, 5, 124);

        assertNull(loaded);
    }

    @Test
    public void given_readReturnsNullWhenFileDoesNotExist_when_invoked_then_expected() throws IOException {
        Path nonExistentPath = tempDir.resolve("does-not-exist.bin");

        ArrayMarkovModel loaded = ArrayMarkovModelCache.readIfValid(nonExistentPath, 5, 100);

        assertNull(loaded);
    }

    @Test
    public void given_readInvalidWhenOrderChanges_when_invoked_then_expected() throws IOException {
        ArrayMarkovModel model = new ArrayMarkovModel(5, 0.01f);

        TreeNGram unigram = new TreeNGram("a");
        unigram.setCount(1);
        unigram.setLogProbability(Math.log(0.1d));
        model.addNode(unigram);

        Path cachePath = tempDir.resolve("zenith-model-order.bin");
        ArrayMarkovModelCache.write(cachePath, model, 100);

        // Request a different order
        ArrayMarkovModel loaded = ArrayMarkovModelCache.readIfValid(cachePath, 6, 100);

        assertNull(loaded);
    }
}
