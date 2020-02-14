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
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Component
public class UnwrapTranspositionCipherTransformer extends AbstractTranspositionCipherTransformer {
    public UnwrapTranspositionCipherTransformer(Map<String, Object> data) {
        super(data);
    }

    @Override
    protected Cipher unwrap(Cipher cipher, List<Integer> columnIndices) {
        log.debug("Unwrapping transposition using column key '{}' with indices {}.", transpositionKeyString, columnIndices);

        int rows = cipher.length() / columnIndices.size();

        Cipher transformed = cipher.clone();
        Cipher clone = cipher;

        int k = 0;

        for (int i = 0; i < columnIndices.size(); i++) {
            int columnIndex = columnIndices.indexOf(i);

            for (int j = 0; j < rows; j++) {
                transformed.replaceCiphertextCharacter((j * columnIndices.size()) + columnIndex, clone.getCiphertextCharacters().get(k).clone());
                k++;
            }
        }

        return transformed;
    }

    @Override
    public CipherTransformer getInstance(Map<String, Object> data) {
        return new UnwrapTranspositionCipherTransformer(data);
    }

    @Override
    public int getOrder() {
        return 19;
    }
}
