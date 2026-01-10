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

package com.ciphertool.zenith.api.graphql;

import com.ciphertool.zenith.api.model.*;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.optimizer.OnEpochComplete;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Controller
public class SolutionController extends AbstractSolutionController {
    private static final Logger log = LoggerFactory.getLogger(SolutionController.class);
    private static final Map<String, Sinks.Many<SolutionUpdate>> REQUEST_SINKS = new ConcurrentHashMap<>();

    @Override
    public OnEpochComplete getCallback(SolutionRequest request) {
        return (i) -> {
            EpochCompleteResponse epochResponse = new EpochCompleteResponse(i, request.getEpochs());
            SolutionUpdate update = new SolutionUpdate();
            update.setRequestId(request.getRequestId());
            update.setType(WebSocketResponseType.EPOCH_COMPLETE);
            update.setEpochData(epochResponse);
            REQUEST_SINKS.computeIfAbsent(request.getRequestId(), k -> Sinks.many().multicast().onBackpressureBuffer()).tryEmitNext(update);
        };
    }

    @MutationMapping
    public CompletableFuture<String> solveSolution(@Argument @Valid SolutionRequest input) {
        if (StringUtils.isBlank(input.getRequestId())) {
            input.setRequestId(UUID.randomUUID().toString());
        }

        CompletableFuture.runAsync(() -> {
            CipherSolution cipherSolution;
            try {
                cipherSolution = doSolve(input);
            } catch (Exception e) {
                SolutionUpdate errorUpdate = new SolutionUpdate();
                errorUpdate.setRequestId(input.getRequestId());
                errorUpdate.setType(WebSocketResponseType.ERROR);
                REQUEST_SINKS.computeIfAbsent(input.getRequestId(), k -> Sinks.many().multicast().onBackpressureBuffer()).tryEmitNext(errorUpdate);
                return;
            }

            float[] scores = new float[cipherSolution.getScores().length];
            for (int i = 0; i < cipherSolution.getScores().length; i++) {
                scores[i] = (float) cipherSolution.getScores()[i].getValue();
            }

            SolutionResponse solutionResp = new SolutionResponse(cipherSolution.asSingleLineString(), scores);
            SolutionUpdate update = new SolutionUpdate();
            update.setRequestId(input.getRequestId());
            update.setType(WebSocketResponseType.SOLUTION);
            update.setSolutionData(solutionResp);
            REQUEST_SINKS.computeIfAbsent(input.getRequestId(), k -> Sinks.many().multicast().onBackpressureBuffer()).tryEmitNext(update);
        });
        return CompletableFuture.completedFuture(input.getRequestId());
    }

    @SubscriptionMapping
    public Flux<SolutionUpdate> solutionUpdates(@Argument String requestId) {
        log.info("Subscription requested for requestId: {}", requestId);
        Sinks.Many<SolutionUpdate> sink = REQUEST_SINKS.computeIfAbsent(requestId, k -> Sinks.many().multicast().onBackpressureBuffer());
        return sink.asFlux()
            .doOnSubscribe(s -> log.info("Subscriber attached for requestId: {}", requestId))
            .doOnNext(update -> log.info("Delivering {} to requestId: {}", update.getType(), requestId))
            .doOnError(e -> {
                log.error("Error for requestId {}: {}", requestId, e.getMessage());
                REQUEST_SINKS.remove(requestId);
            })
            .doOnComplete(() -> REQUEST_SINKS.remove(requestId))
            .doOnCancel(() -> REQUEST_SINKS.remove(requestId));
    }
}
