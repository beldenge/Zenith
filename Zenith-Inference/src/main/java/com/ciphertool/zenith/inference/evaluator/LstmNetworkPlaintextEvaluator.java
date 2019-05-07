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
import com.ciphertool.zenith.model.ModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class LstmNetworkPlaintextEvaluator {
    private Logger log						= LoggerFactory.getLogger(getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Value("${lstm.service-url}")
    private String lstmServiceUrl;

    public List<EvaluationResults> evaluate(CipherSolution solution, String ciphertextKey) {
        String solutionString = solution.asSingleLineString();

        long startLstmPrediction = System.currentTimeMillis();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(lstmServiceUrl);

        LstmPredictionRequest request = new LstmPredictionRequest();

        if (ciphertextKey != null) {
            List<String> sequences = new ArrayList<>();

            List<Integer> ciphertextIndices = getCiphertextIndices(solution, ciphertextKey);

            for (Character letter : ModelConstants.LOWERCASE_LETTERS) {
                String nextSolutionString = solutionString;

                for (Integer indice : ciphertextIndices) {
                    nextSolutionString = nextSolutionString.substring(0, indice) + letter + nextSolutionString.substring(indice + 1);
                }

                sequences.add(nextSolutionString);
            }

            request.setSequences(sequences);
        } else {
            request.setSequences(Collections.singletonList(solutionString));
        }

        LstmPrediction response = restTemplate.postForObject(builder.build().encode().toUri(), request, LstmPrediction.class);

        log.debug("LSTM prediction took {}ms.", (System.currentTimeMillis() - startLstmPrediction));

        List<EvaluationResults> distributionToReturn = new ArrayList<>(request.getSequences().size());

        for (LstmProbability probability : response.getProbabilities()) {
            distributionToReturn.add(new EvaluationResults(probability.getProbability(), probability.getLogProbability()));
        }

        return distributionToReturn;
    }

    public List<Integer> getCiphertextIndices(CipherSolution solution, String ciphertextKey) {
        List<Integer> ciphertextIndices = new ArrayList<>();

        for (int i = 0; i < solution.getCipher().getCiphertextCharacters().size(); i++) {
            if (ciphertextKey.equals(solution.getCipher().getCiphertextCharacters().get(i).getValue())) {
                ciphertextIndices.add(i);
            }
        }

        return ciphertextIndices;
    }
}
