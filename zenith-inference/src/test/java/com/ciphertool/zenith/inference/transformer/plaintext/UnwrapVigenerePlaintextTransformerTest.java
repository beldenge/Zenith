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

import static org.junit.Assert.assertEquals;

public class UnwrapVigenerePlaintextTransformerTest {
    @Test
    public void testTransform() {
        Map<String, Object> data = new HashMap<>();
        data.put(AbstractVigenerePlaintextTransformer.KEY, "lemon");

        UnwrapVigenerePlaintextTransformer transformer = new UnwrapVigenerePlaintextTransformer(data);

        String transformed = transformer.transform("lxfopvefrnhr");

        assertEquals("attackatdawn", transformed);
    }

    @Test
    public void testTransform_Kryptos1() {
        Map<String, Object> data = new HashMap<>();
        data.put(AbstractVigenerePlaintextTransformer.KEY, "palimpsest");
        data.put(AbstractVigenerePlaintextTransformer.VIGENERE_SQUARE, "KRYPTOSABCDEFGHIJLMNQUVWXZRYPTOSABCDEFGHIJLMNQUVWXZKYPTOSABCDEFGHIJLMNQUVWXZKRPTOSABCDEFGHIJLMNQUVWXZKRYTOSABCDEFGHIJLMNQUVWXZKRYPOSABCDEFGHIJLMNQUVWXZKRYPTSABCDEFGHIJLMNQUVWXZKRYPTOABCDEFGHIJLMNQUVWXZKRYPTOSBCDEFGHIJLMNQUVWXZKRYPTOSACDEFGHIJLMNQUVWXZKRYPTOSABDEFGHIJLMNQUVWXZKRYPTOSABCEFGHIJLMNQUVWXZKRYPTOSABCDFGHIJLMNQUVWXZKRYPTOSABCDEGHIJLMNQUVWXZKRYPTOSABCDEFHIJLMNQUVWXZKRYPTOSABCDEFGIJLMNQUVWXZKRYPTOSABCDEFGHJLMNQUVWXZKRYPTOSABCDEFGHILMNQUVWXZKRYPTOSABCDEFGHIJMNQUVWXZKRYPTOSABCDEFGHIJLNQUVWXZKRYPTOSABCDEFGHIJLMQUVWXZKRYPTOSABCDEFGHIJLMNUVWXZKRYPTOSABCDEFGHIJLMNQVWXZKRYPTOSABCDEFGHIJLMNQUWXZKRYPTOSABCDEFGHIJLMNQUVXZKRYPTOSABCDEFGHIJLMNQUVWZKRYPTOSABCDEFGHIJLMNQUVWX");

        UnwrapVigenerePlaintextTransformer transformer = new UnwrapVigenerePlaintextTransformer(data);

        String transformed = transformer.transform("emufphzlrfaxyusdjkzldkrnshgnfivjyqtquxqbqvyuvlltrevjyqtmkyrdmfd");

        assertEquals("betweensubtleshadingandtheabsenceoflightliesthenuanceofiqlusion", transformed);
    }
}
