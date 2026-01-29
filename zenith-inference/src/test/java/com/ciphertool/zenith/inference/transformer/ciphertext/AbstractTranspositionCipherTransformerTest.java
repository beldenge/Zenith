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
import com.ciphertool.zenith.inference.entities.Ciphertext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractTranspositionCipherTransformerTest {
    @Test
    public void testNullKeyDefaultsToNoOp() {
        TestTranspositionTransformer transformer = new TestTranspositionTransformer(new HashMap<>());

        Cipher cipher = buildCipher(2, 2, "a", "b", "c", "d");

        assertNull(transformer.getTranspositionKey());
        assertSame(cipher, transformer.transform(cipher));
    }

    @Test
    public void testGetIndicesForTranspositionKey() {
        Map<String, Object> data = new HashMap<>();
        data.put(AbstractTranspositionCipherTransformer.KEY, "bca");

        TestTranspositionTransformer transformer = new TestTranspositionTransformer(data);

        assertEquals(List.of(1, 2, 0), transformer.getTranspositionKey());
    }

    @Test
    public void testTransformRejectsKeyLengthOutOfRange() {
        Map<String, Object> data = new HashMap<>();
        data.put(AbstractTranspositionCipherTransformer.KEY, "abcd");

        TestTranspositionTransformer transformer = new TestTranspositionTransformer(data);
        Cipher cipher = buildCipher(2, 2, "a", "b", "c", "d");

        assertThrows(IllegalArgumentException.class, () -> transformer.transform(cipher));
    }

    private static Cipher buildCipher(int rows, int columns, String... values) {
        Cipher cipher = new Cipher("test", rows, columns);
        for (String value : values) {
            cipher.addCiphertextCharacter(new Ciphertext(value));
        }
        return cipher;
    }

    private static final class TestTranspositionTransformer extends AbstractTranspositionCipherTransformer {
        private TestTranspositionTransformer(Map<String, Object> data) {
            super(data);
        }

        @Override
        protected Cipher unwrap(Cipher cipher, List<Integer> columnIndices) {
            return cipher;
        }

        @Override
        public CipherTransformer getInstance(Map<String, Object> data) {
            return new TestTranspositionTransformer(data);
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public String getHelpText() {
            return "test";
        }

        private List<Integer> getTranspositionKey() {
            return transpositionKey;
        }
    }
}
