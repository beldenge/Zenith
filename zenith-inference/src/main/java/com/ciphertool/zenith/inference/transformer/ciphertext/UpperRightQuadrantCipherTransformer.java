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

package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.inference.entities.FormlyForm;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UpperRightQuadrantCipherTransformer implements CipherTransformer {
    @Override
    public Cipher transform(Cipher cipher) {
        int halfOfRows = cipher.getRows() / 2;
        int halfOfColumns = cipher.getColumns() / 2;

        int columnOffset = halfOfColumns;

        if ((cipher.getColumns() % 2) != 0) {
            columnOffset += 1;
        }

        Cipher quadrant = new Cipher(cipher.getName(), halfOfRows, halfOfColumns, cipher.isReadOnly());

        int id = 0;
        for (int i = 0; i < halfOfRows; i++) {
            for (int j = columnOffset; j < cipher.getColumns(); j++) {
                Ciphertext toAdd = cipher.getCiphertextCharacters().get((i * cipher.getColumns()) + j).clone();
                toAdd.setCiphertextId(id);
                quadrant.addCiphertextCharacter(toAdd);
                id++;
            }
        }

        return quadrant;
    }

    @Override
    public CipherTransformer getInstance(Map<String, Object> data) {
        return new UpperRightQuadrantCipherTransformer();
    }

    @Override
    public FormlyForm getForm() {
        return null;
    }

    @Override
    public int getOrder() {
        return 7;
    }

    @Override
    public String getHelpText() {
        return "Crops the cipher to only the upper right quadrant";
    }
}
