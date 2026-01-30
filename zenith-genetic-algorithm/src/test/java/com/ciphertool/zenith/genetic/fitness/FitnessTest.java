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

package com.ciphertool.zenith.genetic.fitness;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class FitnessTest {
    @Test
    public void given_validInput_when_maximizingFitnessCompare_then_comparesAsExpected() {
        MaximizingFitness higher = new MaximizingFitness(2.0d);
        MaximizingFitness lower = new MaximizingFitness(1.0d);

        assertEquals(1, higher.compareTo(lower));
        assertEquals(-1, lower.compareTo(higher));
        assertEquals(0, higher.compareTo(new MaximizingFitness(2.0d)));
    }

    @Test
    public void given_validInput_when_maximizingFitnessGuardRail_then_returnsExpectedValue() {
        MaximizingFitness first = new MaximizingFitness(11.0d, 10.0d);
        MaximizingFitness second = new MaximizingFitness(12.0d, 10.0d);

        assertEquals(0, first.compareTo(second));
        assertEquals(0, second.compareTo(first));
    }

    @Test
    public void given_validInput_when_minimizingFitnessCompare_then_comparesAsExpected() {
        MinimizingFitness lower = new MinimizingFitness(1.0d);
        MinimizingFitness higher = new MinimizingFitness(2.0d);

        assertEquals(1, lower.compareTo(higher));
        assertEquals(-1, higher.compareTo(lower));
        assertEquals(0, lower.compareTo(new MinimizingFitness(1.0d)));
    }

    @Test
    public void given_validInput_when_minimizingFitnessGuardRail_then_returnsExpectedValue() {
        MinimizingFitness first = new MinimizingFitness(0.1d, 0.5d);
        MinimizingFitness second = new MinimizingFitness(0.2d, 0.5d);

        assertEquals(0, first.compareTo(second));
        assertEquals(0, second.compareTo(first));
    }

    @Test
    public void given_validInput_when_proximityFitnessCompare_then_comparesAsExpected() {
        ProximityFitness closer = new ProximityFitness(10.0d, 9.0d);
        ProximityFitness farther = new ProximityFitness(10.0d, 12.0d);

        assertEquals(1, closer.compareTo(farther));
        assertEquals(-1, farther.compareTo(closer));
        assertEquals(0, closer.compareTo(new ProximityFitness(10.0d, 11.0d)));
    }

    @Test
    public void given_validInput_when_proximityFitnessGuardRail_then_returnsExpectedValue() {
        ProximityFitness first = new ProximityFitness(10.0d, 9.9d, 0.5d);
        ProximityFitness second = new ProximityFitness(10.0d, 10.1d, 0.5d);

        assertEquals(0, first.compareTo(second));
        assertEquals(0, second.compareTo(first));
    }

    @Test
    public void given_validInput_when_fitnessClone_then_copiesState() {
        MaximizingFitness max = new MaximizingFitness(1.0d, 5.0d);
        MinimizingFitness min = new MinimizingFitness(2.0d, 3.0d);
        ProximityFitness prox = new ProximityFitness(10.0d, 9.0d, 0.5d);

        MaximizingFitness maxClone = max.clone();
        MinimizingFitness minClone = min.clone();
        ProximityFitness proxClone = prox.clone();

        assertNotSame(max, maxClone);
        assertEquals(max.getValue(), maxClone.getValue(), 0.000001d);

        assertNotSame(min, minClone);
        assertEquals(min.getValue(), minClone.getValue(), 0.000001d);

        assertNotSame(prox, proxClone);
        assertEquals(prox.getValue(), proxClone.getValue(), 0.000001d);
    }
}
