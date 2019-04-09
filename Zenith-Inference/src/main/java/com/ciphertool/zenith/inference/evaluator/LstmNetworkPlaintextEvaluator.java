/**
 * Copyright 2017 George Belden
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

import com.ciphertool.zenith.inference.dto.EvaluationResults;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LstmNetworkPlaintextEvaluator {
    private Logger log						= LoggerFactory.getLogger(getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Value("${lstm.service-url}")
    private String lstmServiceUrl;

    public EvaluationResults evaluate(CipherSolution solution, String ciphertextKey) {
        String solutionString = solution.asSingleLineString();

        long startLstmPrediction = System.currentTimeMillis();

        LstmPrediction lstmPrediction = restTemplate.getForObject(lstmServiceUrl + solutionString, LstmPrediction.class);

        log.debug("LSTM prediction took {}ms.", (System.currentTimeMillis() - startLstmPrediction));

        Float probability = lstmPrediction.getProbability();

        if (probability == 0f) {
            probability = 0.000001f;
        }

        return new EvaluationResults(probability, lstmPrediction.getLogProbability());
    }
}
