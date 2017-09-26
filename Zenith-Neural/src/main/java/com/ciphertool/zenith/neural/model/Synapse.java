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

import java.math.BigDecimal;

public class Synapse {
	private BigDecimal	weight;
	private Neuron		outGoingNeuron;

	/**
	 * @param outGoingNeuron
	 *            the outgoing Neuron to set
	 * @param weight
	 *            the initial weight to set
	 */
	public Synapse(Neuron outGoingNeuron, BigDecimal weight) {
		this.outGoingNeuron = outGoingNeuron;
		this.weight = weight;
	}

	/**
	 * @return the outGoingNeuron
	 */
	public Neuron getOutGoingNeuron() {
		return outGoingNeuron;
	}

	/**
	 * @return the weight
	 */
	public BigDecimal getWeight() {
		return weight;
	}

	/**
	 * @param weight
	 *            the weight to set
	 */
	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}
}
