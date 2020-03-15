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

package com.ciphertool.zenith.inference.transformer.plaintext;

import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.FormlyFormField;
import com.ciphertool.zenith.inference.entities.FormlyTemplateOptions;
import com.ciphertool.zenith.model.LanguageConstants;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public abstract class AbstractVigenerePlaintextTransformer implements PlaintextTransformer {
    protected Logger log = LoggerFactory.getLogger(getClass());

    public static final int VIGENERE_SQUARE_LENGTH = 26 * 26;
    public static final String VIGENERE_SQUARE = "vigenereSquare";
    public static final String KEY = "key";

    protected static String defaultVigenereSquare = "";
    protected static char[][] vigenereSquare = new char[26][26];

    private static char[] letters = LanguageConstants.LOWERCASE_LETTERS;

    static {
        for (int i = 0; i < 26; i ++) {
            for (int j = 0; j < 26; j ++) {
                defaultVigenereSquare += letters[(i + j) % 26];
            }
        }
    }

    protected String key;

    public AbstractVigenerePlaintextTransformer(Map<String, Object> data) {
        String rawVigenereSquare = (String) data.get(VIGENERE_SQUARE);
        String vignereSquareAsSingleLine = defaultVigenereSquare;

        if (rawVigenereSquare != null) {
            if (rawVigenereSquare.length() != VIGENERE_SQUARE_LENGTH){
                throw new IllegalArgumentException("Argument " + VIGENERE_SQUARE + " must be exactly " + VIGENERE_SQUARE_LENGTH + " characters long.");
            }

            vignereSquareAsSingleLine = rawVigenereSquare;
        }

        for (int i = 0; i < 26; i ++) {
            for (int j = 0; j < 26; j ++) {
                vigenereSquare[i][j] = vignereSquareAsSingleLine.charAt((j * 26) + i);
            }
        }

        key = (String) data.get(KEY);
    }

    @Override
    public FormlyForm getForm() {
        FormlyForm form = new FormlyForm();

        List<FormlyFormField> fields = new ArrayList<>(1);

        FormlyTemplateOptions vigenereSquareOptions = new FormlyTemplateOptions();
        vigenereSquareOptions.setLabel("Vigenere Square");
        vigenereSquareOptions.setRequired(true);
        vigenereSquareOptions.setPattern("[a-z]+");
        vigenereSquareOptions.setRows(26);
        vigenereSquareOptions.setCols(26);
        vigenereSquareOptions.setMinLength(VIGENERE_SQUARE_LENGTH);
        vigenereSquareOptions.setMaxLength(VIGENERE_SQUARE_LENGTH);

        FormlyFormField vigenereSquare = new FormlyFormField();
        vigenereSquare.setKey(VIGENERE_SQUARE);
        vigenereSquare.setType("textarea");
        vigenereSquare.setTemplateOptions(vigenereSquareOptions);
        vigenereSquare.setDefaultValue(defaultVigenereSquare);

        fields.add(vigenereSquare);

        FormlyTemplateOptions keyOptions = new FormlyTemplateOptions();
        keyOptions.setLabel("Key");
        keyOptions.setRequired(true);
        keyOptions.setPattern("[a-z]+");

        FormlyFormField key = new FormlyFormField();
        key.setKey(KEY);
        key.setType("input");
        key.setTemplateOptions(keyOptions);

        fields.add(key);

        form.setFields(fields);

        return form;
    }
}
