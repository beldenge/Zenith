/**
 * Copyright 2017-2019 George Belden
 * <p>
 * This file is part of Zenith.
 * <p>
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.inference.genetic.common;

import java.util.concurrent.ThreadLocalRandom;

public class LetterUtils {
    private static final int MINIMUM_ASCII_VALUE = 97;
    private static final int MAXIMUM_ASCII_VALUE = 122;
    private static final int NUMBER_OF_LETTERS = 26;

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
        int randomIndex = (int) (ThreadLocalRandom.current().nextDouble() * NUMBER_OF_LETTERS) + MINIMUM_ASCII_VALUE;

        return getLetterByAsciiValue(randomIndex);
    }

    /**
     * Casts the ASCII value to a char. The asciiValue is expected to be the decimal offset.
     *
     * @param asciiValue
     *            the ASCII value to use
     * @return the corresponding English letter
     */
    public static char getLetterByAsciiValue(int asciiValue) {
        if (asciiValue < MINIMUM_ASCII_VALUE || asciiValue > MAXIMUM_ASCII_VALUE) {
            throw new IllegalArgumentException("Unable to get English letter by ASCII value of " + asciiValue
                    + ".  Expecting a value in the range [" + MINIMUM_ASCII_VALUE + "-" + MAXIMUM_ASCII_VALUE + "].");
        }

        return (char) asciiValue;
    }
}
