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

package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.transformer.TransformerInputType;
import org.springframework.stereotype.Component;

@Component
public class PeriodCipherTransformer implements CipherTransformer {
    private int period;

    public PeriodCipherTransformer() {}

    public PeriodCipherTransformer(int period) {
        this.period = period;
    }

    @Override
    public Cipher transform(Cipher cipher) {
        Cipher transformed = cipher.clone();

        int k = 0;

        for (int i = 0; i < period; i ++) {
            for (int j = 0; i + j < cipher.length(); j += period) {
                transformed.replaceCiphertextCharacter(k, cipher.getCiphertextCharacters().get(i + j).clone());
                k ++;
            }
        }

        return transformed;
    }

    @Override
    public TransformerInputType getInputType() {
        return TransformerInputType.NUMBER;
    }
}
