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

import java.math.BigDecimal;

public class WordProbability implements Probability<String> {
	private Integer		previous;
	private Integer		next;
	private BigDecimal	probability;
	private BigDecimal	logProbability;
	private String		value;

	/**
	 * @param previous
	 *            the previous word boundary
	 * @param next
	 *            the next word boundary
	 * @param value
	 *            the word String
	 */
	public WordProbability(Integer previous, Integer next, String value) {
		this.previous = previous;
		this.next = next;
		this.value = value;
	}

	/**
	 * @param previous
	 *            the previous word boundary
	 * @param next
	 *            the next word boundary
	 * @param value
	 *            the word String
	 * @param probability
	 *            the probability
	 * @param logProbability
	 *            the log probability
	 */
	public WordProbability(Integer previous, Integer next, String value, BigDecimal probability,
			BigDecimal logProbability) {
		this.previous = previous;
		this.next = next;
		this.probability = probability;
		this.value = value;
		this.logProbability = logProbability;
	}

	public WordProbability clone() {
		return new WordProbability(this.previous, this.next, this.value, this.probability, this.logProbability);
	}

	/**
	 * @return the previous
	 */
	public Integer getPrevious() {
		return previous;
	}

	/**
	 * @return the next
	 */
	public Integer getNext() {
		return next;
	}

	/**
	 * @return the probability
	 */
	@Override
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
	@Override
	public BigDecimal getLogProbability() {
		return logProbability;
	}

	/**
	 * @param logProbability
	 *            the logProbability to set
	 */
	public void setLogProbability(BigDecimal logProbability) {
		this.logProbability = logProbability;
	}

	/**
	 * @return the value
	 */
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((next == null) ? 0 : next.hashCode());
		result = prime * result + ((previous == null) ? 0 : previous.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (!(obj instanceof WordProbability)) {
			return false;
		}
		WordProbability other = (WordProbability) obj;
		if (next == null) {
			if (other.next != null) {
				return false;
			}
		} else if (!next.equals(other.next)) {
			return false;
		}
		if (previous == null) {
			if (other.previous != null) {
				return false;
			}
		} else if (!previous.equals(other.previous)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "WordProbability [previous=" + previous + ", next=" + next + ", probability=" + probability
				+ ", logProbability=" + logProbability + ", value=" + value + "]";
	}
}
