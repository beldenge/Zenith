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
