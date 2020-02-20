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

package com.ciphertool.zenith.inference.optimizer;

import com.ciphertool.zenith.inference.printer.CipherSolutionPrinter;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationManager;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSolutionOptimizer implements SolutionOptimizer {
    public static final String KNOWN_SOLUTION_CORRECTNESS_THRESHOLD = "knownSolutionCorrectnessThreshold";

    @Autowired
    protected PlaintextTransformationManager plaintextTransformationManager;

    @Autowired
    protected CipherSolutionPrinter cipherSolutionPrinter;
}
