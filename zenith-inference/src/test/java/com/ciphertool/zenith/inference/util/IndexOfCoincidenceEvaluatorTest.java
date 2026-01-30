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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexOfCoincidenceEvaluatorTest {
    @Test
    public void given_missingDependency_when_evaluatingNoRepeatsReturnsZero_then_returnsExpectedValue() {
        IndexOfCoincidenceEvaluator evaluator = new IndexOfCoincidenceEvaluator();

        Cipher cipher = new Cipher("test", 1, 4);

        float result = evaluator.evaluate(null, cipher, "abcd");

        assertEquals(0f, result, 0.0001f);
    }

    @Test
    public void given_validInput_when_evaluatingRepeatedLettersReturnsExpectedValue_then_returnsExpectedValue() {
        IndexOfCoincidenceEvaluator evaluator = new IndexOfCoincidenceEvaluator();

        Cipher cipher = new Cipher("test", 1, 4);

        float result = evaluator.evaluate(null, cipher, "aabb");

        assertEquals(1f / 3f, result, 0.0001f);
    }
}
