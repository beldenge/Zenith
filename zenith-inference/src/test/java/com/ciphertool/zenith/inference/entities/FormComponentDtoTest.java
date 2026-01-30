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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FormComponentDtoTest {
    @Test
    public void given_validInput_when_gettingTersAndSetters_then_returnsNotNull() {
        FormComponentDto dto = new FormComponentDto();
        FormlyForm form = new FormlyForm();

        dto.setName("name");
        dto.setDisplayName("display");
        dto.setForm(form);
        dto.setOrder(3);
        dto.setHelpText("help");

        assertEquals("name", dto.getName());
        assertEquals("display", dto.getDisplayName());
        assertEquals(form, dto.getForm());
        assertEquals(3, dto.getOrder());
        assertEquals("help", dto.getHelpText());

        assertNotNull(dto);
    }

    @Test
    public void given_validInput_when_formlyFormDefaults_then_returnsNotNull() {
        FormlyForm form = new FormlyForm();

        assertNotNull(form.getFields());
        assertNotNull(form.getModel());

        form.getFields().add(new FormlyFormField());
        form.getModel().put("key", "value");

        assertEquals(1, form.getFields().size());
        assertEquals(Collections.singletonMap("key", "value"), form.getModel());
    }
}