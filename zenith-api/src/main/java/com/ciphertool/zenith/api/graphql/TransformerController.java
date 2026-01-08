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
