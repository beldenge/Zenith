package com.ciphertool.zenith.inference.transformer;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.model.ModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class TranspositionCipherTransformer implements CipherTransformer {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${decipherment.transposition.column-key:#{null}}")
    protected String transpositionKey;

    @Value("${decipherment.transposition.iterations:1}")
    protected int transpositionIterations;

    @Override
    public Cipher transform(Cipher cipher) {
        if (transpositionKey == null || transpositionKey.isEmpty()) {
            log.debug("{} performing no-op since the transposition key is unspecified.", getClass().getName());
            return cipher;
        }

        if (transpositionKey.length() < 2 || transpositionKey.length() >= cipher.length()) {
            throw new IllegalArgumentException("The transposition key length of " + transpositionKey.length()
                    + " must be greater than one and less than the cipher length of " + cipher.length() + ".");
        }

        List<Integer> columnIndices = getIndicesForTranspositionKey();

        log.info("Unwrapping transposition {} time{} using column key '{}' with indices {}.", transpositionIterations, (transpositionIterations > 1 ? "s" : ""), transpositionKey, columnIndices);

        int rows = cipher.length() / transpositionKey.length();

        Cipher transformed = cipher.clone();
        Cipher clone = cipher;
        for (int iter = 0; iter < transpositionIterations; iter ++) {
            int k = 0;

            for (int i = 0; i < transpositionKey.length(); i++) {
                for (int j = 0; j < rows; j++) {
                    transformed.replaceCiphertextCharacter((j * transpositionKey.length()) + columnIndices.indexOf(i), clone.getCiphertextCharacters().get(k).clone());
                    k++;
                }
            }

            clone = transformed.clone();
        }

        return transformed;
    }

    protected List<Integer> getIndicesForTranspositionKey() {
        int next = 0;
        Integer[] columnIndices = new Integer[transpositionKey.length()];

        for (int i = 0; i < ModelConstants.LOWERCASE_LETTERS.size(); i ++) {
            char letter = ModelConstants.LOWERCASE_LETTERS.get(i);

            for (int j = 0; j < transpositionKey.length(); j++) {
                if (transpositionKey.toLowerCase().charAt(j) == letter) {
                    columnIndices[j] = next;
                    next ++;
                }
            }
        }

        return Arrays.asList(columnIndices);
    }
}
