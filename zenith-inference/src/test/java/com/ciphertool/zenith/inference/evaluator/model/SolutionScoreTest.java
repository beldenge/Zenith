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

package com.ciphertool.zenith.inference.evaluator.model;

import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class SolutionScoreTest {
    @Test
    public void given_validInput_when_constructing_then_returnsExpected() {
        float[][] updated = new float[][] { new float[] { 0f }, new float[] { 1f } };
        Fitness[] scores = new Fitness[] { new MaximizingFitness(2.0d) };

        SolutionScore score = new SolutionScore(updated, scores);

        assertSame(updated, score.getNgramProbabilitiesUpdated());
        assertSame(scores, score.getScores());
        assertEquals(1f, score.getNgramProbabilitiesUpdated()[1][0]);
    }
}