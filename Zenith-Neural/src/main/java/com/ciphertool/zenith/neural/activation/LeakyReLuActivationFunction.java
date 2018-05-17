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

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.BooleanIndexing;
import org.nd4j.linalg.indexing.conditions.Conditions;

public class LeakyReLuActivationFunction implements ActivationFunction {
	private static final Float SLOPE_CLOSE_TO_ZERO = 0.01f;

	@Override
	public void transformInputSignal(INDArray layer) {
		BooleanIndexing.applyWhere(layer, Conditions.lessThanOrEqual(0.0f), value -> value.floatValue() * SLOPE_CLOSE_TO_ZERO);
	}

	@Override
	public void calculateDerivative(INDArray layer) {
		BooleanIndexing.applyWhere(layer, Conditions.greaterThan(0.0f), 1.0f);

		BooleanIndexing.applyWhere(layer, Conditions.lessThan(0.0f), SLOPE_CLOSE_TO_ZERO);

		/*
		 * The derivative of ReLU is undefined at zero, but I can't think of any better way to deal with it than to just
		 * return zero.
		 */
		BooleanIndexing.applyWhere(layer, Conditions.equals(0.0f), 0.0f);
	}
}
