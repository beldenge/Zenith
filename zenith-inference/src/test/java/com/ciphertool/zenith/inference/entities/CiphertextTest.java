/**
 * Copyright 2017-2019 George Belden
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

import org.junit.Test;

import static org.junit.Assert.*;

public class CiphertextTest {
    @Test
    public void testConstructor() {
        Integer ciphertextIdToSet = new Integer(123);
        String valueToSet = "ciphertextValue";
        Ciphertext ciphertext = new Ciphertext(ciphertextIdToSet, valueToSet);

        assertSame(ciphertextIdToSet, ciphertext.getCiphertextId());
        assertEquals(valueToSet, ciphertext.getValue());
    }

    @Test
    public void testSetCiphertextId() {
        Integer ciphertextIdToSet = new Integer(123);
        Ciphertext ciphertext = new Ciphertext();
        ciphertext.setCiphertextId(ciphertextIdToSet);

        assertSame(ciphertextIdToSet, ciphertext.getCiphertextId());
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
        Integer baseCiphertextId = new Integer(123);
        String baseValue = "baseValue";

        Ciphertext base = new Ciphertext(baseCiphertextId, baseValue);

        Ciphertext ciphertextEqualToBase = new Ciphertext(baseCiphertextId, baseValue);
        assertEquals(base, ciphertextEqualToBase);

        Ciphertext ciphertextWithDifferentCiphertextId = new Ciphertext(321, baseValue);
        assertFalse(base.equals(ciphertextWithDifferentCiphertextId));

        Ciphertext ciphertextWithDifferentValue = new Ciphertext(baseCiphertextId, "differentValue");
        assertFalse(base.equals(ciphertextWithDifferentValue));

        Ciphertext ciphertextWithNullPropertiesA = new Ciphertext();
        Ciphertext ciphertextWithNullPropertiesB = new Ciphertext();
        assertEquals(ciphertextWithNullPropertiesA, ciphertextWithNullPropertiesB);
    }
}
