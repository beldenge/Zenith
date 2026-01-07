/*
 * Copyright 2017-2020 George Belden
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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RemoveLastRowCipherTransformerTest {
    @Test
    public void testTransform() {
        RemoveLastRowCipherTransformer cipherTransformer = new RemoveLastRowCipherTransformer();

        Cipher cipher = new Cipher("tomato", 7, 6);

        cipher.addCiphertextCharacter(new Ciphertext("T"));
        cipher.addCiphertextCharacter(new Ciphertext("H"));
        cipher.addCiphertextCharacter(new Ciphertext("E"));
        cipher.addCiphertextCharacter(new Ciphertext("T"));
        cipher.addCiphertextCharacter(new Ciphertext("O"));
        cipher.addCiphertextCharacter(new Ciphertext("M"));
        cipher.addCiphertextCharacter(new Ciphertext("A"));
        cipher.addCiphertextCharacter(new Ciphertext("T"));
        cipher.addCiphertextCharacter(new Ciphertext("O"));
        cipher.addCiphertextCharacter(new Ciphertext("I"));
        cipher.addCiphertextCharacter(new Ciphertext("S"));
        cipher.addCiphertextCharacter(new Ciphertext("A"));
        cipher.addCiphertextCharacter(new Ciphertext("P"));
        cipher.addCiphertextCharacter(new Ciphertext("L"));
        cipher.addCiphertextCharacter(new Ciphertext("A"));
        cipher.addCiphertextCharacter(new Ciphertext("N"));
        cipher.addCiphertextCharacter(new Ciphertext("T"));
        cipher.addCiphertextCharacter(new Ciphertext("I"));
        cipher.addCiphertextCharacter(new Ciphertext("N"));
        cipher.addCiphertextCharacter(new Ciphertext("T"));
        cipher.addCiphertextCharacter(new Ciphertext("H"));
        cipher.addCiphertextCharacter(new Ciphertext("E"));
        cipher.addCiphertextCharacter(new Ciphertext("N"));
        cipher.addCiphertextCharacter(new Ciphertext("I"));
        cipher.addCiphertextCharacter(new Ciphertext("G"));
        cipher.addCiphertextCharacter(new Ciphertext("H"));
        cipher.addCiphertextCharacter(new Ciphertext("T"));
        cipher.addCiphertextCharacter(new Ciphertext("S"));
        cipher.addCiphertextCharacter(new Ciphertext("H"));
        cipher.addCiphertextCharacter(new Ciphertext("A"));
        cipher.addCiphertextCharacter(new Ciphertext("D"));
        cipher.addCiphertextCharacter(new Ciphertext("E"));
        cipher.addCiphertextCharacter(new Ciphertext("F"));
        cipher.addCiphertextCharacter(new Ciphertext("A"));
        cipher.addCiphertextCharacter(new Ciphertext("M"));
        cipher.addCiphertextCharacter(new Ciphertext("I"));
        cipher.addCiphertextCharacter(new Ciphertext("L"));
        cipher.addCiphertextCharacter(new Ciphertext("Y"));
        cipher.addCiphertextCharacter(new Ciphertext("X"));
        cipher.addCiphertextCharacter(new Ciphertext("X"));
        cipher.addCiphertextCharacter(new Ciphertext("X"));
        cipher.addCiphertextCharacter(new Ciphertext("X"));

        Cipher transformed = cipherTransformer.transform(cipher);

        assertEquals(36, transformed.length());
        assertEquals(6, transformed.getColumns());
        assertEquals(6, transformed.getRows());

        assertEquals("T", transformed.getCiphertextCharacters().get(0).getValue());
        assertEquals("H", transformed.getCiphertextCharacters().get(1).getValue());
        assertEquals("E", transformed.getCiphertextCharacters().get(2).getValue());
        assertEquals("T", transformed.getCiphertextCharacters().get(3).getValue());
        assertEquals("O", transformed.getCiphertextCharacters().get(4).getValue());
        assertEquals("M", transformed.getCiphertextCharacters().get(5).getValue());
        assertEquals("A", transformed.getCiphertextCharacters().get(6).getValue());
        assertEquals("T", transformed.getCiphertextCharacters().get(7).getValue());
        assertEquals("O", transformed.getCiphertextCharacters().get(8).getValue());
        assertEquals("I", transformed.getCiphertextCharacters().get(9).getValue());
        assertEquals("S", transformed.getCiphertextCharacters().get(10).getValue());
        assertEquals("A", transformed.getCiphertextCharacters().get(11).getValue());
        assertEquals("P", transformed.getCiphertextCharacters().get(12).getValue());
        assertEquals("L", transformed.getCiphertextCharacters().get(13).getValue());
        assertEquals("A", transformed.getCiphertextCharacters().get(14).getValue());
        assertEquals("N", transformed.getCiphertextCharacters().get(15).getValue());
        assertEquals("T", transformed.getCiphertextCharacters().get(16).getValue());
        assertEquals("I", transformed.getCiphertextCharacters().get(17).getValue());
        assertEquals("N", transformed.getCiphertextCharacters().get(18).getValue());
        assertEquals("T", transformed.getCiphertextCharacters().get(19).getValue());
        assertEquals("H", transformed.getCiphertextCharacters().get(20).getValue());
        assertEquals("E", transformed.getCiphertextCharacters().get(21).getValue());
        assertEquals("N", transformed.getCiphertextCharacters().get(22).getValue());
        assertEquals("I", transformed.getCiphertextCharacters().get(23).getValue());
        assertEquals("G", transformed.getCiphertextCharacters().get(24).getValue());
        assertEquals("H", transformed.getCiphertextCharacters().get(25).getValue());
        assertEquals("T", transformed.getCiphertextCharacters().get(26).getValue());
        assertEquals("S", transformed.getCiphertextCharacters().get(27).getValue());
        assertEquals("H", transformed.getCiphertextCharacters().get(28).getValue());
        assertEquals("A", transformed.getCiphertextCharacters().get(29).getValue());
        assertEquals("D", transformed.getCiphertextCharacters().get(30).getValue());
        assertEquals("E", transformed.getCiphertextCharacters().get(31).getValue());
        assertEquals("F", transformed.getCiphertextCharacters().get(32).getValue());
        assertEquals("A", transformed.getCiphertextCharacters().get(33).getValue());
        assertEquals("M", transformed.getCiphertextCharacters().get(34).getValue());
        assertEquals("I", transformed.getCiphertextCharacters().get(35).getValue());

        System.out.println(transformed);
    }
}
