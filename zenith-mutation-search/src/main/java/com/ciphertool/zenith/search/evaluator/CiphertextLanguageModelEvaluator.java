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

package com.ciphertool.zenith.search.evaluator;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.inference.optimizer.SimulatedAnnealingSolutionOptimizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CiphertextLanguageModelEvaluator {
    @Value("${decipherment.epochs:1}")
    private int epochs;

    @Autowired
    private Cipher originalCipher;

    @Autowired
    private SimulatedAnnealingSolutionOptimizer optimizer;

    public float evaluate(Cipher mutatedCipher) {
        /*
         * Backup the originalCipher and then overwrite it from the mutatedCipher so that it's transformed in memory
         * across all places where it was initially injected.
         */
        Cipher backupOfOriginalCipher = originalCipher.clone();
        overwriteCipher(mutatedCipher, originalCipher);

        CipherSolution cipherSolution = optimizer.optimize(mutatedCipher, epochs, null);

        overwriteCipher(backupOfOriginalCipher, originalCipher);

        return cipherSolution.getScore();
    }

    private void overwriteCipher(Cipher source, Cipher target) {
        target.setRows(source.getRows());
        target.setColumns(source.getColumns());

        List<Ciphertext> sourceCiphertextCharacters = source.getCiphertextCharacters();
        for (int i = 0; i < sourceCiphertextCharacters.size(); i ++) {
            target.replaceCiphertextCharacter(i, sourceCiphertextCharacters.get(i).clone());
        }
    }
}
