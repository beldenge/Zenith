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
import com.ciphertool.zenith.inference.entities.FormlyFormField;
import com.ciphertool.zenith.inference.entities.FormlyTemplateOptions;
import com.ciphertool.zenith.model.LanguageConstants;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@NoArgsConstructor
public abstract class AbstractTranspositionCipherTransformer implements CipherTransformer {
    protected Logger log = LoggerFactory.getLogger(getClass());

    public static final String KEY = "key";

    protected List<Integer> transpositionKey;

    protected String transpositionKeyString;

    public void init() {
        if (transpositionKeyString != null && !transpositionKeyString.isEmpty()) {
            transpositionKey = getIndicesForTranspositionKey();
        }

        List<Integer> toCheck = new ArrayList<>(transpositionKey);

        toCheck.sort(Comparator.comparing(Integer::intValue));

        for (int i = 0; i < toCheck.size(); i++) {
            if (toCheck.get(i) != i) {
                throw new IllegalArgumentException("The transposition column key indices must be zero-based with no gaps or duplicates.");
            }
        }
    }

    public AbstractTranspositionCipherTransformer(Map<String, Object> data) {
        transpositionKeyString = (String) data.get(KEY);

        // Support backwards compatibility with command-line method
        if (transpositionKeyString == null) {
            transpositionKeyString = (String) data.get("argument");
        }

        init();
    }

    @Override
    public Cipher transform(Cipher cipher) {
        if (transpositionKey == null || transpositionKey.isEmpty()) {
            log.debug("{} performing no-op since the transposition key is unspecified.", getClass().getName());
            return cipher;
        }

        if (transpositionKey.size() < 2 || transpositionKey.size() >= cipher.length()) {
            throw new IllegalArgumentException("The transposition key length of " + transpositionKeyString.length()
                    + " must be greater than one and less than the cipher length of " + cipher.length() + ".");
        }

        return unwrap(cipher, transpositionKey);
    }

    protected abstract Cipher unwrap(Cipher cipher, List<Integer> columnIndices);

    public Cipher transform(Cipher cipher, List<Integer> columnIndices) {
        if (columnIndices.size() < 2 || columnIndices.size() >= cipher.length()) {
            throw new IllegalArgumentException("The transposition key length of " + columnIndices.size()
                    + " must be greater than one and less than the cipher length of " + cipher.length() + ".");
        }

        return unwrap(cipher, columnIndices);
    }

    protected List<Integer> getIndicesForTranspositionKey() {
        int next = 0;
        Integer[] columnIndices = new Integer[transpositionKeyString.length()];

        for (int i = 0; i < LanguageConstants.LOWERCASE_LETTERS_SIZE; i++) {
            char letter = LanguageConstants.LOWERCASE_LETTERS[i];

            for (int j = 0; j < transpositionKeyString.length(); j++) {
                if (transpositionKeyString.toLowerCase().charAt(j) == letter) {
                    columnIndices[j] = next;
                    next++;
                }
            }
        }

        return Arrays.asList(columnIndices);
    }

    @Override
    public FormlyForm getForm() {
        FormlyForm form = new FormlyForm();

        FormlyTemplateOptions templateOptions = new FormlyTemplateOptions();
        templateOptions.setLabel("Key");
        templateOptions.setRequired(true);

        FormlyFormField key = new FormlyFormField();
        key.setKey(KEY);
        key.setType("input");
        key.setTemplateOptions(templateOptions);

        form.setFields(Collections.singletonList(key));

        return form;
    }
}
