/**
 * Copyright 2017 George Belden
 * 
 * This file is part of Zenith.
 * 
 * Zenith is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Zenith is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.model.markov;

import java.math.BigDecimal;

import org.nevec.rjm.BigDecimalMath;

public class TerminalInfo {
	private int			level	= 0;
	private long		count	= 0L;
	private BigDecimal	probability;
	private BigDecimal	logProbability;
	private BigDecimal	conditionalProbability;

	/**
	 * Default no-args constructor
	 */
	public TerminalInfo() {
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public TerminalInfo(int level) {
		this.level = level;
	}

	/**
	 * @param level
	 *            the level to set
	 * @param count
	 *            the count to set
	 */
	public TerminalInfo(int level, long count) {
		this.count = count;
	}

	public void increment() {
		this.count += 1L;
	}

	/**
	 * @return the count
	 */
	public long getCount() {
		return this.count;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return this.level;
	}

	/**
	 * @return the probability
	 */
	public BigDecimal getProbability() {
		return this.probability;
	}

	/**
	 * @return the log probability (with logarithm base of 10)
	 */
	public BigDecimal getLogProbability() {
		if (probability == null) {
			return BigDecimal.ZERO;
		}

		if (logProbability == null) {
			this.logProbability = BigDecimalMath.log(this.probability);
		}

		return this.logProbability;
	}

	/**
	 * All current usages of this method are thread-safe, but since it's used in a multi-threaded way, this is a
	 * defensive measure in case future usage changes are not thread-safe.
	 * 
	 * @param probability
	 *            the probability to set
	 */
	public synchronized void setProbability(BigDecimal probability) {
		this.probability = probability;
	}

	/**
	 * @return the conditionalProbability
	 */
	public BigDecimal getConditionalProbability() {
		return conditionalProbability;
	}

	/**
	 * All current usages of this method are thread-safe, but since it's used in a multi-threaded way, this is a
	 * defensive measure in case future usage changes are not thread-safe.
	 * 
	 * @param conditionalProbability
	 *            the conditionalProbability to set
	 */
	public synchronized void setConditionalProbability(BigDecimal conditionalProbability) {
		this.conditionalProbability = conditionalProbability;
	}
}
