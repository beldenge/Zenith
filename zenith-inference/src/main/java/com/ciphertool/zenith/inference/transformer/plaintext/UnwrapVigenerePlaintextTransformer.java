/*
 * Copyright 2017-2026 George Belden
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

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@NoArgsConstructor
@Component
public class UnwrapVigenerePlaintextTransformer extends AbstractVigenerePlaintextTransformer {
    public UnwrapVigenerePlaintextTransformer(Map<String, Object> data) {
        super(data);

        String rawVigenereSquare = (String) data.get(VIGENERE_SQUARE);
        String vignereSquareAsSingleLine = defaultVigenereSquare;

        if (rawVigenereSquare != null) {
            if (rawVigenereSquare.length() != VIGENERE_SQUARE_LENGTH){
                throw new IllegalArgumentException("Argument " + VIGENERE_SQUARE + " must be exactly " + VIGENERE_SQUARE_LENGTH + " characters long.");
            }

            vignereSquareAsSingleLine = rawVigenereSquare.toLowerCase();
        }

        for (int i = 0; i < 26; i ++) {
            for (int j = 0; j < 26; j ++) {
                char rowIndex = vignereSquareAsSingleLine.charAt(j * 26);
                char columnIndex = vignereSquareAsSingleLine.charAt((j * 26) + i);

                vigenereSquare[rowIndex][columnIndex] = vignereSquareAsSingleLine.charAt(i);
            }
        }
    }

    @Override
    public PlaintextTransformer getInstance(Map<String, Object> data) {
        return new UnwrapVigenerePlaintextTransformer(data);
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getHelpText() {
        return "Performs the inverse operation of the Vigenere transformer";
    }
}
