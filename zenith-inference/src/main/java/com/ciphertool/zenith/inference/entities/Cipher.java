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

package com.ciphertool.zenith.inference.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class Cipher {
    @NotBlank
    private String name;

    @Min(1)
    private int columns;

    @Min(1)
    private int rows;

    private boolean readOnly;

    @NotEmpty
    private List<String> ciphertext = new ArrayList<>();

    private Map<String, String> knownSolutionKey = new HashMap<>();

    @JsonIgnore
    private List<Ciphertext> ciphertextCharacters = new ArrayList<>();

    @JsonIgnore
    private Map<String, int[]> cipherSymbolIndicesMap = new HashMap<>();

    public Cipher(String name, int rows, int columns) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
    }

    public Cipher(String name, int rows, int columns, boolean readOnly) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.readOnly = readOnly;
    }

    public void setCiphertext(List<String> ciphertext) {
        this.ciphertext = (ciphertext == null) ? new ArrayList<>() : ciphertext;
        this.ciphertextCharacters.clear();
        this.cipherSymbolIndicesMap.clear();

        if (CollectionUtils.isNotEmpty(ciphertext)) {
            for (String next : ciphertext) {
                ciphertextCharacters.add(new Ciphertext(next));
            }
        }
    }

    public int length() {
        return rows * columns;
    }

    public boolean hasKnownSolution() {
        return !knownSolutionKey.isEmpty();
    }

    public Map<String, int[]> getCipherSymbolIndicesMap() {
        if (!cipherSymbolIndicesMap.isEmpty()) {
            return cipherSymbolIndicesMap;
        }

        for (Ciphertext ciphertextCharacter : ciphertextCharacters) {
            if (cipherSymbolIndicesMap.containsKey(ciphertextCharacter.getValue())) {
                continue;
            }

            String symbol = ciphertextCharacter.getValue();

            int count = (int) ciphertextCharacters.stream()
                    .map(Ciphertext::getValue)
                    .filter(value -> value.equals(symbol))
                    .count();

            cipherSymbolIndicesMap.put(symbol, new int[count]);

            int i = 0;
            for (int j = 0; j < ciphertextCharacters.size(); j ++) {
                Ciphertext ciphertextMatch = ciphertextCharacters.get(j);
                if (ciphertextMatch.getValue().equals(symbol)) {
                    cipherSymbolIndicesMap.get(symbol)[i] = j;
                    i++;
                }
            }
        }

        return cipherSymbolIndicesMap;
    }

    public List<Ciphertext> getCiphertextCharacters() {
        return Collections.unmodifiableList(ciphertextCharacters);
    }

    public void addCiphertextCharacter(Ciphertext ciphertext) {
        this.ciphertextCharacters.add(ciphertext);
        this.ciphertext.add(ciphertext.getValue());
        this.cipherSymbolIndicesMap.clear();
    }

    public void removeCiphertextCharacter(int i) {
        this.ciphertextCharacters.remove(i);
        this.ciphertext.remove(i);
        this.cipherSymbolIndicesMap.clear();
    }

    public void replaceCiphertextCharacter(int index, Ciphertext ciphertext) {
        Ciphertext toReplace = this.ciphertextCharacters.get(index);

        toReplace.setValue(ciphertext.getValue());
        this.ciphertextCharacters.set(index, ciphertext);
        this.ciphertext.set(index, ciphertext.getValue());
        this.cipherSymbolIndicesMap.clear();
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

    public Cipher clone() {
        Cipher cloned = new Cipher(this.name, this.rows, this.columns);
        cloned.readOnly = this.readOnly;

        for (Ciphertext ciphertext : this.ciphertextCharacters) {
            cloned.addCiphertextCharacter(ciphertext.clone());
        }

        for (Map.Entry<String, String> entry : this.knownSolutionKey.entrySet()) {
            cloned.putKnownSolutionMapping(entry.getKey(), entry.getValue());
        }

        cloned.cipherSymbolIndicesMap = new HashMap<>();

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

        if (columns != other.columns) {
            return false;
        }

        if (rows != other.rows) {
            return false;
        }

        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }

        if (ciphertextCharacters == null) {
            if (other.ciphertextCharacters != null) {
                return false;
            }
        } else if (!ciphertextCharacters.equals(other.ciphertextCharacters)) {
            return false;
        }

        return true;
    }

    public String asSingleLineString() {
        StringBuilder sb = new StringBuilder();

        boolean first = true;

        for (Ciphertext ciphertext : ciphertextCharacters) {
            if (!first) {
                sb.append(" ");
            }

            first = false;

            sb.append(ciphertext.getValue());
        }

        return sb.toString();
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
