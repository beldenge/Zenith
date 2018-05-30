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

import com.ciphertool.zenith.neural.generate.SampleGenerator;
import com.ciphertool.zenith.neural.log.ConsoleProgressBar;
import com.ciphertool.zenith.neural.model.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Min;

@Component
public class Predictor {
	private static Logger				log	= LoggerFactory.getLogger(Predictor.class);

	@Min(1)
	@Value("${network.testSamples.count}")
	private int						numberOfTests;

	@Value("${network.testSamples.marginOfError:0.01}")
	private Float				marginOfErrorRegression;

	@Autowired
	private SampleGenerator generator;

	public PredictionStats predict(NeuralNetwork network) {
		PredictionStats stats = new PredictionStats(0, 0, 0);

		ConsoleProgressBar progressBar = new ConsoleProgressBar();

		for (int i = 0; i < numberOfTests; i++) {
			long start = System.currentTimeMillis();

			DataSet nextSample = generator.generateTestSample();

			int sampleSize = nextSample.getInputs().size(0);

			for (int j = 0; j < sampleSize; j ++) {
				feedForward(network, nextSample.getInputs().getRow(j));

				INDArray outputLayerActivations = network.getActivationLayers()[network.getActivationLayers().length - 1];

				log.info("Finished predicting sample {} in {}ms.", (i * sampleSize) + j + 1, System.currentTimeMillis() - start);
				log.debug("Inputs: {}", nextSample.getInputs().getRow(j));

				compareExpectationToPrediction(network, nextSample.getOutputs().getRow(j), outputLayerActivations, stats);
			}

			progressBar.tick((float) i, (float) numberOfTests);
		}

		return stats;
	}

	private void compareExpectationToPrediction(NeuralNetwork network, INDArray outputs, INDArray predictions, PredictionStats stats) {
		boolean isIncorrect = false;

		Float highestProbability = 0.0f;
		int indexOfHighestProbability = -1;

		for (int j = 0; j < predictions.size(1); j++) {
			Float prediction = predictions.getFloat(j);
			Float expected = outputs.getFloat(j);

			log.info("Expected: {}, Prediction: {}", expected, prediction);

			if (network.getProblemType() == ProblemType.CLASSIFICATION) {
				if (highestProbability < prediction) {
					highestProbability = prediction;
					indexOfHighestProbability = j;
				}
			}

			// We can't test the exact values of 1 and 0 since the output from the network is a decimal value
			if (prediction.isNaN() || expected.isNaN() || Math.abs(prediction - expected) > marginOfErrorRegression) {
				isIncorrect = true;
			}
		}

		if (network.getProblemType() == ProblemType.CLASSIFICATION
				&& indexOfHighestProbability >= 0 && 1.0 == outputs.getFloat(indexOfHighestProbability)) {
			stats.incrementBestProbabilityCount();
		}

		if (!isIncorrect) {
			stats.incrementCorrectCount();
		}

		stats.incrementTotalPredictions();
	}

	public void feedForward(NeuralNetwork network, INDArray inputs) {
		Layer inputLayer = network.getInputLayer();

		int nonBiasNeurons = inputLayer.getNeurons().length - (inputLayer.hasBias() ? 1 : 0);

		if (inputs.size(1) != nonBiasNeurons) {
			throw new IllegalArgumentException("The sample input size of " + inputs.size(1)
					+ " does not match the input layer size of " + nonBiasNeurons
					+ ".  Unable to continue with feed forward step.");
		}

		// Insert the inputs, overwriting all except the bias
		network.getActivationLayers()[0].put(NDArrayIndex.createCoveringShape(inputs.shape()), inputs);

		INDArray fromLayer;
		INDArray synapticGap;
		INDArray toLayer;
		INDArray outputSumLayer;
		Layer[] layers = network.getLayers();

		for (int i = 0; i < layers.length - 1; i++) {
			fromLayer = network.getActivationLayers()[i];
			synapticGap = network.getWeightLayers()[i];
			toLayer = network.getActivationLayers()[i + 1];
			outputSumLayer = network.getOutputSumLayers()[i + 1];

			INDArray intermediateLayer = fromLayer.mmul(synapticGap);
			// Get a subset of the outputSumLayer so as not to overwrite the bias neuron
			outputSumLayer.get(NDArrayIndex.all(), NDArrayIndex.interval(0, intermediateLayer.size(1))).assign(intermediateLayer.dup());

			network.getLayers()[i + 1].getActivationFunctionType().getActivationFunction().transformInputSignal(intermediateLayer);

			// Insert the activation values, overwriting all except the bias
			toLayer.put(NDArrayIndex.createCoveringShape(intermediateLayer.shape()), intermediateLayer);
		}
	}
}
