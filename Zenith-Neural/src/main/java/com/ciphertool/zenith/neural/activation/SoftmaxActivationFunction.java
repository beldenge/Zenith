package com.ciphertool.zenith.neural.activation;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.nevec.rjm.BigDecimalMath;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;

@Component
public class SoftmaxActivationFunction implements OutputActivationFunction {
	@Override
	public BigDecimal transformInputSignal(BigDecimal sum, BigDecimal[] allSums) {
		// Use the maximum input as an arbitrary constant for numerical stability
		BigDecimal max = BigDecimal.ZERO;

		for (int i = 0; i < allSums.length; i++) {
			max = max.max(allSums[i]);
		}

		BigDecimal numerator = BigDecimalMath.exp(sum.subtract(max)).setScale(10, RoundingMode.UP);

		BigDecimal denominator = BigDecimal.ZERO;
		for (int i = 0; i < allSums.length; i++) {
			denominator = denominator.add(BigDecimalMath.exp(allSums[i].subtract(max)).setScale(10, RoundingMode.UP));
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
