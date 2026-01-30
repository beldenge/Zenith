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

package com.ciphertool.zenith.inference.optimizer;

import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.model.SolutionScore;
import com.ciphertool.zenith.inference.printer.CipherSolutionPrinter;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationManager;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SimulatedAnnealingSolutionOptimizerTest {
    private ThreadLocalRandom originalRandom;

    @AfterEach
    public void tearDown() throws Exception {
        if (originalRandom != null) {
            setStaticField(SimulatedAnnealingSolutionOptimizer.class, "RANDOM", originalRandom);
        }
    }

    @Test
    public void given_singleEpoch_when_optimizing_then_returnsSolutionAndFiresCallback() throws Exception {
        SimulatedAnnealingSolutionOptimizer optimizer = buildOptimizer();

        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        originalRandom = (ThreadLocalRandom) getStaticField(SimulatedAnnealingSolutionOptimizer.class, "RANDOM");
        when(random.nextInt(anyInt())).thenReturn(0);
        setStaticField(SimulatedAnnealingSolutionOptimizer.class, "RANDOM", random);

        Cipher cipher = buildCipher();
        PlaintextEvaluator evaluator = mock(PlaintextEvaluator.class);
        Map<String, Object> precomputed = Collections.singletonMap("key", "value");

        when(evaluator.getPrecomputedCounterweightData(cipher)).thenReturn(precomputed);
        when(evaluator.evaluate(eq(precomputed), eq(cipher), any(CipherSolution.class), anyString(), isNull()))
                .thenReturn(new SolutionScore(new float[2][0], new Fitness[]{new MaximizingFitness(1.0d)}));

        Map<String, Object> config = buildConfig(1, 0.1f, 1.0f);

        OnEpochComplete callback = mock(OnEpochComplete.class);
        CipherSolution result = optimizer.optimize(cipher, 1, config, Collections.emptyList(), evaluator, callback);

        assertNotNull(result);
        assertEquals(1, result.getMappings().size());
        verify(evaluator).evaluate(eq(precomputed), eq(cipher), any(CipherSolution.class), anyString(), isNull());
        verify(callback).fire(1, result);
    }

    @Test
    public void given_multiObjectiveScores_when_sampling_then_throwsIllegalStateException() throws Exception {
        SimulatedAnnealingSolutionOptimizer optimizer = buildOptimizer();

        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        originalRandom = (ThreadLocalRandom) getStaticField(SimulatedAnnealingSolutionOptimizer.class, "RANDOM");
        when(random.nextInt(anyInt())).thenReturn(0, 1);
        setStaticField(SimulatedAnnealingSolutionOptimizer.class, "RANDOM", random);
        setField(optimizer, "biasedLetterBucket", new char[]{'a', 'b'});

        Cipher cipher = buildCipher();
        PlaintextEvaluator evaluator = mock(PlaintextEvaluator.class);
        Map<String, Object> precomputed = new HashMap<>();

        when(evaluator.getPrecomputedCounterweightData(cipher)).thenReturn(precomputed);
        when(evaluator.evaluate(eq(precomputed), eq(cipher), any(CipherSolution.class), anyString(), any()))
                .thenReturn(new SolutionScore(new float[2][0], new Fitness[]{new MaximizingFitness(1.0d), new MaximizingFitness(0.5d)}));

        Map<String, Object> config = buildConfig(1, 0.1f, 1.0f);

        assertThrows(IllegalStateException.class,
                () -> optimizer.optimize(cipher, 1, config, Collections.emptyList(), evaluator, null));
    }

    private SimulatedAnnealingSolutionOptimizer buildOptimizer() throws Exception {
        SimulatedAnnealingSolutionOptimizer optimizer = new SimulatedAnnealingSolutionOptimizer();

        ArrayMarkovModel model = new ArrayMarkovModel(5, 0.01f);
        TreeNGram a = new TreeNGram("a");
        a.setCount(3);
        TreeNGram b = new TreeNGram("b");
        b.setCount(1);
        model.addNode(a);
        model.addNode(b);

        setField(optimizer, "letterMarkovModel", model);
        setField(optimizer, "plaintextTransformationManager", mock(PlaintextTransformationManager.class));
        setField(optimizer, "cipherSolutionPrinter", mock(CipherSolutionPrinter.class));

        optimizer.init();

        return optimizer;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> current = target.getClass();
        Field field = null;

        while (current != null) {
            try {
                field = current.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException ex) {
                current = current.getSuperclass();
            }
        }

        if (field == null) {
            throw new NoSuchFieldException(fieldName);
        }

        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getStaticField(Class<?> target, String fieldName) throws Exception {
        Field field = target.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    private static void setStaticField(Class<?> target, String fieldName, Object value) throws Exception {
        Field field = target.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    private Map<String, Object> buildConfig(int samplerIterations, float minTemp, float maxTemp) {
        Map<String, Object> config = new HashMap<>();
        config.put(SimulatedAnnealingSolutionOptimizer.SAMPLER_ITERATIONS, samplerIterations);
        config.put(SimulatedAnnealingSolutionOptimizer.ANNEALING_TEMPERATURE_MIN, minTemp);
        config.put(SimulatedAnnealingSolutionOptimizer.ANNEALING_TEMPERATURE_MAX, maxTemp);
        return config;
    }

    private Cipher buildCipher() {
        Cipher cipher = new Cipher("test", 1, 4);
        cipher.setCiphertext(List.of("x", "x", "x", "x"));
        return cipher;
    }
}
