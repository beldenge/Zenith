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

import com.ciphertool.zenith.api.model.SamplePlaintextTransformationRequest;
import com.ciphertool.zenith.api.model.SolutionRequestTransformer;
import com.ciphertool.zenith.api.model.SolutionResponse;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/plaintext-samples", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class PlaintextSampleService {
    @Autowired
    private PlaintextTransformationManager plaintextTransformationManager;

    @PostMapping
    @ResponseBody
    public SolutionResponse transformCipher(@Validated @RequestBody SamplePlaintextTransformationRequest transformationRequest) {
        List<PlaintextTransformationStep> steps = transformationRequest.getPlaintextTransformers().stream()
                .map(SolutionRequestTransformer::asStep)
                .collect(Collectors.toList());

        String transformed = plaintextTransformationManager.transform(transformationRequest.getPlaintext(), steps);

        return new SolutionResponse(transformed, 0d);
    }
}
