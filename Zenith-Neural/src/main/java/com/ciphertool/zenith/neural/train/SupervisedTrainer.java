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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.activation.ActivationFunction;
import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.Neuron;
import com.ciphertool.zenith.neural.model.Synapse;

@Component
public class SupervisedTrainer {
	@Value("${network.learningRate}")
	private BigDecimal			learningRate;

	private boolean				factorLearningRate;

	@Autowired
	private NeuralNetwork		network;

	@Autowired
	private ActivationFunction	activationFunction;

	@PostConstruct
	public void init() {
		if (learningRate != null && learningRate.compareTo(BigDecimal.ONE) != 0) {
			factorLearningRate = true;
		}
	}

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

		// Compute sum of errors
		BigDecimal errorTotal = BigDecimal.ZERO;
		for (int i = 0; i < outputLayer.getNeurons().length; i++) {
			Neuron nextOutputNeuron = outputLayer.getNeurons()[i];

			errorTotal = errorTotal.add(costFunction(expectedOutputs[i], nextOutputNeuron.getActivationValue()));
		}

		BigDecimal[] errorDerivatives = new BigDecimal[outputLayer.getNeurons().length];
		BigDecimal[] activationDerivatives = new BigDecimal[outputLayer.getNeurons().length];

		// Compute deltas for output layer using chain rule and subtract them from current weights
		for (int i = 0; i < outputLayer.getNeurons().length; i++) {
			Neuron nextOutputNeuron = outputLayer.getNeurons()[i];

			BigDecimal errorDerivative = derivativeOfCostFunction(expectedOutputs[i], nextOutputNeuron.getActivationValue());
			errorDerivatives[i] = errorDerivative;

			BigDecimal activationDerivative = activationFunction.calculateDerivative(nextOutputNeuron.getOutputSum());
			activationDerivatives[i] = activationDerivative;

			for (int j = 0; j < fromLayer.getNeurons().length; j++) {
				Neuron nextInputNeuron = fromLayer.getNeurons()[j];

				BigDecimal outputSumDerivative = nextInputNeuron.getActivationValue();

				BigDecimal delta = errorDerivative.multiply(activationDerivative, MathConstants.PREC_10_HALF_UP).multiply(outputSumDerivative, MathConstants.PREC_10_HALF_UP);

				if (factorLearningRate) {
					delta = delta.multiply(learningRate, MathConstants.PREC_10_HALF_UP);
				}

				Synapse nextSynapse = nextInputNeuron.getOutgoingSynapses()[i];
				nextSynapse.setOldWeight(nextSynapse.getWeight());
				nextSynapse.setWeight(nextSynapse.getWeight().subtract(delta));
			}
		}

		Layer toLayer;

		// Compute deltas for hidden layers using chain rule and subtract them from current weights
		for (int i = layers.length - 2; i > 0; i--) {
			fromLayer = layers[i - 1];
			toLayer = layers[i];

			for (int j = 0; j < toLayer.getNeurons().length; j++) {
				Neuron nextToNeuron = toLayer.getNeurons()[j];

				if (nextToNeuron.isBias()) {
					// There are no synapses going into the bias neuron
					continue;
				}

				for (int k = 0; k < fromLayer.getNeurons().length; k++) {
					Neuron nextFromNeuron = fromLayer.getNeurons()[k];

					BigDecimal errorDerivative = BigDecimal.ZERO;

					for (int l = 0; l < nextToNeuron.getOutgoingSynapses().length; l++) {
						Synapse nextSynapse = nextToNeuron.getOutgoingSynapses()[l];

						BigDecimal partialErrorDerivative = errorDerivatives[l].multiply(activationDerivatives[l], MathConstants.PREC_10_HALF_UP);

						BigDecimal weightDerivative = nextSynapse.getOldWeight();

						errorDerivative = errorDerivative.add(partialErrorDerivative.multiply(weightDerivative, MathConstants.PREC_10_HALF_UP));
					}

					BigDecimal activationDerivative = activationFunction.calculateDerivative(nextToNeuron.getOutputSum());

					BigDecimal outputSumDerivative = nextFromNeuron.getActivationValue();

					BigDecimal delta = errorDerivative.multiply(activationDerivative, MathConstants.PREC_10_HALF_UP).multiply(outputSumDerivative, MathConstants.PREC_10_HALF_UP);

					if (factorLearningRate) {
						delta = delta.multiply(learningRate, MathConstants.PREC_10_HALF_UP);
					}

					Synapse nextSynapse = nextFromNeuron.getOutgoingSynapses()[j];
					nextSynapse.setOldWeight(nextSynapse.getWeight());
					nextSynapse.setWeight(nextSynapse.getWeight().subtract(delta));
				}
			}
		}
	}

	protected static BigDecimal costFunction(BigDecimal expected, BigDecimal actual) {
		return expected.subtract(actual).pow(2, MathConstants.PREC_10_HALF_UP).divide(BigDecimal.valueOf(2.0), MathConstants.PREC_10_HALF_UP);
	}

	protected static BigDecimal derivativeOfCostFunction(BigDecimal expected, BigDecimal actual) {
		return expected.subtract(actual).negate();
	}
}
