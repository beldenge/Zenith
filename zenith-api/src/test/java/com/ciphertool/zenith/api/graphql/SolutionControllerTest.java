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
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.inference.entities.config.SimulatedAnnealingConfiguration;
import com.ciphertool.zenith.inference.optimizer.OnEpochComplete;
import com.ciphertool.zenith.inference.optimizer.SimulatedAnnealingSolutionOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SolutionControllerTest {

    @Mock
    private SimulatedAnnealingSolutionOptimizer simulatedAnnealingOptimizer;

    private SolutionController controller;

    @BeforeEach
    void setUp() {
        controller = new SolutionController();
        AbstractSolutionControllerTest.TestPlaintextEvaluator testEvaluator =
                new AbstractSolutionControllerTest.TestPlaintextEvaluator();

        ReflectionTestUtils.setField(controller, "simulatedAnnealingOptimizer", simulatedAnnealingOptimizer);
        ReflectionTestUtils.setField(controller, "geneticAlgorithmEnabled", true);
        ReflectionTestUtils.setField(controller, "maxEpochs", 100);
        ReflectionTestUtils.setField(controller, "simulatedAnnealingMaxIterations", 100000);
        ReflectionTestUtils.setField(controller, "plaintextEvaluators", Arrays.asList(testEvaluator));
    }

    @Test
    void solveSolution_withBlankRequestId_generatesUuid() throws Exception {
        SolutionRequest request = createBasicRequest();
        request.setRequestId("");

        // solveSolution returns immediately with the requestId, the actual solve happens async
        CompletableFuture<String> result = controller.solveSolution(request);
        String requestId = result.get(5, TimeUnit.SECONDS);

        assertNotNull(requestId);
        assertFalse(requestId.isEmpty());
        // Verify it's a valid UUID format
        assertDoesNotThrow(() -> java.util.UUID.fromString(requestId));
    }

    @Test
    void solveSolution_withNullRequestId_generatesUuid() throws Exception {
        SolutionRequest request = createBasicRequest();
        request.setRequestId(null);

        CompletableFuture<String> result = controller.solveSolution(request);
        String requestId = result.get(5, TimeUnit.SECONDS);

        assertNotNull(requestId);
        assertFalse(requestId.isEmpty());
        assertDoesNotThrow(() -> java.util.UUID.fromString(requestId));
    }

    @Test
    void solveSolution_withProvidedRequestId_usesProvidedId() throws Exception {
        SolutionRequest request = createBasicRequest();
        request.setRequestId("my-custom-id");

        CompletableFuture<String> result = controller.solveSolution(request);

        assertEquals("my-custom-id", result.get(5, TimeUnit.SECONDS));
    }

    @Test
    void solutionUpdates_returnsFlux() {
        String requestId = "test-request-id";

        Flux<SolutionUpdate> flux = controller.solutionUpdates(requestId);

        assertNotNull(flux);
    }

    @Test
    void getCallback_returnsCallbackThatEmitsEpochComplete() {
        SolutionRequest request = createBasicRequest();
        request.setRequestId("callback-test-id");
        request.setEpochs(5);

        // Subscribe first so there's a listener
        Flux<SolutionUpdate> flux = controller.solutionUpdates("callback-test-id");

        OnEpochComplete callback = controller.getCallback(request);

        CipherSolution solution = createMockSolution();

        StepVerifier.create(flux.take(1))
                .then(() -> callback.fire(1, solution))
                .assertNext(update -> {
                    assertEquals("callback-test-id", update.getRequestId());
                    assertEquals(WebSocketResponseType.EPOCH_COMPLETE, update.getType());
                    assertEquals(1, update.getEpochData().getEpochsCompleted());
                    assertEquals(5, update.getEpochData().getEpochsTotal());
                    assertNotNull(update.getSolutionData());
                })
                .verifyComplete();
    }

    @Test
    void getCallback_withNullSolution_doesNotIncludeSolutionData() {
        SolutionRequest request = createBasicRequest();
        request.setRequestId("null-solution-test");
        request.setEpochs(3);

        Flux<SolutionUpdate> flux = controller.solutionUpdates("null-solution-test");

        OnEpochComplete callback = controller.getCallback(request);

        StepVerifier.create(flux.take(1))
                .then(() -> callback.fire(1, null))
                .assertNext(update -> {
                    assertEquals(WebSocketResponseType.EPOCH_COMPLETE, update.getType());
                    assertNull(update.getSolutionData());
                })
                .verifyComplete();
    }

    private SolutionRequest createBasicRequest() {
        SolutionRequest request = new SolutionRequest();
        request.setRows(2);
        request.setColumns(3);
        request.setCiphertext(Arrays.asList("A", "B", "C", "D", "E", "F"));
        request.setEpochs(1);

        SimulatedAnnealingConfiguration saConfig = new SimulatedAnnealingConfiguration();
        saConfig.setSamplerIterations(1000);
        request.setSimulatedAnnealingConfiguration(saConfig);

        SolutionRequestFitnessFunction fitnessFunction = new SolutionRequestFitnessFunction();
        fitnessFunction.setName("Test");
        request.setFitnessFunction(fitnessFunction);

        return request;
    }

    private CipherSolution createMockSolution() {
        Cipher cipher = new Cipher("Test", 2, 3);
        cipher.addCiphertextCharacter(new Ciphertext("A"));
        cipher.addCiphertextCharacter(new Ciphertext("B"));
        cipher.addCiphertextCharacter(new Ciphertext("C"));
        cipher.addCiphertextCharacter(new Ciphertext("D"));
        cipher.addCiphertextCharacter(new Ciphertext("E"));
        cipher.addCiphertextCharacter(new Ciphertext("F"));

        CipherSolution solution = new CipherSolution(cipher, 6);
        solution.putMapping("A", 'H');
        solution.putMapping("B", 'E');
        solution.putMapping("C", 'L');
        solution.putMapping("D", 'L');
        solution.putMapping("E", 'O');
        solution.putMapping("F", '!');
        solution.setScores(new MaximizingFitness[]{new MaximizingFitness(1.0)});
        return solution;
    }
}
