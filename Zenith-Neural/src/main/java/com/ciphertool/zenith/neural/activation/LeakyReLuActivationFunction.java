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

import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;

@Component
public class LeakyReLuActivationFunction implements HiddenActivationFunction {
	private static final BigDecimal SLOPE_CLOSE_TO_ZERO = BigDecimal.valueOf(0.01);

	@Override
	public BigDecimal transformInputSignal(BigDecimal sum) {
		if (BigDecimal.ZERO.compareTo(sum) < 0) {
			return sum;
		}

		return SLOPE_CLOSE_TO_ZERO.multiply(sum, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
	}

	@Override
	public BigDecimal calculateDerivative(BigDecimal sum) {
		if (BigDecimal.ZERO.compareTo(sum) < 0) {
			return BigDecimal.ONE;
		} else if (BigDecimal.ZERO.compareTo(sum) > 0) {
			return SLOPE_CLOSE_TO_ZERO;
		}

		/*
		 * The derivative of ReLU is undefined at zero, but I can't think of any better way to deal with it than to just
		 * return zero.
		 */
		return BigDecimal.ZERO;
	}
}
