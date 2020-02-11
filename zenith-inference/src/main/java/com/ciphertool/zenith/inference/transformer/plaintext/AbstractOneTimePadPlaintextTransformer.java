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

package com.ciphertool.zenith.inference.transformer.plaintext;

import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.FormlyFormField;
import com.ciphertool.zenith.inference.entities.FormlyTemplateOptions;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;

public abstract class AbstractOneTimePadPlaintextTransformer implements PlaintextTransformer {
    @Value("${one-time-pad-transformer.key}")
    protected String key;

    @Override
    public FormlyForm getForm() {
        FormlyForm form = new FormlyForm();

        FormlyTemplateOptions templateOptions = new FormlyTemplateOptions();
        templateOptions.setLabel("Key");
        templateOptions.setRequired(true);

        FormlyFormField key = new FormlyFormField();
        key.setKey("key");
        key.setType("input");
        key.setTemplateOptions(templateOptions);

        form.setFields(Collections.singletonList(key));

        return form;
    }
}
