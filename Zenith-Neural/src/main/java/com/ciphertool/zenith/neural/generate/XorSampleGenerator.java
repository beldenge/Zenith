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

import java.util.concurrent.ThreadLocalRandom;

import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.ciphertool.zenith.neural.model.DataSet;

@Component
@Validated
@ConfigurationProperties
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
	public DataSet generateTrainingSample() {
		return generateOne();
	}

	@Override
	public DataSet generateTestSamples(int count) {
		return generate(count);
	}

	@Override
	public DataSet generateTestSample() {
		return generateOne();
	}

	protected DataSet generate(int count) {
		int inputLayerSize = inputLayerNeurons;
		int outputLayerSize = outputLayerNeurons;

		Double[][] inputs = new Double[count][inputLayerSize];
		Double[][] outputs = new Double[count][outputLayerSize];

		for (int i = 0; i < count; i++) {
			DataSet next = generateOne();

			inputs[i] = next.getInputs()[0];
			outputs[i] = next.getOutputs()[0];
		}

		return new DataSet(inputs, outputs);
	}

	public DataSet generateOne() {
		int inputLayerSize = inputLayerNeurons;
		int outputLayerSize = outputLayerNeurons;

		Double[][] inputs = new Double[1][inputLayerSize];
		Double[][] outputs = new Double[1][outputLayerSize];

		for (int j = 0; j < inputLayerSize; j++) {
			inputs[0][j] = (double) ThreadLocalRandom.current().nextInt(2);
		}

		outputs[0] = new Double[] { xor(inputs[0]) };

		return new DataSet(inputs, outputs);
	}

	protected Double xor(Double[] values) {
		if (values.length != 2) {
			throw new IllegalArgumentException("Exclusive or expects only two values, but found " + values.length
					+ ".  Unable to continue.");
		}

		return values[0] == values[1] ? 0.0 : 1.0;
	}
}
