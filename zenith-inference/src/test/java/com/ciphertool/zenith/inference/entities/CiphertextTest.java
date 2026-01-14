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

import static org.junit.jupiter.api.Assertions.*;

public class CiphertextTest {
    @Test
    public void testConstructor() {
        String valueToSet = "ciphertextValue";
        Ciphertext ciphertext = new Ciphertext(valueToSet);

        assertEquals(valueToSet, ciphertext.getValue());
    }

    @Test
    public void testSetValue() {
        String valueToSet = "ciphertextValue";
        Ciphertext ciphertext = new Ciphertext();
        ciphertext.setValue(valueToSet);

        assertEquals(valueToSet, ciphertext.getValue());
    }

    @Test
    public void testEquals() {
        String baseValue = "baseValue";

        Ciphertext base = new Ciphertext(baseValue);

        Ciphertext ciphertextEqualToBase = new Ciphertext(baseValue);
        assertEquals(base, ciphertextEqualToBase);

        Ciphertext ciphertextWithDifferentValue = new Ciphertext("differentValue");
        assertNotEquals(base, ciphertextWithDifferentValue);

        Ciphertext ciphertextWithNullPropertiesA = new Ciphertext();
        Ciphertext ciphertextWithNullPropertiesB = new Ciphertext();
        assertEquals(ciphertextWithNullPropertiesA, ciphertextWithNullPropertiesB);
    }
}
