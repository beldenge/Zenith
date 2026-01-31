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

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VigenerePlaintextTransformerTest {
    @Test
    public void given_validInput_when_transform_then_returnsExpectedValue() {
        Map<String, Object> data = new HashMap<>();
        data.put(AbstractVigenerePlaintextTransformer.KEY, "lemon");

        VigenerePlaintextTransformer transformer = new VigenerePlaintextTransformer(data);

        String transformed = transformer.transform("attackatdawn");

        assertEquals("lxfopvefrnhr", transformed);
    }

    @Test
    public void given_validInput_when_transformKryptos1_then_returnsExpectedValue() {
        Map<String, Object> data = new HashMap<>();
        data.put(AbstractVigenerePlaintextTransformer.KEY, "palimpsest");
        data.put(AbstractVigenerePlaintextTransformer.VIGENERE_SQUARE, "KRYPTOSABCDEFGHIJLMNQUVWXZRYPTOSABCDEFGHIJLMNQUVWXZKYPTOSABCDEFGHIJLMNQUVWXZKRPTOSABCDEFGHIJLMNQUVWXZKRYTOSABCDEFGHIJLMNQUVWXZKRYPOSABCDEFGHIJLMNQUVWXZKRYPTSABCDEFGHIJLMNQUVWXZKRYPTOABCDEFGHIJLMNQUVWXZKRYPTOSBCDEFGHIJLMNQUVWXZKRYPTOSACDEFGHIJLMNQUVWXZKRYPTOSABDEFGHIJLMNQUVWXZKRYPTOSABCEFGHIJLMNQUVWXZKRYPTOSABCDFGHIJLMNQUVWXZKRYPTOSABCDEGHIJLMNQUVWXZKRYPTOSABCDEFHIJLMNQUVWXZKRYPTOSABCDEFGIJLMNQUVWXZKRYPTOSABCDEFGHJLMNQUVWXZKRYPTOSABCDEFGHILMNQUVWXZKRYPTOSABCDEFGHIJMNQUVWXZKRYPTOSABCDEFGHIJLNQUVWXZKRYPTOSABCDEFGHIJLMQUVWXZKRYPTOSABCDEFGHIJLMNUVWXZKRYPTOSABCDEFGHIJLMNQVWXZKRYPTOSABCDEFGHIJLMNQUWXZKRYPTOSABCDEFGHIJLMNQUVXZKRYPTOSABCDEFGHIJLMNQUVWZKRYPTOSABCDEFGHIJLMNQUVWX");

        VigenerePlaintextTransformer transformer = new VigenerePlaintextTransformer(data);

        String transformed = transformer.transform("betweensubtleshadingandtheabsenceoflightliesthenuanceofiqlusion");

        assertEquals("emufphzlrfaxyusdjkzldkrnshgnfivjyqtquxqbqvyuvlltrevjyqtmkyrdmfd", transformed);
    }

    @Test
    public void given_bothTransformers_when_bothAreUsed_then_eachWorksIndependently() {
        // This test verifies the bug fix for the static vigenereSquare field.
        // When the field was static, creating both transformers would cause
        // the second one to overwrite the first one's vigenere square, corrupting
        // the encryption/decryption results. Now each instance has its own square.
        Map<String, Object> data = new HashMap<>();
        data.put(AbstractVigenerePlaintextTransformer.KEY, "lemon");

        // Create the wrap (encrypt) transformer
        VigenerePlaintextTransformer wrapTransformer = new VigenerePlaintextTransformer(data);

        // Create the unwrap (decrypt) transformer - this would corrupt the static field before the fix
        UnwrapVigenerePlaintextTransformer unwrapTransformer = new UnwrapVigenerePlaintextTransformer(data);

        // Now verify BOTH transformers still work correctly
        String originalPlaintext = "attackatdawn";

        // Encrypt with the wrap transformer
        String encrypted = wrapTransformer.transform(originalPlaintext);
        assertEquals("lxfopvefrnhr", encrypted);

        // Decrypt with the unwrap transformer
        String decrypted = unwrapTransformer.transform(encrypted);
        assertEquals(originalPlaintext, decrypted);

        // Also verify the wrap transformer STILL works (it would fail if the static field was corrupted)
        String encryptedAgain = wrapTransformer.transform(originalPlaintext);
        assertEquals("lxfopvefrnhr", encryptedAgain);
    }
}
