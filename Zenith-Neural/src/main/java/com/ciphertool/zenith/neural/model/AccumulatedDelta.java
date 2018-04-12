/**
 * Copyright 2017 George Belden
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

package com.ciphertool.zenith.neural.model;

public class AccumulatedDelta {
    private Float sumOfDeltas = 0.0f;
    private int deltaCount = 0;
    private boolean isPrecomputed;
    private Float averageDelta = 0.0f;

    public Float getSumOfDeltas() {
        return sumOfDeltas;
    }

    public int getDeltaCount() {
        return deltaCount;
    }

    public boolean isPrecomputed() {
        return isPrecomputed;
    }

    public synchronized Float getAverageDelta() {
        if (isPrecomputed) {
            return averageDelta;
        }

        averageDelta = sumOfDeltas / (float) deltaCount;

        isPrecomputed = true;

        return averageDelta;
    }

    public synchronized void addDelta(Float delta) {
        if (isPrecomputed) {
            throw new IllegalStateException("Unable to add delta since the average has already been precomputed.  Please call reset() to reset the accumulated delta to zero if this is intentional.");
        }

        sumOfDeltas += delta;
        deltaCount ++;
    }

    public synchronized void reset() {
        isPrecomputed = false;
        sumOfDeltas = 0.0f;
        deltaCount = 0;
        averageDelta = 0.0f;
    }
}
