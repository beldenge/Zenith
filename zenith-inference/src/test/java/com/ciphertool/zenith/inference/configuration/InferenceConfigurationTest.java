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

package com.ciphertool.zenith.inference.configuration;

import com.ciphertool.zenith.inference.entities.FormComponentDto;
import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.config.ApplicationConfiguration;
import com.ciphertool.zenith.inference.entities.config.CipherConfiguration;
import com.ciphertool.zenith.inference.transformer.ciphertext.TransformationStep;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InferenceConfigurationTest {
    @Test
    public void testPlaintextTransformationStepsMatchesByName() {
        InferenceConfiguration configuration = new InferenceConfiguration();

        ApplicationConfiguration appConfig = buildAppConfig("cipher", "Stub", Map.of("key", "value"));

        List<TransformationStep> steps = configuration.plaintextTransformationSteps(appConfig, List.of(new StubPlaintextTransformer()));

        assertEquals(1, steps.size());
        assertEquals("Stub", steps.get(0).getTransformerName());
        assertEquals("value", steps.get(0).getData().get("key"));
    }

    @Test
    public void testPlaintextTransformationStepsUnknownThrows() {
        InferenceConfiguration configuration = new InferenceConfiguration();

        ApplicationConfiguration appConfig = buildAppConfig("cipher", "Missing", Map.of());

        assertThrows(IllegalArgumentException.class, () -> configuration.plaintextTransformationSteps(appConfig, List.of(new StubPlaintextTransformer())));
    }

    private static ApplicationConfiguration buildAppConfig(String cipherName, String transformerName, Map<String, Object> model) {
        ApplicationConfiguration appConfig = new ApplicationConfiguration();
        appConfig.setSelectedCipher(cipherName);

        FormlyForm form = new FormlyForm();
        form.getModel().putAll(model);

        FormComponentDto dto = new FormComponentDto();
        dto.setName(transformerName);
        dto.setForm(form);

        CipherConfiguration cipherConfiguration = new CipherConfiguration();
        cipherConfiguration.setCipherName(cipherName);
        cipherConfiguration.setAppliedPlaintextTransformers(List.of(dto));

        appConfig.setCipherConfigurations(List.of(cipherConfiguration));

        return appConfig;
    }

    private static final class StubPlaintextTransformer implements PlaintextTransformer {
        @Override
        public String transform(String plaintext) {
            return plaintext;
        }

        @Override
        public PlaintextTransformer getInstance(Map<String, Object> data) {
            return new StubPlaintextTransformer();
        }

        @Override
        public com.ciphertool.zenith.inference.entities.FormlyForm getForm() {
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
