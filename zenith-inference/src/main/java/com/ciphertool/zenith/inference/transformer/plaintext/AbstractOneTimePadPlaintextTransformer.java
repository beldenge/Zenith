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
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

@NoArgsConstructor
public abstract class AbstractOneTimePadPlaintextTransformer implements PlaintextTransformer {
    public static final String KEY = "key";

    protected String key;

    public AbstractOneTimePadPlaintextTransformer(Map<String, Object> data) {
        key = ((String) data.get(KEY)).toLowerCase();
    }

    @Override
    public FormlyForm getForm() {
        FormlyForm form = new FormlyForm();

        FormlyTemplateOptions templateOptions = new FormlyTemplateOptions();
        templateOptions.setLabel("Key");
        templateOptions.setRequired(true);
        templateOptions.setPattern("[A-Za-z]+");

        FormlyFormField key = new FormlyFormField();
        key.setKey(KEY);
        key.setType("textarea");
        key.setTemplateOptions(templateOptions);

        form.setFields(Collections.singletonList(key));

        return form;
    }
}
