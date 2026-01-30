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
import com.ciphertool.zenith.inference.evaluator.model.SolutionScore;
import com.ciphertool.zenith.inference.util.IndexOfCoincidenceEvaluator;
import com.ciphertool.zenith.inference.util.MathUtils;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NgramAndIndexOfCoincidencePlaintextEvaluatorTest {
    @Test
    public void GivenValidInputs_WhenEvaluating_ThenReturnsExpectedFitness() {
        ArrayMarkovModel model = buildModel();
        IndexOfCoincidenceEvaluator indexEvaluator = mock(IndexOfCoincidenceEvaluator.class);
        Cipher cipher = buildCipher();
        CipherSolution solution = new CipherSolution(cipher, 1);
        String solutionString = "abcdeabcde";
        Map<String, Object> precomputed = Collections.singletonMap("key", "value");

        when(indexEvaluator.evaluate(precomputed, cipher, solutionString)).thenReturn(0.18f);

        NgramAndIndexOfCoincidencePlaintextEvaluator evaluator = new NgramAndIndexOfCoincidencePlaintextEvaluator(model, indexEvaluator, precomputed);

        SolutionScore score = evaluator.evaluate(precomputed, cipher, solution, solutionString, null);

        assertNotNull(score);
        assertNotNull(score.getScores());
        assertEquals(1, score.getScores().length);

        double expected = (solution.getLogProbability() / (double) solution.getLogProbabilities().length)
                * MathUtils.powSixthRoot(0.18f);
        assertEquals(expected, score.getScores()[0].getValue(), 0.000001d);
        verify(indexEvaluator).evaluate(precomputed, cipher, solutionString);
    }

    @Test
    public void GivenCipher_WhenGetPrecomputedCounterweightData_ThenDelegatesToEvaluator() {
        ArrayMarkovModel model = buildModel();
        IndexOfCoincidenceEvaluator indexEvaluator = mock(IndexOfCoincidenceEvaluator.class);
        Cipher cipher = buildCipher();
        Map<String, Object> precomputed = Collections.singletonMap("weight", 1);

        when(indexEvaluator.precompute(cipher)).thenReturn(precomputed);

        NgramAndIndexOfCoincidencePlaintextEvaluator evaluator = new NgramAndIndexOfCoincidencePlaintextEvaluator(model, indexEvaluator, Collections.emptyMap());

        assertEquals(precomputed, evaluator.getPrecomputedCounterweightData(cipher));
        verify(indexEvaluator).precompute(cipher);
    }

    @Test
    public void GivenData_WhenGetInstance_ThenReturnsNewEvaluator() {
        ArrayMarkovModel model = buildModel();
        IndexOfCoincidenceEvaluator indexEvaluator = mock(IndexOfCoincidenceEvaluator.class);
        Map<String, Object> data = Collections.singletonMap("key", "value");

        NgramAndIndexOfCoincidencePlaintextEvaluator evaluator = new NgramAndIndexOfCoincidencePlaintextEvaluator(model, indexEvaluator, data);
        PlaintextEvaluator instance = evaluator.getInstance(data);

        assertNotNull(instance);
        assertNotSame(evaluator, instance);
        assertEquals(NgramAndIndexOfCoincidencePlaintextEvaluator.class, instance.getClass());
    }

    private ArrayMarkovModel buildModel() {
        ArrayMarkovModel model = new ArrayMarkovModel(5, 0.01f);
        TreeNGram ngram = new TreeNGram("abcde");
        ngram.setLogProbability(-2.0d);
        model.addNode(ngram);
        return model;
    }

    private Cipher buildCipher() {
        Cipher cipher = new Cipher("test", 1, 10);
        cipher.setCiphertext(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"));
        return cipher;
    }
}
