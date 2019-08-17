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

package com.ciphertool.zenith.inference.entities;

import java.util.*;

public class Cipher {
    private String name;

    private int columns;

    private int rows;

    private List<Ciphertext> ciphertextCharacters = new ArrayList<>();

    private Map<String, String> knownSolutionKey = new HashMap<>();

    public Cipher() {
    }

    public Cipher(String name, int rows, int columns) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
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

    public List<Ciphertext> getCiphertextCharacters() {
        return Collections.unmodifiableList(ciphertextCharacters);
    }

    public void addCiphertextCharacter(Ciphertext ciphertext) {
        this.ciphertextCharacters.add(ciphertext);
    }

    public void removeCiphertextCharacter(Ciphertext ciphertext) {
        this.ciphertextCharacters.remove(ciphertext);
    }

    public void replaceCiphertextCharacter(int index, Ciphertext ciphertext) {
        this.ciphertextCharacters.remove(index);
        this.ciphertextCharacters.add(index, ciphertext);
        ciphertext.setCiphertextId(index);
    }

    public Map<String, String> getKnownSolutionKey() {
        return Collections.unmodifiableMap(knownSolutionKey);
    }

    public void putKnownSolutionMapping(String key, String value) {
        knownSolutionKey.put(key, value);
    }

    public void clearKnownSolutionKey() {
        this.knownSolutionKey = new HashMap<>();
    }

    public int length() {
        return rows * columns;
    }

    /**
     * @return the hasKnownSolution
     */
    public boolean hasKnownSolution() {
        return !knownSolutionKey.isEmpty();
    }

    public Cipher clone() {
        Cipher cloned = new Cipher(this.name, this.rows, this.columns);

        for (Ciphertext ciphertext : this.ciphertextCharacters) {
            cloned.addCiphertextCharacter(ciphertext.clone());
        }

        for (Map.Entry<String, String> entry : this.knownSolutionKey.entrySet()) {
            cloned.putKnownSolutionMapping(entry.getKey(), entry.getValue());
        }

        return cloned;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ciphertextCharacters == null) ? 0 : ciphertextCharacters.hashCode());
        result = prime * result + columns;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + rows;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Cipher other = (Cipher) obj;
        if (ciphertextCharacters == null) {
            if (other.ciphertextCharacters != null) {
                return false;
            }
        } else if (!ciphertextCharacters.equals(other.ciphertextCharacters)) {
            return false;
        }
        if (columns != other.columns) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (rows != other.rows) {
            return false;
        }
        return true;
    }

    /*
     * Prints the properties of the cipher and then outputs the entire ciphertext list in block format.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cipher [name=" + name + ", columns=" + columns + ", rows=" + rows + ", hasKnownSolution="
                + hasKnownSolution() + ", ciphertextCharacters=" + ciphertextCharacters + "]\n");

        int maxLength = this.ciphertextCharacters.stream()
                .map(Ciphertext::getValue)
                .map(String::length)
                .max(Comparator.comparing(Integer::intValue))
                .orElse(0);

        int actualSize = this.ciphertextCharacters.size();
        for (int i = 0; i < actualSize; i++) {
            String nextValue = this.ciphertextCharacters.get(i).getValue();

            sb.append(" ");
            sb.append(nextValue);
            sb.append(" ");

            for (int j = 0; j < (maxLength - nextValue.length()); j++) {
                sb.append(" ");
            }

            /*
             * Print a newline if we are at the end of the row. Add 1 to the index so the modulus function doesn't
             * break.
             */
            if (((i + 1) % this.columns) == 0) {
                sb.append("\n");
            } else {
                sb.append(" ");
            }
        }

        return sb.toString();
    }
}
