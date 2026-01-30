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

package com.ciphertool.zenith.genetic.population;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractPopulationTest {
    @Test
    public void given_negativeLogProbability_when_convertFromLogProbability_then_returnsExponent() {
        double value = AbstractPopulation.convertFromLogProbability(-1.0d);

        assertEquals(Math.exp(-1.0d), value, 0.000001d);
    }

    @Test
    public void given_nonNegativeLogProbability_when_convertFromLogProbability_then_returnsOriginalValue() {
        double value = AbstractPopulation.convertFromLogProbability(0.5d);

        assertEquals(0.5d, value, 0.000001d);
    }
}