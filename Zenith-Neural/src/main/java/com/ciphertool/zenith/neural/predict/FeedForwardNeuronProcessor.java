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

package com.ciphertool.zenith.neural.predict;

import com.ciphertool.zenith.neural.model.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

@Component
public class FeedForwardNeuronProcessor {
	@Async
	public Future<Void> processNeuron(NeuralNetwork network, int j, Layer toLayer, Layer fromLayer) {
		Neuron nextOutputNeuron = toLayer.getNeurons()[j];

		if (nextOutputNeuron.isBias()) {
			// There is no synapse going into a bias neuron
			return new AsyncResult<>(null);
		}

		Float sum = 0.0f;

		for (int k = 0; k < fromLayer.getNeurons().length; k++) {
			Neuron nextInputNeuron = fromLayer.getNeurons()[k];

			Synapse nextSynapse = nextInputNeuron.getOutgoingSynapses()[j];

			// For sparsely coded inputs, skipping this gives a meaningful performance gain
			if (nextInputNeuron.getActivationValue() != 0.0f) {
				sum = sum + (nextInputNeuron.getActivationValue() * nextSynapse.getWeight());
			}
		}

		nextOutputNeuron.setOutputSum(sum);

		if (network.getProblemType() == ProblemType.REGRESSION || toLayer != network.getOutputLayer()) {
			nextOutputNeuron.setActivationValue(toLayer.getActivationFunctionType().getActivationFunction().transformInputSignal(sum, null));
		}

		return new AsyncResult<>(null);
	}

	@Async
	public Future<Void> processOutputNeuron(NeuralNetwork network, int i, Float[] allSums) {
		Neuron nextOutputNeuron = network.getOutputLayer().getNeurons()[i];

		nextOutputNeuron.setActivationValue(network.getOutputLayer().getActivationFunctionType().getActivationFunction().transformInputSignal(nextOutputNeuron.getOutputSum(), allSums));

		return new AsyncResult<>(null);
	}
}
