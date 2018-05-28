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

import com.ciphertool.zenith.neural.generate.SampleGenerator;
import com.ciphertool.zenith.neural.log.ConsoleProgressBar;
import com.ciphertool.zenith.neural.model.*;
import com.ciphertool.zenith.neural.predict.Predictor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;

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

	@Min(0)
	@Value("${network.iterationsBetweenSaves:0}")
	private int iterationsBetweenSaves;

	@Autowired
	@Qualifier("outputFileNameWithDate")
	private String outputFileNameWithDate;

	@Autowired
	private SampleGenerator generator;

	@Autowired
	private Predictor						predictor;

	@PostConstruct
	public void validate() {
		if (iterationsBetweenSaves > 0 && outputFileNameWithDate == null) {
			throw new IllegalArgumentException("Property network.iterationsBetweenSaves was set, but property " +
					"network.output.fileName was null.  Please set network.output.fileName if saving of the network is " +
					"desired.");
		}
	}

	public void train(NeuralNetwork network, int batchSize) {
		ConsoleProgressBar progressBar = new ConsoleProgressBar();

		int currentBatchSize = 0;

		int i;
		long batchStart = System.currentTimeMillis();
		for (i = 0; i < numberOfSamples; i++) {
			long start = System.currentTimeMillis();

			DataSet nextSample = generator.generateTrainingSample();

			for (int j = 0; j < nextSample.getInputs().size(0); j ++) {
				predictor.feedForward(network, nextSample.getInputs().getRow(j));

				log.debug("Finished feed-forward in: {}ms", (System.currentTimeMillis() - start));

				start = System.currentTimeMillis();

				backPropagate(network, nextSample.getOutputs().getRow(j));

				log.debug("Finished back-propagation in: {}ms", (System.currentTimeMillis() - start));
			}

			currentBatchSize++;

			if (currentBatchSize == batchSize) {
				long applyAccumulatedDeltasTime = network.applyAccumulatedDeltas(learningRate, weightDecayPercent, currentBatchSize);
				log.debug("Finished applying accumulated deltas in {}ms", applyAccumulatedDeltasTime);

				log.info("Finished training batch {} in {}ms.", (int) ((i + 1) / batchSize), (System.currentTimeMillis()
						- batchStart));

				currentBatchSize = 0;

				batchStart = System.currentTimeMillis();
			}

			progressBar.tick((float) i, (float) numberOfSamples);

			network.incrementSamplesTrained();

			if (outputFileNameWithDate != null && iterationsBetweenSaves > 0 && ((i + 1) % iterationsBetweenSaves) == 0) {
				//NetworkMapper.saveToFile(network, outputFileNameWithDate);
			}
		}

		if (outputFileNameWithDate != null && (iterationsBetweenSaves == 0 || (i % iterationsBetweenSaves) != 0)) {
			//NetworkMapper.saveToFile(network, outputFileNameWithDate);
		}

		if (currentBatchSize > 0) {
			log.info("Finished training batch {} in {}ms.", (int) ((i + 1) / batchSize), (System.currentTimeMillis()
					- batchStart));

			long applyAccumulatedDeltasTime = network.applyAccumulatedDeltas(learningRate, weightDecayPercent, currentBatchSize);
			log.debug("Finished applying accumulated deltas in {}ms", applyAccumulatedDeltasTime);
		}
	}

	protected void backPropagate(NeuralNetwork network, INDArray expectedOutputs) {
		Layer outputLayer = network.getOutputLayer();

		if (expectedOutputs.size(1) != outputLayer.getNeurons().length) {
			throw new IllegalArgumentException("The expected output size of " + expectedOutputs.size(1)
					+ " does not match the actual output size of " + outputLayer.getNeurons().length
					+ ".  Unable to continue with back propagation step.");
		}

		/*
		 * The sum of errors is not actually used by the backpropagation algorithm, but it may be useful for debugging
		 * purposes
		 */
		if (COMPUTE_SUM_OF_ERRORS) {
			computeSumOfErrors(network, expectedOutputs);
		}

		// START - PROCESS OUTPUT LAYER
		INDArray errorDerivatives;
		INDArray activationDerivatives;

		INDArray actualOutputs = network.getActivationLayers()[network.getActivationLayers().length - 1].dup();

		// Compute deltas for output layer using chain rule and subtract them from current weights
		if (network.getProblemType() == ProblemType.REGRESSION) {
			derivativeOfCostFunctionRegression(expectedOutputs, actualOutputs);
		} else {
			derivativeOfCostFunctionClassification(expectedOutputs, actualOutputs);
		}

		errorDerivatives = actualOutputs;

		INDArray outputSums = network.getOutputSumLayers()[network.getOutputSumLayers().length - 1].dup();

		if (network.getProblemType() == ProblemType.REGRESSION) {
			outputLayer.getActivationFunctionType().getActivationFunction().calculateDerivative(outputSums);

			activationDerivatives = outputSums;
		} else {
			// For CLASSIFICATION, including softmax/cross entropy loss, the activationDerivative is accounted for in the errorDerivative
			activationDerivatives = Nd4j.ones(outputSums.shape());
		}

		INDArray outputWeights = network.getWeightLayers()[network.getWeightLayers().length - 1];
		INDArray deltas = Nd4j.ones(outputWeights.shape());
		INDArray outputSumDerivatives = network.getActivationLayers()[network.getActivationLayers().length - 2];
		deltas.muliColumnVector(outputSumDerivatives.transpose());
		deltas.muliRowVector(errorDerivatives);
		deltas.muliRowVector(activationDerivatives);

		INDArray deltaLayer = network.getAccumulatedDeltaLayers()[network.getAccumulatedDeltaLayers().length - 1];
		deltaLayer.addi(deltas);
		// END - PROCESS OUTPUT LAYER

		INDArray toLayer;
		INDArray oldErrorDerivatives;
		INDArray oldActivationDerivatives;

		// Compute deltas for hidden layers using chain rule and subtract them from current weights
		for (int i = network.getActivationLayers().length - 2; i > 0; i--) {
			outputSumDerivatives = network.getActivationLayers()[i - 1];
			toLayer = network.getActivationLayers()[i];

			oldErrorDerivatives = errorDerivatives;
			oldActivationDerivatives = activationDerivatives;

			errorDerivatives = Nd4j.create(toLayer.size(1));
			activationDerivatives = Nd4j.create(toLayer.size(1));

			for (int j = 0; j < toLayer.getNeurons().length; j++) {
				// ********** PROCESS HIDDEN NEURON **********
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

				for (int k = 0; k < outputSumDerivatives.getNeurons().length; k++) {
					Neuron nextFromNeuron = outputSumDerivatives.getNeurons()[k];

					Float outputSumDerivative = nextFromNeuron.getActivationValue();

					// For sparsely coded inputs, skipping this gives a meaningful performance gain
					Float delta = outputSumDerivative == 0.0f ? 0.0f : (errorTimesActivation * outputSumDerivative);

					Synapse nextSynapse = nextFromNeuron.getOutgoingSynapses()[j];
					nextSynapse.addDelta(delta);
				}
			}
		}
	}

	protected static Float computeSumOfErrors(NeuralNetwork network, INDArray expectedOutputs) {
		INDArray actualOutputs = network.getActivationLayers()[network.getActivationLayers().length - 1].dup();

		if (network.getProblemType() == ProblemType.REGRESSION) {
			costFunctionRegression(expectedOutputs, actualOutputs);
		} else {
			costFunctionClassification(expectedOutputs, actualOutputs);
		}

		return actualOutputs.sumNumber().floatValue();
	}

	protected static void costFunctionRegression(INDArray expectedOutputs, INDArray actualOutputs) {
		actualOutputs.rsubi(expectedOutputs);
		Transforms.pow(actualOutputs, 2, false);
		actualOutputs.divi(2.0f);
	}

	protected static void costFunctionClassification(INDArray expectedOutputs, INDArray actualOutputs) {
		Transforms.log(actualOutputs, false);
		actualOutputs.muli(expectedOutputs);
	}

	protected static void derivativeOfCostFunctionRegression(INDArray expectedOutputs, INDArray actualOutputs) {
		actualOutputs.rsubi(expectedOutputs);
		actualOutputs.negi();
	}

	protected static void derivativeOfCostFunctionClassification(INDArray expectedOutputs, INDArray actualOutputs) {
		actualOutputs.subi(expectedOutputs);
	}
}
