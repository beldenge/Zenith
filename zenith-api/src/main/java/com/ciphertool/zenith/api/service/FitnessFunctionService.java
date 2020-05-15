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

package com.ciphertool.zenith.api.service;

import com.ciphertool.zenith.api.model.FitnessFunctionResponse;
import com.ciphertool.zenith.inference.entities.ZenithFitnessFunction;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/fitness-functions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class FitnessFunctionService {
    @Autowired
    private List<PlaintextEvaluator> plaintextEvaluators;

    @GetMapping
    @ResponseBody
    @Cacheable("fitnessFunctions")
    public FitnessFunctionResponse findFitnessFunctions() {
        FitnessFunctionResponse fitnessFunctionResponse = new FitnessFunctionResponse();

        for (PlaintextEvaluator plaintextEvaluator : plaintextEvaluators) {
            ZenithFitnessFunction responseItem = new ZenithFitnessFunction();
            responseItem.setName(plaintextEvaluator.getClass().getSimpleName().replace(PlaintextEvaluator.class.getSimpleName(), ""));
            responseItem.setDisplayName(plaintextEvaluator.getDisplayName());
            responseItem.setForm(plaintextEvaluator.getForm());
            responseItem.setOrder(plaintextEvaluator.getOrder());
            responseItem.setHelpText(plaintextEvaluator.getHelpText());

            fitnessFunctionResponse.getFitnessFunctions().add(responseItem);
        }

        return fitnessFunctionResponse;
    }
}
