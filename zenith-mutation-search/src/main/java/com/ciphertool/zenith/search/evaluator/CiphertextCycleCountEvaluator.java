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

package com.ciphertool.zenith.search.evaluator;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CiphertextCycleCountEvaluator {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${decipherment.remove-last-row:true}")
    private boolean removeLastRow;

    @Autowired
    private Cipher cipher;

    private List<String> uniqueCiphertextCharacters;

    private List<CyclePair> uniqueCyclePairs;

    @PostConstruct
    public void init() {
        uniqueCiphertextCharacters = cipher.getCiphertextCharacters().stream()
                .map(ciphertext -> ciphertext.getValue())
                .distinct()
                .collect(Collectors.toList());

        uniqueCyclePairs = new ArrayList<>((uniqueCiphertextCharacters.size() * (uniqueCiphertextCharacters.size() - 1)) / 2);
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

        int end = cipher.length();

        if (removeLastRow) {
            end = (cipher.getColumns() * (cipher.getRows() - 1));
        }

        // Remove cycle pairs that are guaranteed to not produce useful scores, interpreted as occurence sequences which don't contain at least two of each ciphertext value
        // TODO: a simpler way to do this would be to simply remove all cycle pairs where one of the ciphertext values only occurs once in the cipher
        List<CyclePair> insignificantCyclePairs = new ArrayList<>();

        for (CyclePair cyclePair : uniqueCyclePairs) {
            for (int i = 0; i < end - 1; i++) {
                String ciphertext = cipher.getCiphertextCharacters().get(i).getValue();

                if (cyclePair.contains(ciphertext)) {
                    cyclePair.addCiphertext(ciphertext);
                }
            }

            int firstCount = (int) cyclePair.getOccurenceSequence().stream()
                    .filter(occurence -> occurence.equals(cyclePair.getFirst()))
                    .count();

            int secondCount = (int) cyclePair.getOccurenceSequence().stream()
                    .filter(occurence -> occurence.equals(cyclePair.getSecond()))
                    .count();

            if (firstCount < 2 || secondCount < 2) {
                insignificantCyclePairs.add(cyclePair);
            }
        }

        for (CyclePair insignificantCyclePair : insignificantCyclePairs) {
            uniqueCyclePairs.remove(insignificantCyclePair);
        }
    }

    public int evaluate(CipherSolution solutionProposal) {
        long startEvaluation = System.currentTimeMillis();

        Cipher cipherProposal = solutionProposal.getCipher();
        int end = cipherProposal.length();

        if (removeLastRow) {
            end = (cipherProposal.getColumns() * (cipherProposal.getRows() - 1));
        }

        Map<String, List<Integer>> ciphertextIndices = new HashMap<>();
        for (int i = 0; i < end; i++) {
            String ciphertext = cipherProposal.getCiphertextCharacters().get(i).getValue();

            if (!ciphertextIndices.containsKey(ciphertext)) {
                ciphertextIndices.put(ciphertext, new ArrayList<>());
            }

            ciphertextIndices.get(ciphertext).add(i);
        }

        List<CyclePair> cyclePairs = new ArrayList<>((uniqueCiphertextCharacters.size() * (uniqueCiphertextCharacters.size() - 1)) / 2);
        for (CyclePair cyclePair : uniqueCyclePairs) {
            cyclePairs.add((CyclePair) cyclePair.clone());
        }

        for (CyclePair cyclePair : cyclePairs) {
            List<Integer> firstIndices = ciphertextIndices.get(cyclePair.getFirst());
            List<Integer> secondIndices = ciphertextIndices.get(cyclePair.getSecond());

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
        }

        int score = 0;
        for (CyclePair cyclePair : cyclePairs) {
            int longestSequenceLength = cyclePair.getLongestSequenceLength();

            // Cycles of less than four in length are insignificant
            if (longestSequenceLength > 3) {
                score += (int) Math.pow(cyclePair.getLongestSequenceLength(), 2);
            }
        }

        solutionProposal.setProbability((double) score);
        solutionProposal.clearLogProbabilities();
        solutionProposal.addLogProbability((double) score);

        log.debug("Cipher evaluation took {}ms.", (System.currentTimeMillis() - startEvaluation));

        return score;
    }

    public class CyclePair implements Cloneable {
        @Getter
        private String first;

        @Getter
        private String second;

        @Getter
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
