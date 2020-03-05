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

package com.ciphertool.zenith.inference.entities;

import java.util.HashMap;
import java.util.Map;

public class CipherJson {
    private String name;

    private int columns;

    private int rows;

    private String ciphertext;

    private boolean readOnly;

    private Map<String, String> knownSolutionKey = new HashMap<>();

    public CipherJson() {
    }

    public CipherJson(String name, int rows, int columns) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
    }

    public CipherJson(Cipher cipher) {
        this(cipher.getName(), cipher.getRows(), cipher.getColumns());

        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (Ciphertext ciphertext : cipher.getCiphertextCharacters()) {
            if (!first) {
                sb.append(" ");
            }

            first = false;

            sb.append(ciphertext.getValue());
        }

        ciphertext = sb.toString();

        if (cipher.hasKnownSolution()) {
            for (Map.Entry<String, String> entry : cipher.getKnownSolutionKey().entrySet()) {
                knownSolutionKey.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Map<String, String> getKnownSolutionKey() {
        return knownSolutionKey;
    }

    public void setKnownSolutionKey(Map<String, String> knownSolutionKey) {
        this.knownSolutionKey = knownSolutionKey;
    }
}
