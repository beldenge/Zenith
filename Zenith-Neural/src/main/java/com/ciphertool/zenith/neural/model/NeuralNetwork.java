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
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.activation.ActivationFunction;

@Component
public class NeuralNetwork {
	@Autowired
	private ActivationFunction	activationFunction;

	private Layer[]				layers;

	@PostConstruct
	public void init() {
		Layer fromLayer;
		Layer toLayer;

		for (int i = 0; i < layers.length - 1; i++) {
			fromLayer = layers[i];
			toLayer = layers[i + 1];

			for (int j = 0; j < fromLayer.getNeurons().length; j++) {
				Neuron nextInputNeuron = fromLayer.getNeurons()[j];
				nextInputNeuron.setOutgoingSynapses(new Synapse[toLayer.getNeurons().length]);

				for (int k = 0; k < toLayer.getNeurons().length; k++) {
					BigDecimal initialWeight = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble());

					nextInputNeuron.getOutgoingSynapses()[k] = new Synapse(toLayer.getNeurons()[k], initialWeight);
				}
			}
		}
	}

	public NeuralNetwork(@Value("${network.layers.input}") int inputLayerNeurons,
			@Value("${network.layers.hidden}") int[] hiddenLayersNeurons,
			@Value("${network.layers.output}") int outputLayerNeurons) {

		layers = new Layer[hiddenLayersNeurons.length + 2];

		layers[0] = new Layer(inputLayerNeurons);

		for (int i = 1; i <= hiddenLayersNeurons.length; i++) {
			layers[i] = new Layer(hiddenLayersNeurons[i - 1]);
		}

		layers[layers.length - 1] = new Layer(outputLayerNeurons);
	}

	public void feedForward(BigDecimal[] inputs) {
		Layer inputLayer = this.getInputLayer();

		if (inputs.length != inputLayer.getNeurons().length) {
			throw new IllegalArgumentException("The sample input size of " + inputs.length
					+ " does not match the input layer size of " + inputLayer.getNeurons().length
					+ ".  Unable to continue with feed forward step.");
		}

		for (int i = 0; i < inputLayer.getNeurons().length; i++) {
			inputLayer.getNeurons()[i].setActivationValue(inputs[i]);
		}

		Layer fromLayer;
		Layer toLayer;
		Layer[] layers = this.getLayers();

		for (int i = 0; i < layers.length - 1; i++) {
			fromLayer = layers[i];
			toLayer = layers[i + 1];

			for (int j = 0; j < toLayer.getNeurons().length; j++) {
				BigDecimal sum = BigDecimal.ZERO;

				for (int k = 0; k < fromLayer.getNeurons().length; k++) {
					Neuron nextInputNeuron = fromLayer.getNeurons()[k];

					Synapse nextSynapse = nextInputNeuron.getOutgoingSynapses()[j];

					sum = sum.add(nextInputNeuron.getActivationValue().multiply(nextSynapse.getWeight(), MathConstants.PREC_10_HALF_UP));
				}

				Neuron nextOutputNeuron = toLayer.getNeurons()[j];

				nextOutputNeuron.setOutputSum(sum);
				nextOutputNeuron.setActivationValue(activationFunction.transformInputSignal(sum));
			}
		}
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
}
