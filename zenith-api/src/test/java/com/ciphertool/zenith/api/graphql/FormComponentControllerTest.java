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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormComponentControllerTest {

    @InjectMocks
    private FormComponentController controller;

    @Test
    void nameSchemaMapping_returnsComponentName() {
        FormComponent component = mock(FormComponent.class);
        when(component.getName()).thenReturn("ComponentName");

        assertEquals("ComponentName", controller.name(component));
    }

    @Test
    void displayNameSchemaMapping_returnsComponentDisplayName() {
        FormComponent component = mock(FormComponent.class);
        when(component.getDisplayName()).thenReturn("Display Name");

        assertEquals("Display Name", controller.displayName(component));
    }

    @Test
    void formSchemaMapping_returnsComponentForm() {
        FormComponent component = mock(FormComponent.class);
        FormlyForm form = mock(FormlyForm.class);
        when(component.getForm()).thenReturn(form);

        assertSame(form, controller.form(component));
    }

    @Test
    void orderSchemaMapping_returnsComponentOrder() {
        FormComponent component = mock(FormComponent.class);
        when(component.getOrder()).thenReturn(5);

        assertEquals(5, controller.order(component));
    }

    @Test
    void helpTextSchemaMapping_returnsComponentHelpText() {
        FormComponent component = mock(FormComponent.class);
        when(component.getHelpText()).thenReturn("Help text here");

        assertEquals("Help text here", controller.helpText(component));
    }
}
