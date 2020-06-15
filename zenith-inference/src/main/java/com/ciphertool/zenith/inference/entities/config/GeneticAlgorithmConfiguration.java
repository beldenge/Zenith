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

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

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
    private String crossoverAlgorithmName;

    @NotBlank
    private String mutationAlgorithmName;

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

    private boolean enableFitnessSharing;
}
