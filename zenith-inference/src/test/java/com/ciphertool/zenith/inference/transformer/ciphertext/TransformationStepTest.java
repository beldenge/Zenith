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

package com.ciphertool.zenith.inference.transformer.ciphertext;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TransformationStepTest {
    @Test
    public void given_validInput_when_constructing_then_returnsNotNull() {
        TransformationStep step = new TransformationStep("Transformer", Collections.singletonMap("key", 1));

        assertEquals("Transformer", step.getTransformerName());
        assertEquals(Collections.singletonMap("key", 1), step.getData());

        step.setTransformerName("Other");
        step.setData(Collections.singletonMap("value", "v"));

        assertEquals("Other", step.getTransformerName());
        assertEquals(Collections.singletonMap("value", "v"), step.getData());
        assertNotNull(step);
    }
}