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

package com.ciphertool.zenith.neural.model;

import com.ciphertool.zenith.neural.activation.ActivationFunctionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class Layer {
	@JsonProperty(value = "hasBias")
	private boolean	hasBias;

	private ActivationFunctionType activationFunctionType;

	private LayerType type;

	private INDArray activations;
	private INDArray outputSums;
	private INDArray accumulatedDeltas;
	private INDArray outgoingWeights;
	private INDArray recurrentActivations;
	private INDArray recurrentAccumulatedDeltas;
	private INDArray recurrentOutgoingWeights;

	@SuppressWarnings("unused")
	private Layer() {
		// Exists purely for Jackson deserialization
	}

	public Layer(int neurons, boolean hasBias, LayerType type) {
		this.activations = Nd4j.create(1, neurons + (hasBias ? 1 : 0));
		this.outputSums = Nd4j.create(1, neurons + (hasBias ? 1 : 0));
		this.hasBias = hasBias;
		this.type = type;
	}

	public Layer(int neurons, ActivationFunctionType activationFunctionType, boolean hasBias, LayerType type) {
		this(neurons, hasBias, type);

		this.activationFunctionType = activationFunctionType;
	}

	/**
	 * @return the activationFunctionType
	 */
	public ActivationFunctionType getActivationFunctionType() {
		return activationFunctionType;
	}

	/**
	 * @return the hasBias
	 */
	public boolean hasBias() {
		return hasBias;
	}

	/**
	 * @return the number of neurons in this layer
	 */
	public int getNeurons() {
		return activations.size(1);
	}

	public INDArray getActivations() {
		return activations;
	}

	public INDArray getOutputSums() {
		return outputSums;
	}

	public INDArray getAccumulatedDeltas() {
		return accumulatedDeltas;
	}

	public void setAccumulatedDeltas(INDArray accumulatedDeltas) {
		this.accumulatedDeltas = accumulatedDeltas;
	}

	public INDArray getOutgoingWeights() {
		return outgoingWeights;
	}

	public void setOutgoingWeights(INDArray outgoingWeights) {
		this.outgoingWeights = outgoingWeights;
	}

	public LayerType getType() {
		return type;
	}

	public INDArray getRecurrentActivations() {
		return recurrentActivations;
	}

	public void setRecurrentActivations(INDArray recurrentActivations) {
		this.recurrentActivations = recurrentActivations;
	}

	public INDArray getRecurrentAccumulatedDeltas() {
		return recurrentAccumulatedDeltas;
	}

	public void setRecurrentAccumulatedDeltas(INDArray recurrentAccumulatedDeltas) {
		this.recurrentAccumulatedDeltas = recurrentAccumulatedDeltas;
	}

	public INDArray getRecurrentOutgoingWeights() {
		return recurrentOutgoingWeights;
	}

	public void setRecurrentOutgoingWeights(INDArray recurrentOutgoingWeights) {
		this.recurrentOutgoingWeights = recurrentOutgoingWeights;
	}
}
