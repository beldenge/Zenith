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

import java.math.BigDecimal;
import java.util.Map;

public class EvaluationResults {
	private BigDecimal	probability;
	private Float	logProbability;
	private Map<String, EvaluationResults> distribution;

	public EvaluationResults(BigDecimal probability, Float logProbability, Map<String, EvaluationResults> distribution) {
		this.probability = probability;
		this.logProbability = logProbability;
		this.distribution = distribution;
	}

	public EvaluationResults(BigDecimal probability, Float logProbability) {
		this.probability = probability;
		this.logProbability = logProbability;
	}

	public EvaluationResults(Float probability, Float logProbability) {
		this.probability = BigDecimal.valueOf(probability);
		this.logProbability = logProbability;
	}

	/**
	 * @return the probability
	 */
	public BigDecimal getProbability() {
		return probability;
	}

	/**
	 * @param probability
	 *            the probability to set
	 */
	public void setProbability(BigDecimal probability) {
		this.probability = probability;
	}

	/**
	 * @return the logProbability
	 */
	public Float getLogProbability() {
		return logProbability;
	}

	/**
	 * @param logProbability
	 *            the logProbability to set
	 */
	public void setLogProbability(Float logProbability) {
		this.logProbability = logProbability;
	}

	public Map<String, EvaluationResults> getDistribution() {
		return distribution;
	}

	public void setDistribution(Map<String, EvaluationResults> distribution) {
		this.distribution = distribution;
	}

	@Override
	public String toString() {
		return "EvaluationResults{" +
				"probability=" + probability +
				", logProbability=" + logProbability +
				", distribution=" + distribution +
				'}';
	}
}
