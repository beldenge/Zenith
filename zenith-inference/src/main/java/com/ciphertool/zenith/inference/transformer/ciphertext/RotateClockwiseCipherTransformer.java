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
import org.springframework.stereotype.Component;

@Component
public class RotateClockwiseCipherTransformer implements CipherTransformer {
    @Override
    public Cipher transform(Cipher cipher) {
        Cipher transformed = cipher.clone();
        transformed.setRows(cipher.getColumns());
        transformed.setColumns(cipher.getRows());

        int k = 0;

        for (int i = 0; i < cipher.getColumns(); i++) {
            for (int j = cipher.getRows() - 1; j >= 0; j--) {
                transformed.replaceCiphertextCharacter(k, cipher.getCiphertextCharacters().get((j * cipher.getColumns()) + i).clone());
                k++;
            }
        }

        return transformed;
    }
}
