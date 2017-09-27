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

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.activation.ActivationFunction;
import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.Neuron;
import com.ciphertool.zenith.neural.model.Synapse;

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
			feedForward(inputs[i]);

			backPropagate(outputs[i]);
		}
	}

	protected void feedForward(BigDecimal[] inputs) {
		Layer inputLayer = network.getInputLayer();

		if (inputs.length != inputLayer.getNeurons().length) {
			throw new IllegalArgumentException("The sample input size of " + inputs.length
					+ " does not match the input layer size of " + inputLayer.getNeurons().length
					+ ".  Unable to continue with feed forward step.");
		}

		for (int i = 0; i < inputLayer.getNeurons().length; i++) {
			inputLayer.getNeurons()[i].setActivationValue(inputs[i]);
		}

		Layer fromLayer;
		Layer toLayer;
		Layer[] hiddenLayers = network.getHiddenLayers();
		Layer outputLayer = network.getOutputLayer();

		for (int i = 0; i < hiddenLayers.length + 1; i++) {
			fromLayer = i == 0 ? inputLayer : hiddenLayers[i - 1];
			toLayer = i < hiddenLayers.length ? hiddenLayers[i] : outputLayer;

			for (int j = 0; j < toLayer.getNeurons().length; j++) {
				BigDecimal sum = BigDecimal.ZERO;

				for (int k = 0; k < fromLayer.getNeurons().length; k++) {
					Neuron nextInputNeuron = fromLayer.getNeurons()[k];

					Synapse nextSynapse = nextInputNeuron.getOutgoingSynapses()[j];

					sum = sum.add(nextInputNeuron.getActivationValue().multiply(nextSynapse.getWeight(), MathConstants.PREC_10_HALF_UP));
				}

				Neuron nextOutputNeuron = toLayer.getNeurons()[j];

				nextOutputNeuron.setOutputSum(sum);
				nextOutputNeuron.setActivationValue(activationFunction.transformInputSignal(sum));
			}
		}
	}

	protected void backPropagate(BigDecimal[] expectedOutputs) {
		Layer outputLayer = network.getOutputLayer();

		if (expectedOutputs.length != outputLayer.getNeurons().length) {
			throw new IllegalArgumentException("The expected output size of " + expectedOutputs.length
					+ " does not match the actual output size of " + outputLayer.getNeurons().length
					+ ".  Unable to continue with back propagation step.");
		}

		Layer[] hiddenLayers = network.getHiddenLayers();
		Layer fromLayer = hiddenLayers[hiddenLayers.length - 1];

		/*
		 * TODO: This really only works for a single output neuron right now -- I need to figure out how this works for
		 * multiple output neurons.
		 * 
		 */
		BigDecimal deltaSum = null;
		for (int j = 0; j < outputLayer.getNeurons().length; j++) {
			Neuron nextOutputNeuron = outputLayer.getNeurons()[j];

			BigDecimal marginOfError = expectedOutputs[j].subtract(nextOutputNeuron.getActivationValue());
			BigDecimal derivative = activationFunction.calculateDerivative(nextOutputNeuron.getOutputSum());
			deltaSum = derivative.multiply(marginOfError, MathConstants.PREC_10_HALF_UP);

			for (int k = 0; k < fromLayer.getNeurons().length; k++) {
				Neuron nextInputNeuron = fromLayer.getNeurons()[k];

				// TODO: this could be pre-computed before entering the loop
				BigDecimal deltaWeight = deltaSum.divide(nextInputNeuron.getActivationValue(), MathConstants.PREC_10_HALF_UP);

				Synapse nextSynapse = nextInputNeuron.getOutgoingSynapses()[j];
				nextSynapse.setOldWeight(nextSynapse.getWeight());
				nextSynapse.setWeight(nextSynapse.getWeight().add(deltaWeight));
			}
		}

		Layer toLayer;
		Layer inputLayer = network.getInputLayer();

		for (int i = hiddenLayers.length - 1; i >= 0; i--) {
			fromLayer = i == 0 ? inputLayer : hiddenLayers[i - 1];
			toLayer = hiddenLayers[i];

			for (int j = 0; j < toLayer.getNeurons().length; j++) {
				Neuron nextOutputNeuron = toLayer.getNeurons()[j];

				for (int k = 0; k < nextOutputNeuron.getOutgoingSynapses().length; k++) {
					Synapse nextOutgoingSynapse = nextOutputNeuron.getOutgoingSynapses()[k];
					BigDecimal deltaHiddenSum = deltaSum.divide(nextOutgoingSynapse.getOldWeight(), MathConstants.PREC_10_HALF_UP).multiply(activationFunction.calculateDerivative(nextOutputNeuron.getOutputSum()), MathConstants.PREC_10_HALF_UP);

					for (int l = 0; l < fromLayer.getNeurons().length; l++) {
						Neuron nextInputNeuron = fromLayer.getNeurons()[l];

						BigDecimal deltaWeight = deltaHiddenSum.divide(nextInputNeuron.getActivationValue(), MathConstants.PREC_10_HALF_UP);

						Synapse nextIncomingSynpase = nextInputNeuron.getOutgoingSynapses()[l];
						nextIncomingSynpase.setOldWeight(nextIncomingSynpase.getWeight());
						nextIncomingSynpase.setWeight(nextIncomingSynpase.getWeight().add(deltaWeight));
					}
				}
			}
		}
	}
}
