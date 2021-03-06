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

import com.ciphertool.zenith.api.model.TransformerResponse;
import com.ciphertool.zenith.inference.entities.ZenithTransformer;
import com.ciphertool.zenith.inference.transformer.ciphertext.CipherTransformer;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/transformers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class TransformerService {
    @Autowired
    private List<CipherTransformer> cipherTransformers;

    @Autowired
    private List<PlaintextTransformer> plaintextTransformers;

    @GetMapping("/ciphertext")
    @ResponseBody
    @Cacheable("ciphertextTransformers")
    public TransformerResponse findCiphertextTransformers() {
        TransformerResponse transformerResponse = new TransformerResponse();

        for (CipherTransformer cipherTransformer : cipherTransformers) {
            ZenithTransformer responseItem = new ZenithTransformer();
            responseItem.setName(cipherTransformer.getClass().getSimpleName().replace(CipherTransformer.class.getSimpleName(), ""));
            responseItem.setDisplayName(cipherTransformer.getDisplayName());
            responseItem.setForm(cipherTransformer.getForm());
            responseItem.setOrder(cipherTransformer.getOrder());
            responseItem.setHelpText(cipherTransformer.getHelpText());

            transformerResponse.getTransformers().add(responseItem);
        }

        return transformerResponse;
    }

    @GetMapping("/plaintext")
    @ResponseBody
    @Cacheable("plaintextTransformers")
    public TransformerResponse findPlaintextTransformers() {
        TransformerResponse transformerResponse = new TransformerResponse();

        for (PlaintextTransformer plaintextTransformer : plaintextTransformers) {
            ZenithTransformer responseItem = new ZenithTransformer();
            responseItem.setName(plaintextTransformer.getClass().getSimpleName().replace(PlaintextTransformer.class.getSimpleName(), ""));
            responseItem.setDisplayName(plaintextTransformer.getDisplayName());
            responseItem.setForm(plaintextTransformer.getForm());
            responseItem.setOrder(plaintextTransformer.getOrder());
            responseItem.setHelpText(plaintextTransformer.getHelpText());

            transformerResponse.getTransformers().add(responseItem);
        }

        return transformerResponse;
    }
}
