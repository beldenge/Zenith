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

package com.ciphertool.zenith.api.model;

import com.ciphertool.zenith.inference.entities.config.GeneticAlgorithmConfiguration;
import com.ciphertool.zenith.inference.entities.config.SimulatedAnnealingConfiguration;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SolutionRequest {
    @NotNull
    @Min(0)
    private int rows;

    @NotNull
    @Min(0)
    private int columns;

    @NotEmpty
    private List<String> ciphertext = new ArrayList<>();

    @Min(1)
    private int epochs = 1;

    @Valid
    private List<SolutionRequestTransformer> plaintextTransformers;

    @Valid
    private SolutionRequestFitnessFunction fitnessFunction;

    @Valid
    private SimulatedAnnealingConfiguration simulatedAnnealingConfiguration;

    @Valid
    private GeneticAlgorithmConfiguration geneticAlgorithmConfiguration;

    @AssertTrue(message = "The ciphertext length must match the product of rows and columns.")
    public boolean isLengthValid() {
        return (rows * columns) == ciphertext.size();
    }

    @AssertTrue(message = "One and only one of simulatedAnnealingConfiguration or geneticAlgorithmConfiguration must be set")
    public boolean isAnyOptimizerConfigured() {
        return simulatedAnnealingConfiguration != null || geneticAlgorithmConfiguration != null;
    }

    @AssertTrue(message = "One and only one of simulatedAnnealingConfiguration or geneticAlgorithmConfiguration must be set")
    public boolean isOnlyOneOptimizerConfigured() {
        return (simulatedAnnealingConfiguration != null && geneticAlgorithmConfiguration == null) || (simulatedAnnealingConfiguration == null && geneticAlgorithmConfiguration != null);
    }
}
