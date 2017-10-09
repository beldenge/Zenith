package com.ciphertool.zenith.neural.activation;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.nevec.rjm.BigDecimalMath;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;

@Component
public class SoftmaxActivationFunction implements OutputActivationFunction {
	private static final BigDecimal EULERS_NUMBER = BigDecimal.valueOf(Math.E);

	@Override
	public BigDecimal transformInputSignal(BigDecimal sum, BigDecimal sumOfSums) {
		BigDecimal numerator = BigDecimalMath.pow(EULERS_NUMBER, sum).setScale(10, RoundingMode.UP);
		BigDecimal denominator = BigDecimalMath.pow(EULERS_NUMBER, sumOfSums).setScale(10, RoundingMode.UP);

		return numerator.divide(denominator, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
	}

	@Override
	public BigDecimal calculateDerivative(BigDecimal sum, BigDecimal sumOfSums) {
		return BigDecimal.ZERO;
	}
}
