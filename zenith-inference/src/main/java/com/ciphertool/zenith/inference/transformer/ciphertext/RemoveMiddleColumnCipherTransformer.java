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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RemoveMiddleColumnCipherTransformer implements CipherTransformer {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public Cipher transform(Cipher cipher) {
        if (cipher.getColumns() % 2 == 0) {
            log.error("Unable to remove middle column as there are an even number of columns={}.  Returning original cipher.", cipher.getColumns());
            throw new IllegalArgumentException("Unable to remove middle column as there are an even number of columns.");
        }

        Cipher transformed = cipher.clone();

        int middleColumn = cipher.getColumns() / 2;

        // Remove the middle ciphertext character starting from the last row
        for (int i = cipher.getRows() - 1; i >= 0; i--) {
            transformed.removeCiphertextCharacter(transformed.getCiphertextCharacters().get((i * cipher.getColumns()) + middleColumn));
        }

        transformed.setColumns(transformed.getColumns() - 1);

        return transformed;
    }

    @Override
    public CipherTransformer getInstance(Map<String, Object> data) {
        return new RemoveMiddleColumnCipherTransformer();
    }

    @Override
    public FormlyForm getForm() {
        return null;
    }

    @Override
    public int getOrder() {
        return 14;
    }

    @Override
    public String getHelpText() {
        return "If and only if there are an odd number of columns, removes the entire middle column";
    }
}
