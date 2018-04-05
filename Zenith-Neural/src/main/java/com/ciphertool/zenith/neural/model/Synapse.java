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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Synapse {
	private Double			weight;

	@JsonIgnore
	private List<Double>	accumulatedDeltas	= new ArrayList<>(1);

	@JsonIgnore
	private Neuron				outGoingNeuron;

	@SuppressWarnings("unused")
	private Synapse() {
		// Exists purely for Jackson deserialization
	}

	/**
	 * @param outGoingNeuron
	 *            the outgoing Neuron to set
	 * @param weight
	 *            the initial weight to set
	 */
	public Synapse(Neuron outGoingNeuron, Double weight, int batchSize) {
		this.outGoingNeuron = outGoingNeuron;
		this.weight = weight;
		this.accumulatedDeltas = new ArrayList<>(batchSize);
	}

	/**
	 * @return the outGoingNeuron
	 */
	public Neuron getOutGoingNeuron() {
		return outGoingNeuron;
	}

	/**
	 * @param outGoingNeuron
	 *            the outGoingNeuron to set
	 */
	public void setOutGoingNeuron(Neuron outGoingNeuron) {
		this.outGoingNeuron = outGoingNeuron;
	}

	/**
	 * @return the weight
	 */
	public Double getWeight() {
		return weight;
	}

	/**
	 * @param weight
	 *            the weight to set
	 */
	public void setWeight(Double weight) {
		this.weight = weight;
	}

	/**
	 * @param delta
	 *            the delta to add
	 */
	public void addDelta(Double delta) {
		this.accumulatedDeltas.add(delta);
	}

	/**
	 * @return the accumulatedDeltas
	 */
	public List<Double> getAccumulatedDeltas() {
		return Collections.unmodifiableList(accumulatedDeltas);
	}

	public void clearAccumulatedDeltas() {
		this.accumulatedDeltas.clear();
	}
}
