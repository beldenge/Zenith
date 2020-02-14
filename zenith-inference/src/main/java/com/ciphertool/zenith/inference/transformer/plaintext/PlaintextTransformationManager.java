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

package com.ciphertool.zenith.inference.transformer.plaintext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlaintextTransformationManager {
    private Logger log = LoggerFactory.getLogger(getClass());

    private List<String> existentPlaintextTransformers;

    @Autowired
    private List<PlaintextTransformer> plaintextTransformers;

    @PostConstruct
    public void init() {
        existentPlaintextTransformers = plaintextTransformers.stream()
                .map(transformer -> transformer.getClass().getSimpleName().replace(PlaintextTransformer.class.getSimpleName(), ""))
                .collect(Collectors.toList());
    }

    public String transform(String plaintext, List<PlaintextTransformationStep> steps) {
        List<PlaintextTransformer> toUse = new ArrayList<>(steps.size());

        for (PlaintextTransformationStep step : steps) {
            String transformerName = step.getTransformerName();

            if (!existentPlaintextTransformers.contains(transformerName)) {
                log.error("The PlaintextTransformer with name {} does not exist.  Please use a name from the following: {}", transformerName, existentPlaintextTransformers);
                throw new IllegalArgumentException("The PlaintextTransformer with name " + transformerName + " does not exist.");
            }

            for (PlaintextTransformer plaintextTransformer : plaintextTransformers) {
                if (plaintextTransformer.getClass().getSimpleName().replace(PlaintextTransformer.class.getSimpleName(), "").equals(transformerName)) {
                    toUse.add(plaintextTransformer.getInstance(step.getData()));

                    break;
                }
            }
        }

        for (PlaintextTransformer plaintextTransformer : toUse) {
            plaintext = plaintextTransformer.transform(plaintext);
        }

        return plaintext;
    }
}
