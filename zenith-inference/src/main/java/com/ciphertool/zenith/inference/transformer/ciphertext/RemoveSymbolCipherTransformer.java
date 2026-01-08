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

import com.ciphertool.zenith.inference.entities.*;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@NoArgsConstructor
@Component
public class RemoveSymbolCipherTransformer implements CipherTransformer {
    public static final String SYMBOL = "symbol";
    private String symbolToRemove;

    public RemoveSymbolCipherTransformer(Map<String, Object> data) {
        symbolToRemove = (String) data.get(SYMBOL);

        // Support backwards compatibility with command-line method
        if (symbolToRemove == null) {
            symbolToRemove = (String) data.get("argument");
        }
    }

    @Override
    public Cipher transform(Cipher cipher) {
        Cipher transformed = cipher.clone();
        int length = cipher.length();

        // Remove the last row altogether
        for (int i = cipher.length() - 1; i >= 0; i--) {
            Ciphertext next = transformed.getCiphertextCharacters().get(i);

            if (symbolToRemove.equals(next.getValue())) {
                transformed.removeCiphertextCharacter(i);
                length --;
            }
        }

        // This transformer flattens the shape since we cannot be sure whether the resultant cipher length will be divisible by the original number of columns
        transformed.setRows(1);
        transformed.setColumns(length);

        return transformed;
    }

    @Override
    public CipherTransformer getInstance(Map<String, Object> data) {
        return new RemoveSymbolCipherTransformer(data);
    }

    @Override
    public FormlyForm getForm() {
        FormlyForm form = new FormlyForm();

        FormlyFieldProps templateOptions = new FormlyFieldProps();
        templateOptions.setLabel("Symbol");
        templateOptions.setRequired(true);
        templateOptions.setType("text");

        FormlyFormField key = new FormlyFormField();
        key.setKey(SYMBOL);
        key.setType("input");
        key.setProps(templateOptions);

        form.setFields(Collections.singletonList(key));

        return form;
    }

    @Override
    public int getOrder() {
        return 15;
    }

    @Override
    public String getHelpText() {
        return "Removes all instances of the specified symbol and flattens the cipher to a single row as a consequence";
    }
}