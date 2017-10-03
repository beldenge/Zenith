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

public class Layer {
	protected Neuron[]	neurons;
	protected boolean	hasBias;

	public Layer(int neurons, boolean hasBias) {
		this.hasBias = hasBias;

		this.neurons = new Neuron[neurons + (hasBias ? 1 : 0)];

		for (int i = 0; i < neurons; i++) {
			this.neurons[i] = new Neuron();
		}

		if (hasBias) {
			this.neurons[this.neurons.length - 1] = new Neuron(true);
		}
	}

	/**
	 * @return the neurons
	 */
	public Neuron[] getNeurons() {
		return neurons;
	}

	/**
	 * @return the hasBias
	 */
	public boolean hasBias() {
		return hasBias;
	}
}
