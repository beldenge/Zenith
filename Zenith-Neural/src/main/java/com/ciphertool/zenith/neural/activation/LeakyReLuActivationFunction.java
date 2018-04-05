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

public class LeakyReLuActivationFunction implements ActivationFunction {
	private static final Float SLOPE_CLOSE_TO_ZERO = 0.01f;

	@Override
	public Float transformInputSignal(Float sum, Float[] allSums) {
		if (0.0 < sum) {
			return sum;
		}

		return SLOPE_CLOSE_TO_ZERO * sum;
	}

	@Override
	public Float calculateDerivative(Float sum, Float[] allSums) {
		if (0.0 < sum) {
			return 1.0f;
		} else if (0.0 > sum) {
			return SLOPE_CLOSE_TO_ZERO;
		}

		/*
		 * The derivative of ReLU is undefined at zero, but I can't think of any better way to deal with it than to just
		 * return zero.
		 */
		return 0.0f;
	}
}
