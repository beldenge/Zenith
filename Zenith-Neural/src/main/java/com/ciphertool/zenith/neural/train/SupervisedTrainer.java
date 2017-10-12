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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.nevec.rjm.BigDecimalMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.log.ConsoleProgressBar;
import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.Neuron;
import com.ciphertool.zenith.neural.model.ProblemType;

@Component
public class SupervisedTrainer {
	private static Logger					log	= LoggerFactory.getLogger(SupervisedTrainer.class);

	@Value("${problem.type}")
	private ProblemType						problemType;

	@Autowired
	private NeuralNetwork					network;

	@Autowired
	private BackPropagationNeuronProcessor	neuronProcessor;

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

			long feedForwardMillis = System.currentTimeMillis() - start;
			start = System.currentTimeMillis();

			backPropagate(outputs[i]);

			long backPropagationMillis = System.currentTimeMillis() - start;

			log.info("Finished training sample {} in {}ms.  Feed-forward: {}ms, Backprop: {}ms", i
					+ 1, feedForwardMillis + backPropagationMillis, feedForwardMillis, backPropagationMillis);

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

		List<Future<Void>> futures = new ArrayList<>(outputLayer.getNeurons().length);

		// Compute deltas for output layer using chain rule and subtract them from current weights
		for (int i = 0; i < outputLayer.getNeurons().length; i++) {
			futures.add(neuronProcessor.processOutputNeuron(i, fromLayer, outputLayer, errorDerivatives, activationDerivatives, expectedOutputs, allSums));
		}

		for (Future<Void> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new IllegalStateException("Unable to process output neuron.", e);
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

			futures = new ArrayList<>(toLayer.getNeurons().length);

			for (int j = 0; j < toLayer.getNeurons().length; j++) {
				futures.add(neuronProcessor.processHiddenNeuron(j, fromLayer, toLayer, errorDerivatives, activationDerivatives, oldErrorDerivatives, oldActivationDerivatives));
			}

			for (Future<Void> future : futures) {
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new IllegalStateException("Unable to process neuron.", e);
				}
			}
		}
	}

	protected static BigDecimal costFunctionRegression(BigDecimal expected, BigDecimal actual) {
		return expected.subtract(actual).pow(2, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP).divide(BigDecimal.valueOf(2.0), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
	}

	protected BigDecimal costFunctionClassification(BigDecimal expected, BigDecimal actual) {
		return BigDecimalMath.log(actual).multiply(expected, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP).negate();
	}
}
