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

package com.ciphertool.zenith.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CipherResponseItem {
    private String name;
    private int rows;
    private int columns;
    private boolean readOnly;
    private String ciphertext;
    private double multiplicity;
    private double entropy;
    private double indexOfCoincidence;
    private double chiSquared;
    private int bigramRepeats;
    private int cycleScore;

    public CipherResponseItem(String name, int rows, int columns, String ciphertext, boolean readOnly) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.ciphertext = ciphertext;
        this.readOnly = readOnly;
    }
}
