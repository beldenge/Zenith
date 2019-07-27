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
import com.ciphertool.zenith.inference.transformer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractSolutionOptimizer {
    private Logger log = LoggerFactory.getLogger(getClass());

    private final static String CIPHER_TRANSFORMER_SUFFIX = CipherTransformer.class.getSimpleName();

    @Value("${decipherment.transformers.list}")
    private List<String> transformersToUse;

    @Value("${decipherment.evaluator.plaintext}")
    private String plaintextEvaluatorName;

    @Autowired
    protected Cipher cipher;

    @Autowired
    protected List<CipherTransformer> cipherTransformers;

    @Autowired
    private List<PlaintextEvaluator> plaintextEvaluators;

    protected PlaintextEvaluator plaintextEvaluator;

    @PostConstruct
    public void init() {
        if (cipherTransformers != null && !cipherTransformers.isEmpty()) {
            List<CipherTransformer> toUse = new ArrayList<>();
            List<String> existentCipherTransformers = cipherTransformers.stream()
                    .map(transformer -> transformer.getClass().getSimpleName().replace(CIPHER_TRANSFORMER_SUFFIX, ""))
                    .collect(Collectors.toList());

            for (String transformerName : transformersToUse) {
                String transformerNameBeforeParenthesis = transformerName.contains("(") ? transformerName.substring(0, transformerName.indexOf('(')) : transformerName;

                if (!existentCipherTransformers.contains(transformerNameBeforeParenthesis)) {
                    log.error("The CipherTransformer with name {} does not exist.  Please use a name from the following: {}", transformerNameBeforeParenthesis, existentCipherTransformers);
                    throw new IllegalArgumentException("The CipherTransformer with name " + transformerNameBeforeParenthesis + " does not exist.");
                }

                for (CipherTransformer cipherTransformer : cipherTransformers) {
                    if (cipherTransformer.getClass().getSimpleName().replace(CIPHER_TRANSFORMER_SUFFIX, "").equals(transformerNameBeforeParenthesis)) {
                        if (transformerName.contains("(") && transformerName.endsWith(")")) {
                            String parameter = transformerName.substring(transformerName.indexOf('(') + 1, transformerName.length() - 1);

                            if (cipherTransformer instanceof TranspositionCipherTransformer) {
                                String transpositionKeyString = parameter;
                                TranspositionCipherTransformer nextTransformer = new TranspositionCipherTransformer(transpositionKeyString);
                                nextTransformer.init();
                                toUse.add(nextTransformer);
                            } else if (cipherTransformer instanceof UnwrapTranspositionCipherTransformer) {
                                String transpositionKeyString = parameter;
                                UnwrapTranspositionCipherTransformer nextTransformer = new UnwrapTranspositionCipherTransformer(transpositionKeyString);
                                nextTransformer.init();
                                toUse.add(nextTransformer);
                            } else if (cipherTransformer instanceof PeriodCipherTransformer) {
                                int period = Integer.parseInt(parameter);
                                toUse.add(new PeriodCipherTransformer(period));
                            } else if (cipherTransformer instanceof UnwrapPeriodCipherTransformer) {
                                int period = Integer.parseInt(parameter);
                                toUse.add(new UnwrapPeriodCipherTransformer(period));
                            } else {
                                throw new IllegalArgumentException("The CipherTransformer with name " + transformerNameBeforeParenthesis + " does not accept parameters.");
                            }
                        } else {
                            toUse.add(cipherTransformer);
                        }

                        break;
                    }
                }
            }

            cipherTransformers.clear();
            cipherTransformers.addAll(toUse);
        }

        cipher = transformCipher(cipher);

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
