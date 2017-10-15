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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.nevec.rjm.BigDecimalMath;

import com.ciphertool.zenith.math.MathConstants;

public class SoftmaxActivationFunction implements ActivationFunction {
	@Override
	public BigDecimal transformInputSignal(BigDecimal sum, BigDecimal[] allSums) {
		// Use the maximum input as an arbitrary constant for numerical stability
		BigDecimal max = BigDecimal.ZERO;

		for (int i = 0; i < allSums.length; i++) {
			max = max.max(allSums[i]);
		}

		BigDecimal numerator = BigDecimalMath.pow(MathConstants.EULERS_CONSTANT, sum.subtract(max)).setScale(10, RoundingMode.UP);

		BigDecimal denominator = BigDecimal.ZERO;
		for (int i = 0; i < allSums.length; i++) {
			denominator = denominator.add(BigDecimalMath.pow(MathConstants.EULERS_CONSTANT, allSums[i].subtract(max)).setScale(10, RoundingMode.UP));
		}

		return numerator.divide(denominator, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
	}

	@Override
	public BigDecimal calculateDerivative(BigDecimal sum, BigDecimal[] allSums) {
		BigDecimal softMax = transformInputSignal(sum, allSums);

		/*
		 * This is only true when the output neuron index equals the index of the softmax of that neuron (i.e. this
		 * works for the output layer only)
		 */
		return softMax.multiply(BigDecimal.ONE.subtract(softMax), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
	}
}
