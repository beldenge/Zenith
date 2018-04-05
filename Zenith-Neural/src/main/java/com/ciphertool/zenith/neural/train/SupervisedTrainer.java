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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import com.ciphertool.zenith.neural.generate.SampleGenerator;
import com.ciphertool.zenith.neural.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.ciphertool.zenith.neural.log.ConsoleProgressBar;
import com.ciphertool.zenith.neural.predict.Predictor;

@Component
@Validated
@ConfigurationProperties
public class SupervisedTrainer {
	private static Logger					log						= LoggerFactory.getLogger(SupervisedTrainer.class);

	private static final boolean			COMPUTE_SUM_OF_ERRORS	= false;

	@DecimalMin("0.0")
	@Value("${network.learningRate}")
	private Float						learningRate;

	@DecimalMin("0.0")
	@DecimalMax("1.0")
	@Value("${network.weightDecay}")
	private Float						weightDecayPercent;

	@Value("${network.trainingSamples.count}")
	private int						numberOfSamples;

	@Autowired
	private SampleGenerator generator;

	@Autowired
	private Predictor						predictor;

	@Autowired
	private BackPropagationNeuronProcessor	neuronProcessor;

	public void train(NeuralNetwork network, int batchSize) {
		ConsoleProgressBar progressBar = new ConsoleProgressBar();

		int currentBatchSize = 0;

		int i;
		long batchStart = System.currentTimeMillis();
		for (i = 0; i < numberOfSamples; i++) {
			long start = System.currentTimeMillis();

			DataSet nextSample = generator.generateTrainingSample();

			for (int j = 0; j < nextSample.getInputs().length; j ++) {
				predictor.feedForward(network, nextSample.getInputs()[j]);

				log.debug("Finished feed-forward in: {}ms", (System.currentTimeMillis() - start));

				start = System.currentTimeMillis();

				backPropagate(network, nextSample.getOutputs()[j]);

				log.debug("Finished back-propagation in: {}ms", (System.currentTimeMillis() - start));
			}

			currentBatchSize++;

			if (currentBatchSize == batchSize) {
				network.applyAccumulatedDeltas(learningRate, weightDecayPercent);

				log.info("Finished training batch {} in {}ms.", (int) ((i + 1) / batchSize), (System.currentTimeMillis()
						- batchStart));

				currentBatchSize = 0;

				batchStart = System.currentTimeMillis();
			}

			progressBar.tick((float) i, (float) numberOfSamples);
		}

		if (currentBatchSize > 0) {
			log.info("Finished training batch {} in {}ms.", (int) ((i + 1) / batchSize), (System.currentTimeMillis()
					- batchStart));

			network.applyAccumulatedDeltas(learningRate, weightDecayPercent);
		}
	}

	protected void backPropagate(NeuralNetwork network, Float[] expectedOutputs) {
		Layer outputLayer = network.getOutputLayer();

		if (expectedOutputs.length != outputLayer.getNeurons().length) {
			throw new IllegalArgumentException("The expected output size of " + expectedOutputs.length
					+ " does not match the actual output size of " + outputLayer.getNeurons().length
					+ ".  Unable to continue with back propagation step.");
		}

		Layer[] layers = network.getLayers();
		Layer fromLayer = layers[layers.length - 2];

		/*
		 * The sum of errors is not actually used by the backpropagation algorithm, but it may be useful for debugging
		 * purposes
		 */
		if (COMPUTE_SUM_OF_ERRORS) {
			// Compute sum of errors
			Float errorTotal = 0.0f;
			Float outputSumTotal = 0.0f;

			for (int i = 0; i < outputLayer.getNeurons().length; i++) {
				Neuron nextOutputNeuron = outputLayer.getNeurons()[i];

				if (network.getProblemType() == ProblemType.REGRESSION) {
					errorTotal = errorTotal + costFunctionRegression(expectedOutputs[i], nextOutputNeuron.getActivationValue());
				} else {
					errorTotal = errorTotal + costFunctionClassification(expectedOutputs[i], nextOutputNeuron.getActivationValue());
				}

				outputSumTotal = outputSumTotal + nextOutputNeuron.getOutputSum();
			}
		}

		Float[] errorDerivatives = new Float[outputLayer.getNeurons().length];
		Float[] activationDerivatives = new Float[outputLayer.getNeurons().length];

		Float[] allSums = new Float[outputLayer.getNeurons().length];

		if (network.getProblemType() == ProblemType.CLASSIFICATION) {
			for (int i = 0; i < outputLayer.getNeurons().length; i++) {
				Neuron nextOutputNeuron = outputLayer.getNeurons()[i];

				allSums[i] = nextOutputNeuron.getOutputSum();
			}
		}

		List<Future<Void>> futures = new ArrayList<>(outputLayer.getNeurons().length);

		// Compute deltas for output layer using chain rule and subtract them from current weights
		for (int i = 0; i < outputLayer.getNeurons().length; i++) {
			futures.add(neuronProcessor.processOutputNeuron(i, fromLayer, outputLayer, errorDerivatives, activationDerivatives, expectedOutputs, allSums, network.getProblemType()));
		}

		for (Future<Void> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new IllegalStateException("Unable to process output neuron.", e);
			}
		}

		Layer toLayer;
		Float[] oldErrorDerivatives;
		Float[] oldActivationDerivatives;

		// Compute deltas for hidden layers using chain rule and subtract them from current weights
		for (int i = layers.length - 2; i > 0; i--) {
			fromLayer = layers[i - 1];
			toLayer = layers[i];

			oldErrorDerivatives = errorDerivatives;
			oldActivationDerivatives = activationDerivatives;

			errorDerivatives = new Float[toLayer.getNeurons().length];
			activationDerivatives = new Float[toLayer.getNeurons().length];

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

	protected static Float costFunctionRegression(Float expected, Float actual) {
		return (float) Math.pow(expected - actual, 2) / 2.0f;
	}

	protected Float costFunctionClassification(Float expected, Float actual) {
		return (float) Math.log(actual) * expected;
	}
}
