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

package com.ciphertool.zenith.inference.evaluator;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.evaluator.model.SolutionScore;
import com.ciphertool.zenith.inference.util.EntropyEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NGramAndEntropyPlaintextEvaluator extends AbstractNGramEvaluator implements PlaintextEvaluator {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private EntropyEvaluator entropyEvaluator;

    @Override
    public SolutionScore evaluate(Cipher cipher, CipherSolution solution, String solutionString, String ciphertextKey) {
        long startLetter = System.currentTimeMillis();

        float[][] logProbabilitiesUpdated = evaluateLetterNGrams(cipher, solution, solutionString, ciphertextKey);

        if (log.isDebugEnabled()) {
            log.debug("Letter N-Grams took {}ms.", (System.currentTimeMillis() - startLetter));
        }

        float score = (solution.getLogProbability() / (float) solution.getLogProbabilities().length) / (entropyEvaluator.evaluate(cipher, solutionString) * 0.25f);

        return new SolutionScore(logProbabilitiesUpdated, score);
    }
}
