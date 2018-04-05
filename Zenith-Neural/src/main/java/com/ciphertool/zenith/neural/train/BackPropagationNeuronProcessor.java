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

package com.ciphertool.zenith.neural.train;

import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.Neuron;
import com.ciphertool.zenith.neural.model.ProblemType;
import com.ciphertool.zenith.neural.model.Synapse;

@Component
public class BackPropagationNeuronProcessor {
	@Async
	public Future<Void> processOutputNeuron(int i, Layer fromLayer, Layer outputLayer, Float[] errorDerivatives, Float[] activationDerivatives, Float[] expectedOutputs, Float[] allSums, ProblemType problemType) {
		Neuron nextOutputNeuron = outputLayer.getNeurons()[i];

		Float errorDerivative;

		if (problemType == ProblemType.REGRESSION) {
			errorDerivative = derivativeOfCostFunctionRegression(expectedOutputs[i], nextOutputNeuron.getActivationValue());
		} else {
			errorDerivative = derivativeOfCostFunctionClassification(expectedOutputs[i], nextOutputNeuron.getActivationValue());
		}

		errorDerivatives[i] = errorDerivative;

		Float activationDerivative;

		if (problemType == ProblemType.CLASSIFICATION) {
			// For softmax/cross entropy loss, the activationDerivative is accounted for in the errorDerivative
			activationDerivative = 1.0f;
		} else {
			activationDerivative = outputLayer.getActivationFunctionType().getActivationFunction().calculateDerivative(nextOutputNeuron.getOutputSum(), null);
		}

		activationDerivatives[i] = activationDerivative;

		for (int j = 0; j < fromLayer.getNeurons().length; j++) {
			Neuron nextInputNeuron = fromLayer.getNeurons()[j];

			Float outputSumDerivative = nextInputNeuron.getActivationValue();

			Float delta = errorDerivative * activationDerivative * outputSumDerivative;

			Synapse nextSynapse = nextInputNeuron.getOutgoingSynapses()[i];
			nextSynapse.addDelta(delta);
		}

		return new AsyncResult<>(null);
	}

	@Async
	public Future<Void> processHiddenNeuron(int j, Layer fromLayer, Layer toLayer, Float[] errorDerivatives, Float[] activationDerivatives, Float[] oldErrorDerivatives, Float[] oldActivationDerivatives) {
		Neuron nextToNeuron = toLayer.getNeurons()[j];

		if (nextToNeuron.isBias()) {
			// There are no synapses going into the bias neuron
			return new AsyncResult<>(null);
		}

		Float activationDerivative = toLayer.getActivationFunctionType().getActivationFunction().calculateDerivative(nextToNeuron.getOutputSum(), null);
		activationDerivatives[j] = activationDerivative;

		Float errorDerivative = 0.0f;

		for (int l = 0; l < nextToNeuron.getOutgoingSynapses().length; l++) {
			Synapse nextSynapse = nextToNeuron.getOutgoingSynapses()[l];

			Float partialErrorDerivative = oldErrorDerivatives[l] * oldActivationDerivatives[l];

			Float weightDerivative = nextSynapse.getWeight();

			errorDerivative = errorDerivative + (partialErrorDerivative * weightDerivative);
		}

		errorDerivatives[j] = errorDerivative;

		Float errorTimesActivation = errorDerivative * activationDerivative;

		for (int k = 0; k < fromLayer.getNeurons().length; k++) {
			Neuron nextFromNeuron = fromLayer.getNeurons()[k];

			Float outputSumDerivative = nextFromNeuron.getActivationValue();

			Float delta = errorTimesActivation * outputSumDerivative;

			Synapse nextSynapse = nextFromNeuron.getOutgoingSynapses()[j];
			nextSynapse.addDelta(delta);
		}

		return new AsyncResult<>(null);
	}

	protected static Float derivativeOfCostFunctionRegression(Float expected, Float actual) {
		return (expected - actual) * -1.0f;
	}

	protected static Float derivativeOfCostFunctionClassification(Float expected, Float actual) {
		return actual - expected;
	}
}
