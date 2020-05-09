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

package com.ciphertool.zenith.inference;

import com.ciphertool.zenith.inference.configuration.ConfigurationResolver;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.config.ApplicationConfiguration;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.optimizer.SolutionOptimizer;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class InferenceApplication implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private Cipher cipher;

    @Autowired
    protected List<PlaintextTransformationStep> plaintextTransformationSteps;

    @Autowired
    private List<SolutionOptimizer> optimizers;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @Autowired
    private List<PlaintextEvaluator> plaintextEvaluators;

    public static void main(String[] args) {
        SpringApplication.run(InferenceApplication.class, args).close();
    }

    @Override
    public void run(String... arg0) {
        Map<String, Object> configuration = ConfigurationResolver.resolveConfiguration(applicationConfiguration);
        SolutionOptimizer solutionOptimizer = ConfigurationResolver.resolveSolutionOptimizer(applicationConfiguration, optimizers);
        PlaintextEvaluator plaintextEvaluator = ConfigurationResolver.resolvePlaintextEvaluator(applicationConfiguration, plaintextEvaluators);

        solutionOptimizer.optimize(cipher, applicationConfiguration.getEpochs(), configuration, plaintextTransformationSteps, plaintextEvaluator, null);
    }
}
