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

package com.ciphertool.zenith.neural.activation;

import com.ciphertool.zenith.math.MathConstants;

public class SoftmaxActivationFunction implements ActivationFunction {
	@Override
	public Double transformInputSignal(Double sum, Double[] allSums) {
		// Use the maximum input as an arbitrary constant for numerical stability
		Double max = 0.0;

		for (int i = 0; i < allSums.length; i++) {
			max = Math.max(max, allSums[i]);
		}

		Double numerator = Math.pow(MathConstants.EULERS_CONSTANT, sum - max);

		Double denominator = 0.0;
		for (int i = 0; i < allSums.length; i++) {
			denominator = denominator + Math.pow(MathConstants.EULERS_CONSTANT, allSums[i] - max);
		}

		return numerator / denominator;
	}

	@Override
	public Double calculateDerivative(Double sum, Double[] allSums) {
		Double softMax = transformInputSignal(sum, allSums);

		/*
		 * This is only true when the output neuron index equals the index of the softmax of that neuron (i.e. this
		 * works for the output layer only)
		 */
		return softMax * (1.0 - softMax);
	}
}
