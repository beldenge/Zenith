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

package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.FormlyFormField;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AbstractPeriodCipherTransformerTest {
    @Test
    public void given_validInput_when_constructing_then_returnsExpectedValue() {
        Map<String, Object> data = new HashMap<>();
        data.put(AbstractPeriodCipherTransformer.LENGTH, 7);

        TestPeriodTransformer transformer = new TestPeriodTransformer(data);

        assertEquals(7, transformer.getPeriod());
    }

    @Test
    public void given_validInput_when_constructing_then_returnsExpectedValueCase2() {
        Map<String, Object> data = new HashMap<>();
        data.put("argument", 5);

        TestPeriodTransformer transformer = new TestPeriodTransformer(data);

        assertEquals(5, transformer.getPeriod());
    }

    @Test
    public void given_validInput_when_gettingForm_then_returnsNotNull() {
        TestPeriodTransformer transformer = new TestPeriodTransformer(new HashMap<>());

        FormlyForm form = transformer.getForm();
        assertNotNull(form);
        assertEquals(1, form.getFields().size());

        FormlyFormField field = form.getFields().get(0);
        assertEquals(AbstractPeriodCipherTransformer.LENGTH, field.getKey());
        assertEquals("input", field.getType());
        assertEquals("Length", field.getProps().getLabel());
        assertEquals("number", field.getProps().getType());
    }

    private static final class TestPeriodTransformer extends AbstractPeriodCipherTransformer {
        private TestPeriodTransformer(Map<String, Object> data) {
            super(data);
        }

        @Override
        public Cipher transform(Cipher cipher) {
            return cipher;
        }

        @Override
        public CipherTransformer getInstance(Map<String, Object> data) {
            return new TestPeriodTransformer(data);
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public String getHelpText() {
            return "test";
        }

        private Integer getPeriod() {
            return period;
        }
    }
}
