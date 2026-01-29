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

package com.ciphertool.zenith.inference.statistics;

import com.ciphertool.zenith.inference.entities.Cipher;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CiphertextNgramEvaluatorTest {
    @Test
    public void testEvaluateBuildsCounts() {
        Cipher cipher = new Cipher("test", 1, 3);
        cipher.setCiphertext(Arrays.asList("A", "B", "A"));

        CiphertextNgramEvaluator evaluator = new CiphertextNgramEvaluator();

        Map<String, Integer> bigrams = evaluator.evaluate(cipher, 2);

        assertEquals(2, bigrams.size());
        assertEquals(1, bigrams.get("A B"));
        assertEquals(1, bigrams.get("B A"));
    }

    @Test
    public void testEvaluateSizeOneAndOversized() {
        Cipher cipher = new Cipher("test", 1, 3);
        cipher.setCiphertext(Arrays.asList("A", "B", "A"));

        CiphertextNgramEvaluator evaluator = new CiphertextNgramEvaluator();

        Map<String, Integer> unigrams = evaluator.evaluate(cipher, 1);
        assertEquals(2, unigrams.get("A"));
        assertEquals(1, unigrams.get("B"));

        Map<String, Integer> oversized = evaluator.evaluate(cipher, 4);
        assertTrue(oversized.isEmpty());
    }
}
