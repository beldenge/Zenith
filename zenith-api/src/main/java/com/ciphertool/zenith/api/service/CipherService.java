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

import com.ciphertool.zenith.api.model.CipherResponse;
import com.ciphertool.zenith.api.model.CiphertextTransformationRequest;
import com.ciphertool.zenith.api.model.CiphertextTransformationRequestStep;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherJson;
import com.ciphertool.zenith.inference.transformer.ciphertext.CiphertextTransformationManager;
import com.ciphertool.zenith.inference.transformer.ciphertext.CiphertextTransformationStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/ciphers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class CipherService {
    @Autowired
    private CiphertextTransformationManager ciphertextTransformationManager;

    @PostMapping
    @ResponseBody
    public CipherResponse transformCipher(@Validated @RequestBody CiphertextTransformationRequest transformationRequest) {
        CipherResponse cipherResponse = new CipherResponse();

        Cipher cipher = transformationRequest.getCipher().asCipher();

        List<CiphertextTransformationStep> steps = transformationRequest.getSteps().stream()
                .map(CiphertextTransformationRequestStep::asStep)
                .collect(Collectors.toList());

        cipher = ciphertextTransformationManager.transform(cipher, steps);

        CipherJson cipherResponseItem = new CipherJson(cipher.getName(), cipher.getRows(), cipher.getColumns(), cipher.asSingleLineString(), cipher.isReadOnly());

        cipherResponse.getCiphers().add(cipherResponseItem);

        return cipherResponse;
    }
}
