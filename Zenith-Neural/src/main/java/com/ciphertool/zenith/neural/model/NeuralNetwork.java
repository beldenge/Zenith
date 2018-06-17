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

package com.ciphertool.zenith.neural.model;

import com.ciphertool.zenith.neural.activation.ActivationFunctionType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class NeuralNetwork {
	private Float biasWeight;

	private ProblemType	problemType;

	private NetworkType type = NetworkType.FEED_FORWARD;

	private Layer[]	layers;

	private long samplesTrained = 0;

	@SuppressWarnings("unused")
	private NeuralNetwork() {
		// Exists purely for Jackson deserialization
	}

	public NeuralNetwork(LayerConfiguration[] layerConfigurations, Float biasWeight) {
		this.biasWeight = biasWeight;
		boolean addBias = biasWeight != null ? true : false;

		layers = new Layer[layerConfigurations.length];

		layers[0] = new Layer(layerConfigurations[0].getNumberOfNeurons(), addBias, LayerType.FEED_FORWARD);
		int nextLayerNeurons = layerConfigurations[1].getNumberOfNeurons();
		layers[0].setAccumulatedDeltas(Nd4j.create(layers[0].getActivations().size(1), nextLayerNeurons));
		layers[0].setOutgoingWeights(Nd4j.create(layers[0].getActivations().size(1), nextLayerNeurons));

		for (int i = 1; i < layerConfigurations.length - 1; i++) {
			int currentLayerNeurons = layerConfigurations[i].getNumberOfNeurons();

			ActivationFunctionType activationFunctionType = layerConfigurations[i].getActivationType();

			LayerType layerType = layerConfigurations[i].getLayerType();

			layers[i] = new Layer(currentLayerNeurons, activationFunctionType, addBias, layerType);
			nextLayerNeurons = layerConfigurations[i + 1].getNumberOfNeurons();
			layers[i].setAccumulatedDeltas(Nd4j.create(layers[i].getActivations().size(1), nextLayerNeurons));
			layers[i].setOutgoingWeights(Nd4j.create(layers[i].getActivations().size(1), nextLayerNeurons));

			if (LayerType.RECURRENT == layers[i].getType()) {
				type = NetworkType.RECURRENT;

				// Initialize recurrent inputs to zeros
				int previousLayerNeurons = layerConfigurations[i - 1].getNumberOfNeurons();
				layers[i - 1].getRecurrentActivations().push(Nd4j.zeros(1, currentLayerNeurons));
				layers[i - 1].setRecurrentAccumulatedDeltas(Nd4j.create(currentLayerNeurons, currentLayerNeurons));
				layers[i - 1].setRecurrentOutgoingWeights(Nd4j.create(currentLayerNeurons, currentLayerNeurons));
			}
		}

		LayerConfiguration outputLayerConfiguration = layerConfigurations[layerConfigurations.length - 1];
		int outputLayerNeurons = outputLayerConfiguration.getNumberOfNeurons();

		ActivationFunctionType activationFunctionType = outputLayerNeurons == 1 ? ActivationFunctionType.LEAKY_RELU : ActivationFunctionType.SOFTMAX;

		layers[layers.length - 1] = new Layer(outputLayerNeurons, activationFunctionType, false, LayerType.FEED_FORWARD);

		layers[layers.length - 2].setAccumulatedDeltas(Nd4j.create(layers[layers.length - 2].getActivations().size(1), outputLayerNeurons));
		layers[layers.length - 2].setOutgoingWeights(Nd4j.create(layers[layers.length - 2].getActivations().size(1), outputLayerNeurons));

		problemType = outputLayerNeurons == 1 ? ProblemType.REGRESSION : ProblemType.CLASSIFICATION;

		if (biasWeight != null) {
			for (int i = 0; i < layers.length - 1; i++) {
				layers[i].getActivations().putScalar(layers[i].getActivations().size(1) - 1, biasWeight);
			}
		}
	}

	/**
	 * Construct a NeuralNetwork loaded from file.
	 *
	 * @param network the NeuralNetwork from file
	 */
	public NeuralNetwork(NeuralNetwork network) {
		this.layers = network.layers;
		this.biasWeight = network.biasWeight;
		this.problemType = network.problemType;
		this.samplesTrained = network.samplesTrained;
	}

	public long applyAccumulatedDeltas(Float learningRate, Float weightDecayPercent, int deltaCount) {
		long start = System.currentTimeMillis();

		for (int i = 0; i < layers.length - 1; i++) {
			INDArray accumulatedDeltas = layers[i].getAccumulatedDeltas();
			INDArray weights = layers[i].getOutgoingWeights();

			accumulatedDeltas.divi(deltaCount);

			if (learningRate != null) {
				accumulatedDeltas.muli(learningRate);
			}

			if (weightDecayPercent != null && weightDecayPercent != 0.0f) {
				INDArray regularization = weights.mul(weightDecayPercent);

				if (learningRate != null) {
					regularization.muli(learningRate);
				}

				weights.subi(regularization);
			}

			weights.subi(accumulatedDeltas);
			accumulatedDeltas.assign(0.0f);
		}

		return System.currentTimeMillis() - start;
	}

	/**
	 * @return the problemType
	 */
	public ProblemType getProblemType() {
		return problemType;
	}

	/**
	 * @return the inputLayer
	 */
	public Layer getInputLayer() {
		return layers[0];
	}

	/**
	 * @return the layers
	 */
	public Layer[] getLayers() {
		return layers;
	}

	/**
	 * @return the outputLayer
	 */
	public Layer getOutputLayer() {
		return layers[layers.length - 1];
	}

	/**
	 * @return the biasWeight
	 */
	public Float getBiasWeight() {
		return biasWeight;
	}

	/**
	 * @param biasWeight
	 *            the biasWeight to set
	 */
	public void setBiasWeight(Float biasWeight) {
		this.biasWeight = biasWeight;
	}

	/**
	 * @param problemType
	 *            the problemType to set
	 */
	public void setProblemType(ProblemType problemType) {
		this.problemType = problemType;
	}

	/**
	 * @param layers
	 *            the layers to set
	 */
	public void setLayers(Layer[] layers) {
		this.layers = layers;
	}

	/**
	 * @return the number of samples trained
	 */
	public long getSamplesTrained() {
		return samplesTrained;
	}

	/**
	 * @param samplesTrained the samplesTrained to set
	 */
	public void setSamplesTrained(long samplesTrained) {
		this.samplesTrained = samplesTrained;
	}

	public void incrementSamplesTrained() {
		this.samplesTrained ++;
	}

	public NetworkType getType() {
		return type;
	}
}
