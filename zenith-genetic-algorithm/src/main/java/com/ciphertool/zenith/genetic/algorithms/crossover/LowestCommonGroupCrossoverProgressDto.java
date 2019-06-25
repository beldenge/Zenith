/**
 * Copyright 2017-2019 George Belden
 * <p>
 * This file is part of Zenith.
 * <p>
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.genetic.algorithms.crossover;

/**
 * This class is a Data Transformation Object used to hold the index values needed during crossover. This allows these
 * values to be passed in methods and incremented by reference.
 */
public class LowestCommonGroupCrossoverProgressDto {
    private int firstChromosomeSequencePosition = 0;
    private int secondChromosomeSequencePosition = 0;
    private int firstChromosomeBeginGeneIndex = 0;
    private int secondChromosomeBeginGeneIndex = 0;
    private int firstChromosomeEndGeneIndex = 0;
    private int secondChromosomeEndGeneIndex = 0;

    public LowestCommonGroupCrossoverProgressDto() {
    }

    public void advanceFirstChromosomeSequencePositionBy(int amountToAdvance) {
        this.firstChromosomeSequencePosition += amountToAdvance;
    }

    public void advanceSecondChromosomeSequencePositionBy(int amountToAdvance) {
        this.secondChromosomeSequencePosition += amountToAdvance;
    }

    public void advanceFirstChromosomeEndGeneIndexBy(int amountToAdvance) {
        this.firstChromosomeEndGeneIndex += amountToAdvance;
    }

    public void advanceSecondChromosomeEndGeneIndexBy(int amountToAdvance) {
        this.secondChromosomeEndGeneIndex += amountToAdvance;
    }

    /**
     * @param firstChromosomeBeginGeneIndex
     *            the firstChromosomeBeginGeneIndex to set
     */
    public void setFirstChromosomeBeginGeneIndex(int firstChromosomeBeginGeneIndex) {
        this.firstChromosomeBeginGeneIndex = firstChromosomeBeginGeneIndex;
    }

    /**
     * @param secondChromosomeBeginGeneIndex
     *            the secondChromosomeBeginGeneIndex to set
     */
    public void setSecondChromosomeBeginGeneIndex(int secondChromosomeBeginGeneIndex) {
        this.secondChromosomeBeginGeneIndex = secondChromosomeBeginGeneIndex;
    }

    /**
     * @param firstChromosomeSequencePosition
     *            the firstChromosomeSequencePosition to set
     */
    public void setFirstChromosomeSequencePosition(int firstChromosomeSequencePosition) {
        this.firstChromosomeSequencePosition = firstChromosomeSequencePosition;
    }

    /**
     * @param secondChromosomeSequencePosition
     *            the secondChromosomeSequencePosition to set
     */
    public void setSecondChromosomeSequencePosition(int secondChromosomeSequencePosition) {
        this.secondChromosomeSequencePosition = secondChromosomeSequencePosition;
    }

    /**
     * @return the firstChromosomeSequencePosition
     */
    public int getFirstChromosomeSequencePosition() {
        return firstChromosomeSequencePosition;
    }

    /**
     * @return the secondChromosomeSequencePosition
     */
    public int getSecondChromosomeSequencePosition() {
        return secondChromosomeSequencePosition;
    }

    /**
     * @return the firstChromosomeBeginGeneIndex
     */
    public int getFirstChromosomeBeginGeneIndex() {
        return firstChromosomeBeginGeneIndex;
    }

    /**
     * @return the secondChromosomeBeginGeneIndex
     */
    public int getSecondChromosomeBeginGeneIndex() {
        return secondChromosomeBeginGeneIndex;
    }

    /**
     * @return the firstChromosomeEndGeneIndex
     */
    public int getFirstChromosomeEndGeneIndex() {
        return firstChromosomeEndGeneIndex;
    }

    /**
     * @return the secondChromosomeEndGeneIndex
     */
    public int getSecondChromosomeEndGeneIndex() {
        return secondChromosomeEndGeneIndex;
    }
}
