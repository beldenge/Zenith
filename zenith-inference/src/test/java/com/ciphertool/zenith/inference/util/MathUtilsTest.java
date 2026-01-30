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

package com.ciphertool.zenith.inference.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MathUtilsTest {
    private static final float FAST_EXP_RELATIVE_TOLERANCE = 0.05f;
    private static final float FAST_LOG_RELATIVE_TOLERANCE = 0.02f;
    private static final float FAST_POW_RELATIVE_TOLERANCE = 0.05f;

    @Test
    public void given_validInput_when_powRoot_then_returnsExpectedValue() {
        assertEquals(Math.pow(81d, 1d / 4d), MathUtils.powRoot(81f, 4f), 0.0001d);
        assertEquals(Math.pow(32d, 1d / 5d), MathUtils.powRoot(32f, 5f), 0.0001d);
        assertEquals(Math.pow(0.5d, 1d / 2d), MathUtils.powRoot(0.5f, 2f), 0.0001d);
    }

    @Test
    public void given_validInput_when_powSixthRoot_then_returnsExpectedValue() {
        float value = 64f;
        assertEquals(MathUtils.powRoot(value, 6f), MathUtils.powSixthRoot(value), 0.0001d);
    }

    @Test
    public void given_validInput_when_fastExpApproximation_then_matchesExpectations() {
        float[] values = new float[] { -2f, -1f, -0.5f, 0f, 0.5f, 1f, 2f, 3f };

        for (float value : values) {
            float expected = (float) Math.exp(value);
            float actual = MathUtils.fastExp(value);
            assertRelativeErrorWithin(expected, actual, FAST_EXP_RELATIVE_TOLERANCE);
        }
    }

    @Test
    public void given_validInput_when_fastLogApproximation_then_returnsExpectedValue() {
        float[] values = new float[] { 0.1f, 0.5f, 1f, 2f, 3f, 5f, 10f };

        for (float value : values) {
            float expected = (float) Math.log(value);
            float actual = MathUtils.fastLog(value);
            if (value == 1f) {
                assertEquals(0f, actual, 0.000001d);
            } else {
                assertRelativeErrorWithin(expected, actual, FAST_LOG_RELATIVE_TOLERANCE);
            }
        }
    }

    @Test
    public void given_validInput_when_fastPowApproximation_then_matchesExpectations() {
        float[][] pairs = new float[][] {
            { 0.5f, 2f },
            { 2f, 2f },
            { 2f, 0.5f },
            { 5f, 1.5f },
            { 10f, 2f }
        };

        for (float[] pair : pairs) {
            float base = pair[0];
            float exponent = pair[1];
            float expected = (float) Math.pow(base, exponent);
            float actual = MathUtils.fastPow(base, exponent);
            assertRelativeErrorWithin(expected, actual, FAST_POW_RELATIVE_TOLERANCE);
        }
    }

    @Test
    public void given_validInput_when_logBase_then_returnsExpectedValue() {
        assertEquals(2d, MathUtils.logBase(100d, 10f), 0.0000001d);
        assertEquals(3d, MathUtils.logBase(8d, 2f), 0.0000001d);
        assertEquals(Math.log(7d) / Math.log(3d), MathUtils.logBase(7d, 3f), 0.0000001d);
    }

    private void assertRelativeErrorWithin(float expected, float actual, float relativeTolerance) {
        float denominator = Math.abs(expected) < 1e-6f ? 1f : Math.abs(expected);
        float relativeError = Math.abs(expected - actual) / denominator;
        assertTrue(relativeError <= relativeTolerance,
            "Expected relative error <= " + relativeTolerance + " but was " + relativeError + " for expected " + expected + " and actual " + actual);
    }
}
