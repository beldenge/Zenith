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
import com.ciphertool.zenith.inference.transformer.ciphertext.CipherTransformer;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransformerControllerTest {

    @Mock
    private List<CipherTransformer> cipherTransformers;

    @Mock
    private List<PlaintextTransformer> plaintextTransformers;

    @InjectMocks
    private TransformerController controller;

    @Test
    void given_validInput_when_ciphertextTransformersReturnsInjectedTransformers_then_returnsSameInstance() {
        List<CipherTransformer> result = controller.ciphertextTransformers();

        assertSame(cipherTransformers, result);
    }

    @Test
    void given_validInput_when_plaintextTransformersReturnsInjectedTransformers_then_returnsSameInstance() {
        List<PlaintextTransformer> result = controller.plaintextTransformers();

        assertSame(plaintextTransformers, result);
    }

    @Test
    void given_validInput_when_modelSchemaMappingReturnsFormModel_then_returnsExpectedValue() {
        FormlyForm form = mock(FormlyForm.class);
        Map<String, Object> model = new HashMap<>();
        model.put("key", "value");
        when(form.getModel()).thenReturn(model);

        Map<String, Object> result = controller.model(form);

        assertEquals(model, result);
    }

    @Test
    void given_validInput_when_fieldsSchemaMappingReturnsFormFields_then_returnsExpectedValue() {
        FormlyForm form = mock(FormlyForm.class);
        List<FormlyFormField> fields = Arrays.asList(mock(FormlyFormField.class));
        when(form.getFields()).thenReturn(fields);

        List<FormlyFormField> result = controller.fields(form);

        assertEquals(fields, result);
    }

    @Test
    void given_validInput_when_keySchemaMappingReturnsFieldKey_then_returnsExpectedValue() {
        FormlyFormField field = mock(FormlyFormField.class);
        when(field.getKey()).thenReturn("testKey");

        assertEquals("testKey", controller.key(field));
    }

    @Test
    void given_validInput_when_typeSchemaMappingForFieldReturnsFieldType_then_returnsExpectedValue() {
        FormlyFormField field = mock(FormlyFormField.class);
        when(field.getType()).thenReturn("input");

        assertEquals("input", controller.type(field));
    }

    @Test
    void given_validInput_when_propsSchemaMappingReturnsFieldProps_then_returnsSameInstance() {
        FormlyFormField field = mock(FormlyFormField.class);
        FormlyFieldProps props = mock(FormlyFieldProps.class);
        when(field.getProps()).thenReturn(props);

        assertSame(props, controller.props(field));
    }

    @Test
    void given_validInput_when_defaultValueSchemaMappingReturnsFieldDefaultValue_then_returnsExpectedValue() {
        FormlyFormField field = mock(FormlyFormField.class);
        when(field.getDefaultValue()).thenReturn("default");

        assertEquals("default", controller.defaultValue(field));
    }

    @Test
    void given_validInput_when_labelSchemaMappingReturnsPropsLabel_then_returnsExpectedValue() {
        FormlyFieldProps props = mock(FormlyFieldProps.class);
        when(props.getLabel()).thenReturn("Label Text");

        assertEquals("Label Text", controller.label(props));
    }

    @Test
    void given_validInput_when_placeholderSchemaMappingReturnsPropsPlaceholder_then_returnsExpectedValue() {
        FormlyFieldProps props = mock(FormlyFieldProps.class);
        when(props.getPlaceholder()).thenReturn("Enter value...");

        assertEquals("Enter value...", controller.placeholder(props));
    }

    @Test
    void given_validInput_when_requiredSchemaMappingReturnsPropsRequired_then_returnsTrue() {
        FormlyFieldProps props = mock(FormlyFieldProps.class);
        when(props.isRequired()).thenReturn(true);

        assertTrue(controller.required(props));
    }

    @Test
    void given_validInput_when_typeSchemaMappingForPropsReturnsPropsType_then_returnsExpectedValue() {
        FormlyFieldProps props = mock(FormlyFieldProps.class);
        when(props.getType()).thenReturn("number");

        assertEquals("number", controller.type(props));
    }

    @Test
    void given_validInput_when_rowsSchemaMappingReturnsPropsRows_then_returnsExpectedValue() {
        FormlyFieldProps props = mock(FormlyFieldProps.class);
        when(props.getRows()).thenReturn(5);

        assertEquals(5, controller.rows(props));
    }

    @Test
    void given_validInput_when_colsSchemaMappingReturnsPropsCols_then_returnsExpectedValue() {
        FormlyFieldProps props = mock(FormlyFieldProps.class);
        when(props.getCols()).thenReturn(10);

        assertEquals(10, controller.cols(props));
    }

    @Test
    void given_validInput_when_maxSchemaMappingReturnsPropsMax_then_returnsExpectedValue() {
        FormlyFieldProps props = mock(FormlyFieldProps.class);
        when(props.getMax()).thenReturn(100.0);

        assertEquals(100.0, controller.max(props));
    }

    @Test
    void given_validInput_when_minSchemaMappingReturnsPropsMin_then_returnsExpectedValue() {
        FormlyFieldProps props = mock(FormlyFieldProps.class);
        when(props.getMin()).thenReturn(0.0);

        assertEquals(0.0, controller.min(props));
    }

    @Test
    void given_validInput_when_minLengthSchemaMappingReturnsPropsMinLength_then_returnsExpectedValue() {
        FormlyFieldProps props = mock(FormlyFieldProps.class);
        when(props.getMinLength()).thenReturn(1);

        assertEquals(1, controller.minLength(props));
    }

    @Test
    void given_validInput_when_maxLengthSchemaMappingReturnsPropsMaxLength_then_returnsExpectedValue() {
        FormlyFieldProps props = mock(FormlyFieldProps.class);
        when(props.getMaxLength()).thenReturn(255);

        assertEquals(255, controller.maxLength(props));
    }

    @Test
    void given_validInput_when_patternSchemaMappingReturnsPropsPattern_then_returnsExpectedValue() {
        FormlyFieldProps props = mock(FormlyFieldProps.class);
        when(props.getPattern()).thenReturn("[A-Z]+");

        assertEquals("[A-Z]+", controller.pattern(props));
    }
}
