/**
 * Copyright 2017-2019 George Belden
 * <p>
 * This file is part of Zenith.
 * <p>
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.inference.transformer;

import com.ciphertool.zenith.inference.entities.Cipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FourSquareZPatternCipherTransformer implements CipherTransformer {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public Cipher transform(Cipher cipher) {
        if (cipher.length() % 4 != 0 || cipher.getRows() % 2 != 0) {
            log.error("Unable to perform four square transformation as the cipher length must be divisible by 4, and " +
                    "the rows must be divisible by 2, but instead the length was {} and the rows were {}.  Returning " +
                    "original cipher.", cipher.length(), cipher.getRows());
            throw new IllegalArgumentException("Unable to perform four square transformation as the cipher length " +
                    "must be divisible by 4, and the rows must be divisible by 2");
        }

        Cipher transformed = cipher.clone();

        int l = 0;
        int i = 0;
        while (i < cipher.length() - cipher.getColumns() - 1) {
            transformed.replaceCiphertextCharacter(l, cipher.getCiphertextCharacters().get(i).clone());
            l++;

            if ((i + 1) % cipher.getColumns() == 0) {
                transformed.replaceCiphertextCharacter(l, cipher.getCiphertextCharacters().get(i + cipher.getColumns() + 1).clone());
            } else {
                transformed.replaceCiphertextCharacter(l, cipher.getCiphertextCharacters().get(i + 1).clone());
            }
            l++;

            transformed.replaceCiphertextCharacter(l, cipher.getCiphertextCharacters().get(i + cipher.getColumns()).clone());
            l++;

            if ((i + 1) % cipher.getColumns() == 0) {
                transformed.replaceCiphertextCharacter(l, cipher.getCiphertextCharacters().get(i + (cipher.getColumns() * 2) + 1).clone());
            } else {
                transformed.replaceCiphertextCharacter(l, cipher.getCiphertextCharacters().get(i + cipher.getColumns() + 1).clone());
            }
            l++;

            if ((i + 1) % cipher.getColumns() == 0 || (i + 2) % cipher.getColumns() == 0) {
                i += cipher.getColumns();
            }

            i += 2;
        }

        return transformed;
    }
}
