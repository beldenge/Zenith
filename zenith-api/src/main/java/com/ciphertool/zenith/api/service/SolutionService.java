/**
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
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.inference.optimizer.SimulatedAnnealingSolutionOptimizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/solutions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class SolutionService {
    @Autowired
    private SimulatedAnnealingSolutionOptimizer optimizer;

    @PostMapping
    @ResponseBody
    public SolutionResponse solve(@Validated @RequestBody SolutionRequest request) {
        Cipher cipher = new Cipher(null, request.getRows(), request.getColumns());

        for (int i = 0; i < request.getCiphertext().length(); i ++) {
            cipher.addCiphertextCharacter(new Ciphertext(i, String.valueOf(request.getCiphertext().charAt(i))));
        }

        CipherSolution cipherSolution = optimizer.optimize(cipher, request.getEpochs());

        return new SolutionResponse(cipherSolution.asSingleLineString());
    }
}
