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

package com.ciphertool.zenith.inference.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LetterUtilsTest {
    @Test
    public void testCharToOrdinal() {
        assertEquals(0, LetterUtils.charToOrdinal('a'));
        assertEquals(1, LetterUtils.charToOrdinal('b'));
        assertEquals(2, LetterUtils.charToOrdinal('c'));
        assertEquals(3, LetterUtils.charToOrdinal('d'));
        assertEquals(4, LetterUtils.charToOrdinal('e'));
        assertEquals(5, LetterUtils.charToOrdinal('f'));
        assertEquals(6, LetterUtils.charToOrdinal('g'));
        assertEquals(7, LetterUtils.charToOrdinal('h'));
        assertEquals(8, LetterUtils.charToOrdinal('i'));
        assertEquals(9, LetterUtils.charToOrdinal('j'));
        assertEquals(10, LetterUtils.charToOrdinal('k'));
        assertEquals(11, LetterUtils.charToOrdinal('l'));
        assertEquals(12, LetterUtils.charToOrdinal('m'));
        assertEquals(13, LetterUtils.charToOrdinal('n'));
        assertEquals(14, LetterUtils.charToOrdinal('o'));
        assertEquals(15, LetterUtils.charToOrdinal('p'));
        assertEquals(16, LetterUtils.charToOrdinal('q'));
        assertEquals(17, LetterUtils.charToOrdinal('r'));
        assertEquals(18, LetterUtils.charToOrdinal('s'));
        assertEquals(19, LetterUtils.charToOrdinal('t'));
        assertEquals(20, LetterUtils.charToOrdinal('u'));
        assertEquals(21, LetterUtils.charToOrdinal('v'));
        assertEquals(22, LetterUtils.charToOrdinal('w'));
        assertEquals(23, LetterUtils.charToOrdinal('x'));
        assertEquals(24, LetterUtils.charToOrdinal('y'));
        assertEquals(25, LetterUtils.charToOrdinal('z'));
    }

    @Test
    public void testOrdinalToChar() {
        assertEquals(LetterUtils.ordinalToChar(0), 'a');
        assertEquals(LetterUtils.ordinalToChar(1), 'b');
        assertEquals(LetterUtils.ordinalToChar(2), 'c');
        assertEquals(LetterUtils.ordinalToChar(3), 'd');
        assertEquals(LetterUtils.ordinalToChar(4), 'e');
        assertEquals(LetterUtils.ordinalToChar(5), 'f');
        assertEquals(LetterUtils.ordinalToChar(6), 'g');
        assertEquals(LetterUtils.ordinalToChar(7), 'h');
        assertEquals(LetterUtils.ordinalToChar(8), 'i');
        assertEquals(LetterUtils.ordinalToChar(9), 'j');
        assertEquals(LetterUtils.ordinalToChar(10), 'k');
        assertEquals(LetterUtils.ordinalToChar(11), 'l');
        assertEquals(LetterUtils.ordinalToChar(12), 'm');
        assertEquals(LetterUtils.ordinalToChar(13), 'n');
        assertEquals(LetterUtils.ordinalToChar(14), 'o');
        assertEquals(LetterUtils.ordinalToChar(15), 'p');
        assertEquals(LetterUtils.ordinalToChar(16), 'q');
        assertEquals(LetterUtils.ordinalToChar(17), 'r');
        assertEquals(LetterUtils.ordinalToChar(18), 's');
        assertEquals(LetterUtils.ordinalToChar(19), 't');
        assertEquals(LetterUtils.ordinalToChar(20), 'u');
        assertEquals(LetterUtils.ordinalToChar(21), 'v');
        assertEquals(LetterUtils.ordinalToChar(22), 'w');
        assertEquals(LetterUtils.ordinalToChar(23), 'x');
        assertEquals(LetterUtils.ordinalToChar(24), 'y');
        assertEquals(LetterUtils.ordinalToChar(25), 'z');
    }

    @Test
    public void testOrdinalToCharTooLow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            LetterUtils.ordinalToChar(-1);
        });
    }

    @Test
    public void testOrdinalToCharTooHigh() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            LetterUtils.ordinalToChar(26);
        });
    }

    @Test
    public void testGetRandomCharacter() {
        char c;
        for (int i = 0; i < 100; i++) {
            c = LetterUtils.getRandomLetter();
            assertTrue(String.valueOf(c).matches("[a-z]"));
        }
    }
}
