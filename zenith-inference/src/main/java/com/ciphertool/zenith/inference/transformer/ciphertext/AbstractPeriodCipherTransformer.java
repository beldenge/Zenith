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

import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.FormlyFormField;
import com.ciphertool.zenith.inference.entities.FormlyTemplateOptions;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

@NoArgsConstructor
public abstract class AbstractPeriodCipherTransformer implements CipherTransformer {
    public static final String LENGTH = "length";
    protected Integer period;

    public AbstractPeriodCipherTransformer(Map<String, Object> data) {
        period = (Integer) data.get(LENGTH);

        // Support backwards compatibility with command-line method
        if (period == null) {
            period = (Integer) data.get("argument");
        }
    }

    @Override
    public FormlyForm getForm() {
        FormlyForm form = new FormlyForm();

        FormlyTemplateOptions templateOptions = new FormlyTemplateOptions();
        templateOptions.setLabel("Length");
        templateOptions.setRequired(true);

        FormlyFormField key = new FormlyFormField();
        key.setKey(LENGTH);
        key.setType("number");
        key.setTemplateOptions(templateOptions);

        form.setFields(Collections.singletonList(key));

        return form;
    }
}
