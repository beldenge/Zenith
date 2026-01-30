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

package com.ciphertool.zenith.inference.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CiphertextTest {
    @Test
    public void given_validInput_when_constructing_then_returnsExpectedValue() {
        Ciphertext ciphertext = new Ciphertext("A");
        assertEquals("A", ciphertext.getValue());
        assertFalse(ciphertext.isLocked());

        Ciphertext locked = new Ciphertext("B", true);
        assertEquals("B", locked.getValue());
        assertTrue(locked.isLocked());
    }

    @Test
    public void given_validInput_when_cloningCopiesState_then_copiesState() {
        Ciphertext original = new Ciphertext("C", true);
        Ciphertext clone = original.clone();

        assertNotSame(original, clone);
        assertEquals(original.getValue(), clone.getValue());
        assertEquals(original.isLocked(), clone.isLocked());
    }
}
