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
import com.ciphertool.zenith.neural.model.*;
import com.ciphertool.zenith.neural.predict.Predictor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Validated
@ConfigurationProperties(prefix = "training")
public class SupervisedTrainer {
	private static Logger					log						= LoggerFactory.getLogger(SupervisedTrainer.class);

	private boolean computeLoss;

	@DecimalMin("0.0")
	private Float learningRate;

	@DecimalMin("0.0")
	@DecimalMax("1.0")
	private Float weightDecayPercent;

	private int trainingSampleCount;

	@Min(0)
	private int iterationsBetweenSaves = 0;

	private String outputFileNameWithDate;

	@Value("${network.outputFileName}")
	private String outputFileName;

	@Min(1)
	private int sequenceLength = 1;

	@Autowired
	private SampleGenerator generator;

	@Autowired
	private Predictor predictor;

	@PostConstruct
	public void init() {
		if (outputFileName == null) {
			if (iterationsBetweenSaves > 0) {
				throw new IllegalArgumentException("Property network.iterationsBetweenSaves was set, but property " +
						"network.outputFileName was null.  Please set network.outputFileName if saving of the network is " +
						"desired.");
			}

			return;
		}

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
		String dateText = now.format(formatter);

		String extension = outputFileName.substring(outputFileName.indexOf('.'));
		String beforeExtension = outputFileName.replace(extension, "");

		outputFileNameWithDate = beforeExtension + "-" + dateText + extension;
	}

	public void train(NeuralNetwork network, int batchSize) {
		int currentBatchSize = 0;

		int i;
		long batchStart = System.currentTimeMillis();
		Float sumOfLosses = 0.0f;
		for (i = 0; i < trainingSampleCount; i++) {
			long start = System.currentTimeMillis();

			DataSet nextSample = generator.generateTrainingSample();

			if (nextSample.getInputs().size(0) > 1) {
				throw new IllegalStateException(String.format("Only expected one sample to be generated, but %s were generated.",
						nextSample.getInputs().size(0)));
			}

			predictor.feedForward(network, nextSample.getInputs().getRow(0), nextSample.getOutputs().getRow(0), true);

			log.debug("Finished feed-forward in: {}ms.", (System.currentTimeMillis() - start));

			start = System.currentTimeMillis();

			Float loss = backPropagate(network);
			sumOfLosses += loss;

			log.debug("Finished back-propagation in: {}ms.", (System.currentTimeMillis() - start));

			currentBatchSize++;

			if (currentBatchSize == batchSize) {
				long applyAccumulatedDeltasTime = network.applyAccumulatedDeltas(learningRate, weightDecayPercent, currentBatchSize);
				log.debug("Finished applying accumulated deltas in {}ms.", applyAccumulatedDeltasTime);

				Float averageLoss = sumOfLosses / (float) currentBatchSize;

				log.info("Finished training batch {} in {}ms" + (computeLoss ? " with loss: {}." : "."), ((i + 1) / batchSize), (System.currentTimeMillis()
						- batchStart), averageLoss);

				currentBatchSize = 0;

				batchStart = System.currentTimeMillis();

				sumOfLosses = 0.0f;
			}

			network.incrementSamplesTrained();

			if (outputFileNameWithDate != null && iterationsBetweenSaves > 0 && ((i + 1) % iterationsBetweenSaves) == 0) {
				// FIXME: re-implement saving of network to file
				//NetworkMapper.saveToFile(network, outputFileNameWithDate);
			}
		}

		if (outputFileNameWithDate != null && (iterationsBetweenSaves == 0 || (i % iterationsBetweenSaves) != 0)) {
			// FIXME: re-implement saving of network to file
			//NetworkMapper.saveToFile(network, outputFileNameWithDate);
		}

		if (currentBatchSize > 0) {
			Float averageLoss = sumOfLosses / (float) currentBatchSize;
			log.info("Finished training batch {} in {}ms" + (computeLoss ? " with loss: {}." : "."), ((i + 1) / batchSize), (System.currentTimeMillis()
					- batchStart), averageLoss);

			long applyAccumulatedDeltasTime = network.applyAccumulatedDeltas(learningRate, weightDecayPercent, currentBatchSize);
			log.debug("Finished applying accumulated deltas in {}ms.", applyAccumulatedDeltasTime);
		}
	}

	protected Float backPropagate(NeuralNetwork network) {
		Float loss = 0.0f;
		Layer outputLayer = network.getOutputLayer();

		// START - PROCESS OUTPUT LAYER
		INDArray errorDerivatives;
		INDArray activationDerivatives;

		errorDerivatives = network.getAccumulatedCost();

		INDArray outputSums = network.getOutputLayer().getOutputSums().dup();

		if (network.getProblemType() == ProblemType.REGRESSION) {
			outputLayer.getActivationFunctionType().getActivationFunction().calculateDerivative(outputSums);

			activationDerivatives = outputSums;
		} else {
			// For CLASSIFICATION, including softmax/cross entropy loss, the activationDerivative is accounted for in the errorDerivative
			activationDerivatives = Nd4j.ones(outputSums.shape());
		}

		Layer secondToLast = network.getLayers()[network.getLayers().length - 2];

		INDArray outputWeights = secondToLast.getOutgoingWeights();
		INDArray deltas = Nd4j.ones(outputWeights.shape());
		INDArray outputSumDerivatives = secondToLast.getActivations();
		deltas.muliColumnVector(outputSumDerivatives.transpose());
		deltas.muliRowVector(errorDerivatives);
		deltas.muliRowVector(activationDerivatives);

		INDArray deltaLayer = secondToLast.getAccumulatedDeltas();
		deltaLayer.addi(deltas);
		// END - PROCESS OUTPUT LAYER

		Layer toLayer;
		INDArray oldErrorDerivatives;
		INDArray oldActivationDerivatives;
		INDArray recurrentDeltaLayer;

		// Compute deltas for hidden layers using chain rule and subtract them from current weights
		for (int i = network.getLayers().length - 2; i > 0; i--) {
			for (int j = 0; j < (NetworkType.RECURRENT == network.getType() ? network.getLayers()[i - 1].getRecurrentActivations().size() : 1); j++) {
				if (NetworkType.RECURRENT == network.getType()) {
					int recurrentActivations = network.getLayers()[i - 1].getRecurrentActivations().peek().size(1);
					int inputActivations = network.getLayers()[i - 1].getActivations().size(1);
					outputSumDerivatives = Nd4j.create(1, recurrentActivations + inputActivations);
					outputSumDerivatives.put(new INDArrayIndex[]{NDArrayIndex.interval(0, 1), NDArrayIndex.interval(0, recurrentActivations)}, network.getLayers()[i - 1].getRecurrentActivations().pop());
					outputSumDerivatives.put(new INDArrayIndex[]{NDArrayIndex.interval(0, 1), NDArrayIndex.interval(recurrentActivations, recurrentActivations + inputActivations)}, network.getLayers()[i - 1].getActivations());
				} else {
					outputSumDerivatives = network.getLayers()[i - 1].getActivations();
				}

				toLayer = network.getLayers()[i];

				oldErrorDerivatives = errorDerivatives;
				oldActivationDerivatives = activationDerivatives;

				if (NetworkType.RECURRENT == network.getType()) {
					outputSums = network.getLayers()[i].getRecurrentOutputSums().pop();
				} else {
					outputSums = network.getLayers()[i].getOutputSums().get(NDArrayIndex.all(), NDArrayIndex.interval(0, network.getLayers()[i].getOutputSums().size(1) - (toLayer.hasBias() ? 1 : 0))).dup();
				}

				toLayer.getActivationFunctionType().getActivationFunction().calculateDerivative(outputSums);

				activationDerivatives = outputSums;

				INDArray partialErrorDerivatives = oldErrorDerivatives.mul(oldActivationDerivatives);
				INDArray toWeightLayer = network.getLayers()[i].getOutgoingWeights();

				if (NetworkType.RECURRENT == network.getType() && j > 0) {
					toWeightLayer = network.getLayers()[i - 1].getRecurrentOutgoingWeights();
				} else {
					// Get a subset of the toWeightLayer so as to skip the bias neuron
					toWeightLayer = toWeightLayer.get(NDArrayIndex.interval(0, toWeightLayer.size(0) - (network.getLayers()[i].hasBias() ? 1 : 0)), NDArrayIndex.all());
				}

				errorDerivatives = toWeightLayer.mmul(partialErrorDerivatives.transpose()).transpose();

				outputWeights = network.getLayers()[i - 1].getOutgoingWeights();

				if (NetworkType.RECURRENT == network.getType()) {
					int recurrentOutputWeights = network.getLayers()[i - 1].getRecurrentOutgoingWeights().size(0);
					int feedForwardOutputWeights = network.getLayers()[i - 1].getOutgoingWeights().size(0);
					outputWeights = Nd4j.create(recurrentOutputWeights + feedForwardOutputWeights, network.getLayers()[i - 1].getOutgoingWeights().size(1));
					outputWeights.put(new INDArrayIndex[] {NDArrayIndex.interval(0, recurrentOutputWeights), NDArrayIndex.all()}, network.getLayers()[i - 1].getRecurrentOutgoingWeights());
					outputWeights.put(new INDArrayIndex[] {NDArrayIndex.interval(recurrentOutputWeights, recurrentOutputWeights + feedForwardOutputWeights), NDArrayIndex.all()}, network.getLayers()[i - 1].getOutgoingWeights());
				}

				deltas = Nd4j.ones(outputWeights.shape());
				deltas.muliColumnVector(outputSumDerivatives.transpose());
				deltas.muliRowVector(errorDerivatives);
				deltas.muliRowVector(activationDerivatives);

				deltaLayer = network.getLayers()[i - 1].getAccumulatedDeltas();

				if (NetworkType.RECURRENT == network.getType()) {
					recurrentDeltaLayer = network.getLayers()[i - 1].getRecurrentAccumulatedDeltas();
					recurrentDeltaLayer.addi(deltas.get(NDArrayIndex.interval(0, recurrentDeltaLayer.size(0)), NDArrayIndex.all()));
					deltas = deltas.get(NDArrayIndex.interval(recurrentDeltaLayer.size(0), deltas.size(0)), NDArrayIndex.all());
				}

				deltaLayer.addi(deltas);
			}
		}

		return loss;
	}

	public void setLearningRate(Float learningRate) {
		this.learningRate = learningRate;
	}

	public void setWeightDecayPercent(Float weightDecayPercent) {
		this.weightDecayPercent = weightDecayPercent;
	}

	public void setTrainingSampleCount(int trainingSampleCount) {
		this.trainingSampleCount = trainingSampleCount;
	}

	public void setIterationsBetweenSaves(int iterationsBetweenSaves) {
		this.iterationsBetweenSaves = iterationsBetweenSaves;
	}

	public void setComputeLoss(boolean computeLoss) {
		this.computeLoss = computeLoss;
	}

	public void setSequenceLength(int sequenceLength) {
		this.sequenceLength = sequenceLength;
	}
}
