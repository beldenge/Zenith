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

package com.ciphertool.zenith.api.service;

import com.ciphertool.zenith.api.model.*;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.inference.entities.config.GeneticAlgorithmConfiguration;
import com.ciphertool.zenith.inference.entities.config.SimulatedAnnealingConfiguration;
import com.ciphertool.zenith.inference.optimizer.GeneticAlgorithmSolutionOptimizer;
import com.ciphertool.zenith.inference.optimizer.OnEpochComplete;
import com.ciphertool.zenith.inference.optimizer.SimulatedAnnealingSolutionOptimizer;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class WebSocketSolutionService extends AbstractSolutionService {
    private static final String TYPE_HEADER_KEY = "type";

    private static Map<String, Object> epochCompleteHeaders = new HashMap<>();

    static {
        epochCompleteHeaders.put(TYPE_HEADER_KEY, WebSocketResponseType.EPOCH_COMPLETE);
    }

    @Autowired
    private SimpMessagingTemplate template;

    @Override
    public OnEpochComplete getCallback(SolutionRequest request) {
        return (i) -> template.convertAndSend("/topic/solutions", new EpochCompleteResponse(i, request.getEpochs()), epochCompleteHeaders);
    }

    @MessageMapping("/solutions")
    public void solve(@Validated @RequestBody SolutionRequest request) {
        CipherSolution cipherSolution = doSolve(request);

        Map<String, Object> solutionHeaders = new HashMap<>();
        solutionHeaders.put(TYPE_HEADER_KEY, WebSocketResponseType.SOLUTION);

        template.convertAndSend("/topic/solutions", new SolutionResponse(cipherSolution.asSingleLineString(), Double.valueOf(cipherSolution.getScore())), solutionHeaders);
    }
}
