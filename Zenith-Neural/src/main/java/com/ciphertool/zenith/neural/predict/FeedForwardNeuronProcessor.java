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

package com.ciphertool.zenith.neural.predict;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.Neuron;
import com.ciphertool.zenith.neural.model.ProblemType;
import com.ciphertool.zenith.neural.model.Synapse;

@Component
public class FeedForwardNeuronProcessor {
	@Async
	public Future<Void> processNeuron(NeuralNetwork network, int j, Layer toLayer, Layer fromLayer) {
		Neuron nextOutputNeuron = toLayer.getNeurons()[j];

		if (nextOutputNeuron.isBias()) {
			// There is no synapse going into a bias neuron
			return new AsyncResult<>(null);
		}

		BigDecimal sum = BigDecimal.ZERO;

		for (int k = 0; k < fromLayer.getNeurons().length; k++) {
			Neuron nextInputNeuron = fromLayer.getNeurons()[k];

			Synapse nextSynapse = nextInputNeuron.getOutgoingSynapses()[j];

			sum = sum.add(nextInputNeuron.getActivationValue().multiply(nextSynapse.getWeight(), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP));
		}

		nextOutputNeuron.setOutputSum(sum);

		if (network.getProblemType() == ProblemType.REGRESSION || toLayer != network.getOutputLayer()) {
			nextOutputNeuron.setActivationValue(toLayer.getActivationFunctionType().getActivationFunction().transformInputSignal(sum, null));
		}

		return new AsyncResult<>(null);
	}

	@Async
	public Future<Void> processOutputNeuron(NeuralNetwork network, int i, BigDecimal[] allSums) {
		Neuron nextOutputNeuron = network.getOutputLayer().getNeurons()[i];

		nextOutputNeuron.setActivationValue(network.getOutputLayer().getActivationFunctionType().getActivationFunction().transformInputSignal(nextOutputNeuron.getOutputSum(), allSums));

		return new AsyncResult<>(null);
	}
}
