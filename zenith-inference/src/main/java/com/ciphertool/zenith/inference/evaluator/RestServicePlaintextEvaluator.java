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

import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import com.ciphertool.zenith.inference.entities.*;
import com.ciphertool.zenith.inference.evaluator.model.RestServiceEvaluation;
import com.ciphertool.zenith.inference.evaluator.model.RestServiceEvaluationRequest;
import com.ciphertool.zenith.inference.evaluator.model.SolutionScore;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

@NoArgsConstructor
@Component
public class RestServicePlaintextEvaluator implements PlaintextEvaluator {
    private Logger log = LoggerFactory.getLogger(getClass());

    public static final String REST_SERVICE_URL = "restServiceUrl";

    @Autowired
    private RestTemplate restTemplate;

    private String restServiceUrl;

    private URI evaluationRestServiceEndpoint;

    public RestServicePlaintextEvaluator(RestTemplate restTemplate, Map<String, Object> data) {
        this.restTemplate = restTemplate;
        this.restServiceUrl = (String) data.get(REST_SERVICE_URL);
        this.evaluationRestServiceEndpoint = UriComponentsBuilder.fromHttpUrl(restServiceUrl).build().encode().toUri();
    }

    @Override
    public SolutionScore evaluate(Map<String, Object> precomputedData, Cipher cipher, CipherSolution solution, String solutionString, String ciphertextKey) {
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

        return new SolutionScore(logProbabilitiesUpdated, new Fitness[] { new MaximizingFitness(solution.getLogProbability()) });
    }

    @Override
    public Map<String, Object> getPrecomputedCounterweightData(Cipher cipher) {
        return null;
    }

    @Override
    public PlaintextEvaluator getInstance(Map<String, Object> data) {
        return new RestServicePlaintextEvaluator(restTemplate, data);
    }

    @Override
    public FormlyForm getForm() {
        FormlyForm form = new FormlyForm();

        FormlyTemplateOptions templateOptions = new FormlyTemplateOptions();
        templateOptions.setLabel("REST Service URL");
        templateOptions.setRequired(true);
        templateOptions.setType("url");

        FormlyFormField restServiceUrl = new FormlyFormField();
        restServiceUrl.setKey(REST_SERVICE_URL);
        restServiceUrl.setType("input");
        restServiceUrl.setTemplateOptions(templateOptions);

        form.setFields(Collections.singletonList(restServiceUrl));

        return form;
    }

    @Override
    public int getOrder() {
        return 7;
    }

    @Override
    public String getHelpText() {
        return "Uses a REST service to calculate fitness of each solution proposal.";
    }
}