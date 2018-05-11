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

package com.ciphertool.zenith.neural.io;

import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.Neuron;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NetworkMapper {
	private static Logger log = LoggerFactory.getLogger(NetworkMapper.class);

	public static String saveToFile(NeuralNetwork network, String outputFileName) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.CREATOR, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		log.info("Saving network to file: {}", outputFileName);

		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFileName), network);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to write network parameters to file: " + outputFileName, ioe);
		}

		return outputFileName;
	}

	public static NeuralNetwork loadFromFile(String fileName) {
		log.info("Loading network from file: {}", fileName);

		Path networkFilePath = Paths.get(fileName);

		ObjectMapper mapper = new ObjectMapper();

		NeuralNetwork network;

		try {
			network = mapper.readValue(networkFilePath.toFile(), NeuralNetwork.class);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to read network parameters from file: " + fileName, ioe);
		}

		// Connect Synapses to Neurons
		for (int i = 0; i < network.getLayers().length - 1; i++) {
			Layer fromLayer = network.getLayers()[i];
			Layer toLayer = network.getLayers()[i + 1];

			for (int j = 0; j < toLayer.getNeurons().length; j++) {
				Neuron toNeuron = toLayer.getNeurons()[j];

				if (toNeuron.isBias()) {
					continue;
				}

				for (int k = 0; k < fromLayer.getNeurons().length; k++) {
					Neuron fromNeuron = fromLayer.getNeurons()[k];

					fromNeuron.getOutgoingSynapses()[j].setOutGoingNeuron(toNeuron);

					if (fromNeuron.isBias() && fromNeuron.getActivationValue() == null) {
						fromNeuron.setActivationValue(network.getBiasWeight());
					}
				}
			}
		}

		return network;
	}
}
