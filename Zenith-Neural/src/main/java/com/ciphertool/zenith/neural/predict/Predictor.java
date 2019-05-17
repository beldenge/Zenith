/**
 * Copyright 2017-2019 George Belden
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.neural.log.ConsoleProgressBar;
import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.Neuron;
import com.ciphertool.zenith.neural.model.ProblemType;

@Component
public class Predictor {
	private static Logger				log	= LoggerFactory.getLogger(Predictor.class);

	@Autowired
	private FeedForwardNeuronProcessor	neuronProcessor;

	public BigDecimal[][] predict(NeuralNetwork network, BigDecimal[][] inputs) {
		Neuron[] outputLayerNeurons = network.getOutputLayer().getNeurons();

		BigDecimal[][] predictions = new BigDecimal[inputs.length][outputLayerNeurons.length];

		ConsoleProgressBar progressBar = new ConsoleProgressBar();

		for (int i = 0; i < inputs.length; i++) {
			long start = System.currentTimeMillis();

			feedForward(network, inputs[i]);

			for (int j = 0; j < outputLayerNeurons.length; j++) {
				predictions[i][j] = outputLayerNeurons[j].getActivationValue();
			}

			log.info("Finished predicting sample {} in {}ms.", i + 1, System.currentTimeMillis() - start);

			progressBar.tick((double) i, (double) inputs.length);
		}

		return predictions;
	}

	public void feedForward(NeuralNetwork network, BigDecimal[] inputs) {
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

			BigDecimal[] allSums = new BigDecimal[outputLayerNeurons.length];

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
