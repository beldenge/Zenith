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

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class SolutionRequestFitnessFunction {
    @NotBlank
    private String name;

    private Map<String, Object> data = new HashMap<>();
}
