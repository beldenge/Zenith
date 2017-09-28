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

package com.ciphertool.zenith.neural.generate;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.neural.model.DataSet;
import com.ciphertool.zenith.neural.model.NeuralNetwork;

@Component
public class SampleGenerator {
	@Autowired
	private NeuralNetwork network;

	public DataSet generate(int count) {
		int inputLayerSize = network.getInputLayer().getNeurons().length;
		int outputLayerSize = network.getOutputLayer().getNeurons().length;
		BigDecimal[][] inputs = new BigDecimal[count][inputLayerSize];
		BigDecimal[][] outputs = new BigDecimal[count][outputLayerSize];

		for (int i = 0; i < count; i++) {
			for (int j = 0; j < inputLayerSize; j++) {
				inputs[i][j] = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(2));
			}

			outputs[i] = new BigDecimal[] { xor(inputs[i]) };
		}

		return new DataSet(inputs, outputs);
	}

	protected BigDecimal xor(BigDecimal[] values) {
		if (values.length != 2) {
			throw new IllegalArgumentException("Exclusive or expects only two values, but found " + values.length
					+ ".  Unable to continue.");
		}

		return values[0].equals(values[1]) ? BigDecimal.ZERO : BigDecimal.ONE;
	}
}
