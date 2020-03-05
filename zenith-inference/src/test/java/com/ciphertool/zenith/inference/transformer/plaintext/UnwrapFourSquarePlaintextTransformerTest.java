/*
 * Copyright 2017-2020 George Belden
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

import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static junit.framework.TestCase.assertEquals;

public class UnwrapFourSquarePlaintextTransformerTest {
    @Test
    public void testTransform() {
        UnwrapFourSquarePlaintextTransformer transformer = new UnwrapFourSquarePlaintextTransformer();

        Field keyTopLeftField = ReflectionUtils.findField(FourSquarePlaintextTransformer.class, "keyTopLeft");
        ReflectionUtils.makeAccessible(keyTopLeftField);
        ReflectionUtils.setField(keyTopLeftField, transformer, "byfireacdghklmnopqstuvwxz");

        Field keyTopRightField = ReflectionUtils.findField(FourSquarePlaintextTransformer.class, "keyTopRight");
        ReflectionUtils.makeAccessible(keyTopRightField);
        ReflectionUtils.setField(keyTopRightField, transformer, "bygunacdefhiklmopqrstvwxz");

        Field keyBottomLeftField = ReflectionUtils.findField(FourSquarePlaintextTransformer.class, "keyBottomLeft");
        ReflectionUtils.makeAccessible(keyBottomLeftField);
        ReflectionUtils.setField(keyBottomLeftField, transformer, "byknifeacdghlmopqrstuvwxz");

        Field keyBottomRightField = ReflectionUtils.findField(FourSquarePlaintextTransformer.class, "keyBottomRight");
        ReflectionUtils.makeAccessible(keyBottomRightField);
        ReflectionUtils.setField(keyBottomRightField, transformer, "byropeacdfghiklmnqstuvwxz");

        transformer.init();

        String transformed = transformer.transform("pofpopfqqgpcshcqqomthfkocortifacyakmikgmgm");

        assertEquals("thetomatoisaplantinthenightshadefamilyiiii", transformed);
    }
}
