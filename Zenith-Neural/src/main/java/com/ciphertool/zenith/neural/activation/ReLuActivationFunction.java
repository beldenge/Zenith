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

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class ReLuActivationFunction implements ActivationFunction {
	@Override
	public BigDecimal transformInputSignal(BigDecimal sum) {
		return BigDecimal.ZERO.max(sum);
	}

	@Override
	public BigDecimal calculateDerivative(BigDecimal sum) {
		if (BigDecimal.ZERO.compareTo(sum) < 0) {
			return BigDecimal.ZERO;
		} else if (BigDecimal.ZERO.compareTo(sum) > 0) {
			return BigDecimal.ONE;
		} else {
			/*
			 * The derivative of ReLU is undefined at zero, but I can't think of any better way to deal with it than to
			 * just return zero.
			 */
			return BigDecimal.ZERO;
		}
	}
}
