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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

	@Autowired
	private FeedForwardNeuronProcessor	neuronProcessor;

	public PredictionStats predict(NeuralNetwork network) {
		PredictionStats stats = new PredictionStats(0, 0, 0);

		Neuron[] outputLayerNeurons = network.getOutputLayer().getNeurons();

		ConsoleProgressBar progressBar = new ConsoleProgressBar();

		for (int i = 0; i < numberOfTests; i++) {
			long start = System.currentTimeMillis();

			DataSet nextSample = generator.generateTestSample();

			int sampleSize = nextSample.getInputs().length;

			Float[][] predictions = new Float[sampleSize][outputLayerNeurons.length];

			for (int j = 0; j < sampleSize; j ++) {
				feedForward(network, nextSample.getInputs()[j]);

				for (int k = 0; k < outputLayerNeurons.length; k++) {
					predictions[j][k] = outputLayerNeurons[k].getActivationValue();
				}

				log.info("Finished predicting sample {} in {}ms.", (i * sampleSize) + j + 1, System.currentTimeMillis() - start);

				compareExpectationToPrediction(network, nextSample.getInputs()[j], nextSample.getOutputs()[j], predictions[j], stats);
			}

			progressBar.tick((float) i, (float) numberOfTests);
		}

		return stats;
	}

	private void compareExpectationToPrediction(NeuralNetwork network, Float[] inputs, Float[] outputs, Float[] predictions, PredictionStats stats) {
		boolean isIncorrect = false;

		log.info("Inputs: {}", Arrays.toString(inputs));

		Float highestProbability = 0.0f;
		int indexOfHighestProbability = -1;

		for (int j = 0; j < predictions.length; j++) {
			Float prediction = predictions[j];
			Float expected = outputs[j];

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
				&& indexOfHighestProbability >= 0 && 1.0 == outputs[indexOfHighestProbability]) {
			stats.incrementBestProbabilityCount();
		}

		if (!isIncorrect) {
			stats.incrementCorrectCount();
		}

		stats.incrementTotalPredictions();
	}

	public void feedForward(NeuralNetwork network, Float[] inputs) {
		Layer inputLayer = network.getInputLayer();

		int nonBiasNeurons = inputLayer.getNeurons().length - (inputLayer.hasBias() ? 1 : 0);

		if (inputs.length != nonBiasNeurons) {
			throw new IllegalArgumentException("The sample input size of " + inputs.length
					+ " does not match the input layer size of " + inputLayer.getNeurons().length
					+ ".  Unable to continue with feed forward step.");
		}

		for (int i = 0; i < nonBiasNeurons; i++) {
			inputLayer.getNeurons()[i].setActivationValue(inputs[i]);
		}

		Layer fromLayer;
		Layer toLayer;
		Layer[] layers = network.getLayers();

		for (int i = 0; i < layers.length - 1; i++) {
			fromLayer = layers[i];
			toLayer = layers[i + 1];

			List<Future<Void>> futures = new ArrayList<>(toLayer.getNeurons().length);

			for (int j = 0; j < toLayer.getNeurons().length; j++) {
				futures.add(neuronProcessor.processNeuron(network, j, toLayer, fromLayer));
			}

			for (Future<Void> future : futures) {
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new IllegalStateException("Unable to process neuron.", e);
				}
			}
		}

		if (network.getProblemType() == ProblemType.CLASSIFICATION) {
			Neuron[] outputLayerNeurons = network.getOutputLayer().getNeurons();

			Float[] allSums = new Float[outputLayerNeurons.length];

			for (int i = 0; i < outputLayerNeurons.length; i++) {
				Neuron nextOutputNeuron = outputLayerNeurons[i];

				allSums[i] = nextOutputNeuron.getOutputSum();
			}

			List<Future<Void>> futures = new ArrayList<>(outputLayerNeurons.length);

			for (int i = 0; i < outputLayerNeurons.length; i++) {
				futures.add(neuronProcessor.processOutputNeuron(network, i, allSums));
			}

			for (Future<Void> future : futures) {
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new IllegalStateException("Unable to process output neuron.", e);
				}
			}
		}
	}
}
