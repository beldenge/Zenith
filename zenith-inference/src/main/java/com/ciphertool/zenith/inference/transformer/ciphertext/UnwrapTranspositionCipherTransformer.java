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

package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.transformer.TransformerInputType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UnwrapTranspositionCipherTransformer extends AbstractTranspositionCipherTransformer {
    public UnwrapTranspositionCipherTransformer() {
    }

    public UnwrapTranspositionCipherTransformer(String transpositionKeyString, int transpositionIterations) {
        this.transpositionKeyString = transpositionKeyString;
        this.transpositionIterations = transpositionIterations;
    }

    @Override
    protected Cipher unwrap(Cipher cipher, List<Integer> columnIndices) {
        log.debug("Unwrapping transposition {} time{} using column key '{}' with indices {}.", transpositionIterations, (transpositionIterations > 1 ? "s" : ""), transpositionKeyString, columnIndices);

        int rows = cipher.length() / columnIndices.size();

        Cipher transformed = cipher.clone();
        Cipher clone = cipher;
        for (int iter = 0; iter < transpositionIterations; iter++) {
            if (iter > 0) {
                clone = transformed.clone();
            }

            int k = 0;

            for (int i = 0; i < columnIndices.size(); i++) {
                int columnIndex = columnIndices.indexOf(i);

                for (int j = 0; j < rows; j++) {
                    transformed.replaceCiphertextCharacter((j * columnIndices.size()) + columnIndex, clone.getCiphertextCharacters().get(k).clone());
                    k++;
                }
            }
        }

        return transformed;
    }

    @Override
    public TransformerInputType getInputType() {
        return TransformerInputType.TEXT_OR_NUMBER_ARRAY;
    }
}
