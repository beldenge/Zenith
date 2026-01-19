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

package com.ciphertool.zenith.inference.transformer.ciphertext;

import com.ciphertool.zenith.inference.entities.Cipher;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@NoArgsConstructor
@Component
public class ReverseCipherTransformer extends AbstractRangeLimitedCipherTransformer {
    public ReverseCipherTransformer(Map<String, Object> data) {
        super(data);
    }

    @Override
    public Cipher transform(Cipher cipher) {
        Cipher transformed = cipher.clone();

        int start = 0;
        int end = cipher.length() - 1;

        if (rangeStart != null) {
            start = Math.max(rangeStart, 0);
        }

        if (rangeEnd != null) {
            end = Math.min(rangeEnd, cipher.length() - 1);
        }

        int j = end;
        for (int i = start; i <= end; i++) {
            transformed.replaceCiphertextCharacter(i, cipher.getCiphertextCharacters().get(j).clone());
            j--;
        }

        return transformed;
    }

    @Override
    public CipherTransformer getInstance(Map<String, Object> data) {
        return new ReverseCipherTransformer(data);
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public String getHelpText() {
        return "Reverses the ciphertext such that the last symbol becomes the first and vice versa";
    }
}
