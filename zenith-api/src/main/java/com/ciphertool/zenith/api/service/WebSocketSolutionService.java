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

import com.ciphertool.zenith.api.model.EpochCompleteResponse;
import com.ciphertool.zenith.api.model.SolutionRequest;
import com.ciphertool.zenith.api.model.SolutionResponse;
import com.ciphertool.zenith.api.model.WebSocketResponseType;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.optimizer.OnEpochComplete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;

@Controller
public class WebSocketSolutionService extends AbstractSolutionService {
    private static final String TYPE_HEADER_KEY = "type";

    @Autowired
    private SimpMessagingTemplate template;

    @Override
    public OnEpochComplete getCallback(SolutionRequest request) {
        return (i) -> template.convertAndSend("/topic/solutions", new EpochCompleteResponse(i, request.getEpochs()), Collections.singletonMap(TYPE_HEADER_KEY, WebSocketResponseType.EPOCH_COMPLETE));
    }

    @MessageMapping("/solutions")
    public void solve(@Validated @RequestBody SolutionRequest request) {
        CipherSolution cipherSolution;

        try {
            cipherSolution = doSolve(request);
        } catch (Exception e) {
            template.convertAndSend("/topic/solutions", new SolutionResponse(null, null), Collections.singletonMap(TYPE_HEADER_KEY, WebSocketResponseType.ERROR));
            return;
        }

        template.convertAndSend("/topic/solutions", new SolutionResponse(cipherSolution.asSingleLineString(), Double.valueOf(cipherSolution.getScore())), Collections.singletonMap(TYPE_HEADER_KEY, WebSocketResponseType.SOLUTION));
    }
}
