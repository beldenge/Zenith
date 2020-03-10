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
import com.ciphertool.zenith.inference.entities.FormlyForm;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RemoveLastRowCipherTransformer implements CipherTransformer {
    @Override
    public Cipher transform(Cipher cipher) {
        Cipher transformed = cipher.clone();

        int totalCharacters = transformed.getCiphertextCharacters().size();
        int lastRowBegin = (transformed.getColumns() * (transformed.getRows() - 1));

        // Remove the last row altogether
        for (int i = totalCharacters - 1; i >= lastRowBegin; i--) {
            transformed.removeCiphertextCharacter(transformed.getCiphertextCharacters().get(i));
        }

        transformed.setRows(transformed.getRows() - 1);

        return transformed;
    }

    @Override
    public CipherTransformer getInstance(Map<String, Object> data) {
        return new RemoveLastRowCipherTransformer();
    }

    @Override
    public FormlyForm getForm() {
        return null;
    }

    @Override
    public int getOrder() {
        return 13;
    }

    @Override
    public String getHelpText() {
        return "Removes the entire bottommost row";
    }
}
