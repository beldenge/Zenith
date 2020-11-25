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

import com.ciphertool.zenith.api.model.SolutionRequest;
import com.ciphertool.zenith.api.model.SolutionResponse;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.optimizer.OnEpochComplete;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/solutions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class SolutionService extends AbstractSolutionService {
    @Override
    public OnEpochComplete getCallback(SolutionRequest request) {
        return null;
    }

    @GetMapping
    @ResponseBody
    public SolutionResponse solve(@Validated @RequestBody SolutionRequest request) {
        CipherSolution cipherSolution = doSolve(request);

        float[] scores = new float[cipherSolution.getScores().length];

        for (int i = 0; i < cipherSolution.getScores().length; i ++) {
            scores[i] = (float) cipherSolution.getScores()[i].getValue();
        }

        // TODO: need to update the Angular client to be able to handle the array of scores
        return new SolutionResponse(cipherSolution.asSingleLineString(), scores);
    }
}
