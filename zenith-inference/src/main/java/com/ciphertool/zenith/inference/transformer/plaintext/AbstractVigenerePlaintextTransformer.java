/**
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

import com.ciphertool.zenith.model.LanguageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractVigenerePlaintextTransformer implements PlaintextTransformer {
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected static char[][] vigenereSquare = new char[26][26];

    private static char[] letters = LanguageConstants.LOWERCASE_LETTERS;

    static {
        for (int i = 0; i < 26; i ++) {
            for (int j = 0; j < 26; j ++) {
                vigenereSquare[i][j] = letters[(i + j) % 26];
            }
        }
    }

    @Value("${vigenere-transformer.key}")
    protected String key;
}
