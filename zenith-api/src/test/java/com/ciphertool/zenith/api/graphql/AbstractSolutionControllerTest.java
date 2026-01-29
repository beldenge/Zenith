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

import com.ciphertool.zenith.api.model.SolutionRequest;
import com.ciphertool.zenith.api.model.SolutionRequestFitnessFunction;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.config.GeneticAlgorithmConfiguration;
import com.ciphertool.zenith.inference.entities.config.SimulatedAnnealingConfiguration;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import com.ciphertool.zenith.inference.evaluator.model.SolutionScore;
import com.ciphertool.zenith.inference.optimizer.GeneticAlgorithmSolutionOptimizer;
import com.ciphertool.zenith.inference.optimizer.OnEpochComplete;
import com.ciphertool.zenith.inference.optimizer.SimulatedAnnealingSolutionOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractSolutionControllerTest {

    @Mock
    private SimulatedAnnealingSolutionOptimizer simulatedAnnealingOptimizer;

    @Mock
    private GeneticAlgorithmSolutionOptimizer geneticAlgorithmOptimizer;

    private TestSolutionController controller;
    private TestPlaintextEvaluator testEvaluator;

    @BeforeEach
    void setUp() {
        controller = new TestSolutionController();
        testEvaluator = new TestPlaintextEvaluator();

        ReflectionTestUtils.setField(controller, "simulatedAnnealingOptimizer", simulatedAnnealingOptimizer);
        ReflectionTestUtils.setField(controller, "geneticAlgorithmOptimizer", geneticAlgorithmOptimizer);
        ReflectionTestUtils.setField(controller, "geneticAlgorithmEnabled", true);
        ReflectionTestUtils.setField(controller, "maxEpochs", 100);
        ReflectionTestUtils.setField(controller, "simulatedAnnealingMaxIterations", 100000);
        ReflectionTestUtils.setField(controller, "plaintextEvaluators", Arrays.asList(testEvaluator));
    }

    @Test
    void doSolve_withSimulatedAnnealing_callsSimulatedAnnealingOptimizer() {
        SolutionRequest request = createBasicRequest();
        SimulatedAnnealingConfiguration saConfig = new SimulatedAnnealingConfiguration();
        saConfig.setSamplerIterations(1000);
        saConfig.setAnnealingTemperatureMin(1.0f);
        saConfig.setAnnealingTemperatureMax(100.0f);
        request.setSimulatedAnnealingConfiguration(saConfig);

        CipherSolution expectedSolution = createMockSolution();
        when(simulatedAnnealingOptimizer.optimize(any(), eq(1), any(), any(), any(), any()))
                .thenReturn(expectedSolution);

        CipherSolution result = controller.doSolve(request);

        assertSame(expectedSolution, result);
        verify(simulatedAnnealingOptimizer).optimize(any(), eq(1), any(), any(), any(), any());
        verifyNoInteractions(geneticAlgorithmOptimizer);
    }

    @Test
    void doSolve_withGeneticAlgorithm_callsGeneticAlgorithmOptimizer() {
        SolutionRequest request = createBasicRequest();
        GeneticAlgorithmConfiguration gaConfig = new GeneticAlgorithmConfiguration();
        gaConfig.setPopulationSize(100);
        gaConfig.setNumberOfGenerations(50);
        request.setGeneticAlgorithmConfiguration(gaConfig);

        CipherSolution expectedSolution = createMockSolution();
        when(geneticAlgorithmOptimizer.optimize(any(), eq(1), any(), any(), any(), any()))
                .thenReturn(expectedSolution);

        CipherSolution result = controller.doSolve(request);

        assertSame(expectedSolution, result);
        verify(geneticAlgorithmOptimizer).optimize(any(), eq(1), any(), any(), any(), any());
        verifyNoInteractions(simulatedAnnealingOptimizer);
    }

    @Test
    void doSolve_withGeneticAlgorithmDisabled_throwsException() {
        ReflectionTestUtils.setField(controller, "geneticAlgorithmEnabled", false);

        SolutionRequest request = createBasicRequest();
        request.setGeneticAlgorithmConfiguration(new GeneticAlgorithmConfiguration());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.doSolve(request));

        assertEquals("Genetic Algorithm Optimizer is currently disabled.", exception.getMessage());
    }

    @Test
    void doSolve_withEpochsExceedingMax_throwsException() {
        SolutionRequest request = createBasicRequest();
        request.setEpochs(150); // exceeds max of 100
        request.setSimulatedAnnealingConfiguration(new SimulatedAnnealingConfiguration());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.doSolve(request));

        assertTrue(exception.getMessage().contains("exceeds the maximum supported"));
    }

    @Test
    void doSolve_withSamplerIterationsExceedingMax_throwsException() {
        SolutionRequest request = createBasicRequest();
        SimulatedAnnealingConfiguration saConfig = new SimulatedAnnealingConfiguration();
        saConfig.setSamplerIterations(200000); // exceeds max of 100000
        request.setSimulatedAnnealingConfiguration(saConfig);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.doSolve(request));

        assertTrue(exception.getMessage().contains("sampler iterations"));
    }

    @Test
    void doSolve_withNoOptimizerConfigured_throwsException() {
        SolutionRequest request = createBasicRequest();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> controller.doSolve(request));

        assertTrue(exception.getMessage().contains("Neither simulated annealing nor genetic algorithm"));
    }

    @Test
    void doSolve_withNonExistentPlaintextEvaluator_throwsException() {
        SolutionRequest request = createBasicRequest();
        SolutionRequestFitnessFunction fitnessFunction = new SolutionRequestFitnessFunction();
        fitnessFunction.setName("NonExistent");
        request.setFitnessFunction(fitnessFunction);
        request.setSimulatedAnnealingConfiguration(new SimulatedAnnealingConfiguration());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.doSolve(request));

        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    void doSolve_withMaxEpochsZero_doesNotValidateEpochs() {
        ReflectionTestUtils.setField(controller, "maxEpochs", 0);

        SolutionRequest request = createBasicRequest();
        request.setEpochs(999);
        SimulatedAnnealingConfiguration saConfig = new SimulatedAnnealingConfiguration();
        saConfig.setSamplerIterations(1000);
        request.setSimulatedAnnealingConfiguration(saConfig);

        CipherSolution expectedSolution = createMockSolution();
        when(simulatedAnnealingOptimizer.optimize(any(), eq(999), any(), any(), any(), any()))
                .thenReturn(expectedSolution);

        CipherSolution result = controller.doSolve(request);

        assertSame(expectedSolution, result);
    }

    @Test
    void doSolve_withMaxIterationsZero_doesNotValidateIterations() {
        ReflectionTestUtils.setField(controller, "simulatedAnnealingMaxIterations", 0);

        SolutionRequest request = createBasicRequest();
        SimulatedAnnealingConfiguration saConfig = new SimulatedAnnealingConfiguration();
        saConfig.setSamplerIterations(999999);
        request.setSimulatedAnnealingConfiguration(saConfig);

        CipherSolution expectedSolution = createMockSolution();
        when(simulatedAnnealingOptimizer.optimize(any(), eq(1), any(), any(), any(), any()))
                .thenReturn(expectedSolution);

        CipherSolution result = controller.doSolve(request);

        assertSame(expectedSolution, result);
    }

    private SolutionRequest createBasicRequest() {
        SolutionRequest request = new SolutionRequest();
        request.setRows(2);
        request.setColumns(3);
        request.setCiphertext(Arrays.asList("A", "B", "C", "D", "E", "F"));
        request.setEpochs(1);

        SolutionRequestFitnessFunction fitnessFunction = new SolutionRequestFitnessFunction();
        fitnessFunction.setName("Test"); // Matches TestPlaintextEvaluator
        request.setFitnessFunction(fitnessFunction);

        return request;
    }

    private CipherSolution createMockSolution() {
        Cipher cipher = new Cipher("Test", 2, 3);
        return new CipherSolution(cipher, 6);
    }

    // Test implementation of AbstractSolutionController
    private static class TestSolutionController extends AbstractSolutionController {
        @Override
        public OnEpochComplete getCallback(SolutionRequest request) {
            return (epoch, solution) -> {};
        }
    }

    // Test evaluator class - class name is TestPlaintextEvaluator so "Test" should match
    public static class TestPlaintextEvaluator implements PlaintextEvaluator {
        @Override
        public PlaintextEvaluator getInstance(Map<String, Object> data) {
            return this;
        }

        @Override
        public SolutionScore evaluate(Map<String, Object> precomputedData, Cipher cipher, CipherSolution solution, String solutionString, String ciphertextKey) {
            return new SolutionScore(null, new MaximizingFitness[]{new MaximizingFitness(1.0)});
        }

        @Override
        public Map<String, Object> getPrecomputedCounterweightData(Cipher cipher) {
            return null;
        }

        @Override
        public FormlyForm getForm() {
            return null;
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public String getHelpText() {
            return null;
        }
    }
}
