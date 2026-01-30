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
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FormlyFormTest {
    @Test
    public void given_newForm_when_accessingDefaults_then_collectionsAreInitialized() {
        FormlyForm form = new FormlyForm();

        assertNotNull(form.getFields());
        assertNotNull(form.getModel());
        assertEquals(0, form.getFields().size());
        assertEquals(0, form.getModel().size());
    }

    @Test
    public void given_form_when_mutatingFieldsAndModel_then_changesPersist() {
        FormlyForm form = new FormlyForm();
        FormlyFormField field = new FormlyFormField();
        field.setKey("value");

        form.getFields().add(field);
        form.getModel().put("key", "value");

        assertEquals(1, form.getFields().size());
        assertEquals("value", form.getFields().get(0).getKey());
        assertEquals("value", form.getModel().get("key"));
    }
}
