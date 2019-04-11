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
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LstmNetworkPlaintextEvaluator {
    private Logger log						= LoggerFactory.getLogger(getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Value("${lstm.service-url}")
    private String lstmServiceUrl;

    @Value("${bayes.sampler.go-fast:false}")
    private boolean							goFast;

    public EvaluationResults evaluate(CipherSolution solution, String ciphertextKey) {
        String solutionString = solution.asSingleLineString();

        long startLstmPrediction = System.currentTimeMillis();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(lstmServiceUrl)
                .pathSegment(solutionString)
                .queryParam("include_time_steps", goFast);

        LstmPrediction lstmPrediction = restTemplate.getForObject(builder.build().encode().toUri(), LstmPrediction.class);

        log.debug("LSTM prediction took {}ms.", (System.currentTimeMillis() - startLstmPrediction));

        Map<String, EvaluationResults> distributionToReturn = new HashMap<>();
        Map<String, Float> distribution = null;

        if (goFast && ciphertextKey != null) {
            List<Integer> ciphertextIndices = getCiphertextIndices(solution, ciphertextKey);

            distribution = flattenMap(lstmPrediction.getTimeStepProbabilities().get(ciphertextIndices.get(0)));

            for (Map.Entry<String, Float> entry : distribution.entrySet()) {
                distributionToReturn.put(entry.getKey(), new EvaluationResults(BigDecimal.valueOf(entry.getValue()), (float) Math.log(entry.getValue())));
            }

            for (int i = 1; i < ciphertextIndices.size(); i++) {
                Map<String, Float> next = flattenMap(lstmPrediction.getTimeStepProbabilities().get(ciphertextIndices.get(i)));

                for (String key : distribution.keySet()) {
                    EvaluationResults oldEvaluationResults = distributionToReturn.get(key);

                    BigDecimal nextProbability = oldEvaluationResults.getProbability().multiply(BigDecimal.valueOf(next.get(key)));
                    Float nextLogProbability = oldEvaluationResults.getLogProbability() + (float) Math.log(next.get(key));
                    EvaluationResults newEvaluationResults = new EvaluationResults(nextProbability, nextLogProbability);

                    distributionToReturn.put(key, newEvaluationResults);
                }
            }
        }

        return new EvaluationResults(lstmPrediction.getProbability(), lstmPrediction.getLogProbability(), distributionToReturn);
    }

    public List<Integer> getCiphertextIndices(CipherSolution solution, String ciphertextKey) {
        List<Integer> ciphertextIndices = new ArrayList<>();

        // Subtract one because this only returns the next character predictions, which by definition cannot predict the last character
        // TODO: eventually train a model that predicts the first character, so that we can evaluate every time step
        for (int i = 0; i < solution.getCipher().getCiphertextCharacters().size() - 1; i++) {
            if (ciphertextKey.equals(solution.getCipher().getCiphertextCharacters().get(i).getValue())) {
                ciphertextIndices.add(i);
            }
        }

        return ciphertextIndices;
    }

    public Map<String, Float> flattenMap(List<Map<String, Float>> mapsToFlatten) {
        Map<String, Float> flattened = new HashMap<>();

        for (Map<String, Float> map : mapsToFlatten) {
            for (Map.Entry<String, Float> entry : map.entrySet()) {
                flattened.put(entry.getKey(), entry.getValue());
            }
        }

        return flattened;
    }
}
