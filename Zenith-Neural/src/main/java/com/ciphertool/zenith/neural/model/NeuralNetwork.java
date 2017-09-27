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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NeuralNetwork {
	private Layer	inputLayer;
	private Layer[]	hiddenLayers;
	private Layer	outputLayer;

	@PostConstruct
	public void init() {
		Layer fromLayer;
		Layer toLayer;

		for (int i = 0; i < hiddenLayers.length + 1; i++) {
			fromLayer = i == 0 ? inputLayer : hiddenLayers[i - 1];
			toLayer = i < hiddenLayers.length ? hiddenLayers[i] : outputLayer;

			for (int j = 0; j < fromLayer.getNeurons().length; j++) {
				Neuron nextInputNeuron = fromLayer.getNeurons()[j];
				nextInputNeuron.setOutgoingSynapses(new Synapse[toLayer.getNeurons().length]);

				for (int k = 0; k < toLayer.getNeurons().length; k++) {
					BigDecimal initialWeight = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble());

					nextInputNeuron.getOutgoingSynapses()[k] = new Synapse(toLayer.getNeurons()[k], initialWeight);
				}
			}
		}

		System.out.println("made it!");
	}

	public NeuralNetwork(@Value("${network.layers.input}") int inputLayerNeurons,
			@Value("${network.layers.hidden}") int[] hiddenLayersNeurons,
			@Value("${network.layers.output}") int outputLayerNeurons) {
		inputLayer = new Layer(inputLayerNeurons);

		hiddenLayers = new Layer[hiddenLayersNeurons.length];

		for (int i = 0; i < hiddenLayersNeurons.length; i++) {
			hiddenLayers[i] = new Layer(hiddenLayersNeurons[i]);
		}

		outputLayer = new Layer(outputLayerNeurons);
	}

	/**
	 * @return the inputLayer
	 */
	public Layer getInputLayer() {
		return inputLayer;
	}

	/**
	 * @return the hiddenLayers
	 */
	public Layer[] getHiddenLayers() {
		return hiddenLayers;
	}

	/**
	 * @return the outputLayer
	 */
	public Layer getOutputLayer() {
		return outputLayer;
	}
}
