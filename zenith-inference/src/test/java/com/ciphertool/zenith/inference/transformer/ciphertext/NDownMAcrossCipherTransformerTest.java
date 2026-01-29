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
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NDownMAcrossCipherTransformerTest {
    @Test
    public void testTransformSingleRowNoRange() {
        Map<String, Object> data = new HashMap<>();
        data.put(NDownMAcrossCipherTransformer.DOWN, 0);
        data.put(NDownMAcrossCipherTransformer.ACROSS, 1);

        NDownMAcrossCipherTransformer transformer = new NDownMAcrossCipherTransformer(data);

        Cipher cipher = new Cipher("test", 1, 4);
        cipher.setCiphertext(Arrays.asList("A", "B", "C", "D"));

        Cipher transformed = transformer.transform(cipher);

        assertEquals("A", transformed.getCiphertextCharacters().get(0).getValue());
        assertEquals("B", transformed.getCiphertextCharacters().get(1).getValue());
        assertEquals("C", transformed.getCiphertextCharacters().get(2).getValue());
        assertEquals("D", transformed.getCiphertextCharacters().get(3).getValue());
    }
}
