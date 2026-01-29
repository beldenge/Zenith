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

package com.ciphertool.zenith.model.etl.converters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberToWordsConverterTest {
    @Test
    public void testConvert_ZeroAndSmallNumbers() {
        assertEquals("zero", NumberToWordsConverter.convert(0));
        assertEquals("five", NumberToWordsConverter.convert(5));
        assertEquals("nineteen", NumberToWordsConverter.convert(19));
        assertEquals("twenty", NumberToWordsConverter.convert(20));
        assertEquals("twenty one", NumberToWordsConverter.convert(21));
    }

    @Test
    public void testConvert_Hundreds() {
        assertEquals("one hundred five", NumberToWordsConverter.convert(105));
        assertEquals("five hundred sixty nine", NumberToWordsConverter.convert(569));
    }

    @Test
    public void testConvert_ThousandsAndMillions() {
        assertEquals("one thousand", NumberToWordsConverter.convert(1000));
        assertEquals("one thousand five", NumberToWordsConverter.convert(1005));
        assertEquals("two million three thousand four", NumberToWordsConverter.convert(2003004));
    }

    @Test
    public void testConvert_NegativeNumbers() {
        assertEquals("negative forty two", NumberToWordsConverter.convert(-42));
    }
}
