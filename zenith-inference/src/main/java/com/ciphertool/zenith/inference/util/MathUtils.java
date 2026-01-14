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

public class MathUtils {
    public static float powRoot(float x, float root) {
        return (float) Math.pow(x, 1f / root);
    }

    public static float powSixthRoot(float x) {
        return powRoot(x, 6f);
    }

    // https://martin.ankerl.com/2007/02/11/optimized-exponential-functions-for-java/
    public static float fastExp(float x) {
        final long tmp = (long) (1512775 * x + 1072632447);
        return (float) Double.longBitsToDouble(tmp << 32);
    }

    // https://martin.ankerl.com/2007/02/11/optimized-exponential-functions-for-java/
    public static float fastLog(float x) {
        return 6 * (x - 1) / (x + 1 + 4 * (float) Math.sqrt(x));
    }

    // https://martin.ankerl.com/2007/02/11/optimized-exponential-functions-for-java/
    public static float fastPow(float a, float b) {
        return fastExp(b * fastLog(a));
    }

    public static double logBase(double num, float base) {
        return (Math.log(num) / Math.log(base));
    }
}
