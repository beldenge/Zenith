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

package com.ciphertool.zenith.inference.entities.config;

import com.ciphertool.zenith.genetic.population.LatticePopulation;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.*;

@Getter
@Setter
public class GeneticAlgorithmConfiguration {
    @NotNull
    @Min(1)
    private Integer populationSize;

    @NotNull
    @Min(1)
    private Integer numberOfGenerations;

    private Integer elitism;

    @NotBlank
    private String populationName;

    @Min(1)
    private Integer latticeRows;

    @Min(1)
    private Integer latticeColumns;

    private Boolean latticeWrapAround;

    @Min(1)
    private Integer latticeRadius;

    @NotBlank
    private String breederName;

    @NotBlank
    private String crossoverOperatorName;

    @NotBlank
    private String mutationOperatorName;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double mutationRate;

    @Min(0)
    private Integer maxMutationsPerIndividual;

    @NotBlank
    private String selectorName;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double tournamentSelectorAccuracy;

    @Min(1)
    private Integer tournamentSize;

    @Min(0)
    private Integer minPopulations;

    @Min(0)
    private Integer speciationEvents;

    @Min(2)
    private Integer speciationFactor;

    @Min(0)
    private Integer extinctionCycles;

    @AssertTrue(message = "The population size for LatticePopulation must be equal to the product of its rows and columns.")
    public boolean isPopulationSizeEqualToLatticeDimensions() {
        if (!LatticePopulation.class.getSimpleName().equals(populationName)) {
            return true;
        }

        return populationSize == (latticeColumns * latticeRows);
    }

    @AssertTrue(message = "The elitism must be less than the populationSize.")
    public boolean isElitismLessThanPopulationSize() {
        return elitism < populationSize;
    }

    @AssertTrue(message = "The tournamentSize must be less than the populationSize.")
    public boolean isTournamentSizeLessThanPopulationSize() {
        return tournamentSize < populationSize;
    }
}
