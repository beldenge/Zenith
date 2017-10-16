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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.activation.ActivationFunctionType;
import com.ciphertool.zenith.neural.predict.FeedForwardNeuronProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@Validated
@Component
@ConfigurationProperties
public class NeuralNetwork {
	private static Logger				log	= LoggerFactory.getLogger(NeuralNetwork.class);

	private BigDecimal					biasWeight;

	private ProblemType					problemType;

	@DecimalMin("0.0")
	@Value("${network.learningRate}")
	private BigDecimal					learningRate;

	@NotBlank
	@Value("${network.output.fileName}")
	private String						outputFileName;

	@Min(1)
	@Value("${network.batchSize}")
	private int							batchSize;

	@DecimalMin("0.0")
	@DecimalMax("1.0")
	@Value("${network.weightDecay}")
	private BigDecimal					weightDecayPercent;

	@Autowired
	private FeedForwardNeuronProcessor	neuronProcessor;

	private Layer[]						layers;

	@PostConstruct
	public void init() {
		problemType = this.getOutputLayer().getNeurons().length == 1 ? ProblemType.REGRESSION : ProblemType.CLASSIFICATION;

		Layer fromLayer;
		Layer toLayer;

		for (int i = 0; i < layers.length - 1; i++) {
			fromLayer = layers[i];
			toLayer = layers[i + 1];

			for (int j = 0; j < fromLayer.getNeurons().length; j++) {
				Neuron nextInputNeuron = fromLayer.getNeurons()[j];
				nextInputNeuron.setOutgoingSynapses(new Synapse[toLayer.getNeurons().length
						- (toLayer.hasBias() ? 1 : 0)]);

				if (nextInputNeuron.isBias()) {
					// The bias activation value is static and should never change
					nextInputNeuron.setActivationValue(biasWeight);
				}

				for (int k = 0; k < toLayer.getNeurons().length; k++) {
					Neuron nextOutputNeuron = toLayer.getNeurons()[k];

					if (nextOutputNeuron.isBias()) {
						// We don't want to create a synapse going into a bias neuron
						continue;
					}

					BigDecimal initialWeight = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble());

					nextInputNeuron.getOutgoingSynapses()[k] = new Synapse(nextOutputNeuron, initialWeight, batchSize);
				}
			}
		}
	}

	public NeuralNetwork(@Min(1) @Value("${network.layers.input}") int inputLayerNeurons,
			@NotBlank @Value("${network.layers.hidden}") String[] hiddenLayers,
			@NotBlank @Value("${network.layers.output}") String outputLayer,
			@Value("${network.bias.weight}") BigDecimal biasWeight) {
		this.biasWeight = biasWeight;
		boolean addBias = biasWeight != null ? true : false;

		layers = new Layer[hiddenLayers.length + 2];

		layers[0] = new Layer(inputLayerNeurons, addBias);

		for (int i = 1; i <= hiddenLayers.length; i++) {
			int separatorIndex = hiddenLayers[i - 1].indexOf(':');

			if (separatorIndex < 0) {
				throw new IllegalArgumentException(
						"The hidden layers must be represented as a comma-separated list of numberOfNeurons:activationFunctionType pairs.");
			}

			int numberOfNeurons = Integer.parseInt(hiddenLayers[i - 1].substring(0, separatorIndex));

			ActivationFunctionType activationFunctionType = ActivationFunctionType.valueOf(hiddenLayers[i
					- 1].substring(separatorIndex + 1));

			layers[i] = new Layer(numberOfNeurons, activationFunctionType, addBias);
		}

		int separatorIndex = outputLayer.indexOf(':');

		if (separatorIndex < 0) {
			throw new IllegalArgumentException(
					"The output layer must be represented as a numberOfNeurons:activationFunctionType pair.");
		}

		int numberOfNeurons = Integer.parseInt(outputLayer.substring(0, separatorIndex));

		ActivationFunctionType activationFunctionType = ActivationFunctionType.valueOf(outputLayer.substring(separatorIndex
				+ 1));

		layers[layers.length - 1] = new Layer(numberOfNeurons, activationFunctionType, false);
	}

	public void feedForward(BigDecimal[] inputs) {
		Layer inputLayer = this.getInputLayer();

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
		Layer[] layers = this.getLayers();

		for (int i = 0; i < layers.length - 1; i++) {
			fromLayer = layers[i];
			toLayer = layers[i + 1];

			List<Future<Void>> futures = new ArrayList<>(toLayer.getNeurons().length);

			for (int j = 0; j < toLayer.getNeurons().length; j++) {
				futures.add(neuronProcessor.processNeuron(this, j, toLayer, fromLayer));
			}

			for (Future<Void> future : futures) {
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new IllegalStateException("Unable to process neuron.", e);
				}
			}
		}

		if (problemType == ProblemType.CLASSIFICATION) {
			BigDecimal[] allSums = new BigDecimal[this.getOutputLayer().getNeurons().length];

			for (int i = 0; i < this.getOutputLayer().getNeurons().length; i++) {
				Neuron nextOutputNeuron = this.getOutputLayer().getNeurons()[i];

				allSums[i] = nextOutputNeuron.getOutputSum();
			}

			List<Future<Void>> futures = new ArrayList<>(this.getOutputLayer().getNeurons().length);

			for (int i = 0; i < this.getOutputLayer().getNeurons().length; i++) {
				futures.add(neuronProcessor.processOutputNeuron(this, i, allSums));
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

	public void applyAccumulatedDeltas() {
		for (int i = 0; i < layers.length - 1; i++) {
			Layer fromLayer = layers[i];

			for (int j = 0; j < fromLayer.getNeurons().length; j++) {
				Neuron nextNeuron = fromLayer.getNeurons()[j];

				for (int k = 0; k < nextNeuron.getOutgoingSynapses().length; k++) {
					Synapse nextSynapse = nextNeuron.getOutgoingSynapses()[k];

					BigDecimal sum = BigDecimal.ZERO;

					for (BigDecimal delta : nextSynapse.getAccumulatedDeltas()) {
						sum = sum.add(delta);
					}

					BigDecimal averageDelta = sum.divide(BigDecimal.valueOf(nextSynapse.getAccumulatedDeltas().size()), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

					if (learningRate != null) {
						averageDelta = averageDelta.multiply(learningRate, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
					}

					BigDecimal regularization = BigDecimal.ZERO;

					if (weightDecayPercent != null && !nextNeuron.isBias()) {
						regularization = nextSynapse.getWeight().multiply(weightDecayPercent, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);

						if (learningRate != null) {
							regularization = regularization.multiply(learningRate, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
						}
					}

					nextSynapse.setWeight(nextSynapse.getWeight().subtract(averageDelta).subtract(regularization));

					nextSynapse.clearAccumulatedDeltas();
				}
			}
		}
	}

	public void saveToFile() {
		ObjectMapper mapper = new ObjectMapper();

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
		String dateText = now.format(formatter);

		String extension = outputFileName.substring(outputFileName.indexOf('.'));
		String beforeExtension = outputFileName.replace(extension, "");
		String fileNameWithDate = beforeExtension + "-" + dateText + extension;

		log.info("Saving network to file: {}", fileNameWithDate);

		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileNameWithDate), this);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to write network parameters to file: " + fileNameWithDate, ioe);
		}
	}

	/**
	 * @return the problemType
	 */
	public ProblemType getProblemType() {
		return problemType;
	}

	/**
	 * @return the inputLayer
	 */
	public Layer getInputLayer() {
		return layers[0];
	}

	/**
	 * @return the layers
	 */
	public Layer[] getLayers() {
		return layers;
	}

	/**
	 * @return the outputLayer
	 */
	public Layer getOutputLayer() {
		return layers[layers.length - 1];
	}

	/**
	 * @return the batchSize
	 */
	public int getBatchSize() {
		return batchSize;
	}
}
