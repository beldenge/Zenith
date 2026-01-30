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

import com.ciphertool.zenith.genetic.population.LatticePopulation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneticAlgorithmConfigurationTest {
    @Test
    public void given_validInput_when_populationSizeEqualToLatticeDimensions_then_matchesExpectations() {
        GeneticAlgorithmConfiguration configuration = new GeneticAlgorithmConfiguration();
        configuration.setPopulationName(LatticePopulation.class.getSimpleName());
        configuration.setPopulationSize(6);
        configuration.setLatticeRows(2);
        configuration.setLatticeColumns(3);

        assertTrue(configuration.isPopulationSizeEqualToLatticeDimensions());

        configuration.setLatticeColumns(2);
        assertFalse(configuration.isPopulationSizeEqualToLatticeDimensions());
    }

    @Test
    public void given_missingInput_when_populationSizeEqualToLatticeDimensionsMissingValues_then_returnsFalse() {
        GeneticAlgorithmConfiguration configuration = new GeneticAlgorithmConfiguration();
        configuration.setPopulationName(LatticePopulation.class.getSimpleName());
        configuration.setPopulationSize(6);

        assertFalse(configuration.isPopulationSizeEqualToLatticeDimensions());
    }

    @Test
    public void given_validInput_when_populationSizeEqualToLatticeDimensionsNonLattice_then_returnsTrue() {
        GeneticAlgorithmConfiguration configuration = new GeneticAlgorithmConfiguration();
        configuration.setPopulationName("StandardPopulation");

        assertTrue(configuration.isPopulationSizeEqualToLatticeDimensions());
    }

    @Test
    public void given_validInput_when_elitismLessThanPopulationSize_then_matchesExpectations() {
        GeneticAlgorithmConfiguration configuration = new GeneticAlgorithmConfiguration();
        configuration.setPopulationSize(10);

        assertTrue(configuration.isElitismLessThanPopulationSize());

        configuration.setElitism(10);
        assertFalse(configuration.isElitismLessThanPopulationSize());

        configuration.setElitism(9);
        assertTrue(configuration.isElitismLessThanPopulationSize());
    }

    @Test
    public void given_validInput_when_tournamentSizeLessThanPopulationSize_then_matchesExpectations() {
        GeneticAlgorithmConfiguration configuration = new GeneticAlgorithmConfiguration();
        configuration.setPopulationSize(10);

        assertTrue(configuration.isTournamentSizeLessThanPopulationSize());

        configuration.setTournamentSize(10);
        assertFalse(configuration.isTournamentSizeLessThanPopulationSize());

        configuration.setTournamentSize(3);
        assertTrue(configuration.isTournamentSizeLessThanPopulationSize());
    }
}
