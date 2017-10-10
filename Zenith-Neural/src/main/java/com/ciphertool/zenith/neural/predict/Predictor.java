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

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.neural.log.ConsoleProgressBar;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.Neuron;

@Component
public class Predictor {
	private static Logger	log	= LoggerFactory.getLogger(Predictor.class);

	@Autowired
	private NeuralNetwork	network;

	public BigDecimal[][] predict(BigDecimal[][] inputs) {
		Neuron[] outputLayerNeurons = network.getOutputLayer().getNeurons();

		BigDecimal[][] predictions = new BigDecimal[inputs.length][outputLayerNeurons.length];

		ConsoleProgressBar progressBar = new ConsoleProgressBar();

		for (int i = 0; i < inputs.length; i++) {
			long start = System.currentTimeMillis();

			network.feedForward(inputs[i]);

			for (int j = 0; j < outputLayerNeurons.length; j++) {
				predictions[i][j] = outputLayerNeurons[j].getActivationValue();
			}

			log.info("Finished predicting sample {} in {}ms.", i + 1, System.currentTimeMillis() - start);

			progressBar.tick((double) i, (double) inputs.length);
		}

		return predictions;
	}
}
