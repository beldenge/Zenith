/**
 * Copyright 2017-2019 George Belden
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

import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.evaluator.model.LstmPrediction;
import com.ciphertool.zenith.inference.evaluator.model.LstmPredictionRequest;
import com.ciphertool.zenith.inference.evaluator.model.LstmProbability;
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
@ConditionalOnProperty(value="decipherment.evaluator.plaintext", havingValue = "LstmNetworkPlaintextEvaluator")
public class LstmNetworkPlaintextEvaluator implements PlaintextEvaluator {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Value("${lstm.service-url}")
    private String lstmServiceUrl;

    private URI lstmServiceEndpoint;

    @PostConstruct
    public void init() {
        lstmServiceEndpoint = UriComponentsBuilder.fromHttpUrl(lstmServiceUrl).build().encode().toUri();
    }

    @Override
    public void evaluate(CipherSolution solution, String ciphertextKey) {
        String solutionString = solution.asSingleLineString();

        long startLstmPrediction = System.currentTimeMillis();

        LstmPredictionRequest request = new LstmPredictionRequest();

        request.setSequences(Collections.singletonList(solutionString));

        LstmPrediction response = restTemplate.postForObject(lstmServiceEndpoint, request, LstmPrediction.class);

        log.debug("LSTM prediction took {}ms.", (System.currentTimeMillis() - startLstmPrediction));

        solution.clearLogProbabilities();

        for (LstmProbability probability : response.getProbabilities()) {
            solution.addLogProbability(probability.getLogProbability());
        }
    }
}