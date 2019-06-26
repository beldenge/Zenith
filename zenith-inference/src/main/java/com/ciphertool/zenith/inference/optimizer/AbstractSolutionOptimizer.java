/**
 * Copyright 2017-2019 George Belden
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

package com.ciphertool.zenith.inference.optimizer;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.known.KnownPlaintextEvaluator;
import com.ciphertool.zenith.inference.model.ModelUnzipper;
import com.ciphertool.zenith.inference.transformer.CipherTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractSolutionOptimizer {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${language-model.filename}")
    private String modelFilename;

    @Value("${decipherment.transformers.list}")
    private List<String> transformersToUse;

    @Value("${decipherment.evaluator.plaintext}")
    private String plaintextEvaluatorName;

    @Value("${decipherment.evaluator.known-plaintext:#{null}}")
    private String knownPlaintextEvaluatorName;

    @Value("${decipherment.use-known-evaluator:false}")
    protected boolean useKnownEvaluator;

    @Autowired
    protected Cipher cipher;

    @Autowired
    private ModelUnzipper modelUnzipper;

    @Autowired
    protected List<CipherTransformer> cipherTransformers;

    @Autowired
    private List<PlaintextEvaluator> plaintextEvaluators;

    @Autowired
    private List<KnownPlaintextEvaluator> knownPlaintextEvaluators;

    protected PlaintextEvaluator plaintextEvaluator;

    protected KnownPlaintextEvaluator knownPlaintextEvaluator;

    @PostConstruct
    public void init() {
        if (!Files.exists(Paths.get(modelFilename))) {
            long start = System.currentTimeMillis();
            log.info("Language model file {} not found.  Unzipping from archive.", modelFilename);

            modelUnzipper.unzip();

            log.info("Finished unzipping language model archive in {}ms.", (System.currentTimeMillis() - start));
        }

        if (cipherTransformers != null && !cipherTransformers.isEmpty()) {
            List<CipherTransformer> toUse = new ArrayList<>();
            List<String> existentCipherTransformers = cipherTransformers.stream()
                    .map(transformer -> transformer.getClass().getSimpleName())
                    .collect(Collectors.toList());

            for (String transformerName : transformersToUse) {
                if (!existentCipherTransformers.contains(transformerName)) {
                    log.error("The CipherTransformer with name {} does not exist.  Please use a name from the following: {}", transformerName, existentCipherTransformers);
                    throw new IllegalArgumentException("The CipherTransformer with name " + transformerName + " does not exist.");
                }

                for (CipherTransformer cipherTransformer : cipherTransformers) {
                    if (cipherTransformer.getClass().getSimpleName().equals(transformerName)) {
                        if (toUse.contains(cipherTransformer)) {
                            log.warn("Transformer with name {} has already been declared.  This will result in the transformer being performed more than once.  Please double check that this is desired.", transformerName);
                        }

                        toUse.add(cipherTransformer);
                        break;
                    }
                }
            }

            cipherTransformers.clear();
            cipherTransformers.addAll(toUse);
        }

        cipher = transformCipher(cipher);

        if (useKnownEvaluator && knownPlaintextEvaluators != null && !knownPlaintextEvaluators.isEmpty()) {
            if (knownPlaintextEvaluatorName == null || knownPlaintextEvaluatorName.isEmpty()) {
                log.error("The property decipherment.use-known-evaluator was set to true, but no KnownPlaintextEvaluator implementation was specified.  Please set decipherment.evaluator.known-plaintext to a valid KnownPlaintextEvaluator or set the former property to false.");
                throw new IllegalArgumentException("The property decipherment.evaluator.known-plaintext cannot be null if decipherment.use-known-evaluator is set to true.");
            }

            List<String> existentKnownPlaintextEvaluators = knownPlaintextEvaluators.stream()
                    .map(evaluator -> evaluator.getClass().getSimpleName())
                    .collect(Collectors.toList());

            for (KnownPlaintextEvaluator evaluator : knownPlaintextEvaluators) {
                if (evaluator.getClass().getSimpleName().equals(knownPlaintextEvaluatorName)) {
                    knownPlaintextEvaluator = evaluator;
                    break;
                }
            }

            if (knownPlaintextEvaluator == null) {
                log.error("The KnownPlaintextEvaluator with name {} does not exist.  Please use a name from the following: {}", knownPlaintextEvaluatorName, existentKnownPlaintextEvaluators);
                throw new IllegalArgumentException("The KnownPlaintextEvaluator with name " + knownPlaintextEvaluatorName + " does not exist.");
            }
        }

        List<String> existentPlaintextEvaluators = plaintextEvaluators.stream()
                .map(evaluator -> evaluator.getClass().getSimpleName())
                .collect(Collectors.toList());

        for (PlaintextEvaluator evaluator : plaintextEvaluators) {
            if (evaluator.getClass().getSimpleName().equals(plaintextEvaluatorName)) {
                plaintextEvaluator = evaluator;
                break;
            }
        }

        if (plaintextEvaluator == null) {
            log.error("The PlaintextEvaluator with name {} does not exist.  Please use a name from the following: {}", plaintextEvaluatorName, existentPlaintextEvaluators);
            throw new IllegalArgumentException("The PlaintextEvaluatorr with name " + plaintextEvaluatorName + " does not exist.");
        }
    }

    private Cipher transformCipher(Cipher cipher) {
        for (CipherTransformer cipherTransformer : cipherTransformers) {
            cipher = cipherTransformer.transform(cipher);
        }

        return cipher;
    }
}
