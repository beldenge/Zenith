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

package com.ciphertool.zenith.api.model;

import com.ciphertool.zenith.inference.entities.Cipher;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CipherRequestTest {

    @Test
    void isLengthValid_whenLengthMatchesRowsTimesColumns_returnsTrue() {
        CipherRequest request = new CipherRequest();
        request.setRows(2);
        request.setColumns(3);
        request.setCiphertext(Arrays.asList("A", "B", "C", "D", "E", "F"));

        assertTrue(request.isLengthValid());
    }

    @Test
    void isLengthValid_whenLengthDoesNotMatch_returnsFalse() {
        CipherRequest request = new CipherRequest();
        request.setRows(2);
        request.setColumns(3);
        request.setCiphertext(Arrays.asList("A", "B", "C", "D", "E"));

        assertFalse(request.isLengthValid());
    }

    @Test
    void isLengthValid_whenEmpty_andDimensionsZero_returnsTrue() {
        CipherRequest request = new CipherRequest();
        request.setRows(0);
        request.setColumns(0);
        request.setCiphertext(List.of());

        assertTrue(request.isLengthValid());
    }

    @Test
    void asCipher_createsCorrectCipher() {
        CipherRequest request = new CipherRequest();
        request.setName("TestCipher");
        request.setRows(2);
        request.setColumns(3);
        request.setCiphertext(Arrays.asList("A", "B", "C", "D", "E", "F"));

        Cipher cipher = request.asCipher();

        assertEquals("TestCipher", cipher.getName());
        assertEquals(2, cipher.getRows());
        assertEquals(3, cipher.getColumns());
        assertEquals(6, cipher.length());
        assertEquals("A", cipher.getCiphertextCharacters().get(0).getValue());
        assertEquals("F", cipher.getCiphertextCharacters().get(5).getValue());
    }
}
