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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Stack;

public class Layer {
	private int numberOfNeurons;

	@JsonProperty(value = "hasBias")
	private boolean	hasBias;

	private ActivationFunctionType activationFunctionType;

	private LayerType type;

	@JsonIgnore
	private INDArray activations;

	@JsonIgnore
	private INDArray outputSums;

	@JsonIgnore
	private INDArray accumulatedDeltas;

	private INDArray outgoingWeights;

	@JsonIgnore
	private Stack<INDArray> recurrentActivations = new Stack<>();

	@JsonIgnore
	private Stack<INDArray> recurrentOutputSums = new Stack<>();

	@JsonIgnore
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

	public void setActivationFunctionType(ActivationFunctionType activationFunctionType) {
		this.activationFunctionType = activationFunctionType;
	}

	/**
	 * @return the hasBias
	 */
	public boolean hasBias() {
		return hasBias;
	}

	public void setHasBias(boolean hasBias) {
		this.hasBias = hasBias;
	}

	/**
	 * @return the number of neurons in this layer
	 */
	public int getNumberOfNeurons() {
		return numberOfNeurons;
	}

	public void setNumberOfNeurons(int numberOfNeurons) {
		this.numberOfNeurons = numberOfNeurons;
	}

	public INDArray getActivations() {
		return activations;
	}

	public void setActivations(INDArray activations) {
		this.activations = activations;
	}

	public INDArray getOutputSums() {
		return outputSums;
	}

	public void setOutputSums(INDArray outputSums) {
		this.outputSums = outputSums;
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

	public void setType(LayerType type) {
		this.type = type;
	}

	public Stack<INDArray> getRecurrentActivations() {
		return recurrentActivations;
	}

	public void setRecurrentActivations(Stack<INDArray> recurrentActivations) {
		this.recurrentActivations = recurrentActivations;
	}

	public Stack<INDArray> getRecurrentOutputSums() {
		return recurrentOutputSums;
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
