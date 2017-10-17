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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.activation.ActivationFunctionType;

public class NeuralNetwork {
	private BigDecimal	biasWeight;

	private ProblemType	problemType;

	private Layer[]		layers;

	protected void init(int batchSize) {
		problemType = this.getOutputLayer().getNeurons().length == 1 ? ProblemType.REGRESSION : ProblemType.CLASSIFICATION;

		Layer fromLayer;
		Layer toLayer;

		for (int i = 0; i < layers.length - 1; i++) {
			fromLayer = layers[i];
			toLayer = layers[i + 1];

			for (int j = 0; j < fromLayer.getNeurons().length; j++) {
				Neuron nextInputNeuron = fromLayer.getNeurons()[j];
				nextInputNeuron.setOutgoingSynapses(new Synapse[toLayer.getNeurons().length
						- (toLayer.hasBias() ? 1 : 0)]);

				if (nextInputNeuron.isBias()) {
					// The bias activation value is static and should never change
					nextInputNeuron.setActivationValue(biasWeight);
				}

				for (int k = 0; k < toLayer.getNeurons().length; k++) {
					Neuron nextOutputNeuron = toLayer.getNeurons()[k];

					if (nextOutputNeuron.isBias()) {
						// We don't want to create a synapse going into a bias neuron
						continue;
					}

					BigDecimal initialWeight = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble()).subtract(BigDecimal.valueOf(0.5));

					nextInputNeuron.getOutgoingSynapses()[k] = new Synapse(nextOutputNeuron, initialWeight, batchSize);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private NeuralNetwork() {
		// Exists purely for Jackson deserialization
		System.out.println("");
	}

	@Autowired
	public NeuralNetwork(int inputLayerNeurons, String[] hiddenLayers, int outputLayerNeurons, BigDecimal biasWeight,
			int batchSize) {
		this.biasWeight = biasWeight;
		boolean addBias = biasWeight != null ? true : false;

		layers = new Layer[hiddenLayers.length + 2];

		layers[0] = new Layer(inputLayerNeurons, addBias);

		for (int i = 1; i <= hiddenLayers.length; i++) {
			int separatorIndex = hiddenLayers[i - 1].indexOf(':');

			if (separatorIndex < 0) {
				throw new IllegalArgumentException(
						"The hidden layers must be represented as a comma-separated list of numberOfNeurons:activationFunctionType pairs.");
			}

			int numberOfNeurons = Integer.parseInt(hiddenLayers[i - 1].substring(0, separatorIndex));

			ActivationFunctionType activationFunctionType = ActivationFunctionType.valueOf(hiddenLayers[i
					- 1].substring(separatorIndex + 1));

			layers[i] = new Layer(numberOfNeurons, activationFunctionType, addBias);
		}

		ActivationFunctionType activationFunctionType = outputLayerNeurons == 1 ? ActivationFunctionType.LEAKY_RELU : ActivationFunctionType.SOFTMAX;

		layers[layers.length - 1] = new Layer(outputLayerNeurons, activationFunctionType, false);

		init(batchSize);
	}

	public void replaceWithExisting(NeuralNetwork network) {
		setLayers(network.getLayers());
		setBiasWeight(network.getBiasWeight());
		setProblemType(network.getProblemType());
	}

	public void applyAccumulatedDeltas(BigDecimal learningRate, BigDecimal weightDecayPercent) {
		for (int i = 0; i < layers.length - 1; i++) {
			Layer fromLayer = layers[i];

			for (int j = 0; j < fromLayer.getNeurons().length; j++) {
				Neuron nextNeuron = fromLayer.getNeurons()[j];

				for (int k = 0; k < nextNeuron.getOutgoingSynapses().length; k++) {
					Synapse nextSynapse = nextNeuron.getOutgoingSynapses()[k];

					BigDecimal sum = BigDecimal.ZERO;

					for (BigDecimal delta : nextSynapse.getAccumulatedDeltas()) {
						sum = sum.add(delta);
					}

					BigDecimal averageDelta = sum.divide(BigDecimal.valueOf(nextSynapse.getAccumulatedDeltas().size()), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

					if (learningRate != null) {
						averageDelta = averageDelta.multiply(learningRate, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
					}

					BigDecimal regularization = BigDecimal.ZERO;

					if (weightDecayPercent != null && !nextNeuron.isBias()) {
						regularization = nextSynapse.getWeight().multiply(weightDecayPercent, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

						if (learningRate != null) {
							regularization = regularization.multiply(learningRate, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
						}
					}

					nextSynapse.setWeight(nextSynapse.getWeight().subtract(averageDelta).subtract(regularization));

					nextSynapse.clearAccumulatedDeltas();
				}
			}
		}
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
	public BigDecimal getBiasWeight() {
		return biasWeight;
	}

	/**
	 * @param biasWeight
	 *            the biasWeight to set
	 */
	public void setBiasWeight(BigDecimal biasWeight) {
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
}
