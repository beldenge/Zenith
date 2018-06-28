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

package com.ciphertool.zenith.neural.sample;

import com.ciphertool.zenith.neural.model.Layer;
import com.ciphertool.zenith.neural.model.LayerType;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.predict.Predictor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Min;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ConfigurationProperties(prefix = "training")
public class Sampler {
    private static Logger log	= LoggerFactory.getLogger(Predictor.class);

    @Min(1)
    private int testSampleCount;

    @Min(1)
    private int sequenceLength = 1;

    public INDArray[] sample(NeuralNetwork network) {
        INDArray[] sampleMatrices = new INDArray[testSampleCount];

        for (int i = 0; i < testSampleCount; i++) {
            long start = System.currentTimeMillis();

            sampleMatrices[i] = feedForward(network);

            log.info("Finished generating sequence {} in {}ms.", i + 1, System.currentTimeMillis() - start);
        }

        return sampleMatrices;
    }

    public INDArray feedForward(NeuralNetwork network) {
        INDArray oneHotVectors = Nd4j.create(sequenceLength, network.getLayers()[0].getNumberOfNeurons());
        INDArray toLayerActivations;
        INDArray outputSumLayer;

        for (int j = 0; j < sequenceLength; j++) {
            INDArray iterationInputs;

            if (j == 0) {
                iterationInputs = Nd4j.zeros(network.getLayers()[0].getNumberOfNeurons());
            } else {
                iterationInputs = oneHotVectors.getRow(j - 1);
            }

            // Insert the inputs, overwriting all except the bias
            network.getInputLayer().getActivations().put(NDArrayIndex.createCoveringShape(iterationInputs.shape()), iterationInputs);

            for (int i = 0; i < network.getLayers().length - 1; i++) {
                Layer fromLayer = network.getLayers()[i];
                Layer toLayer = network.getLayers()[i + 1];

                INDArray combinedInput = fromLayer.getActivations();
                INDArray combinedWeights = fromLayer.getOutgoingWeights();

                if (LayerType.RECURRENT == toLayer.getType()) {
                    // Add previous hidden-to-hidden activations and weights to the fromLayer and synapticGap matrices
                    int recurrentActivations = fromLayer.getRecurrentActivations().peek().size(1);
                    int inputActivations = fromLayer.getActivations().size(1);
                    combinedInput = Nd4j.create(1, recurrentActivations + inputActivations);
                    combinedInput.put(new INDArrayIndex[]{NDArrayIndex.interval(0, 1), NDArrayIndex.interval(0, recurrentActivations)}, fromLayer.getRecurrentActivations().peek().dup());
                    combinedInput.put(new INDArrayIndex[]{NDArrayIndex.interval(0, 1), NDArrayIndex.interval(recurrentActivations, recurrentActivations + inputActivations)}, fromLayer.getActivations().dup());

                    int nextLayerNeurons = toLayer.getNumberOfNeurons();
                    int recurrentWeights = fromLayer.getRecurrentOutgoingWeights().size(0);
                    int fromLayerWeights = fromLayer.getOutgoingWeights().size(0);
                    combinedWeights = Nd4j.create(recurrentWeights + fromLayerWeights, nextLayerNeurons);
                    combinedWeights.put(new INDArrayIndex[]{NDArrayIndex.interval(0, recurrentWeights), NDArrayIndex.interval(0, nextLayerNeurons)}, fromLayer.getRecurrentOutgoingWeights().dup());
                    combinedWeights.put(new INDArrayIndex[]{NDArrayIndex.interval(recurrentWeights, recurrentWeights + fromLayerWeights), NDArrayIndex.interval(0, nextLayerNeurons)}, fromLayer.getOutgoingWeights().dup());
                }

                toLayerActivations = toLayer.getActivations();
                outputSumLayer = toLayer.getOutputSums();

                INDArray newActivations = combinedInput.mmul(combinedWeights);

                // Get a subset of the outputSumLayer so as not to overwrite the bias neuron
                outputSumLayer.get(NDArrayIndex.all(), NDArrayIndex.interval(0, newActivations.size(1))).assign(newActivations.dup());

                if (LayerType.RECURRENT == toLayer.getType()) {
                    // Update the hidden-to-hidden activation values
                    toLayer.getRecurrentOutputSums().push(newActivations.dup());
                }

                toLayer.getActivationFunctionType().getActivationFunction().transformInputSignal(newActivations);

                // Insert the activation values, overwriting all except the bias
                toLayerActivations.put(NDArrayIndex.createCoveringShape(newActivations.shape()), newActivations.dup());

                if (LayerType.RECURRENT == toLayer.getType()) {
                    // Update the hidden-to-hidden activation values
                    fromLayer.getRecurrentActivations().push(newActivations);
                }
            }

            oneHotVectors.putRow(j, selectProbabilistically(network.getOutputLayer().getActivations()));
        }

        return oneHotVectors;
    }

    private static INDArray selectProbabilistically(INDArray activations) {
        INDArray oneHotVector = Nd4j.zeros(activations.size(1));

        float rand = ThreadLocalRandom.current().nextFloat();

        for (int i = 0; i < activations.size(1); i ++) {
            rand -= activations.getFloat(i);

            if (rand <= 0.0f) {
                oneHotVector.putScalar(i, 1.0f);

                break;
            }
        }

        return oneHotVector;
    }

    public void setTestSampleCount(int testSampleCount) {
        this.testSampleCount = testSampleCount;
    }

    public void setSequenceLength(int sequenceLength) {
        this.sequenceLength = sequenceLength;
    }
}
