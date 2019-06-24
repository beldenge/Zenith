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

package com.ciphertool.zenith.math.selection;

public class BinaryRouletteNode {
    private Double			value;
    private int					index;
    private BinaryRouletteNode	lessThan;
    private BinaryRouletteNode	greaterThan;

    /**
     * @param index
     *            the index to set
     *
     * @param value
     *            the value to set
     */
    public BinaryRouletteNode(int index, Double value) {
        this.index = index;
        this.value = value;
    }

    /**
     * @return the value
     */
    public Double getValue() {
        return value;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the lessThan
     */
    public BinaryRouletteNode getLessThan() {
        return lessThan;
    }

    /**
     * @param lessThan
     *            the lessThan to set
     */
    public void setLessThan(BinaryRouletteNode lessThan) {
        this.lessThan = lessThan;
    }

    /**
     * @return the greaterThan
     */
    public BinaryRouletteNode getGreaterThan() {
        return greaterThan;
    }

    /**
     * @param greaterThan
     *            the greaterThan to set
     */
    public void setGreaterThan(BinaryRouletteNode greaterThan) {
        this.greaterThan = greaterThan;
    }
}
