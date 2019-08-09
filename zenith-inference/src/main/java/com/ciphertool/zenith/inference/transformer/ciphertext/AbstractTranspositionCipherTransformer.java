package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.model.LanguageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractTranspositionCipherTransformer implements CipherTransformer {
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Value("${decipherment.transposition.column-key:#{null}}")
    protected List<Integer> transpositionKey;

    @Value("${decipherment.transposition.column-key-string:#{null}}")
    protected String transpositionKeyString;

    @Value("${decipherment.transposition.iterations:1}")
    protected int transpositionIterations;

    @PostConstruct
    public void init() {
        if (transpositionKeyString != null && !transpositionKeyString.isEmpty()) {
            transpositionKey = getIndicesForTranspositionKey();
        }

        if (transpositionIterations <= 0) {
            transpositionIterations = 1;
        }

        List<Integer> toCheck = new ArrayList<>(transpositionKey);

        toCheck.sort(Comparator.comparing(Integer::intValue));

        for (int i = 0; i < toCheck.size(); i++) {
            if (toCheck.get(i) != i) {
                throw new IllegalArgumentException("The transposition column key indices must be zero-based with no gaps or duplicates.");
            }
        }
    }

    @Override
    public Cipher transform(Cipher cipher) {
        if (transpositionKey == null || transpositionKey.isEmpty()) {
            log.debug("{} performing no-op since the transposition key is unspecified.", getClass().getName());
            return cipher;
        }

        if (transpositionKey.size() < 2 || transpositionKey.size() >= cipher.length()) {
            throw new IllegalArgumentException("The transposition key length of " + transpositionKeyString.length()
                    + " must be greater than one and less than the cipher length of " + cipher.length() + ".");
        }

        return unwrap(cipher, transpositionKey);
    }

    protected abstract Cipher unwrap(Cipher cipher, List<Integer> columnIndices);

    public Cipher transform(Cipher cipher, List<Integer> columnIndices) {
        if (columnIndices.size() < 2 || columnIndices.size() >= cipher.length()) {
            throw new IllegalArgumentException("The transposition key length of " + columnIndices.size()
                    + " must be greater than one and less than the cipher length of " + cipher.length() + ".");
        }

        return unwrap(cipher, columnIndices);
    }

    protected List<Integer> getIndicesForTranspositionKey() {
        int next = 0;
        Integer[] columnIndices = new Integer[transpositionKeyString.length()];

        for (int i = 0; i < LanguageConstants.LOWERCASE_LETTERS.size(); i++) {
            char letter = LanguageConstants.LOWERCASE_LETTERS.get(i);

            for (int j = 0; j < transpositionKeyString.length(); j++) {
                if (transpositionKeyString.toLowerCase().charAt(j) == letter) {
                    columnIndices[j] = next;
                    next++;
                }
            }
        }

        return Arrays.asList(columnIndices);
    }
}
