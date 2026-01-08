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

import com.ciphertool.zenith.inference.entities.FormlyFieldProps;
import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.FormlyFormField;
import com.ciphertool.zenith.inference.transformer.Transformer;
import com.ciphertool.zenith.inference.transformer.ciphertext.CipherTransformer;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class TransformerController {
    @Autowired
    private List<CipherTransformer> cipherTransformers;

    @Autowired
    private List<PlaintextTransformer> plaintextTransformers;

    @QueryMapping
    public List<CipherTransformer> ciphertextTransformers() {
        return cipherTransformers;
    }

    @QueryMapping
    public List<PlaintextTransformer> plaintextTransformers() {
        return plaintextTransformers;
    }

    @SchemaMapping
    public String name(Transformer transformer) {
        return transformer.getName();
    }

    @SchemaMapping
    public String displayName(Transformer transformer) {
        return transformer.getDisplayName();
    }

    @SchemaMapping
    public FormlyForm form(Transformer transformer) {
        return transformer.getForm();
    }

    @SchemaMapping
    public int order(Transformer transformer) {
        return transformer.getOrder();
    }

    @SchemaMapping
    public String helpText(Transformer transformer) {
        return transformer.getHelpText();
    }

    @SchemaMapping
    public Map<String, Object> model(FormlyForm form) {
        return form.getModel();
    }

    @SchemaMapping
    public List<FormlyFormField> fields(FormlyForm form) {
        return form.getFields();
    }

    @SchemaMapping
    public String key(FormlyFormField field) {
        return field.getKey();
    }

    @SchemaMapping
    public String type(FormlyFormField field) {
        return field.getType();
    }

    @SchemaMapping
    public FormlyFieldProps props(FormlyFormField field) {
        return field.getProps();
    }

    @SchemaMapping
    public String defaultValue(FormlyFormField field) {
        return field.getDefaultValue();
    }

    @SchemaMapping
    public String label(FormlyFieldProps props) {
        return props.getLabel();
    }

    @SchemaMapping
    public String placeholder(FormlyFieldProps props) {
        return props.getPlaceholder();
    }

    @SchemaMapping
    public boolean required(FormlyFieldProps props) {
        return props.isRequired();
    }

    @SchemaMapping
    public String type(FormlyFieldProps props) {
        return props.getType();
    }

    @SchemaMapping
    public Integer rows(FormlyFieldProps props) {
        return props.getRows();
    }

    @SchemaMapping
    public Integer cols(FormlyFieldProps props) {
        return props.getCols();
    }

    @SchemaMapping
    public Double max(FormlyFieldProps props) {
        return props.getMax();
    }

    @SchemaMapping
    public Double min(FormlyFieldProps props) {
        return props.getMin();
    }

    @SchemaMapping
    public Integer minLength(FormlyFieldProps props) {
        return props.getMinLength();
    }

    @SchemaMapping
    public Integer maxLength(FormlyFieldProps props) {
        return props.getMaxLength();
    }

    @SchemaMapping
    public String pattern(FormlyFieldProps props) {
        return props.getPattern();
    }
}
