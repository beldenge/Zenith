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
import java.math.RoundingMode;

import javax.annotation.PostConstruct;

import org.nevec.rjm.BigDecimalMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.activation.HiddenActivationFunction;
import com.ciphertool.zenith.neural.activation.OutputActivationFunction;
import com.ciphertool.zenith.neural.log.ConsoleProgressBar;
import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.Neuron;
import com.ciphertool.zenith.neural.model.ProblemType;
import com.ciphertool.zenith.neural.model.Synapse;

@Component
public class SupervisedTrainer {
	private static Logger				log	= LoggerFactory.getLogger(SupervisedTrainer.class);

	@Value("${network.learningRate}")
	private BigDecimal					learningRate;

	@Value("${problem.type}")
	private ProblemType					problemType;

	private boolean						factorLearningRate;

	@Autowired
	private NeuralNetwork				network;

	@Autowired
	private HiddenActivationFunction	hiddenActivationFunction;

	@Autowired
	private OutputActivationFunction	outputActivationFunction;

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

		ConsoleProgressBar progressBar = new ConsoleProgressBar();

		for (int i = 0; i < inputs.length; i++) {
			long start = System.currentTimeMillis();

			network.feedForward(inputs[i]);

			backPropagate(outputs[i]);

			log.info("Finished training sample {} in {}ms.", i + 1, System.currentTimeMillis() - start);

			progressBar.tick((double) i, (double) inputs.length);
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
		BigDecimal outputSumTotal = BigDecimal.ZERO;
		for (int i = 0; i < outputLayer.getNeurons().length; i++) {
			Neuron nextOutputNeuron = outputLayer.getNeurons()[i];

			if (problemType == ProblemType.REGRESSION) {
				errorTotal = errorTotal.add(costFunctionRegression(expectedOutputs[i], nextOutputNeuron.getActivationValue()));
			} else {
				errorTotal = errorTotal.add(costFunctionClassification(expectedOutputs[i], nextOutputNeuron.getActivationValue()));
			}

			outputSumTotal = outputSumTotal.add(nextOutputNeuron.getOutputSum());
		}

		BigDecimal[] errorDerivatives = new BigDecimal[outputLayer.getNeurons().length];
		BigDecimal[] activationDerivatives = new BigDecimal[outputLayer.getNeurons().length];

		BigDecimal[] allSums = new BigDecimal[outputLayer.getNeurons().length];

		if (problemType == ProblemType.CLASSIFICATION) {
			for (int i = 0; i < outputLayer.getNeurons().length; i++) {
				Neuron nextOutputNeuron = outputLayer.getNeurons()[i];

				allSums[i] = nextOutputNeuron.getOutputSum();
			}
		}

		// Compute deltas for output layer using chain rule and subtract them from current weights
		for (int i = 0; i < outputLayer.getNeurons().length; i++) {
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
				activationDerivative = outputActivationFunction.calculateDerivative(nextOutputNeuron.getOutputSum(), allSums);
			} else {
				activationDerivative = hiddenActivationFunction.calculateDerivative(nextOutputNeuron.getOutputSum());
			}

			activationDerivatives[i] = activationDerivative;

			for (int j = 0; j < fromLayer.getNeurons().length; j++) {
				Neuron nextInputNeuron = fromLayer.getNeurons()[j];

				BigDecimal outputSumDerivative = nextInputNeuron.getActivationValue();

				BigDecimal delta = errorDerivative.multiply(activationDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP).multiply(outputSumDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

				if (factorLearningRate) {
					delta = delta.multiply(learningRate, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
				}

				Synapse nextSynapse = nextInputNeuron.getOutgoingSynapses()[i];
				nextSynapse.setOldWeight(nextSynapse.getWeight());
				nextSynapse.setWeight(nextSynapse.getWeight().subtract(delta));
			}
		}

		Layer toLayer;
		BigDecimal[] oldErrorDerivatives;
		BigDecimal[] oldActivationDerivatives;

		// Compute deltas for hidden layers using chain rule and subtract them from current weights
		for (int i = layers.length - 2; i > 0; i--) {
			fromLayer = layers[i - 1];
			toLayer = layers[i];

			oldErrorDerivatives = errorDerivatives;
			oldActivationDerivatives = activationDerivatives;

			errorDerivatives = new BigDecimal[toLayer.getNeurons().length];
			activationDerivatives = new BigDecimal[toLayer.getNeurons().length];

			for (int j = 0; j < toLayer.getNeurons().length; j++) {
				Neuron nextToNeuron = toLayer.getNeurons()[j];

				if (nextToNeuron.isBias()) {
					// There are no synapses going into the bias neuron
					continue;
				}

				BigDecimal activationDerivative = hiddenActivationFunction.calculateDerivative(nextToNeuron.getOutputSum());
				activationDerivatives[j] = activationDerivative;

				BigDecimal errorDerivative = BigDecimal.ZERO;

				for (int l = 0; l < nextToNeuron.getOutgoingSynapses().length; l++) {
					Synapse nextSynapse = nextToNeuron.getOutgoingSynapses()[l];

					BigDecimal partialErrorDerivative = oldErrorDerivatives[l].multiply(oldActivationDerivatives[l], MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

					BigDecimal weightDerivative = nextSynapse.getOldWeight();

					errorDerivative = errorDerivative.add(partialErrorDerivative.multiply(weightDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP));
				}

				errorDerivatives[j] = errorDerivative;

				BigDecimal errorTimesActivation = errorDerivative.multiply(activationDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

				for (int k = 0; k < fromLayer.getNeurons().length; k++) {
					Neuron nextFromNeuron = fromLayer.getNeurons()[k];

					BigDecimal outputSumDerivative = nextFromNeuron.getActivationValue();

					BigDecimal delta = errorTimesActivation.multiply(outputSumDerivative, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

					if (factorLearningRate) {
						delta = delta.multiply(learningRate, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
					}

					Synapse nextSynapse = nextFromNeuron.getOutgoingSynapses()[j];
					nextSynapse.setOldWeight(nextSynapse.getWeight());
					nextSynapse.setWeight(nextSynapse.getWeight().subtract(delta));
				}
			}
		}
	}

	protected static BigDecimal costFunctionRegression(BigDecimal expected, BigDecimal actual) {
		return expected.subtract(actual).pow(2, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP).divide(BigDecimal.valueOf(2.0), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
	}

	protected static BigDecimal derivativeOfCostFunctionRegression(BigDecimal expected, BigDecimal actual) {
		return expected.subtract(actual).negate();
	}

	protected static BigDecimal costFunctionClassification(BigDecimal expected, BigDecimal actual) {
		return BigDecimalMath.log(actual).multiply(expected, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP).negate();
	}

	protected static BigDecimal derivativeOfCostFunctionClassification(BigDecimal expected, BigDecimal actual) {
		return actual.subtract(expected);
	}
}
