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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CiphertextEntropyEvaluatorTest {
    @Test
    public void testEvaluateUniformEntropy() {
        Cipher cipher = new Cipher("test", 1, 4);
        cipher.setCiphertext(Arrays.asList("A", "B", "A", "B"));

        CiphertextEntropyEvaluator evaluator = new CiphertextEntropyEvaluator();

        assertEquals(1.0f, evaluator.evaluate(cipher), 0.0001f);
    }
}
