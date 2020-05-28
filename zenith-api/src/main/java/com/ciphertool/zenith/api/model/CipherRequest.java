/*
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

package com.ciphertool.zenith.api.model;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class CipherRequest {
    @NotBlank
    private String name;

    @Min(0)
    private int rows;

    @Min(0)
    private int columns;

    @NotBlank
    private String ciphertext;

    @AssertTrue(message = "The ciphertext length must match the product of rows and columns.")
    public boolean isLengthValid() {
        return (rows * columns) == ciphertext.split(" ").length;
    }

    public Cipher asCipher() {
        Cipher cipher = new Cipher(this.name, this.rows, this.columns);

        String[] split = this.ciphertext.split(" ");

        for (int i = 0; i < split.length; i ++) {
            cipher.addCiphertextCharacter(new Ciphertext(i, split[i]));
        }

        return cipher;
    }
}
