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

package com.ciphertool.zenith.inference.dto;

public class EvaluationResults {
	private Double	probability;
	private Double	logProbability;

	/**
	 * @param probability
	 * @param logProbability
	 */
	public EvaluationResults(Double probability, Double logProbability) {
		this.probability = probability;
		this.logProbability = logProbability;
	}

	/**
	 * @return the probability
	 */
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

	/**
	 * @return the logProbability
	 */
	public Double getLogProbability() {
		return logProbability;
	}

	/**
	 * @param logProbability
	 *            the logProbability to set
	 */
	public void setLogProbability(Double logProbability) {
		this.logProbability = logProbability;
	}

	@Override
	public String toString() {
		return "EvaluationResults [probability=" + probability + ", logProbability=" + logProbability + "]";
	}
}
