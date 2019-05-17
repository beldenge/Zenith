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

import com.ciphertool.zenith.neural.activation.ActivationFunctionType;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Layer {
	private Neuron[]				neurons;

	@JsonProperty(value = "hasBias")
	private boolean					hasBias;

	private ActivationFunctionType	activationFunctionType;

	@SuppressWarnings("unused")
	private Layer() {
		// Exists purely for Jackson deserialization
	}

	public Layer(int neurons, boolean hasBias) {
		this.hasBias = hasBias;

		this.neurons = new Neuron[neurons + (hasBias ? 1 : 0)];

		for (int i = 0; i < neurons; i++) {
			this.neurons[i] = new Neuron(false);
		}

		if (hasBias) {
			this.neurons[this.neurons.length - 1] = new Neuron(true);
		}
	}

	public Layer(int neurons, ActivationFunctionType activationFunctionType, boolean hasBias) {
		this(neurons, hasBias);

		this.activationFunctionType = activationFunctionType;
	}

	/**
	 * @return the neurons
	 */
	public Neuron[] getNeurons() {
		return neurons;
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
}
