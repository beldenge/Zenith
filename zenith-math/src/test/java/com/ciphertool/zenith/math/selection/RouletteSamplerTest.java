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

package com.ciphertool.zenith.math.selection;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;

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

    @Test
    public void testSampling() {
        List<DummyProbability> letterUnigramProbabilities = new ArrayList<>();

        letterUnigramProbabilities.add(new DummyProbability('a', 0.07916));
        letterUnigramProbabilities.add(new DummyProbability('b', 0.01543));
        letterUnigramProbabilities.add(new DummyProbability('c', 0.03037));
        letterUnigramProbabilities.add(new DummyProbability('d', 0.03962));
        letterUnigramProbabilities.add(new DummyProbability('e', 0.12454));
        letterUnigramProbabilities.add(new DummyProbability('f', 0.02233));
        letterUnigramProbabilities.add(new DummyProbability('g', 0.01981));
        letterUnigramProbabilities.add(new DummyProbability('h', 0.05323));
        letterUnigramProbabilities.add(new DummyProbability('i', 0.07300));
        letterUnigramProbabilities.add(new DummyProbability('j', 0.00163));
        letterUnigramProbabilities.add(new DummyProbability('k', 0.00701));
        letterUnigramProbabilities.add(new DummyProbability('l', 0.04058));
        letterUnigramProbabilities.add(new DummyProbability('m', 0.02403));
        letterUnigramProbabilities.add(new DummyProbability('n', 0.07289));
        letterUnigramProbabilities.add(new DummyProbability('o', 0.07603));
        letterUnigramProbabilities.add(new DummyProbability('p', 0.02002));
        letterUnigramProbabilities.add(new DummyProbability('q', 0.00104));
        letterUnigramProbabilities.add(new DummyProbability('r', 0.06124));
        letterUnigramProbabilities.add(new DummyProbability('s', 0.06438));
        letterUnigramProbabilities.add(new DummyProbability('t', 0.09284));
        letterUnigramProbabilities.add(new DummyProbability('u', 0.02905));
        letterUnigramProbabilities.add(new DummyProbability('v', 0.01068));
        letterUnigramProbabilities.add(new DummyProbability('w', 0.01930));
        letterUnigramProbabilities.add(new DummyProbability('x', 0.00226));
        letterUnigramProbabilities.add(new DummyProbability('y', 0.01888));
        letterUnigramProbabilities.add(new DummyProbability('z', 0.00068));

        RouletteSampler<DummyProbability> rouletteSampler = new RouletteSampler<>();

        Collections.sort(letterUnigramProbabilities);

        rouletteSampler.reIndex(letterUnigramProbabilities);

        Map<Character, Integer> characterCounts = new HashMap<>();
        int nextIndex;

        int i = 0;

        for (; i < 1000000; i++) {
            nextIndex = rouletteSampler.getNextIndex();

            if (characterCounts.get(letterUnigramProbabilities.get(nextIndex).getValue()) == null) {
                characterCounts.put(letterUnigramProbabilities.get(nextIndex).getValue(), 1);
            }

            characterCounts.put(letterUnigramProbabilities.get(nextIndex).getValue(), characterCounts.get(letterUnigramProbabilities.get(nextIndex).getValue())
                    + 1);
        }

        for (DummyProbability letterProbability : letterUnigramProbabilities) {
            Double actual = letterProbability.getProbability();
            Double estimated = (double) characterCounts.get(letterProbability.getValue()) / (double) i;
            Double difference = Math.abs(actual - estimated);

            System.out.printf(letterProbability.getValue() + ": actual=" + actual.toString() + ", estimated="
                    + estimated.toString() + ", difference=" + difference.toString() + "\n");

            assertTrue(difference < 0.001d);
        }
    }
}
