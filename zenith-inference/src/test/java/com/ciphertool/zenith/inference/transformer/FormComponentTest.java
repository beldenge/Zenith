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

package com.ciphertool.zenith.inference.transformer;

import com.ciphertool.zenith.inference.entities.FormlyForm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormComponentTest {
    @Test
    public void testGetNameStripsSuffix() {
        FormComponent component = new ExampleCipherTransformer();

        assertEquals("Example", component.getName());
    }

    @Test
    public void testGetDisplayNameSplitsCamelCase() {
        FormComponent component = new ZPatternCipherTransformer();

        assertEquals("Z Pattern", component.getDisplayName());
    }

    private static final class ExampleCipherTransformer implements FormComponent {
        @Override
        public FormlyForm getForm() {
            return new FormlyForm();
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public String getHelpText() {
            return "test";
        }
    }

    private static final class ZPatternCipherTransformer implements FormComponent {
        @Override
        public FormlyForm getForm() {
            return new FormlyForm();
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public String getHelpText() {
            return "test";
        }
    }
}
