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

package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.FormlyForm;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TopLeftDiagonalCipherTransformer implements CipherTransformer {
    @Override
    public Cipher transform(Cipher cipher) {
        Cipher transformed = cipher.clone();

        int i = 0;
        int x;
        int y;

        for (int row = 0; row < cipher.getRows(); row++) {
            x = 0;
            y = row;

            for (; x < cipher.getColumns() && y >= 0;) {
                transformed.replaceCiphertextCharacter(i, cipher.getCiphertextCharacters().get((y * cipher.getColumns()) + x).clone());
                x++;
                y--;
                i++;
            }
        }

        for (int column = 1; column < cipher.getColumns(); column++) {
            x = column;
            y = cipher.getRows() - 1;

            for (; x < cipher.getColumns() && y >= 0;) {
                transformed.replaceCiphertextCharacter(i, cipher.getCiphertextCharacters().get((y * cipher.getColumns()) + x).clone());
                x++;
                y--;
                i++;
            }
        }

        return transformed;
    }

    @Override
    public CipherTransformer getInstance(Map<String, Object> data) {
        return new TopLeftDiagonalCipherTransformer();
    }

    @Override
    public FormlyForm getForm() {
        return null;
    }

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public String getHelpText() {
        return "Traverses the ciphertext diagonally as if the cipher was rotated 45 degrees to the right, with the output retaining the same number of rows and columns";
    }
}