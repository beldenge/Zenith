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

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.activation.ActivationFunction;
import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.Neuron;
import com.ciphertool.zenith.neural.model.Synapse;

@Component
public class SupervisedTrainer {
	@Autowired
	private NeuralNetwork		network;

	@Autowired
	private ActivationFunction	activationFunction;

	public void train(BigDecimal[][] inputs, BigDecimal[][] outputs) {
		if (inputs.length != outputs.length) {
			throw new IllegalArgumentException("The sample inputs size of " + inputs.length
					+ " does not match the sample outputs size of " + outputs.length
					+ ".  Unable to continue with training.");
		}

		for (int i = 0; i < inputs.length; i++) {
			network.feedForward(inputs[i]);

			backPropagate(outputs[i]);
		}
	}

	protected void backPropagate(BigDecimal[] expectedOutputs) {
		Layer outputLayer = network.getOutputLayer();

		if (expectedOutputs.length != outputLayer.getNeurons().length) {
			throw new IllegalArgumentException("The expected output size of " + expectedOutputs.length
					+ " does not match the actual output size of " + outputLayer.getNeurons().length
					+ ".  Unable to continue with back propagation step.");
		}

		Layer[] layers = network.getLayers();
		Layer fromLayer = layers[layers.length - 2];

		/*
		 * TODO: This really only works for a single output neuron right now -- I need to figure out how this works for
		 * multiple output neurons. We could also probably skip the rest of the back propagation algorithm if deltaSum
		 * turns out to be zero.
		 * 
		 */
		BigDecimal deltaSum = null;
		for (int i = 0; i < outputLayer.getNeurons().length; i++) {
			Neuron nextOutputNeuron = outputLayer.getNeurons()[i];

			BigDecimal marginOfError = expectedOutputs[i].subtract(nextOutputNeuron.getActivationValue());
			BigDecimal derivative = activationFunction.calculateDerivative(nextOutputNeuron.getOutputSum());

			deltaSum = derivative.multiply(marginOfError, MathConstants.PREC_10_HALF_UP);

			for (int j = 0; j < fromLayer.getNeurons().length; j++) {
				Neuron nextInputNeuron = fromLayer.getNeurons()[j];

				// TODO: this could be pre-computed before entering the loop
				// Leaky ReLU is used to avoid division by zero
				BigDecimal deltaWeight = deltaSum.divide(nextInputNeuron.getActivationValue(), MathConstants.PREC_10_HALF_UP);

				Synapse nextSynapse = nextInputNeuron.getOutgoingSynapses()[i];
				nextSynapse.setOldWeight(nextSynapse.getWeight());
				nextSynapse.setWeight(nextSynapse.getWeight().add(deltaWeight));
			}
		}

		Layer toLayer;

		for (int i = layers.length - 2; i > 0; i--) {
			fromLayer = layers[i - 1];
			toLayer = layers[i];

			for (int j = 0; j < toLayer.getNeurons().length; j++) {
				Neuron nextOutputNeuron = toLayer.getNeurons()[j];

				if (nextOutputNeuron.isBias()) {
					continue;
				}

				for (int k = 0; k < nextOutputNeuron.getOutgoingSynapses().length; k++) {
					Synapse nextOutgoingSynapse = nextOutputNeuron.getOutgoingSynapses()[k];
					BigDecimal deltaHiddenSum = deltaSum.divide(nextOutgoingSynapse.getOldWeight(), MathConstants.PREC_10_HALF_UP).multiply(activationFunction.calculateDerivative(nextOutputNeuron.getOutputSum()), MathConstants.PREC_10_HALF_UP);

					for (int l = 0; l < fromLayer.getNeurons().length; l++) {
						Neuron nextInputNeuron = fromLayer.getNeurons()[l];

						// TODO: Division by zero is still possible if an input is zero
						BigDecimal deltaWeight = BigDecimal.ZERO.equals(nextInputNeuron.getActivationValue()) ? BigDecimal.ZERO : deltaHiddenSum.divide(nextInputNeuron.getActivationValue(), MathConstants.PREC_10_HALF_UP);

						Synapse nextIncomingSynpase = nextInputNeuron.getOutgoingSynapses()[j];
						nextIncomingSynpase.setOldWeight(nextIncomingSynpase.getWeight());
						nextIncomingSynpase.setWeight(nextIncomingSynpase.getWeight().add(deltaWeight));
					}
				}
			}
		}
	}
}
