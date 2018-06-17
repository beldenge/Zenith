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
import com.ciphertool.zenith.neural.model.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Min;

@Component
@ConfigurationProperties(prefix = "training")
public class Predictor {
	private static Logger				log	= LoggerFactory.getLogger(Predictor.class);

	@Min(1)
	private int testSampleCount;

	@Min(1)
	private int sequenceLength = 1;

	private Float marginOfError = 0.01f;

	@Autowired
	private SampleGenerator generator;

	public PredictionStats predict(NeuralNetwork network) {
		PredictionStats stats = new PredictionStats(0, 0, 0);

		for (int i = 0; i < testSampleCount; i++) {
			long start = System.currentTimeMillis();

			DataSet nextSample = generator.generateTestSample();

			if (nextSample.getInputs().size(0) > 1) {
				throw new IllegalStateException(String.format("Only expected one sample to be generated, but %s were generated.",
						nextSample.getInputs().size(0)));
			}

			feedForward(network, nextSample.getInputs().getRow(0), nextSample.getOutputs().getRow(0), false);

			INDArray outputLayerActivations = network.getOutputLayer().getActivations();

			log.info("Finished predicting sample {} in {}ms.", i + 1, System.currentTimeMillis() - start);
			log.debug("Inputs: {}", nextSample.getInputs().getRow(0));

			compareExpectationToPrediction(network, nextSample.getOutputs().getRow(0), outputLayerActivations, stats);
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
			if (prediction.isNaN() || expected.isNaN() || Math.abs(prediction - expected) > marginOfError) {
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

	public void feedForward(NeuralNetwork network, INDArray inputs, INDArray expectedOutputs, boolean computeCost) {
		network.setAccumulatedCost(null);
		INDArray toLayerActivations;
		INDArray outputSumLayer;

		int sequenceIterations = (NetworkType.RECURRENT == network.getType() ? sequenceLength : 1);
		int startIndex = 0;

		for (int j = 0; j < sequenceIterations; j ++) {
			INDArray iterationInputs = inputs;

			if (NetworkType.RECURRENT == network.getType()) {
				if (j == 0) {
					iterationInputs = Nd4j.zeros(network.getLayers()[0].getNeurons());
				} else {
					iterationInputs = inputs.get(NDArrayIndex.all(), NDArrayIndex.interval(startIndex, startIndex + network.getLayers()[0].getNeurons()));
					startIndex += network.getLayers()[0].getNeurons();
				}
			}

			// Insert the inputs, overwriting all except the bias
			network.getInputLayer().getActivations().put(NDArrayIndex.createCoveringShape(iterationInputs.shape()), iterationInputs);

			for (int i = 0; i < network.getLayers().length - 1; i++) {
				Layer fromLayer = network.getLayers()[i];
				Layer toLayer = network.getLayers()[i + 1];

				INDArray combinedInput = fromLayer.getActivations();
				INDArray combinedWeights = fromLayer.getOutgoingWeights();

				if (LayerType.RECURRENT == toLayer.getType()) {
					// Add previous hidden-to-hidden activations and weights to the fromLayer and synapticGap matrices
					int recurrentActivations = fromLayer.getRecurrentActivations().peek().size(1);
					int inputActivations = fromLayer.getActivations().size(1);
					combinedInput = Nd4j.create(1, recurrentActivations + inputActivations);
					combinedInput.put(new INDArrayIndex[] {NDArrayIndex.interval(0, 1), NDArrayIndex.interval(0, recurrentActivations)}, fromLayer.getRecurrentActivations().peek().dup());
					combinedInput.put(new INDArrayIndex[] {NDArrayIndex.interval(0, 1), NDArrayIndex.interval(recurrentActivations, recurrentActivations + inputActivations)}, fromLayer.getActivations().dup());

					int nextLayerNeurons = toLayer.getNeurons() + (toLayer.hasBias() ? 1 : 0);
					int recurrentWeights = fromLayer.getRecurrentOutgoingWeights().size(0);
					int fromLayerWeights = fromLayer.getOutgoingWeights().size(0);
					combinedWeights = Nd4j.create(recurrentWeights + fromLayerWeights,  nextLayerNeurons);
					combinedWeights.put(new INDArrayIndex[] {NDArrayIndex.interval(0, recurrentWeights), NDArrayIndex.interval(0, nextLayerNeurons)}, fromLayer.getRecurrentOutgoingWeights().dup());
					combinedWeights.put(new INDArrayIndex[] {NDArrayIndex.interval(recurrentWeights, recurrentWeights + fromLayerWeights), NDArrayIndex.interval(0, nextLayerNeurons)}, fromLayer.getOutgoingWeights().dup());
				}

				toLayerActivations = toLayer.getActivations();
				outputSumLayer = toLayer.getOutputSums();

				INDArray newActivations = combinedInput.mmul(combinedWeights);

				// Get a subset of the outputSumLayer so as not to overwrite the bias neuron
				outputSumLayer.get(NDArrayIndex.all(), NDArrayIndex.interval(0, newActivations.size(1))).assign(newActivations.dup());

				if (LayerType.RECURRENT == toLayer.getType()) {
					// Update the hidden-to-hidden activation values
					toLayer.getRecurrentOutputSums().push(newActivations.dup());
				}

				toLayer.getActivationFunctionType().getActivationFunction().transformInputSignal(newActivations);

				// Insert the activation values, overwriting all except the bias
				toLayerActivations.put(NDArrayIndex.createCoveringShape(newActivations.shape()), newActivations.dup());

				if (LayerType.RECURRENT == toLayer.getType()) {
					// Update the hidden-to-hidden activation values
					fromLayer.getRecurrentActivations().push(newActivations);
				}
			}

			if (computeCost) {
				INDArray actualOutputs = network.getOutputLayer().getActivations().dup();

				if (NetworkType.RECURRENT == network.getType()) {
					expectedOutputs = inputs.get(NDArrayIndex.all(), NDArrayIndex.interval(startIndex, startIndex + network.getLayers()[0].getNeurons()));
				}

				// Compute deltas for output layer using chain rule
				if (network.getProblemType() == ProblemType.REGRESSION) {
					CostFunctions.derivativeOfCostFunctionRegression(expectedOutputs, actualOutputs);
				} else {
					CostFunctions.derivativeOfCostFunctionClassification(expectedOutputs, actualOutputs);
				}

				if (network.getAccumulatedCost() == null) {
					network.setAccumulatedCost(actualOutputs);
				} else {
					network.getAccumulatedCost().addi(actualOutputs);
				}
			}
        }
	}

	public void setTestSampleCount(int testSampleCount) {
		this.testSampleCount = testSampleCount;
	}

	public void setMarginOfError(Float marginOfError) {
		this.marginOfError = marginOfError;
	}

	public void setSequenceLength(int sequenceLength) {
		this.sequenceLength = sequenceLength;
	}
}
