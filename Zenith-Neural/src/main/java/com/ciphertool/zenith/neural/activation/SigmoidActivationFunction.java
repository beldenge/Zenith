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

public class SigmoidActivationFunction implements ActivationFunction {
	@Override
	public BigDecimal transformInputSignal(BigDecimal sum, BigDecimal[] allSums) {
		BigDecimal denominator = BigDecimal.ONE.add(BigDecimalMath.pow(MathConstants.EULERS_CONSTANT, sum.negate()).setScale(10, RoundingMode.UP));

		return BigDecimal.ONE.divide(denominator, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
	}

	@Override
	public BigDecimal calculateDerivative(BigDecimal sum, BigDecimal[] allSums) {
		BigDecimal sigmoid = transformInputSignal(sum, null);

		return sigmoid.multiply(BigDecimal.ONE.subtract(sigmoid), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
	}
}
