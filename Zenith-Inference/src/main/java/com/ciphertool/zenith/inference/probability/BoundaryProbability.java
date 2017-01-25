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

import java.math.BigDecimal;

public class BoundaryProbability implements Probability<Boolean>, Comparable<BoundaryProbability> {
	private Boolean		option;
	private BigDecimal	probability;

	/**
	 * @param option
	 *            the option
	 * @param probability
	 *            the probability
	 */
	public BoundaryProbability(Boolean option, BigDecimal probability) {
		super();
		this.option = option;
		this.probability = probability;
	}

	@Override
	public Boolean getValue() {
		return this.option;
	}

	@Override
	public BigDecimal getProbability() {
		return this.probability;
	}

	@Override
	public BigDecimal getLogProbability() {
		throw new UnsupportedOperationException("Method not implemented!");
	}

	@Override
	public int compareTo(BoundaryProbability other) {
		return this.probability.compareTo(other.probability);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((option == null) ? 0 : option.hashCode());
		result = prime * result + ((probability == null) ? 0 : probability.hashCode());
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
		if (!(obj instanceof BoundaryProbability)) {
			return false;
		}
		BoundaryProbability other = (BoundaryProbability) obj;
		if (option == null) {
			if (other.option != null) {
				return false;
			}
		} else if (!option.equals(other.option)) {
			return false;
		}
		if (probability == null) {
			if (other.probability != null) {
				return false;
			}
		} else if (!probability.equals(other.probability)) {
			return false;
		}
		return true;
	}
}
