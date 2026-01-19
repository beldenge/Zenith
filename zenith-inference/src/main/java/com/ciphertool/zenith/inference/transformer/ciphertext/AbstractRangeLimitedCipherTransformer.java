package com.ciphertool.zenith.inference.transformer.ciphertext;


import com.ciphertool.zenith.inference.entities.FormlyFieldProps;
import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.FormlyFormField;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Map;

@NoArgsConstructor
public abstract class AbstractRangeLimitedCipherTransformer implements CipherTransformer {
    public static final String RANGE_START = "rangeStart";
    public static final String RANGE_END = "rangeEnd";
    protected Integer rangeStart;
    protected Integer rangeEnd;

    public AbstractRangeLimitedCipherTransformer(Map<String, Object> data) {
        rangeStart = (Integer) data.get(RANGE_START);
        rangeEnd = (Integer) data.get(RANGE_END);

        if (rangeStart != null && rangeEnd != null && rangeStart > rangeEnd) {
            throw new IllegalArgumentException("rangeStart > rangeEnd");
        }
    }

    @Override
    public FormlyForm getForm() {
        FormlyForm form = new FormlyForm();

        FormlyFieldProps rangeStartProps = new FormlyFieldProps();
        rangeStartProps.setLabel("Range Start");
        rangeStartProps.setType("number");
        rangeStartProps.setMin(0.0);

        FormlyFormField rangeStartField = new FormlyFormField();
        rangeStartField.setKey(RANGE_START);
        rangeStartField.setType("input");
        rangeStartField.setProps(rangeStartProps);

        FormlyFieldProps rangeEndProps = new FormlyFieldProps();
        rangeEndProps.setLabel("Range End");
        rangeEndProps.setType("number");

        FormlyFormField rangeEndField = new FormlyFormField();
        rangeEndField.setKey(RANGE_END);
        rangeEndField.setType("input");
        rangeEndField.setProps(rangeEndProps);

        form.setFields(Arrays.asList(rangeStartField, rangeEndField));

        return form;
    }

    protected static void makeRowBased(FormlyForm form) {
        for (FormlyFormField field : form.getFields()) {
            if (field.getKey().equals(RANGE_START)) {
                field.getProps().setLabel("Row Start");
            }

            if (field.getKey().equals(RANGE_END)) {
                field.getProps().setLabel("Row End");
            }
        }
    }
}
