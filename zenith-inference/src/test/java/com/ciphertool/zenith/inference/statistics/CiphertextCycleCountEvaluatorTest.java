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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CiphertextCycleCountEvaluatorTest {
    private CiphertextCycleCountEvaluator evaluator;

    @BeforeEach
    public void setUp() {
        evaluator = new CiphertextCycleCountEvaluator();
    }

    @Test
    public void given_validInput_when_evaluatingWithAlternatingCycle_then_returnsExpectedValue() {
        // Create a cipher with a strong alternating cycle pattern: A B A B A B A B
        // This should produce a cycle of length 7 (each alternation counts)
        // Score = 7^2 = 49
        Cipher cipher = new Cipher("test", 1, 8);
        cipher.setCiphertext(Arrays.asList("A", "B", "A", "B", "A", "B", "A", "B"));

        int score = evaluator.evaluate(cipher);

        assertEquals(49, score);
    }

    @Test
    public void given_missingDependency_when_evaluatingWithNoSignificantCycles_then_returnsExpectedValue() {
        // Create a cipher with cycles of length 3 or less (insignificant)
        // Pattern: A B A B C C - alternating sequence of length 3
        Cipher cipher = new Cipher("test", 1, 6);
        cipher.setCiphertext(Arrays.asList("A", "B", "A", "B", "C", "C"));

        int score = evaluator.evaluate(cipher);

        // The A-B pair has alternating length of 3, which is not > 3, so score is 0
        assertEquals(0, score);
    }

    @Test
    public void given_validInput_when_evaluatingFiltersOutSingleOccurrenceCharacters_then_returnsExpectedValue() {
        // Characters that only occur once should be filtered out
        // Pattern: A B A B A X Y Z - X, Y, Z occur only once
        // Only A-B pair should be considered
        Cipher cipher = new Cipher("test", 1, 8);
        cipher.setCiphertext(Arrays.asList("A", "B", "A", "B", "A", "X", "Y", "Z"));

        int score = evaluator.evaluate(cipher);

        // A-B alternating sequence: A B A B A = length 4
        // Score = 4^2 = 16
        assertEquals(16, score);
    }

    @Test
    public void given_validInput_when_evaluatingWithAllSingleOccurrences_then_returnsExpectedValue() {
        // All characters occur only once - should return 0
        Cipher cipher = new Cipher("test", 1, 5);
        cipher.setCiphertext(Arrays.asList("A", "B", "C", "D", "E"));

        int score = evaluator.evaluate(cipher);

        assertEquals(0, score);
    }

    @Test
    public void given_validInput_when_evaluatingWithMultipleCyclePairs_then_returnsExpectedValue() {
        // Pattern with multiple cycle pairs that have significant cycles
        // A B A B A B C D C D C D
        // A-B: length 5, score = 25
        // C-D: length 5, score = 25
        // Total = 50
        Cipher cipher = new Cipher("test", 1, 12);
        cipher.setCiphertext(Arrays.asList("A", "B", "A", "B", "A", "B", "C", "D", "C", "D", "C", "D"));

        int score = evaluator.evaluate(cipher);

        assertEquals(50, score);
    }

    @Test
    public void given_validInput_when_evaluatingWithBrokenCycle_then_returnsExpectedValue() {
        // Pattern where cycle is broken: A B A B X A B A B
        // The X breaks the alternation, creating two separate sequences
        Cipher cipher = new Cipher("test", 1, 9);
        cipher.setCiphertext(Arrays.asList("A", "B", "A", "B", "X", "A", "B", "A", "B"));

        int score = evaluator.evaluate(cipher);

        // X only occurs once, so it's filtered out from cycle pairs
        // A-B sequence without X: positions 0,1,2,3,5,6,7,8 -> A B A B A B A B
        // This gives alternating length of 7, score = 49
        assertEquals(49, score);
    }

    @Test
    public void given_validInput_when_evaluatingWithRepeatedCharacters_then_returnsExpectedValue() {
        // Pattern: A A B B A A B B - no alternation
        Cipher cipher = new Cipher("test", 1, 8);
        cipher.setCiphertext(Arrays.asList("A", "A", "B", "B", "A", "A", "B", "B"));

        int score = evaluator.evaluate(cipher);

        // Sequence: A A B B A A B B
        // Alternations: A->A (no), A->B (yes), B->B (no), B->A (yes), A->A (no), A->B (yes), B->B (no)
        // Longest alternating sequence = 1
        // Not > 3, so score = 0
        assertEquals(0, score);
    }

    @Test
    public void given_validInput_when_evaluatingMinimalCipher_then_returnsExpectedValue() {
        // Minimal cipher with just two characters each appearing twice
        Cipher cipher = new Cipher("test", 1, 4);
        cipher.setCiphertext(Arrays.asList("A", "B", "A", "B"));

        int score = evaluator.evaluate(cipher);

        // Alternating length = 3, not > 3, so score = 0
        assertEquals(0, score);
    }

    @Test
    public void given_validInput_when_evaluatingJustAboveThreshold_then_returnsExpectedValue() {
        // Create a pattern that just crosses the threshold (length > 3 means length >= 4)
        // A B A B A = alternating length 4
        Cipher cipher = new Cipher("test", 1, 5);
        cipher.setCiphertext(Arrays.asList("A", "B", "A", "B", "A"));

        int score = evaluator.evaluate(cipher);

        // Alternating length = 4, which is > 3
        // Score = 4^2 = 16
        assertEquals(16, score);
    }
}
