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

package com.ciphertool.zenith.genetic.algorithms.crossover;

/**
 * This class is a Data Transformation Object used to hold the index values needed during crossover. This allows these
 * values to be passed in methods and incremented by reference.
 */
public class CrossoverProgressDto {
	private int	firstChromosomeSequencePosition		= 0;
	private int	secondChromosomeSequencePosition	= 0;
	private int	firstChromosomeGeneIndex			= 0;
	private int	secondChromosomeGeneIndex			= 0;

	public CrossoverProgressDto() {
	}

	public void advanceFirstChromosomeSequencePositionBy(int amountToAdvance) {
		this.firstChromosomeSequencePosition += amountToAdvance;
	}

	public void advanceSecondChromosomeSequencePositionBy(int amountToAdvance) {
		this.secondChromosomeSequencePosition += amountToAdvance;
	}

	public void advanceFirstChromosomeGeneIndexBy(int amountToAdvance) {
		this.firstChromosomeGeneIndex += amountToAdvance;
	}

	public void advanceSecondChromosomeGeneIndexBy(int amountToAdvance) {
		this.secondChromosomeGeneIndex += amountToAdvance;
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
	 * @return the firstChromosomeGeneIndex
	 */
	public int getFirstChromosomeGeneIndex() {
		return firstChromosomeGeneIndex;
	}

	/**
	 * @return the secondChromosomeGeneIndex
	 */
	public int getSecondChromosomeGeneIndex() {
		return secondChromosomeGeneIndex;
	}
}