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

public class Neuron {
	private BigDecimal	value;
	private Synapse[]	outgoingSynapses;

	/**
	 * @return the outgoingSynapses the outgoing Synapses to set
	 */
	public Synapse[] getOutgoingSynapses() {
		return outgoingSynapses;
	}

	/**
	 * @param outgoingSynapses
	 *            the outgoingSynapses to set
	 */
	public void setOutgoingSynapses(Synapse[] outgoingSynapses) {
		this.outgoingSynapses = outgoingSynapses;
	}

	/**
	 * @return the value
	 */
	public BigDecimal getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(BigDecimal value) {
		this.value = value;
	}
}
