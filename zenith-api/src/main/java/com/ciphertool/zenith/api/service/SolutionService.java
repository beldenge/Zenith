/**
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
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.inference.optimizer.SimulatedAnnealingSolutionOptimizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SolutionService {
    private static final String TYPE_HEADER_KEY = "type";

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private SimulatedAnnealingSolutionOptimizer optimizer;

    @MessageMapping("/solutions")
    public void solve(@Validated @RequestBody SolutionRequest request) {
        Cipher cipher = new Cipher(null, request.getRows(), request.getColumns());

        for (int i = 0; i < request.getCiphertext().length(); i ++) {
            cipher.addCiphertextCharacter(new Ciphertext(i, String.valueOf(request.getCiphertext().charAt(i))));
        }

        Map<String, Object> epochCompleteHeaders = new HashMap<>();
        epochCompleteHeaders.put(TYPE_HEADER_KEY, WebSocketResponseType.EPOCH_COMPLETE);

        CipherSolution cipherSolution = optimizer.optimize(cipher, request.getEpochs(), (i) ->
            template.convertAndSend("/topic/solutions", new EpochCompleteResponse(i, request.getEpochs()), epochCompleteHeaders)
        );

        Map<String, Object> solutionHeaders = new HashMap<>();
        solutionHeaders.put(TYPE_HEADER_KEY, WebSocketResponseType.SOLUTION);

        template.convertAndSend("/topic/solutions", new SolutionResponse(cipherSolution.asSingleLineString()), solutionHeaders);
    }
}
