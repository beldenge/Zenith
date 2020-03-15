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

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class UnwrapFourSquarePlaintextTransformerTest {
    @Test
    public void testTransform() {
        Map<String, Object> data = new HashMap<>();
        data.put(AbstractFourSquarePlaintextTransformer.KEY_TOP_LEFT, "byfireacdghklmnopqstuvwxz");
        data.put(AbstractFourSquarePlaintextTransformer.KEY_TOP_RIGHT, "bygunacdefhiklmopqrstvwxz");
        data.put(AbstractFourSquarePlaintextTransformer.KEY_BOTTOM_LEFT, "byknifeacdghlmopqrstuvwxz");
        data.put(AbstractFourSquarePlaintextTransformer.KEY_BOTTOM_RIGHT, "byropeacdfghiklmnqstuvwxz");

        UnwrapFourSquarePlaintextTransformer transformer = new UnwrapFourSquarePlaintextTransformer(data);

        transformer.init();

        String transformed = transformer.transform("pofpopfqqgpcshcqqomthfkocortifacyakmikgmgm");

        assertEquals("thetomatoisaplantinthenightshadefamilyiiii", transformed);
    }
}
