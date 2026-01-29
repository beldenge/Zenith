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

package com.ciphertool.zenith.inference.entities.config;

import com.ciphertool.zenith.inference.entities.FormComponentDto;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigurationDtosTest {
    @Test
    public void testSelectOption() {
        SelectOption option = new SelectOption();
        option.setName("name");
        option.setDisplayName("display");

        assertEquals("name", option.getName());
        assertEquals("display", option.getDisplayName());
    }

    @Test
    public void testCipherConfiguration() {
        CipherConfiguration cipherConfiguration = new CipherConfiguration();
        FormComponentDto transformer = new FormComponentDto();
        transformer.setName("Transformer");

        cipherConfiguration.setCipherName("cipher");
        cipherConfiguration.setAppliedCiphertextTransformers(Collections.singletonList(transformer));
        cipherConfiguration.setAppliedPlaintextTransformers(Collections.singletonList(transformer));

        assertEquals("cipher", cipherConfiguration.getCipherName());
        assertEquals(1, cipherConfiguration.getAppliedCiphertextTransformers().size());
        assertEquals(1, cipherConfiguration.getAppliedPlaintextTransformers().size());
    }

    @Test
    public void testSimulatedAnnealingConfiguration() {
        SimulatedAnnealingConfiguration configuration = new SimulatedAnnealingConfiguration();
        configuration.setSamplerIterations(10);
        configuration.setAnnealingTemperatureMin(0.1f);
        configuration.setAnnealingTemperatureMax(1.0f);

        assertEquals(10, configuration.getSamplerIterations());
        assertEquals(0.1f, configuration.getAnnealingTemperatureMin());
        assertEquals(1.0f, configuration.getAnnealingTemperatureMax());
    }

    @Test
    public void testApplicationConfiguration() {
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        SelectOption optimizer = new SelectOption();
        optimizer.setName("optimizer");

        configuration.setSelectedCipher("cipher");
        configuration.setEpochs(5);
        configuration.setCipherConfigurations(Collections.emptyList());
        configuration.setSelectedOptimizer(optimizer);
        configuration.setSelectedFitnessFunction(new FormComponentDto());
        configuration.setSimulatedAnnealingConfiguration(new SimulatedAnnealingConfiguration());
        configuration.setGeneticAlgorithmConfiguration(new GeneticAlgorithmConfiguration());

        assertEquals("cipher", configuration.getSelectedCipher());
        assertEquals(5, configuration.getEpochs());
        assertEquals(Collections.emptyList(), configuration.getCipherConfigurations());
        assertEquals(optimizer, configuration.getSelectedOptimizer());
        assertNotNull(configuration.getSelectedFitnessFunction());
        assertNotNull(configuration.getSimulatedAnnealingConfiguration());
        assertNotNull(configuration.getGeneticAlgorithmConfiguration());
    }
}