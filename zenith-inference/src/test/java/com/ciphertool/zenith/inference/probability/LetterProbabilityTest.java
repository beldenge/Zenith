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

package com.ciphertool.zenith.inference.probability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LetterProbabilityTest {
    @Test
    public void given_validInput_when_gettingTersAndComparison_then_returnsTrue() {
        LetterProbability low = new LetterProbability('a', 0.1d);
        LetterProbability high = new LetterProbability('b', 0.9d);

        assertEquals('a', low.getValue());
        assertEquals(0.1d, low.getProbability());
        assertTrue(low.compareTo(high) < 0);
        assertTrue(high.compareTo(low) > 0);
    }

    @Test
    public void given_validInput_when_equalsAndHashCodeBasedOnLetter_then_comparesAsExpected() {
        LetterProbability aLow = new LetterProbability('a', 0.1d);
        LetterProbability aHigh = new LetterProbability('a', 0.9d);
        LetterProbability b = new LetterProbability('b', 0.1d);

        assertEquals(aLow, aHigh);
        assertEquals(aLow.hashCode(), aHigh.hashCode());
        assertNotEquals(aLow, b);
    }
}