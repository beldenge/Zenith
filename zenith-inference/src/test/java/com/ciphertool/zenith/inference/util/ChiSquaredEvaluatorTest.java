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

package com.ciphertool.zenith.inference.util;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChiSquaredEvaluatorTest {
    @Test
    public void testEvaluate_ReturnsZeroForExpectedDistribution() {
        ChiSquaredEvaluator evaluator = new ChiSquaredEvaluator();
        setLetterMarkovModel(evaluator, buildLetterModel());

        Cipher cipher = new Cipher("test", 1, 4);

        float result = evaluator.evaluate(null, cipher, "aabc");

        assertEquals(0f, result, 0.0001f);
    }

    @Test
    public void testEvaluate_ReturnsExpectedSumForDifferentDistribution() {
        ChiSquaredEvaluator evaluator = new ChiSquaredEvaluator();
        setLetterMarkovModel(evaluator, buildLetterModel());

        Cipher cipher = new Cipher("test", 1, 4);

        Map<String, Object> precomputed = evaluator.precompute(cipher);
        float result = evaluator.evaluate(precomputed, cipher, "aaaa");

        assertEquals(4f, result, 0.0001f);
    }

    private ArrayMarkovModel buildLetterModel() {
        ArrayMarkovModel model = new ArrayMarkovModel(1, 0.01f);

        TreeNGram a = new TreeNGram("a");
        a.setCount(2L);
        TreeNGram b = new TreeNGram("b");
        b.setCount(1L);
        TreeNGram c = new TreeNGram("c");
        c.setCount(1L);

        model.addNode(a);
        model.addNode(b);
        model.addNode(c);

        return model;
    }

    private void setLetterMarkovModel(ChiSquaredEvaluator evaluator, ArrayMarkovModel model) {
        try {
            Field field = ChiSquaredEvaluator.class.getDeclaredField("letterMarkovModel");
            field.setAccessible(true);
            field.set(evaluator, model);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new AssertionError("Unable to set letterMarkovModel for ChiSquaredEvaluator.", exception);
        }
    }
}
