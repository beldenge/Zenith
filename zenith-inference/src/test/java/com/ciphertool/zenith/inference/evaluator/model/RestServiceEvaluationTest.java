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

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RestServiceEvaluationTest {
    @Test
    public void given_validInput_when_settingFields_then_returnsExpected() {
        EvaluationProbability probability = new EvaluationProbability();
        probability.setProbability(new BigDecimal("0.75"));
        probability.setLogProbability(-0.1f);

        RestServiceEvaluation evaluation = new RestServiceEvaluation();
        evaluation.setProbabilities(Arrays.asList(probability));
        evaluation.setScore(1.5f);

        assertEquals(1, evaluation.getProbabilities().size());
        assertEquals(1.5f, evaluation.getScore());
        assertNotNull(evaluation.toString());
    }
}