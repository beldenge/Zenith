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

package com.ciphertool.zenith.api.graphql;

import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.transformer.FormComponent;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class FormComponentController {
    @SchemaMapping
    public String name(FormComponent evaluator) {
        return evaluator.getName();
    }

    @SchemaMapping
    public String displayName(FormComponent evaluator) {
        return evaluator.getDisplayName();
    }

    @SchemaMapping
    public FormlyForm form(FormComponent evaluator) {
        return evaluator.getForm();
    }

    @SchemaMapping
    public int order(FormComponent evaluator) {
        return evaluator.getOrder();
    }

    @SchemaMapping
    public String helpText(FormComponent evaluator) {
        return evaluator.getHelpText();
    }
}
