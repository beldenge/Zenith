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

import com.ciphertool.zenith.inference.util.LetterUtils;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@NoArgsConstructor
@Component
public class OneTimePadPlaintextTransformer extends AbstractOneTimePadPlaintextTransformer {
    public OneTimePadPlaintextTransformer(Map<String, Object> data) {
        super(data);
    }

    @Override
    public String transform(String plaintext) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < plaintext.length(); i ++) {
            int sum = LetterUtils.charToOrdinal(plaintext.charAt(i)) + LetterUtils.charToOrdinal(key.charAt(i % key.length()));

            sb.append(LetterUtils.ordinalToChar(sum % LetterUtils.NUMBER_OF_LETTERS));
        }

        return sb.toString();
    }

    @Override
    public PlaintextTransformer getInstance(Map<String, Object> data) {
        return new OneTimePadPlaintextTransformer(data);
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
