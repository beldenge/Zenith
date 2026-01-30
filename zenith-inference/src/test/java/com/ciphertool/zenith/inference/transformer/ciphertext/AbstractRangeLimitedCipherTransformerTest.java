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
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractRangeLimitedCipherTransformerTest {
    @Test
    public void given_invalidInput_when_constructing_then_throwsIllegalArgumentException() {
        Map<String, Object> data = new HashMap<>();
        data.put(AbstractRangeLimitedCipherTransformer.RANGE_START, 5);
        data.put(AbstractRangeLimitedCipherTransformer.RANGE_END, 2);

        assertThrows(IllegalArgumentException.class, () -> new TestRangeTransformer(data));
    }

    @Test
    public void given_validInput_when_formAndRowBasedLabels_then_returnsExpectedValue() {
        TestRangeTransformer transformer = new TestRangeTransformer(new HashMap<>());

        FormlyForm form = transformer.getForm();
        assertEquals(2, form.getFields().size());

        FormlyFormField startField = form.getFields().get(0);
        FormlyFormField endField = form.getFields().get(1);

        assertEquals(AbstractRangeLimitedCipherTransformer.RANGE_START, startField.getKey());
        assertEquals("Range Start", startField.getProps().getLabel());
        assertEquals(0.0d, startField.getProps().getMin());

        assertEquals(AbstractRangeLimitedCipherTransformer.RANGE_END, endField.getKey());
        assertEquals("Range End", endField.getProps().getLabel());

        transformer.applyRowLabels(form);

        assertEquals("Row Start", startField.getProps().getLabel());
        assertEquals("Row End", endField.getProps().getLabel());
    }

    private static final class TestRangeTransformer extends AbstractRangeLimitedCipherTransformer {
        private TestRangeTransformer(Map<String, Object> data) {
            super(data);
        }

        @Override
        public Cipher transform(Cipher cipher) {
            return cipher;
        }

        @Override
        public CipherTransformer getInstance(Map<String, Object> data) {
            return new TestRangeTransformer(data);
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public String getHelpText() {
            return "test";
        }

        private void applyRowLabels(FormlyForm form) {
            makeRowBased(form);
        }
    }
}
