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

package com.ciphertool.zenith.inference.statistics;

import com.ciphertool.zenith.inference.entities.Cipher;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CiphertextCycleCountEvaluator {
    private Logger log = LoggerFactory.getLogger(getClass());

    public int evaluate(Cipher cipher) {
        List<String> uniqueCiphertextCharacters = cipher.getCiphertextCharacters().stream()
                .map(ciphertext -> ciphertext.getValue())
                .distinct()
                .collect(Collectors.toList());

        long startEvaluation = System.currentTimeMillis();

        int end = cipher.length();

        Map<String, List<Integer>> ciphertextIndices = new HashMap<>();
        for (int i = 0; i < end; i++) {
            String ciphertext = cipher.getCiphertextCharacters().get(i).getValue();

            if (!ciphertextIndices.containsKey(ciphertext)) {
                ciphertextIndices.put(ciphertext, new ArrayList<>());
            }

            ciphertextIndices.get(ciphertext).add(i);
        }

        List<CyclePair> cyclePairs = new ArrayList<>((uniqueCiphertextCharacters.size() * (uniqueCiphertextCharacters.size() - 1)) / 2);
        for (CyclePair cyclePair : calculateUniqueCyclePairs(uniqueCiphertextCharacters, ciphertextIndices)) {
            cyclePairs.add((CyclePair) cyclePair.clone());
        }

        int score = 0;
        for (CyclePair cyclePair : cyclePairs) {
            List<Integer> firstIndices = ciphertextIndices.get(cyclePair.getFirst());
            List<Integer> secondIndices = ciphertextIndices.get(cyclePair.getSecond());

            if (firstIndices == null || secondIndices == null) {
                continue;
            }

            int firstListIndex = 0;
            int secondListIndex = 0;
            for (int i = 0; i < firstIndices.size() + secondIndices.size(); i ++) {
                Integer nextFirst = (firstListIndex < firstIndices.size()) ? firstIndices.get(firstListIndex) : null;
                Integer nextSecond = (secondListIndex < secondIndices.size()) ? secondIndices.get(secondListIndex) : null;

                if (nextFirst != null && (nextSecond == null || nextFirst < nextSecond)) {
                    cyclePair.addCiphertext(cyclePair.getFirst());
                    firstListIndex ++;
                } else {
                    cyclePair.addCiphertext(cyclePair.getSecond());
                    secondListIndex ++;
                }
            }

            int longestSequenceLength = cyclePair.getLongestSequenceLength();

            // Cycles of less than four in length are insignificant
            if (longestSequenceLength > 3) {
                score += (int) Math.pow(longestSequenceLength, 2);
            }
        }

        log.debug("Cipher evaluation took {}ms.", (System.currentTimeMillis() - startEvaluation));

        return score;
    }

    private List<CyclePair> calculateUniqueCyclePairs(List<String> uniqueCiphertextCharacters, Map<String, List<Integer>> ciphertextIndices) {
        List<CyclePair> uniqueCyclePairs = new ArrayList<>((uniqueCiphertextCharacters.size() * (uniqueCiphertextCharacters.size() - 1)) / 2);
        for (String first : uniqueCiphertextCharacters) {
            for (String second : uniqueCiphertextCharacters) {
                if (first == second) {
                    continue;
                }

                CyclePair cyclePair = new CyclePair(first, second);

                if (uniqueCyclePairs.contains(cyclePair)) {
                    continue;
                }

                uniqueCyclePairs.add(cyclePair);
            }
        }

        // Remove cycle pairs where either ciphertext value occurs less than twice in the cipher
        uniqueCyclePairs.removeIf(cyclePair ->
                ciphertextIndices.get(cyclePair.getFirst()).size() < 2 ||
                ciphertextIndices.get(cyclePair.getSecond()).size() < 2
        );

        return uniqueCyclePairs;
    }

    @Getter
    public class CyclePair implements Cloneable {
        private String first;
        private String second;
        private List<String> occurenceSequence = new ArrayList<>();

        public CyclePair(String first, String second) {
            this.first = first;
            this.second = second;
        }

        public boolean contains(String value) {
            return first.equals(value) || second.equals(value);
        }

        @Override
        protected Object clone() {
            return new CyclePair(this.first, this.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second) + Objects.hash(second, first);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CyclePair other = (CyclePair) o;
            return this.contains(other.first) && this.contains(other.second);
        }

        public void addCiphertext(String ciphertext) {
            occurenceSequence.add(ciphertext);
        }

        public int getLongestSequenceLength() {
            if (occurenceSequence.size() == 0) {
                return 0;
            }

            List<Integer> alternatingSequenceLengths = new ArrayList<>(1);

            int alternatingSequenceLength = 0;
            String previous = occurenceSequence.get(0);
            String next;
            for (int i = 1; i < occurenceSequence.size(); i ++) {
                next = occurenceSequence.get(i);

                if (!next.equals(previous)) {
                    alternatingSequenceLength ++;
                } else {
                    alternatingSequenceLengths.add(alternatingSequenceLength);
                    alternatingSequenceLength = 0;
                }

                previous = next;
            }

            alternatingSequenceLengths.add(alternatingSequenceLength);

            return alternatingSequenceLengths.stream()
                    .mapToInt(sequenceLength -> sequenceLength.intValue())
                    .max()
                    .orElse(0);
        }
    }
}
