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
import com.ciphertool.zenith.neural.model.LayerType;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class NetworkMapper {
	private static Logger log = LoggerFactory.getLogger(NetworkMapper.class);

	@Autowired
	private ObjectMapper objectMapper;

	public String saveToFile(NeuralNetwork network, String outputFileName) {
		objectMapper.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.CREATOR, Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

		log.info("Saving network to file: {}", outputFileName);

		try {
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFileName), network);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to write network parameters to file: " + outputFileName, ioe);
		}

		return outputFileName;
	}

	public NeuralNetwork loadFromFile(String fileName) {
		log.info("Loading network from file: {}", fileName);

		Path networkFilePath = Paths.get(fileName);

		NeuralNetwork network;

		try {
			network = objectMapper.readValue(networkFilePath.toFile(), NeuralNetwork.class);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to read network parameters from file: " + fileName, ioe);
		}

		// Initialize layer activations and accumulated deltas
		for (int i = 0; i < network.getLayers().length; i ++) {
			Layer layer = network.getLayers()[i];
			layer.setActivations(Nd4j.create(layer.getNumberOfNeurons() + (layer.hasBias() ? 1 : 0)));
			layer.setOutputSums(Nd4j.create(layer.getNumberOfNeurons() + (layer.hasBias() ? 1 : 0)));

			if (i < network.getLayers().length - 1) {
                layer.setAccumulatedDeltas(Nd4j.create(layer.getOutgoingWeights().shape()));
            }

			if (LayerType.RECURRENT == layer.getType()) {
				Layer lastLayer = network.getLayers()[i - 1];
				lastLayer.getRecurrentActivations().push(Nd4j.zeros(1, layer.getNumberOfNeurons()));

                if (i < network.getLayers().length - 1) {
                    lastLayer.setRecurrentAccumulatedDeltas(Nd4j.create(layer.getOutgoingWeights().shape()));
                }
			}
		}

		return network;
	}
}
