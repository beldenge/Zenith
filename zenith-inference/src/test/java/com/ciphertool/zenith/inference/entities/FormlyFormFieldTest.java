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

package com.ciphertool.zenith.inference.entities;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.*;

public class FormlyFormFieldTest {
    @Test
    public void given_validInput_when_gettingTersAndSetters_then_returnsTrue() {
        FormlyFormField field = new FormlyFormField();
        FormlyFieldProps props = new FormlyFieldProps();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode hooks = mapper.createObjectNode().put("hook", "value");

        field.setId("id");
        field.setKey("key");
        field.setType("input");
        field.setProps(props);
        field.setDefaultValue("default");
        field.setHooks(hooks);
        field.setModelOptions(hooks);
        field.setValidation(hooks);
        field.setResetOnHide(true);
        field.setWrappers(hooks);
        field.setExpressions(hooks);
        field.setExpressionProperties(hooks);
        field.setFocus(true);

        assertEquals("id", field.getId());
        assertEquals("key", field.getKey());
        assertEquals("input", field.getType());
        assertEquals(props, field.getProps());
        assertEquals("default", field.getDefaultValue());
        assertEquals(hooks, field.getHooks());
        assertEquals(hooks, field.getModelOptions());
        assertEquals(hooks, field.getValidation());
        assertTrue(field.isResetOnHide());
        assertEquals(hooks, field.getWrappers());
        assertEquals(hooks, field.getExpressions());
        assertEquals(hooks, field.getExpressionProperties());
        assertTrue(field.isFocus());

        assertNotNull(field);
    }

    @Test
    public void given_validInput_when_fieldProps_then_returnsExpectedValue() {
        FormlyFieldProps props = new FormlyFieldProps();

        props.setLabel("label");
        props.setPlaceholder("placeholder");
        props.setRequired(true);
        props.setType("text");
        props.setRows(2);
        props.setCols(3);
        props.setMax(9.0d);
        props.setMin(1.0d);
        props.setMinLength(2);
        props.setMaxLength(5);
        props.setPattern("[a-z]+");
        props.setDisabled(true);

        assertEquals("label", props.getLabel());
        assertEquals("placeholder", props.getPlaceholder());
        assertTrue(props.isRequired());
        assertEquals("text", props.getType());
        assertEquals(2, props.getRows());
        assertEquals(3, props.getCols());
        assertEquals(9.0d, props.getMax());
        assertEquals(1.0d, props.getMin());
        assertEquals(2, props.getMinLength());
        assertEquals(5, props.getMaxLength());
        assertEquals("[a-z]+", props.getPattern());
        assertTrue(props.isDisabled());

        props.setRequired(false);
        props.setDisabled(false);

        assertFalse(props.isRequired());
        assertFalse(props.isDisabled());
    }
}