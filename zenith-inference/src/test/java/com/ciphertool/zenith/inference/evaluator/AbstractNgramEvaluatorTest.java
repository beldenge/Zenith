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

package com.ciphertool.zenith.inference.evaluator;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AbstractNgramEvaluatorTest {
    @Test
    public void given_existingLogProbabilities_when_ciphertextKeyIsNull_then_replacesAndReturnsPreviousProbabilities() {
        ArrayMarkovModel model = buildModel();
        Cipher cipher = buildCipher("test", Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"));
        CipherSolution solution = new CipherSolution(cipher, 1);
        solution.addLogProbability(0, 0.5f);
        solution.addLogProbability(1, 1.0f);

        TestNgramEvaluator evaluator = new TestNgramEvaluator(model);
        evaluator.init();

        float[][] updated = evaluator.evaluate(cipher, solution, "abcdeabcde", null);

        assertNotNull(updated);
        assertEquals(2, updated.length);
        assertEquals(solution.getLogProbabilities().length, updated[0].length);
        assertEquals(0f, updated[0][0]);
        assertEquals(0.5f, updated[1][0]);
        assertEquals(1f, updated[0][1]);
        assertEquals(1.0f, updated[1][1]);
        assertEquals(-2.0f, solution.getLogProbability(0), 0.0001f);
    }

    @Test
    public void given_ciphertextKey_when_evaluating_then_updatesOnlyRelevantNgrams() {
        ArrayMarkovModel model = buildModel();
        Cipher cipher = buildCipher("test", Arrays.asList("a", "b", "c", "d", "x", "f", "g", "h", "i", "j"));
        CipherSolution solution = new CipherSolution(cipher, 1);
        solution.addLogProbability(0, 0.25f);
        solution.addLogProbability(1, 0.75f);
        solution.addLogProbability(2, 1.25f);

        TestNgramEvaluator evaluator = new TestNgramEvaluator(model);
        evaluator.init();

        float[][] updated = evaluator.evaluate(cipher, solution, "abcdeabcde", "x");

        assertNotNull(updated);
        assertEquals(2, updated.length);
        assertEquals(3, updated[0].length);
        assertEquals(0f, updated[0][0]);
        assertEquals(0.25f, updated[1][0]);
        assertEquals(1f, updated[0][1]);
        assertEquals(0.75f, updated[1][1]);
        assertEquals(2f, updated[0][2]);
        assertEquals(1.25f, updated[1][2]);
        assertEquals(-2.0f, solution.getLogProbability(0), 0.0001f);
    }

    private ArrayMarkovModel buildModel() {
        ArrayMarkovModel model = new ArrayMarkovModel(5, 0.01f);
        TreeNGram ngram = new TreeNGram("abcde");
        ngram.setLogProbability(-2.0d);
        model.addNode(ngram);
        return model;
    }

    private Cipher buildCipher(String name, java.util.List<String> ciphertext) {
        Cipher cipher = new Cipher(name, 1, ciphertext.size());
        cipher.setCiphertext(ciphertext);
        return cipher;
    }

    private static class TestNgramEvaluator extends AbstractNgramEvaluator {
        TestNgramEvaluator(ArrayMarkovModel model) {
            this.letterMarkovModel = model;
        }

        float[][] evaluate(Cipher cipher, CipherSolution solution, String solutionString, String ciphertextKey) {
            return evaluateLetterNGrams(cipher, solution, solutionString, ciphertextKey);
        }
    }
}
