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

import com.ciphertool.zenith.neural.model.DataSet;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Validated
@ConfigurationProperties
@Profile("xor")
public class XorSampleGenerator implements SampleGenerator {
	@Min(1)
	@Value("${network.inputLayerNeurons}")
	private int	inputLayerNeurons;

	@Min(1)
	@Value("${network.outputLayerNeurons}")
	private int	outputLayerNeurons;

	@Override
	public DataSet generateTrainingSample() {
		return generateOne();
	}

	@Override
	public DataSet generateTestSample() {
		return generateOne();
	}

	@Override
	public void resetSamples(){
		// Nothing to do
	}

	public DataSet generateOne() {
		INDArray inputs = Nd4j.create(inputLayerNeurons);
		INDArray outputs = Nd4j.create(outputLayerNeurons);

		for (int j = 0; j < inputLayerNeurons; j++) {
			inputs.putScalar(0, j, (float) ThreadLocalRandom.current().nextInt(2));
		}

		outputs.putScalar(0, 0, xor(inputs.getRow(0)));

		return new DataSet(inputs, outputs);
	}

	protected Float xor(INDArray values) {
		if (values.size(1) != 2) {
			throw new IllegalArgumentException("Exclusive or expects only two values, but found " + values.size(1)
					+ ".  Unable to continue.");
		}

		return values.getFloat(0) == values.getFloat(1) ? 0.0f : 1.0f;
	}
}
