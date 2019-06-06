/**
 * Copyright 2017-2019 George Belden
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

package com.ciphertool.zenith.inference.transformer;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.model.ModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class TranspositionCipherTransformer implements CipherTransformer {
    private Logger log = LoggerFactory.getLogger(getClass());

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

        List<Integer> toCheck = new ArrayList<>(transpositionKey);

        toCheck.sort(Comparator.comparing(Integer::intValue));

        for (int i = 0; i < toCheck.size(); i ++) {
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

    @Override
    public Cipher transform(Cipher cipher, List<Integer> columnIndices) {
        if (columnIndices.size() < 2 || columnIndices.size() >= cipher.length()) {
            throw new IllegalArgumentException("The transposition key length of " + columnIndices.size()
                    + " must be greater than one and less than the cipher length of " + cipher.length() + ".");
        }

        return unwrap(cipher, columnIndices);
    }

    private Cipher unwrap(Cipher cipher, List<Integer> columnIndices) {
        log.debug("Unwrapping transposition {} time{} using column key '{}' with indices {}.", transpositionIterations, (transpositionIterations > 1 ? "s" : ""), transpositionKeyString, columnIndices);

        int rows = cipher.length() / columnIndices.size();

        Cipher transformed = cipher.clone();
        Cipher clone = cipher;
        for (int iter = 0; iter < transpositionIterations; iter ++) {
            int k = 0;

            for (int i = 0; i < columnIndices.size(); i++) {
                for (int j = 0; j < rows; j++) {
                    transformed.replaceCiphertextCharacter((j * columnIndices.size()) + columnIndices.indexOf(i), clone.getCiphertextCharacters().get(k).clone());
                    k++;
                }
            }

            clone = transformed.clone();
        }

        return transformed;
    }

    protected List<Integer> getIndicesForTranspositionKey() {
        int next = 0;
        Integer[] columnIndices = new Integer[transpositionKeyString.length()];

        for (int i = 0; i < ModelConstants.LOWERCASE_LETTERS.size(); i ++) {
            char letter = ModelConstants.LOWERCASE_LETTERS.get(i);

            for (int j = 0; j < transpositionKeyString.length(); j++) {
                if (transpositionKeyString.toLowerCase().charAt(j) == letter) {
                    columnIndices[j] = next;
                    next ++;
                }
            }
        }

        return Arrays.asList(columnIndices);
    }
}
