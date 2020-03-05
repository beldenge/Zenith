/*
 * Copyright 2017-2020 George Belden
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
import com.ciphertool.zenith.inference.evaluator.model.RestServiceEvaluation;
import com.ciphertool.zenith.inference.evaluator.model.RestServiceEvaluationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Collections;

@Component
@ConditionalOnProperty(value = "decipherment.evaluator.plaintext", havingValue = "RestServicePlaintextEvaluator")
public class RestServicePlaintextEvaluator implements PlaintextEvaluator {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Value("${evaluation.rest-service.url}")
    private String evaluationRestServiceUrl;

    private URI evaluationRestServiceEndpoint;

    @PostConstruct
    public void init() {
        evaluationRestServiceEndpoint = UriComponentsBuilder.fromHttpUrl(evaluationRestServiceUrl).build().encode().toUri();
    }

    @Override
    public float[][] evaluate(Cipher cipher, CipherSolution solution, String solutionString, String ciphertextKey) {
        long startEvaluation = System.currentTimeMillis();

        RestServiceEvaluationRequest request = new RestServiceEvaluationRequest();

        request.setSequences(Collections.singletonList(solutionString));

        RestServiceEvaluation response = restTemplate.postForObject(evaluationRestServiceEndpoint, request, RestServiceEvaluation.class);

        log.debug("Rest service evaluation took {}ms.", (System.currentTimeMillis() - startEvaluation));

        float[] logProbabilities = solution.getLogProbabilities();

        float[][] logProbabilitiesUpdated = new float[2][logProbabilities.length];

        for (int i = 0; i < logProbabilities.length; i ++) {
            logProbabilitiesUpdated[0][i] = i;
            logProbabilitiesUpdated[1][i] = logProbabilities[i];
        }

        solution.clearLogProbabilities();

        for (int i = 0; i < response.getProbabilities().size(); i ++) {
            solution.addLogProbability(i, response.getProbabilities().get(i).getLogProbability());
        }

        return logProbabilitiesUpdated;
    }
}