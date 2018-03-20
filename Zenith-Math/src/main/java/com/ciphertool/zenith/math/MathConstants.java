/**
 * Copyright 2017 George Belden
 * 
 * This file is part of Zenith.
 * 
 * Zenith is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Zenith is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class MathConstants {
	public static final MathContext	PREC_10_HALF_UP	= new MathContext(10, RoundingMode.HALF_UP);

	public static final MathContext	PREC_5_HALF_UP	= new MathContext(5, RoundingMode.HALF_UP);

	public static final BigDecimal	EULERS_CONSTANT	= BigDecimal.valueOf(Math.E);

	public static final BigDecimal SINGLE_LETTER_RANDOM_PROBABILITY = BigDecimal.ONE.divide(BigDecimal.valueOf(26), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
}
