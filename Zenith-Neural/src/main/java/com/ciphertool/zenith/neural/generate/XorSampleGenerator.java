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

import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.neural.model.DataSet;

@Component
@Profile("xor")
public class XorSampleGenerator implements SampleGenerator {
	@Min(1)
	@Value("${network.layers.input}")
	private int	inputLayerNeurons;

	@Min(1)
	@Value("${network.layers.output}")
	private int	outputLayerNeurons;

	@Override
	public DataSet generateTrainingSamples(int count) {
		return generate(count);
	}

	@Override
	public DataSet generateTestSamples(int count) {
		return generate(count);
	}

	protected DataSet generate(int count) {
		int inputLayerSize = inputLayerNeurons;
		int outputLayerSize = outputLayerNeurons;

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
