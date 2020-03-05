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

package com.ciphertool.zenith.inference.printer;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;
import com.ciphertool.zenith.inference.util.ChiSquaredEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CipherSolutionPrinter {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ChiSquaredEvaluator chiSquaredEvaluator;

    @Autowired(required = false)
    @Qualifier("activePlaintextTransformers")
    private List<PlaintextTransformer> plaintextTransformers;

    public void print(CipherSolution solution) {
        String plaintext = solution.asSingleLineString();
        if (plaintextTransformers != null) {
            for (PlaintextTransformer plaintextTransformer : plaintextTransformers) {
                plaintext = plaintextTransformer.transform(plaintext);
            }
        }

        Cipher cipher = solution.getCipher();
        StringBuilder sb = new StringBuilder();
        sb.append("Solution [probability=" + solution.getProbability() + ", logProbability=" + solution.getLogProbability() + ", score=" + solution.getScore()
                + ", indexOfCoincidence=" + solution.getIndexOfCoincidence() + ", chiSquared=" + chiSquaredEvaluator.evaluate(cipher, plaintext) + (cipher.hasKnownSolution() ? ", proximity="
                + String.format("%1$,.2f", solution.evaluateKnownSolution() * 100.0) + "%" : "") + "]\n");

        for (int i = 0; i < plaintext.length(); i++) {
            sb.append(" ");
            sb.append(plaintext.charAt(i));
            sb.append(" ");

            /*
             * Print a newline if we are at the end of the row. Add 1 to the index so the modulus function doesn't
             * break.
             */
            if (((i + 1) % cipher.getColumns()) == 0) {
                sb.append("\n");
            } else {
                sb.append(" ");
            }
        }

        sb.append("\nMappings for best probability:\n");
        sb.append("------------------------------\n");
        sb.append("|  ciphertext  |  plaintext  |\n");
        sb.append("|--------------+-------------|\n");
        for (Map.Entry<String, Character> entry : solution.getMappings().entrySet()) {
            sb.append("|  " + String.format("%-10s", entry.getKey()) + "  |  " + entry.getValue() + "          |\n");
        }

        log.info(sb.toString());
    }
}
