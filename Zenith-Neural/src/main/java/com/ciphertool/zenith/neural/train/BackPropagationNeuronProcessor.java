/**
 * Copyright 2017-2019 George Belden
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

package com.ciphertool.zenith.neural.train;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.Neuron;
import com.ciphertool.zenith.neural.model.ProblemType;
import com.ciphertool.zenith.neural.model.Synapse;

@Component
public class BackPropagationNeuronProcessor {
	@Async
	public Future<Void> processOutputNeuron(int i, Layer fromLayer, Layer outputLayer, BigDecimal[] errorDerivatives, BigDecimal[] activationDerivatives, BigDecimal[] expectedOutputs, BigDecimal[] allSums, ProblemType problemType) {
		Neuron nextOutputNeuron = outputLayer.getNeurons()[i];

		BigDecimal errorDerivative;

		if (problemType == ProblemType.REGRESSION) {
			errorDerivative = derivativeOfCostFunctionRegression(expectedOutputs[i], nextOutputNeuron.getActivationValue());
		} else {
			errorDerivative = derivativeOfCostFunctionClassification(expectedOutputs[i], nextOutputNeuron.getActivationValue());
		}

		errorDerivatives[i] = errorDerivative;

		BigDecimal activationDerivative;

		if (problemType == ProblemType.CLASSIFICATION) {
			// For softmax/cross entropy loss, the activationDerivative is accounted for in the errorDerivative
			activationDerivative = BigDecimal.ONE;
		} else {
			activationDerivative = outputLayer.getActivationFunctionType().getActivationFunction().calculateDerivative(nextOutputNeuron.getOutputSum(), null);
		}

		activationDerivatives[i] = activationDerivative;

		for (int j = 0; j < fromLayer.getNeurons().length; j++) {
			Neuron nextInputNeuron = fromLayer.getNeurons()[j];

			BigDecimal outputSumDerivative = nextInputNeuron.getActivationValue();

			BigDecimal delta = errorDerivative.multiply(activationDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP).multiply(outputSumDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

			Synapse nextSynapse = nextInputNeuron.getOutgoingSynapses()[i];
			nextSynapse.addDelta(delta);
		}

		return new AsyncResult<>(null);
	}

	@Async
	public Future<Void> processHiddenNeuron(int j, Layer fromLayer, Layer toLayer, BigDecimal[] errorDerivatives, BigDecimal[] activationDerivatives, BigDecimal[] oldErrorDerivatives, BigDecimal[] oldActivationDerivatives) {
		Neuron nextToNeuron = toLayer.getNeurons()[j];

		if (nextToNeuron.isBias()) {
			// There are no synapses going into the bias neuron
			return new AsyncResult<>(null);
		}

		BigDecimal activationDerivative = toLayer.getActivationFunctionType().getActivationFunction().calculateDerivative(nextToNeuron.getOutputSum(), null);
		activationDerivatives[j] = activationDerivative;

		BigDecimal errorDerivative = BigDecimal.ZERO;

		for (int l = 0; l < nextToNeuron.getOutgoingSynapses().length; l++) {
			Synapse nextSynapse = nextToNeuron.getOutgoingSynapses()[l];

			BigDecimal partialErrorDerivative = oldErrorDerivatives[l].multiply(oldActivationDerivatives[l], MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

			BigDecimal weightDerivative = nextSynapse.getWeight();

			errorDerivative = errorDerivative.add(partialErrorDerivative.multiply(weightDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP));
		}

		errorDerivatives[j] = errorDerivative;

		BigDecimal errorTimesActivation = errorDerivative.multiply(activationDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

		for (int k = 0; k < fromLayer.getNeurons().length; k++) {
			Neuron nextFromNeuron = fromLayer.getNeurons()[k];

			BigDecimal outputSumDerivative = nextFromNeuron.getActivationValue();

			BigDecimal delta = errorTimesActivation.multiply(outputSumDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

			Synapse nextSynapse = nextFromNeuron.getOutgoingSynapses()[j];
			nextSynapse.addDelta(delta);
		}

		return new AsyncResult<>(null);
	}

	protected static BigDecimal derivativeOfCostFunctionRegression(BigDecimal expected, BigDecimal actual) {
		return expected.subtract(actual).negate();
	}

	protected static BigDecimal derivativeOfCostFunctionClassification(BigDecimal expected, BigDecimal actual) {
		return actual.subtract(expected);
	}
}
