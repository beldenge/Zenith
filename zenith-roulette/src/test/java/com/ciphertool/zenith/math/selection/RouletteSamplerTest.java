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

package com.ciphertool.zenith.math.selection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RouletteSamplerTest {
    @Test
    public void testReIndexRejectsNullAndEmpty() {
        RouletteSampler<SampleProbability> sampler = new RouletteSampler<>();

        assertEquals(-1d, sampler.reIndex(null), 0.0001d);
        assertEquals(-1d, sampler.reIndex(Collections.emptyList()), 0.0001d);
    }

    @Test
    public void testReIndexRequiresSortedInput() {
        RouletteSampler<SampleProbability> sampler = new RouletteSampler<>();

        SampleProbability higher = new SampleProbability("b", 0.7d);
        SampleProbability lower = new SampleProbability("a", 0.3d);

        List<SampleProbability> unsorted = Arrays.asList(higher, lower);

        assertThrows(IllegalStateException.class, () -> sampler.reIndex(unsorted));
    }

    @Test
    public void testReIndexAndSampling() {
        TestSampler sampler = new TestSampler();

        List<SampleProbability> sorted = Arrays.asList(
                new SampleProbability("a", 0.2d),
                new SampleProbability("b", 0.3d),
                new SampleProbability("c", 0.5d)
        );

        assertEquals(1.0d, sampler.reIndex(sorted), 0.0001d);
        assertEquals(0, sampler.getNextIndexFor(0.1d));
        assertEquals(1, sampler.getNextIndexFor(0.25d));
        assertEquals(2, sampler.getNextIndexFor(0.75d));
    }

    private static final class SampleProbability implements Probability<String>, Comparable<SampleProbability> {
        private final String value;
        private final Double probability;

        private SampleProbability(String value, Double probability) {
            this.value = value;
            this.probability = probability;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Double getProbability() {
            return probability;
        }

        @Override
        public int compareTo(SampleProbability other) {
            int compare = this.probability.compareTo(other.probability);
            if (compare != 0) {
                return compare;
            }
            return this.value.compareTo(other.value);
        }
    }

    private static final class TestSampler extends RouletteSampler<SampleProbability> {
        private int getNextIndexFor(double magicNumber) {
            return getNextIndex(magicNumber);
        }
    }
}
