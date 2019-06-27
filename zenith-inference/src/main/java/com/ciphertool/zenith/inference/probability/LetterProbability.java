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

package com.ciphertool.zenith.inference.probability;

public class LetterProbability implements Probability<Character>, Comparable<LetterProbability> {
    private Character letter;
    private Double probability;

    /**
     * @param letter
     *            the letter
     * @param probability
     *            the probability
     */
    public LetterProbability(Character letter, Double probability) {
        this.letter = letter;
        this.probability = probability;
    }

    @Override
    public Character getValue() {
        return letter;
    }

    @Override
    public Double getProbability() {
        return probability;
    }

    @Override
    public int compareTo(LetterProbability other) {
        return this.probability.compareTo(other.probability);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((letter == null) ? 0 : letter.hashCode());
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
        if (!(obj instanceof LetterProbability)) {
            return false;
        }
        LetterProbability other = (LetterProbability) obj;
        if (letter == null) {
            if (other.letter != null) {
                return false;
            }
        } else if (!letter.equals(other.letter)) {
            return false;
        }
        return true;
    }
}
