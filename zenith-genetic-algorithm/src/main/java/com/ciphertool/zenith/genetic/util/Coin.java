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

package com.ciphertool.zenith.genetic.util;

import java.util.concurrent.ThreadLocalRandom;

public class Coin {
    /**
     * @return a random boolean representing the result a coin flip
     */
    public Boolean flip() {
        return flip(ThreadLocalRandom.current().nextDouble());
    }

    // Separate method purely to support testability since we can't rely on randomness during tests
    public Boolean flip(double magicNumber) {
        return ((int) (magicNumber * 2)) == 0;
    }
}
