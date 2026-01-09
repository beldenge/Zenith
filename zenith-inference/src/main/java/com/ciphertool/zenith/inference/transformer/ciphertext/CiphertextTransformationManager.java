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

package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.transformer.FormComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CiphertextTransformationManager {
    private Logger log = LoggerFactory.getLogger(getClass());

    private List<String> existentCipherTransformers;

    @Autowired
    private List<CipherTransformer> cipherTransformers;

    @PostConstruct
    public void init() {
        existentCipherTransformers = cipherTransformers.stream()
                .map(FormComponent::getName)
                .collect(Collectors.toList());
    }

    public Cipher transform(Cipher cipher, List<TransformationStep> steps) {
        List<CipherTransformer> toUse = new ArrayList<>(steps.size());

        for (TransformationStep step : steps) {
            String transformerName = step.getTransformerName();

            if (!existentCipherTransformers.contains(transformerName)) {
                log.error("The CipherTransformer with name {} does not exist.  Please use a name from the following: {}", transformerName, existentCipherTransformers);
                throw new IllegalArgumentException("The CipherTransformer with name " + transformerName + " does not exist.");
            }

            for (CipherTransformer cipherTransformer : cipherTransformers) {
                if (cipherTransformer.getName().equals(transformerName)) {
                    toUse.add(cipherTransformer.getInstance(step.getData()));

                    break;
                }
            }
        }

        for (CipherTransformer cipherTransformer : toUse) {
            cipher = cipherTransformer.transform(cipher);
        }

        return cipher;
    }
}
