/**
 * Copyright 2017 George Belden
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

import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.math.probability.Probability;

public class SolutionProbability implements Probability<CipherSolution>, Comparable<SolutionProbability> {
	private CipherSolution	solution;
	private Double		probability;

	/**
	 * @param solution
	 *            the CipherSolution
	 * @param probability
	 *            the probability
	 */
	public SolutionProbability(CipherSolution solution, Double probability) {
		super();
		this.solution = solution;
		this.probability = probability;
	}

	/**
	 * @return the solution
	 */
	@Override
	public CipherSolution getValue() {
		return solution;
	}

	/**
	 * @return the probability
	 */
	@Override
	public Double getProbability() {
		return probability;
	}

	/**
	 * @param probability
	 *            the probability to set
	 */
	public void setProbability(Double probability) {
		this.probability = probability;
	}

	@Override
	public Double getLogProbability() {
		throw new UnsupportedOperationException("Method not implemented!");
	}

	@Override
	public int compareTo(SolutionProbability other) {
		return this.probability.compareTo(other.probability);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((solution == null) ? 0 : solution.hashCode());
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
		if (!(obj instanceof SolutionProbability)) {
			return false;
		}
		SolutionProbability other = (SolutionProbability) obj;
		if (solution == null) {
			if (other.solution != null) {
				return false;
			}
		} else if (!solution.equals(other.solution)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SolutionProbability [probability=" + probability + "]";
	}
}
