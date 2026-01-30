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

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpperRightQuadrantCipherTransformerTest {
    @Test
    public void given_validInput_when_transformOddColumns_then_returnsExpectedValue() {
        UpperRightQuadrantCipherTransformer cipherTransformer = new UpperRightQuadrantCipherTransformer();

        Cipher cipher = new Cipher("tomato", 6, 7);

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

        assertEquals(9, transformed.length());
        assertEquals(3, transformed.getColumns());
        assertEquals(3, transformed.getRows());

        assertEquals("O", transformed.getCiphertextCharacters().get(0).getValue());
        assertEquals("M", transformed.getCiphertextCharacters().get(1).getValue());
        assertEquals("A", transformed.getCiphertextCharacters().get(2).getValue());
        assertEquals("A", transformed.getCiphertextCharacters().get(3).getValue());
        assertEquals("P", transformed.getCiphertextCharacters().get(4).getValue());
        assertEquals("L", transformed.getCiphertextCharacters().get(5).getValue());
        assertEquals("N", transformed.getCiphertextCharacters().get(6).getValue());
        assertEquals("T", transformed.getCiphertextCharacters().get(7).getValue());
        assertEquals("H", transformed.getCiphertextCharacters().get(8).getValue());

        System.out.println(transformed);
    }

    @Test
    public void given_validInput_when_transformOddRows_then_returnsExpectedValue() {
        UpperRightQuadrantCipherTransformer cipherTransformer = new UpperRightQuadrantCipherTransformer();

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

        assertEquals(9, transformed.length());
        assertEquals(3, transformed.getColumns());
        assertEquals(3, transformed.getRows());

        assertEquals("T", transformed.getCiphertextCharacters().get(0).getValue());
        assertEquals("O", transformed.getCiphertextCharacters().get(1).getValue());
        assertEquals("M", transformed.getCiphertextCharacters().get(2).getValue());
        assertEquals("I", transformed.getCiphertextCharacters().get(3).getValue());
        assertEquals("S", transformed.getCiphertextCharacters().get(4).getValue());
        assertEquals("A", transformed.getCiphertextCharacters().get(5).getValue());
        assertEquals("N", transformed.getCiphertextCharacters().get(6).getValue());
        assertEquals("T", transformed.getCiphertextCharacters().get(7).getValue());
        assertEquals("I", transformed.getCiphertextCharacters().get(8).getValue());

        System.out.println(transformed);
    }
}
