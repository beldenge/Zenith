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

package com.ciphertool.zenith.inference.util;

import java.util.concurrent.ThreadLocalRandom;

public class LetterUtils {
    private static final int ASCII_OFFSET = 97;
    public static final int NUMBER_OF_LETTERS = 26;

    /**
     * Gets a random ASCII value for all lower case English letters and returns the appropriate char.
     *
     * @return the char representing an English letter
     */
    public static char getRandomLetter() {
        /*
         * Get a random number between 1 and 26 (inclusive) for a letter in the English alphabet using the ASCII decimal
         * offset.
         */
        int randomIndex = (int) (ThreadLocalRandom.current().nextDouble() * NUMBER_OF_LETTERS);

        return ordinalToChar(randomIndex);
    }

    public static int charToOrdinal(char c) {
        return ((int) c) - ASCII_OFFSET;
    }

    /**
     * Casts the ASCII value to a char. The asciiValue is expected to be the decimal offset.
     *
     * @param asciiValue the ASCII value to use
     * @return the corresponding English letter
     */
    public static char ordinalToChar(int asciiValue) {
        if (asciiValue < 0 || asciiValue > NUMBER_OF_LETTERS - 1) {
            throw new IllegalArgumentException("Unable to get English letter by ASCII value of " + asciiValue
                    + ".  Expecting a value in the range [" + 0 + "-" + (NUMBER_OF_LETTERS - 1) + "].");
        }

        return (char) (asciiValue + ASCII_OFFSET);
    }
}
