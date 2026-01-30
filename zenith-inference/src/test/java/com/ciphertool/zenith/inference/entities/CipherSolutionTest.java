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

package com.ciphertool.zenith.inference.entities;

import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CipherSolutionTest {
    @Test
    public void given_nullInput_when_constructing_then_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CipherSolution(null, 1));
    }

    @Test
    public void given_validInput_when_logProbabilitiesAndReplace_then_returnsExpectedValue() {
        Cipher cipher = new Cipher("test", 1, 3);
        cipher.setCiphertext(Arrays.asList("A", "B", "C"));

        CipherSolution solution = new CipherSolution(cipher, 3);
        solution.addLogProbability(0, 0.1f);
        solution.addLogProbability(1, 0.2f);
        solution.addLogProbability(2, 0.3f);

        assertEquals(0.6f, solution.getLogProbability(), 0.000001f);

        solution.replaceLogProbability(1, 0.5f);
        assertEquals(0.9f, solution.getLogProbability(), 0.000001f);

        solution.clearLogProbabilities();
        assertEquals(0f, solution.getLogProbability(), 0.000001f);
    }

    @Test
    public void given_validInput_when_mappingsPutAndReplace_then_returnsExpectedValue() {
        Cipher cipher = new Cipher("test", 1, 1);
        cipher.setCiphertext(Arrays.asList("A"));

        CipherSolution solution = new CipherSolution(cipher, 1);

        solution.putMapping("A", 'x');
        assertEquals('x', solution.getMappings().get("A"));

        solution.putMapping("A", 'y');
        assertEquals('x', solution.getMappings().get("A"));

        solution.replaceMapping("A", 'z');
        assertEquals('z', solution.getMappings().get("A"));

        solution.replaceMapping("B", 'q');
        assertEquals(1, solution.getMappings().size());
    }

    @Test
    public void given_knownSolution_when_evaluatingKnownSolution_then_returnsExpectedValue() {
        Cipher cipher = new Cipher("test", 1, 2);
        cipher.setCiphertext(Arrays.asList("A", "B"));
        cipher.putKnownSolutionMapping("A", "x");
        cipher.putKnownSolutionMapping("B", "y");

        CipherSolution solution = new CipherSolution(cipher, 2);
        solution.putMapping("A", 'x');
        solution.putMapping("B", 'z');

        assertEquals(0.5f, solution.evaluateKnownSolution(), 0.000001f);
    }

    @Test
    public void given_validInput_when_asSingleLineString_then_returnsExpectedValue() {
        Cipher cipher = new Cipher("test", 1, 3);
        cipher.setCiphertext(Arrays.asList("A", "B", "A"));

        CipherSolution solution = new CipherSolution(cipher, 2);
        solution.putMapping("A", 'x');
        solution.putMapping("B", 'y');

        assertEquals("xyx", solution.asSingleLineString());
    }

    @Test
    public void given_validInput_when_asSingleLineStringRequiresCipher_then_throwsIllegalStateException() {
        Cipher cipher = new Cipher("test", 1, 1);
        cipher.setCiphertext(Arrays.asList("A"));

        CipherSolution solution = new CipherSolution(cipher, 1);
        solution.setCipher(null);

        assertThrows(IllegalStateException.class, solution::asSingleLineString);
    }

    @Test
    public void given_validInput_when_cloningCopiesState_then_copiesState() {
        Cipher cipher = new Cipher("test", 1, 2);
        cipher.setCiphertext(Arrays.asList("A", "B"));

        CipherSolution solution = new CipherSolution(cipher, 2);
        solution.putMapping("A", 'x');
        solution.putMapping("B", 'y');
        solution.addLogProbability(0, 0.1f);
        solution.addLogProbability(1, 0.2f);
        solution.setScores(new Fitness[] { new MaximizingFitness(1.0d) });

        CipherSolution clone = solution.clone();

        assertNotSame(solution, clone);
        assertEquals(solution.getMappings(), clone.getMappings());
        assertNotSame(solution.getLogProbabilities(), clone.getLogProbabilities());
        assertEquals(solution.getLogProbability(), clone.getLogProbability(), 0.000001f);
        assertNotSame(solution.getScores()[0], clone.getScores()[0]);
    }
}
