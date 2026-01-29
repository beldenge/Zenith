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

package com.ciphertool.zenith.inference.configuration;

import com.ciphertool.zenith.inference.entities.FormComponentDto;
import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.config.ApplicationConfiguration;
import com.ciphertool.zenith.inference.entities.config.GeneticAlgorithmConfiguration;
import com.ciphertool.zenith.inference.entities.config.SelectOption;
import com.ciphertool.zenith.inference.entities.config.SimulatedAnnealingConfiguration;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.model.SolutionScore;
import com.ciphertool.zenith.inference.optimizer.OnEpochComplete;
import com.ciphertool.zenith.inference.optimizer.SolutionOptimizer;
import com.ciphertool.zenith.inference.transformer.ciphertext.TransformationStep;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigurationResolverTest {
    @Test
    public void testResolveConfigurationCopiesValues() {
        SimulatedAnnealingConfiguration simulatedAnnealingConfiguration = new SimulatedAnnealingConfiguration();
        simulatedAnnealingConfiguration.setSamplerIterations(5);
        simulatedAnnealingConfiguration.setAnnealingTemperatureMin(0.1f);
        simulatedAnnealingConfiguration.setAnnealingTemperatureMax(1.5f);

        GeneticAlgorithmConfiguration geneticAlgorithmConfiguration = new GeneticAlgorithmConfiguration();
        geneticAlgorithmConfiguration.setPopulationSize(100);
        geneticAlgorithmConfiguration.setNumberOfGenerations(50);
        geneticAlgorithmConfiguration.setElitism(3);
        geneticAlgorithmConfiguration.setPopulationName("StandardPopulation");
        geneticAlgorithmConfiguration.setLatticeRows(10);
        geneticAlgorithmConfiguration.setLatticeColumns(10);
        geneticAlgorithmConfiguration.setLatticeWrapAround(true);
        geneticAlgorithmConfiguration.setLatticeRadius(2);
        geneticAlgorithmConfiguration.setBreederName("Breeder");
        geneticAlgorithmConfiguration.setCrossoverOperatorName("Crossover");
        geneticAlgorithmConfiguration.setMutationOperatorName("Mutation");
        geneticAlgorithmConfiguration.setMutationRate(0.2d);
        geneticAlgorithmConfiguration.setMaxMutationsPerIndividual(4);
        geneticAlgorithmConfiguration.setSelectorName("Selector");
        geneticAlgorithmConfiguration.setTournamentSelectorAccuracy(0.7d);
        geneticAlgorithmConfiguration.setTournamentSize(5);
        geneticAlgorithmConfiguration.setMinPopulations(1);
        geneticAlgorithmConfiguration.setSpeciationEvents(2);
        geneticAlgorithmConfiguration.setSpeciationFactor(3);
        geneticAlgorithmConfiguration.setExtinctionCycles(4);

        ApplicationConfiguration configuration = new ApplicationConfiguration();
        configuration.setSimulatedAnnealingConfiguration(simulatedAnnealingConfiguration);
        configuration.setGeneticAlgorithmConfiguration(geneticAlgorithmConfiguration);

        Map<String, Object> resolved = ConfigurationResolver.resolveConfiguration(configuration);

        assertEquals(5, resolved.get("samplerIterations"));
        assertEquals(0.1f, resolved.get("annealingTemperatureMin"));
        assertEquals(1.5f, resolved.get("annealingTemperatureMax"));
        assertEquals(100, resolved.get("populationSize"));
        assertEquals(50, resolved.get("numberOfGenerations"));
        assertEquals("Selector", resolved.get("selectorName"));
        assertEquals(3, resolved.get("elitism"));
        assertEquals(0.2d, resolved.get("mutationRate"));
        assertEquals(4, resolved.get("extinctionCycles"));
    }

    @Test
    public void testResolveSolutionOptimizerMatchesByName() {
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        SelectOption selected = new SelectOption();
        selected.setName("Stub");
        configuration.setSelectedOptimizer(selected);

        StubSolutionOptimizer optimizer = new StubSolutionOptimizer();

        SolutionOptimizer resolved = ConfigurationResolver.resolveSolutionOptimizer(configuration, List.of(optimizer));

        assertSame(optimizer, resolved);
    }

    @Test
    public void testResolveSolutionOptimizerUnknownThrows() {
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        SelectOption selected = new SelectOption();
        selected.setName("Missing");
        configuration.setSelectedOptimizer(selected);

        assertThrows(IllegalArgumentException.class, () -> ConfigurationResolver.resolveSolutionOptimizer(configuration, List.of(new StubSolutionOptimizer())));
    }

    @Test
    public void testResolvePlaintextEvaluatorUsesFormModel() {
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        FormComponentDto selected = new FormComponentDto();
        selected.setName("Stub");
        FormlyForm form = new FormlyForm();
        form.getModel().put("key", "value");
        selected.setForm(form);
        configuration.setSelectedFitnessFunction(selected);

        StubPlaintextEvaluator evaluator = new StubPlaintextEvaluator();

        PlaintextEvaluator resolved = ConfigurationResolver.resolvePlaintextEvaluator(configuration, List.of(evaluator));

        StubPlaintextEvaluator resolvedStub = (StubPlaintextEvaluator) resolved;
        assertEquals("value", resolvedStub.getData().get("key"));
    }

    @Test
    public void testResolvePlaintextEvaluatorHandlesNullForm() {
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        FormComponentDto selected = new FormComponentDto();
        selected.setName("Stub");
        configuration.setSelectedFitnessFunction(selected);

        StubPlaintextEvaluator evaluator = new StubPlaintextEvaluator();

        PlaintextEvaluator resolved = ConfigurationResolver.resolvePlaintextEvaluator(configuration, List.of(evaluator));

        StubPlaintextEvaluator resolvedStub = (StubPlaintextEvaluator) resolved;
        assertNull(resolvedStub.getData());
    }

    private static final class StubSolutionOptimizer implements SolutionOptimizer {
        @Override
        public com.ciphertool.zenith.inference.entities.CipherSolution optimize(com.ciphertool.zenith.inference.entities.Cipher cipher,
                                                                               int epochs,
                                                                               Map<String, Object> configuration,
                                                                               List<TransformationStep> plaintextTransformationSteps,
                                                                               PlaintextEvaluator plaintextEvaluator,
                                                                               OnEpochComplete onEpochComplete) {
            return null;
        }
    }

    private static final class StubPlaintextEvaluator implements PlaintextEvaluator {
        private final Map<String, Object> data;

        private StubPlaintextEvaluator() {
            this.data = null;
        }

        private StubPlaintextEvaluator(Map<String, Object> data) {
            this.data = data;
        }

        @Override
        public SolutionScore evaluate(Map<String, Object> precomputedData,
                                      com.ciphertool.zenith.inference.entities.Cipher cipher,
                                      com.ciphertool.zenith.inference.entities.CipherSolution solution,
                                      String solutionString,
                                      String ciphertextKey) {
            return null;
        }

        @Override
        public Map<String, Object> getPrecomputedCounterweightData(com.ciphertool.zenith.inference.entities.Cipher cipher) {
            return new HashMap<>();
        }

        @Override
        public PlaintextEvaluator getInstance(Map<String, Object> data) {
            return new StubPlaintextEvaluator(data);
        }

        @Override
        public FormlyForm getForm() {
            return new FormlyForm();
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public String getHelpText() {
            return "test";
        }

        private Map<String, Object> getData() {
            return data;
        }
    }
}
