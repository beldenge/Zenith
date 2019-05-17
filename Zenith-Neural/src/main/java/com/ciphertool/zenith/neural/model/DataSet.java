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

package com.ciphertool.zenith.neural.model;

import java.math.BigDecimal;

public class DataSet {
	private BigDecimal[][]	inputs;
	private BigDecimal[][]	outputs;

	/**
	 * @param inputs
	 *            the inputs to set
	 * @param outputs
	 *            the outputs to set
	 */
	public DataSet(BigDecimal[][] inputs, BigDecimal[][] outputs) {
		this.inputs = inputs;
		this.outputs = outputs;
	}

	/**
	 * @return the inputs
	 */
	public BigDecimal[][] getInputs() {
		return inputs;
	}

	/**
	 * @return the outputs
	 */
	public BigDecimal[][] getOutputs() {
		return outputs;
	}
}
