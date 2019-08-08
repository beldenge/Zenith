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

package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemoveLastColumnCipherTransformerTest {
    @Test
    public void testTransform() {
        RemoveLastColumnCipherTransformer cipherTransformer = new RemoveLastColumnCipherTransformer();

        Cipher cipher = new Cipher("tomato", 7, 6);

        cipher.addCiphertextCharacter(new Ciphertext(0, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(1, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(2, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(3, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(4, "O"));
        cipher.addCiphertextCharacter(new Ciphertext(5, "M"));
        cipher.addCiphertextCharacter(new Ciphertext(6, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(7, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(8, "O"));
        cipher.addCiphertextCharacter(new Ciphertext(9, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(10, "S"));
        cipher.addCiphertextCharacter(new Ciphertext(11, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(12, "P"));
        cipher.addCiphertextCharacter(new Ciphertext(13, "L"));
        cipher.addCiphertextCharacter(new Ciphertext(14, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(15, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(16, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(17, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(18, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(19, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(20, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(21, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(22, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(23, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(24, "G"));
        cipher.addCiphertextCharacter(new Ciphertext(25, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(26, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(27, "S"));
        cipher.addCiphertextCharacter(new Ciphertext(28, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(29, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(30, "D"));
        cipher.addCiphertextCharacter(new Ciphertext(31, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(32, "F"));
        cipher.addCiphertextCharacter(new Ciphertext(33, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(34, "M"));
        cipher.addCiphertextCharacter(new Ciphertext(35, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(36, "L"));
        cipher.addCiphertextCharacter(new Ciphertext(37, "Y"));
        cipher.addCiphertextCharacter(new Ciphertext(38, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(39, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(40, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(41, "X"));
        System.out.println(cipher);

        Cipher transformed = cipherTransformer.transform(cipher);

        assertEquals(35, transformed.length());
        assertEquals(5, transformed.getColumns());
        assertEquals(7, transformed.getRows());

        assertEquals("T", transformed.getCiphertextCharacters().get(0).getValue());
        assertEquals("H", transformed.getCiphertextCharacters().get(1).getValue());
        assertEquals("E", transformed.getCiphertextCharacters().get(2).getValue());
        assertEquals("T", transformed.getCiphertextCharacters().get(3).getValue());
        assertEquals("O", transformed.getCiphertextCharacters().get(4).getValue());
        assertEquals("A", transformed.getCiphertextCharacters().get(5).getValue());
        assertEquals("T", transformed.getCiphertextCharacters().get(6).getValue());
        assertEquals("O", transformed.getCiphertextCharacters().get(7).getValue());
        assertEquals("I", transformed.getCiphertextCharacters().get(8).getValue());
        assertEquals("S", transformed.getCiphertextCharacters().get(9).getValue());
        assertEquals("P", transformed.getCiphertextCharacters().get(10).getValue());
        assertEquals("L", transformed.getCiphertextCharacters().get(11).getValue());
        assertEquals("A", transformed.getCiphertextCharacters().get(12).getValue());
        assertEquals("N", transformed.getCiphertextCharacters().get(13).getValue());
        assertEquals("T", transformed.getCiphertextCharacters().get(14).getValue());
        assertEquals("N", transformed.getCiphertextCharacters().get(15).getValue());
        assertEquals("T", transformed.getCiphertextCharacters().get(16).getValue());
        assertEquals("H", transformed.getCiphertextCharacters().get(17).getValue());
        assertEquals("E", transformed.getCiphertextCharacters().get(18).getValue());
        assertEquals("N", transformed.getCiphertextCharacters().get(19).getValue());
        assertEquals("G", transformed.getCiphertextCharacters().get(20).getValue());
        assertEquals("H", transformed.getCiphertextCharacters().get(21).getValue());
        assertEquals("T", transformed.getCiphertextCharacters().get(22).getValue());
        assertEquals("S", transformed.getCiphertextCharacters().get(23).getValue());
        assertEquals("H", transformed.getCiphertextCharacters().get(24).getValue());
        assertEquals("D", transformed.getCiphertextCharacters().get(25).getValue());
        assertEquals("E", transformed.getCiphertextCharacters().get(26).getValue());
        assertEquals("F", transformed.getCiphertextCharacters().get(27).getValue());
        assertEquals("A", transformed.getCiphertextCharacters().get(28).getValue());
        assertEquals("M", transformed.getCiphertextCharacters().get(29).getValue());
        assertEquals("L", transformed.getCiphertextCharacters().get(30).getValue());
        assertEquals("Y", transformed.getCiphertextCharacters().get(31).getValue());
        assertEquals("X", transformed.getCiphertextCharacters().get(32).getValue());
        assertEquals("X", transformed.getCiphertextCharacters().get(33).getValue());
        assertEquals("X", transformed.getCiphertextCharacters().get(34).getValue());

        System.out.println(transformed);
    }
}
