package com.ciphertool.zenith.inference.statistics;

import com.ciphertool.zenith.inference.entities.Cipher;
import org.springframework.stereotype.Component;

@Component
public class CiphertextMultiplicityEvaluator {
    private long uniqueCiphertextCharacters;
    private Cipher initialized = null;

    public void init(Cipher cipher) {
        uniqueCiphertextCharacters = cipher.getCiphertextCharacters().stream()
                .map(ciphertext -> ciphertext.getValue())
                .distinct()
                .count();
    }

    public float evaluate(Cipher cipher) {
        if (initialized == null || initialized != cipher) {
            init(cipher);
        }

        return ((float) uniqueCiphertextCharacters / (float) cipher.length()) * 100f;
    }
}
