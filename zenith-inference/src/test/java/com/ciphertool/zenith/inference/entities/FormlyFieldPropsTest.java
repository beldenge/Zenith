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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FormlyFieldPropsTest {
    @Test
    public void given_defaults_when_accessingValues_then_returnsExpectedDefaults() {
        FormlyFieldProps props = new FormlyFieldProps();

        assertFalse(props.isRequired());
        assertFalse(props.isDisabled());
    }

    @Test
    public void given_values_when_settingProps_then_returnsExpectedValues() {
        FormlyFieldProps props = new FormlyFieldProps();
        props.setLabel("Label");
        props.setPlaceholder("Placeholder");
        props.setRequired(true);
        props.setType("text");
        props.setRows(2);
        props.setCols(3);
        props.setMax(10.5d);
        props.setMin(1.5d);
        props.setMinLength(2);
        props.setMaxLength(10);
        props.setPattern("[a-z]+");
        props.setDisabled(true);

        assertEquals("Label", props.getLabel());
        assertEquals("Placeholder", props.getPlaceholder());
        assertEquals(true, props.isRequired());
        assertEquals("text", props.getType());
        assertEquals(2, props.getRows());
        assertEquals(3, props.getCols());
        assertEquals(10.5d, props.getMax());
        assertEquals(1.5d, props.getMin());
        assertEquals(2, props.getMinLength());
        assertEquals(10, props.getMaxLength());
        assertEquals("[a-z]+", props.getPattern());
        assertEquals(true, props.isDisabled());
    }
}
