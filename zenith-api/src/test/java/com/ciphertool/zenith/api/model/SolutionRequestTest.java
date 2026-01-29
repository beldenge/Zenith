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

package com.ciphertool.zenith.api.model;

import com.ciphertool.zenith.inference.entities.config.GeneticAlgorithmConfiguration;
import com.ciphertool.zenith.inference.entities.config.SimulatedAnnealingConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SolutionRequestTest {

    @Test
    void isLengthValid_whenLengthMatchesRowsTimesColumns_returnsTrue() {
        SolutionRequest request = new SolutionRequest();
        request.setRows(2);
        request.setColumns(3);
        request.setCiphertext(Arrays.asList("A", "B", "C", "D", "E", "F"));

        assertTrue(request.isLengthValid());
    }

    @Test
    void isLengthValid_whenLengthDoesNotMatch_returnsFalse() {
        SolutionRequest request = new SolutionRequest();
        request.setRows(2);
        request.setColumns(3);
        request.setCiphertext(Arrays.asList("A", "B", "C", "D", "E"));

        assertFalse(request.isLengthValid());
    }

    @Test
    void isAnyOptimizerConfigured_whenSimulatedAnnealingConfigured_returnsTrue() {
        SolutionRequest request = new SolutionRequest();
        request.setSimulatedAnnealingConfiguration(new SimulatedAnnealingConfiguration());

        assertTrue(request.isAnyOptimizerConfigured());
    }

    @Test
    void isAnyOptimizerConfigured_whenGeneticAlgorithmConfigured_returnsTrue() {
        SolutionRequest request = new SolutionRequest();
        request.setGeneticAlgorithmConfiguration(new GeneticAlgorithmConfiguration());

        assertTrue(request.isAnyOptimizerConfigured());
    }

    @Test
    void isAnyOptimizerConfigured_whenNeitherConfigured_returnsFalse() {
        SolutionRequest request = new SolutionRequest();

        assertFalse(request.isAnyOptimizerConfigured());
    }

    @Test
    void isOnlyOneOptimizerConfigured_whenOnlySimulatedAnnealing_returnsTrue() {
        SolutionRequest request = new SolutionRequest();
        request.setSimulatedAnnealingConfiguration(new SimulatedAnnealingConfiguration());

        assertTrue(request.isOnlyOneOptimizerConfigured());
    }

    @Test
    void isOnlyOneOptimizerConfigured_whenOnlyGeneticAlgorithm_returnsTrue() {
        SolutionRequest request = new SolutionRequest();
        request.setGeneticAlgorithmConfiguration(new GeneticAlgorithmConfiguration());

        assertTrue(request.isOnlyOneOptimizerConfigured());
    }

    @Test
    void isOnlyOneOptimizerConfigured_whenBothConfigured_returnsFalse() {
        SolutionRequest request = new SolutionRequest();
        request.setSimulatedAnnealingConfiguration(new SimulatedAnnealingConfiguration());
        request.setGeneticAlgorithmConfiguration(new GeneticAlgorithmConfiguration());

        assertFalse(request.isOnlyOneOptimizerConfigured());
    }

    @Test
    void isOnlyOneOptimizerConfigured_whenNeitherConfigured_returnsFalse() {
        SolutionRequest request = new SolutionRequest();

        assertFalse(request.isOnlyOneOptimizerConfigured());
    }
}
