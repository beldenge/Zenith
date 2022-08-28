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

public class UnwrapTranspositionCipherTransformerTest {
    @Test
    public void testTransposeOnce() {
        UnwrapTranspositionCipherTransformer cipherTransformer = new UnwrapTranspositionCipherTransformer();
        cipherTransformer.transpositionKeyString = "TOMATO";
        cipherTransformer.init();

        Cipher cipher = new Cipher("tomato", 7, 6);

        cipher.addCiphertextCharacter(new Ciphertext(0, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(1, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(2, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(3, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(4, "S"));
        cipher.addCiphertextCharacter(new Ciphertext(5, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(6, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(7, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(8, "O"));
        cipher.addCiphertextCharacter(new Ciphertext(9, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(10, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(11, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(12, "F"));
        cipher.addCiphertextCharacter(new Ciphertext(13, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(14, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(15, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(16, "L"));
        cipher.addCiphertextCharacter(new Ciphertext(17, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(18, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(19, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(20, "Y"));
        cipher.addCiphertextCharacter(new Ciphertext(21, "M"));
        cipher.addCiphertextCharacter(new Ciphertext(22, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(23, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(24, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(25, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(26, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(27, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(28, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(29, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(30, "P"));
        cipher.addCiphertextCharacter(new Ciphertext(31, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(32, "G"));
        cipher.addCiphertextCharacter(new Ciphertext(33, "D"));
        cipher.addCiphertextCharacter(new Ciphertext(34, "L"));
        cipher.addCiphertextCharacter(new Ciphertext(35, "O"));
        cipher.addCiphertextCharacter(new Ciphertext(36, "S"));
        cipher.addCiphertextCharacter(new Ciphertext(37, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(38, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(39, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(40, "M"));
        cipher.addCiphertextCharacter(new Ciphertext(41, "X"));

        Cipher transformed = cipherTransformer.transform(cipher);

        assertEquals(42, cipher.length());
        assertEquals(6, transformed.getColumns());
        assertEquals(7, transformed.getRows());

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
        assertEquals("L", transformed.getCiphertextCharacters().get(36).getValue());
        assertEquals("Y", transformed.getCiphertextCharacters().get(37).getValue());
        assertEquals("X", transformed.getCiphertextCharacters().get(38).getValue());
        assertEquals("X", transformed.getCiphertextCharacters().get(39).getValue());
        assertEquals("X", transformed.getCiphertextCharacters().get(40).getValue());
        assertEquals("X", transformed.getCiphertextCharacters().get(41).getValue());

        System.out.println(transformed);
    }

    @Test
    public void testTransposeTwice() {
        UnwrapTranspositionCipherTransformer cipherTransformer = new UnwrapTranspositionCipherTransformer();
        cipherTransformer.transpositionKeyString = "TOMATO";
        cipherTransformer.init();

        Cipher cipher = new Cipher("tomato", 7, 6);

        cipher.addCiphertextCharacter(new Ciphertext(0, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(1, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(2, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(3, "M"));
        cipher.addCiphertextCharacter(new Ciphertext(4, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(5, "D"));
        cipher.addCiphertextCharacter(new Ciphertext(6, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(7, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(8, "O"));
        cipher.addCiphertextCharacter(new Ciphertext(9, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(10, "Y"));
        cipher.addCiphertextCharacter(new Ciphertext(11, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(12, "G"));
        cipher.addCiphertextCharacter(new Ciphertext(13, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(14, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(15, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(16, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(17, "E"));
        cipher.addCiphertextCharacter(new Ciphertext(18, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(19, "N"));
        cipher.addCiphertextCharacter(new Ciphertext(20, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(21, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(22, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(23, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(24, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(25, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(26, "O"));
        cipher.addCiphertextCharacter(new Ciphertext(27, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(28, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(29, "X"));
        cipher.addCiphertextCharacter(new Ciphertext(30, "F"));
        cipher.addCiphertextCharacter(new Ciphertext(31, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(32, "I"));
        cipher.addCiphertextCharacter(new Ciphertext(33, "P"));
        cipher.addCiphertextCharacter(new Ciphertext(34, "S"));
        cipher.addCiphertextCharacter(new Ciphertext(35, "S"));
        cipher.addCiphertextCharacter(new Ciphertext(36, "H"));
        cipher.addCiphertextCharacter(new Ciphertext(37, "L"));
        cipher.addCiphertextCharacter(new Ciphertext(38, "A"));
        cipher.addCiphertextCharacter(new Ciphertext(39, "T"));
        cipher.addCiphertextCharacter(new Ciphertext(40, "L"));
        cipher.addCiphertextCharacter(new Ciphertext(41, "M"));

        Cipher transformed = cipherTransformer.transform(cipher);
        transformed = cipherTransformer.transform(transformed);

        assertEquals(42, cipher.length());
        assertEquals(6, transformed.getColumns());
        assertEquals(7, transformed.getRows());

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
        assertEquals("L", transformed.getCiphertextCharacters().get(36).getValue());
        assertEquals("Y", transformed.getCiphertextCharacters().get(37).getValue());
        assertEquals("X", transformed.getCiphertextCharacters().get(38).getValue());
        assertEquals("X", transformed.getCiphertextCharacters().get(39).getValue());
        assertEquals("X", transformed.getCiphertextCharacters().get(40).getValue());
        assertEquals("X", transformed.getCiphertextCharacters().get(41).getValue());

        System.out.println(transformed);
    }
}
