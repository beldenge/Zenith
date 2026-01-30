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

public class CiphertextIndexOfCoincidenceEvaluatorTest {
    @Test
    public void given_validInput_when_evaluatingCountsCoincidence_then_returnsExpectedValue() {
        Cipher cipher = new Cipher("test", 1, 4);
        cipher.setCiphertext(Arrays.asList("A", "B", "A", "B"));

        CiphertextIndexOfCoincidenceEvaluator evaluator = new CiphertextIndexOfCoincidenceEvaluator();

        assertEquals(1f / 3f, evaluator.evaluate(cipher), 0.0001f);
    }

    @Test
    public void given_validInput_when_evaluatingShortCipherReturnsZero_then_returnsExpectedValue() {
        Cipher cipher = new Cipher("test", 1, 1);
        cipher.setCiphertext(Arrays.asList("A"));

        CiphertextIndexOfCoincidenceEvaluator evaluator = new CiphertextIndexOfCoincidenceEvaluator();

        assertEquals(0f, evaluator.evaluate(cipher), 0.0001f);
    }
}
