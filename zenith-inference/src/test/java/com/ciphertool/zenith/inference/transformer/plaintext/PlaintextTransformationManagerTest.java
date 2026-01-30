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

package com.ciphertool.zenith.inference.transformer.plaintext;

import com.ciphertool.zenith.inference.transformer.ciphertext.TransformationStep;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlaintextTransformationManagerTest {
    @Test
    public void given_missingInput_when_transformUnknownTransformerThrows_then_throwsIllegalArgumentException() throws Exception {
        PlaintextTransformationManager manager = new PlaintextTransformationManager();
        setField(manager, "plaintextTransformers", List.of(new NamedPlaintextTransformer("First")));
        manager.init();

        List<TransformationStep> steps = List.of(new TransformationStep("Missing", new HashMap<>()));

        assertThrows(IllegalArgumentException.class, () -> manager.transform("AbC", steps));
    }

    @Test
    public void given_validInput_when_transformAppliesInOrderLowercasesFirst_then_returnsExpectedValue() throws Exception {
        PlaintextTransformationManager manager = new PlaintextTransformationManager();
        setField(manager, "plaintextTransformers", List.of(new NamedPlaintextTransformer("First"), new NamedPlaintextTransformer("Second")));
        manager.init();

        List<TransformationStep> steps = List.of(
                new TransformationStep("First", Map.of("suffix", "1")),
                new TransformationStep("Second", Map.of("suffix", "2"))
        );

        String transformed = manager.transform("AbC", steps);

        assertEquals("abc|First:1|Second:2", transformed);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static final class NamedPlaintextTransformer implements PlaintextTransformer {
        private final String name;
        private final String suffix;

        private NamedPlaintextTransformer(String name) {
            this(name, "");
        }

        private NamedPlaintextTransformer(String name, String suffix) {
            this.name = name;
            this.suffix = suffix;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String transform(String plaintext) {
            return plaintext + "|" + name + ":" + suffix;
        }

        @Override
        public PlaintextTransformer getInstance(Map<String, Object> data) {
            return new NamedPlaintextTransformer(name, (String) data.get("suffix"));
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public String getHelpText() {
            return "test";
        }

        @Override
        public com.ciphertool.zenith.inference.entities.FormlyForm getForm() {
            return new com.ciphertool.zenith.inference.entities.FormlyForm();
        }
    }
}
