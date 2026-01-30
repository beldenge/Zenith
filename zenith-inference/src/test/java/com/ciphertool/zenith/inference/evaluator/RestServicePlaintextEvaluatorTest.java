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

import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.FormlyFormField;
import com.ciphertool.zenith.inference.evaluator.model.EvaluationProbability;
import com.ciphertool.zenith.inference.evaluator.model.RestServiceEvaluation;
import com.ciphertool.zenith.inference.evaluator.model.SolutionScore;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestServicePlaintextEvaluatorTest {
    @Test
    public void given_validInput_when_evaluate_then_returnsExpectedScoreAndUpdatesSolution() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        HashMap<String, Object> data = new HashMap<>();
        data.put(RestServicePlaintextEvaluator.REST_SERVICE_URL, "http://localhost:8080/eval");

        RestServicePlaintextEvaluator evaluator = new RestServicePlaintextEvaluator(restTemplate, data);

        EvaluationProbability p1 = new EvaluationProbability();
        p1.setProbability(new BigDecimal("0.1"));
        p1.setLogProbability(-1.0f);
        EvaluationProbability p2 = new EvaluationProbability();
        p2.setProbability(new BigDecimal("0.2"));
        p2.setLogProbability(-2.0f);
        EvaluationProbability p3 = new EvaluationProbability();
        p3.setProbability(new BigDecimal("0.3"));
        p3.setLogProbability(-3.0f);

        RestServiceEvaluation response = new RestServiceEvaluation();
        response.setProbabilities(Arrays.asList(p1, p2, p3));
        response.setScore(1.0f);

        when(restTemplate.postForObject(any(), any(), eq(RestServiceEvaluation.class))).thenReturn(response);

        Cipher cipher = new Cipher("test", 1, 3);
        cipher.setCiphertext(Arrays.asList("A", "B", "C"));

        CipherSolution solution = new CipherSolution(cipher, 3);
        solution.putMapping("A", 'a');
        solution.putMapping("B", 'b');
        solution.putMapping("C", 'c');
        solution.addLogProbability(0, 0.1f);
        solution.addLogProbability(1, 0.2f);
        solution.addLogProbability(2, 0.3f);

        SolutionScore score = evaluator.evaluate(null, cipher, solution, "abc", null);

        assertNotNull(score);
        assertEquals(3, score.getNgramProbabilitiesUpdated()[0].length);
        assertEquals(0.1f, score.getNgramProbabilitiesUpdated()[1][0]);
        assertEquals(0.2f, score.getNgramProbabilitiesUpdated()[1][1]);
        assertEquals(0.3f, score.getNgramProbabilitiesUpdated()[1][2]);

        Fitness[] scores = score.getScores();
        assertEquals(1, scores.length);
        assertTrue(scores[0] instanceof MaximizingFitness);
        assertEquals(-6.0d, scores[0].getValue(), 0.000001d);
        assertEquals(-6.0f, solution.getLogProbability(), 0.000001f);
    }

    @Test
    public void given_validInput_when_getForm_then_returnsRestServiceField() {
        RestServicePlaintextEvaluator evaluator = new RestServicePlaintextEvaluator();
        FormlyForm form = evaluator.getForm();

        List<FormlyFormField> fields = form.getFields();
        assertEquals(1, fields.size());
        FormlyFormField field = fields.get(0);
        assertEquals(RestServicePlaintextEvaluator.REST_SERVICE_URL, field.getKey());
        assertEquals("input", field.getType());
        assertTrue(field.getProps().isRequired());
        assertEquals("url", field.getProps().getType());
        assertEquals("REST Service URL", field.getProps().getLabel());
    }

    @Test
    public void given_validInput_when_getInstance_then_returnsNewEvaluator() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        HashMap<String, Object> data = new HashMap<>();
        data.put(RestServicePlaintextEvaluator.REST_SERVICE_URL, "http://localhost:8080/eval");

        RestServicePlaintextEvaluator evaluator = new RestServicePlaintextEvaluator(restTemplate, data);

        PlaintextEvaluator instance = evaluator.getInstance(data);

        assertNotNull(instance);
        assertTrue(instance instanceof RestServicePlaintextEvaluator);
    }
}
