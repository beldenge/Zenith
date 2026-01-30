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

package com.ciphertool.zenith.inference.genetic.fitness;

import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.model.SolutionScore;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyGene;
import com.ciphertool.zenith.inference.transformer.ciphertext.TransformationStep;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationManager;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlaintextEvaluatorWrappingFitnessEvaluatorTest {
    @Test
    public void given_noTransformations_when_evaluate_then_returnsScoresWithoutTransform() {
        Cipher cipher = new Cipher("test", 1, 3);
        cipher.setCiphertext(Arrays.asList("A", "B", "C"));

        Genome genome = new Genome(true, null, null);
        CipherKeyChromosome chromosome = new CipherKeyChromosome(genome, cipher, 3);
        chromosome.putGene("A", new CipherKeyGene(chromosome, "a"));
        chromosome.putGene("B", new CipherKeyGene(chromosome, "b"));
        chromosome.putGene("C", new CipherKeyGene(chromosome, "c"));
        genome.addChromosome(chromosome);

        PlaintextEvaluator evaluator = mock(PlaintextEvaluator.class);
        PlaintextTransformationManager manager = mock(PlaintextTransformationManager.class);

        Fitness[] scores = new Fitness[] { new MaximizingFitness(1.0d) };
        SolutionScore score = new SolutionScore(new float[2][0], scores);
        when(evaluator.evaluate(anyMap(), eq(cipher), any(), eq("abc"), isNull())).thenReturn(score);

        PlaintextEvaluatorWrappingFitnessEvaluator wrapper = new PlaintextEvaluatorWrappingFitnessEvaluator(
                Collections.emptyMap(), evaluator, manager, Collections.emptyList());

        Fitness[] result = wrapper.evaluate(genome);

        assertSame(scores, result);
        verify(manager, never()).transform(any(), any());
        verify(evaluator).evaluate(anyMap(), eq(cipher), any(), eq("abc"), isNull());
    }

    @Test
    public void given_transformations_when_evaluate_then_usesTransformedString() {
        Cipher cipher = new Cipher("test", 1, 3);
        cipher.setCiphertext(Arrays.asList("A", "B", "C"));

        Genome genome = new Genome(true, null, null);
        CipherKeyChromosome chromosome = new CipherKeyChromosome(genome, cipher, 3);
        chromosome.putGene("A", new CipherKeyGene(chromosome, "a"));
        chromosome.putGene("B", new CipherKeyGene(chromosome, "b"));
        chromosome.putGene("C", new CipherKeyGene(chromosome, "c"));
        genome.addChromosome(chromosome);

        PlaintextEvaluator evaluator = mock(PlaintextEvaluator.class);
        PlaintextTransformationManager manager = mock(PlaintextTransformationManager.class);
        List<TransformationStep> steps = Collections.singletonList(new TransformationStep("T", Collections.emptyMap()));

        when(manager.transform("abc", steps)).thenReturn("xyz");

        Fitness[] scores = new Fitness[] { new MaximizingFitness(2.0d) };
        SolutionScore score = new SolutionScore(new float[2][0], scores);
        when(evaluator.evaluate(anyMap(), eq(cipher), any(), eq("xyz"), isNull())).thenReturn(score);

        PlaintextEvaluatorWrappingFitnessEvaluator wrapper = new PlaintextEvaluatorWrappingFitnessEvaluator(
                Collections.emptyMap(), evaluator, manager, steps);

        Fitness[] result = wrapper.evaluate(genome);

        assertSame(scores, result);
        verify(manager).transform("abc", steps);
        verify(evaluator).evaluate(anyMap(), eq(cipher), any(), eq("xyz"), isNull());
    }
}