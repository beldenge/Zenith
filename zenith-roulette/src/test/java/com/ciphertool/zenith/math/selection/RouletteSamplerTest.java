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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RouletteSamplerTest {
    public class DummyProbability implements Probability<Character>, Comparable<DummyProbability> {
        private Character value;
        private Double probability;

        public DummyProbability(Character value, Double probability) {
            this.value = value;
            this.probability = probability;
        }

        @Override
        public Character getValue() {
            return value;
        }

        @Override
        public Double getProbability() {
            return probability;
        }

        @Override
        public int compareTo(DummyProbability other) {
            return this.probability.compareTo(other.probability);
        }
    }

    // This is a valid test but mark it as @Ignore because it sometimes fails due to the nature of random chance
    @Disabled
    @Test
    public void testSampling() {
        List<DummyProbability> dummyProbabilities = new ArrayList<>();

        dummyProbabilities.add(new DummyProbability('a', 0.07916));
        dummyProbabilities.add(new DummyProbability('b', 0.01543));
        dummyProbabilities.add(new DummyProbability('c', 0.03037));
        dummyProbabilities.add(new DummyProbability('d', 0.03962));
        dummyProbabilities.add(new DummyProbability('e', 0.12454));
        dummyProbabilities.add(new DummyProbability('f', 0.02233));
        dummyProbabilities.add(new DummyProbability('g', 0.01981));
        dummyProbabilities.add(new DummyProbability('h', 0.05323));
        dummyProbabilities.add(new DummyProbability('i', 0.07300));
        dummyProbabilities.add(new DummyProbability('j', 0.00163));
        dummyProbabilities.add(new DummyProbability('k', 0.00701));
        dummyProbabilities.add(new DummyProbability('l', 0.04058));
        dummyProbabilities.add(new DummyProbability('m', 0.02403));
        dummyProbabilities.add(new DummyProbability('n', 0.07289));
        dummyProbabilities.add(new DummyProbability('o', 0.07603));
        dummyProbabilities.add(new DummyProbability('p', 0.02002));
        dummyProbabilities.add(new DummyProbability('q', 0.00104));
        dummyProbabilities.add(new DummyProbability('r', 0.06124));
        dummyProbabilities.add(new DummyProbability('s', 0.06438));
        dummyProbabilities.add(new DummyProbability('t', 0.09284));
        dummyProbabilities.add(new DummyProbability('u', 0.02905));
        dummyProbabilities.add(new DummyProbability('v', 0.01068));
        dummyProbabilities.add(new DummyProbability('w', 0.01930));
        dummyProbabilities.add(new DummyProbability('x', 0.00226));
        dummyProbabilities.add(new DummyProbability('y', 0.01888));
        dummyProbabilities.add(new DummyProbability('z', 0.00068));

        RouletteSampler<DummyProbability> rouletteSampler = new RouletteSampler<>();

        Collections.sort(dummyProbabilities);
        rouletteSampler.reIndex(dummyProbabilities);

        Map<Character, Integer> characterCounts = new HashMap<>();
        int nextIndex;

        int i = 0;

        for (; i < 1000000; i++) {
            nextIndex = rouletteSampler.getNextIndex();

            if (characterCounts.get(dummyProbabilities.get(nextIndex).getValue()) == null) {
                characterCounts.put(dummyProbabilities.get(nextIndex).getValue(), 1);
            }

            characterCounts.put(dummyProbabilities.get(nextIndex).getValue(), characterCounts.get(dummyProbabilities.get(nextIndex).getValue()) + 1);
        }

        for (DummyProbability letterProbability : dummyProbabilities) {
            Double actual = letterProbability.getProbability();
            Double estimated = (double) characterCounts.get(letterProbability.getValue()) / (double) i;
            Double difference = Math.abs(actual - estimated);

            System.out.printf(letterProbability.getValue() + ": actual=" + actual.toString() + ", estimated=" + estimated.toString() + ", difference=" + difference.toString() + "\n");

            assertTrue(difference < 0.001d);
        }
    }

    @Test
    public void testGetNextIndex_SingleNode() {
        List<DummyProbability> dummyProbabilities = new ArrayList<>();

        dummyProbabilities.add(new DummyProbability('a', 1.0d));

        RouletteSampler<DummyProbability> rouletteSampler = new RouletteSampler<>();

        Collections.sort(dummyProbabilities);
        rouletteSampler.reIndex(dummyProbabilities);

        assertEquals(0, rouletteSampler.getNextIndex(1.0d));
        assertEquals(0, rouletteSampler.getNextIndex(0.1d));
        assertEquals(0, rouletteSampler.getNextIndex(2.0d));
    }

    @Test
    public void testReindex_Unsorted() {
        List<DummyProbability> dummyProbabilities = new ArrayList<>();

        DummyProbability a = new DummyProbability('a', 0.3d);
        dummyProbabilities.add(a);
        DummyProbability b = new DummyProbability('b', 0.2d);
        dummyProbabilities.add(b);
        DummyProbability c = new DummyProbability('c', 0.5d);
        dummyProbabilities.add(c);

        RouletteSampler<DummyProbability> rouletteSampler = new RouletteSampler<>();

        boolean caught = false;
        try {
            rouletteSampler.reIndex(dummyProbabilities);
        } catch(IllegalStateException ise) {
            caught = true;
        }

        assertTrue(caught);
    }

    @Test
    public void testGetNextIndex_ThreeNodes_Equal() {
        List<DummyProbability> dummyProbabilities = new ArrayList<>();

        DummyProbability a = new DummyProbability('a', 0.3d);
        dummyProbabilities.add(a);
        DummyProbability b = new DummyProbability('b', 0.2d);
        dummyProbabilities.add(b);
        DummyProbability c = new DummyProbability('c', 0.5d);
        dummyProbabilities.add(c);

        RouletteSampler<DummyProbability> rouletteSampler = new RouletteSampler<>();

        Collections.sort(dummyProbabilities);
        rouletteSampler.reIndex(dummyProbabilities);

        int chosen = rouletteSampler.getNextIndex(0.2d);

        assertEquals(0, chosen);
        assertSame(b, dummyProbabilities.get(chosen));
    }

    @Test
    public void testGetNextIndex_ThreeNodes_LessThan_Equal() {
        List<DummyProbability> dummyProbabilities = new ArrayList<>();

        DummyProbability a = new DummyProbability('a', 0.3d);
        dummyProbabilities.add(a);
        DummyProbability b = new DummyProbability('b', 0.2d);
        dummyProbabilities.add(b);
        DummyProbability c = new DummyProbability('c', 0.5d);
        dummyProbabilities.add(c);

        RouletteSampler<DummyProbability> rouletteSampler = new RouletteSampler<>();

        Collections.sort(dummyProbabilities);
        rouletteSampler.reIndex(dummyProbabilities);

        int chosen = rouletteSampler.getNextIndex(0.2d);

        assertEquals(0, chosen);
        assertSame(b, dummyProbabilities.get(chosen));
    }

    @Test
    public void testGetNextIndex_ThreeNodes_LessThan_LessThan() {
        List<DummyProbability> dummyProbabilities = new ArrayList<>();

        DummyProbability a = new DummyProbability('a', 0.3d);
        dummyProbabilities.add(a);
        DummyProbability b = new DummyProbability('b', 0.2d);
        dummyProbabilities.add(b);
        DummyProbability c = new DummyProbability('c', 0.5d);
        dummyProbabilities.add(c);

        RouletteSampler<DummyProbability> rouletteSampler = new RouletteSampler<>();

        Collections.sort(dummyProbabilities);
        rouletteSampler.reIndex(dummyProbabilities);

        int chosen = rouletteSampler.getNextIndex(0.15d);

        assertEquals(0, chosen);
        assertSame(b, dummyProbabilities.get(chosen));
    }

    @Test
    public void testGetNextIndex_ThreeNodes_LessThan_GreaterThan() {
        List<DummyProbability> dummyProbabilities = new ArrayList<>();

        DummyProbability a = new DummyProbability('a', 0.3d);
        dummyProbabilities.add(a);
        DummyProbability b = new DummyProbability('b', 0.2d);
        dummyProbabilities.add(b);
        DummyProbability c = new DummyProbability('c', 0.5d);
        dummyProbabilities.add(c);

        RouletteSampler<DummyProbability> rouletteSampler = new RouletteSampler<>();

        Collections.sort(dummyProbabilities);
        rouletteSampler.reIndex(dummyProbabilities);

        int chosen = rouletteSampler.getNextIndex(0.25d);

        assertEquals(1, chosen);
        assertSame(a, dummyProbabilities.get(chosen));
    }

    @Test
    public void testGetNextIndex_ThreeNodes_GreaterThan_Equal() {
        List<DummyProbability> dummyProbabilities = new ArrayList<>();

        DummyProbability a = new DummyProbability('a', 0.3d);
        dummyProbabilities.add(a);
        DummyProbability b = new DummyProbability('b', 0.2d);
        dummyProbabilities.add(b);
        DummyProbability c = new DummyProbability('c', 0.5d);
        dummyProbabilities.add(c);

        RouletteSampler<DummyProbability> rouletteSampler = new RouletteSampler<>();

        Collections.sort(dummyProbabilities);
        rouletteSampler.reIndex(dummyProbabilities);

        int chosen = rouletteSampler.getNextIndex(1.0d);

        assertEquals(2, chosen);
        assertSame(c, dummyProbabilities.get(chosen));
    }

    @Test
    public void testGetNextIndex_ThreeNodes_GreaterThan_LessThan() {
        List<DummyProbability> dummyProbabilities = new ArrayList<>();

        DummyProbability a = new DummyProbability('a', 0.3d);
        dummyProbabilities.add(a);
        DummyProbability b = new DummyProbability('b', 0.2d);
        dummyProbabilities.add(b);
        DummyProbability c = new DummyProbability('c', 0.5d);
        dummyProbabilities.add(c);

        RouletteSampler<DummyProbability> rouletteSampler = new RouletteSampler<>();

        Collections.sort(dummyProbabilities);
        rouletteSampler.reIndex(dummyProbabilities);

        int chosen = rouletteSampler.getNextIndex(0.6d);

        assertEquals(2, chosen);
        assertSame(c, dummyProbabilities.get(chosen));
    }
}
