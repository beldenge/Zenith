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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.model.DataSet;
import com.ciphertool.zenith.neural.model.NeuralNetwork;

@Component
@Profile("mnist")
public class MnistSampleGenerator implements SampleGenerator {
	private static Logger			log				= LoggerFactory.getLogger(MnistSampleGenerator.class);

	private static final BigDecimal	MAX_PIXEL_VALUE	= BigDecimal.valueOf(256);

	@Value("${task.mnist.directory.trainingImages}")
	private String					trainingImagesFile;

	@Value("${task.mnist.directory.trainingLabels}")
	private String					trainingLabelsFile;

	@Value("${task.mnist.directory.testImages}")
	private String					testImagesFile;

	@Value("${task.mnist.directory.testLabels}")
	private String					testLabelsFile;

	private BigDecimal[][]			trainingImages;
	private BigDecimal[][]			trainingLabels;
	private BigDecimal[][]			testImages;
	private BigDecimal[][]			testLabels;

	@Autowired
	private NeuralNetwork			network;

	@PostConstruct
	public void init() {
		Path trainingImagesPath = Paths.get(trainingImagesFile);

		byte[] trainingImagesBytes;

		try {
			trainingImagesBytes = Files.readAllBytes(trainingImagesPath);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to read training images file.", ioe);
		}

		trainingImages = loadImages(trainingImagesBytes);

		Path trainingLabelsPath = Paths.get(trainingLabelsFile);

		byte[] trainingLabelsBytes;

		try {
			trainingLabelsBytes = Files.readAllBytes(trainingLabelsPath);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to read training labels file.", ioe);
		}

		trainingLabels = loadLabels(trainingLabelsBytes);

		DataSet shuffledTrainingData = shuffleArrays(trainingImages, trainingLabels);

		trainingImages = shuffledTrainingData.getInputs();
		trainingLabels = shuffledTrainingData.getOutputs();

		for (int i = 0; i < trainingImages.length; i++) {
			log.debug("Test image {}: {}", i + 1, Arrays.toString(trainingImages[i]));
			log.debug("Test label {}: {}", i + 1, trainingLabels[i]);
		}

		Path testImagesPath = Paths.get(testImagesFile);

		byte[] testImagesBytes;

		try {
			testImagesBytes = Files.readAllBytes(testImagesPath);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to read test images file.", ioe);
		}

		testImages = loadImages(testImagesBytes);

		Path testLabelsPath = Paths.get(testLabelsFile);

		byte[] testLabelsBytes;

		try {
			testLabelsBytes = Files.readAllBytes(testLabelsPath);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to read test labels file.", ioe);
		}

		testLabels = loadLabels(testLabelsBytes);

		DataSet shuffledTestData = shuffleArrays(testImages, testLabels);

		testImages = shuffledTestData.getInputs();
		testLabels = shuffledTestData.getOutputs();

		for (int i = 0; i < testImages.length; i++) {
			log.debug("Test image {}: {}", i + 1, Arrays.toString(testImages[i]));
			log.debug("Test label {}: {}", i + 1, testLabels[i]);
		}
	}

	protected BigDecimal[][] loadImages(byte[] imagesBytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(imagesBytes);

		// Skip the first four bytes, as it's a magic number
		byteBuffer.getInt();

		// The second four bytes is the number of items
		int numberOfItems = byteBuffer.getInt();

		// The third and fourth sets of four bytes are the number of rows and columns, respectively
		int numberOfRows = byteBuffer.getInt();
		int numberOfColumns = byteBuffer.getInt();
		int totalPixels = numberOfRows * numberOfColumns;

		BigDecimal[][] images = new BigDecimal[numberOfItems][totalPixels];

		for (int i = 0; i < numberOfItems; i++) {
			BigDecimal[] pixels = new BigDecimal[totalPixels];

			for (int j = 0; j < totalPixels; j++) {
				pixels[j] = BigDecimal.valueOf(Byte.toUnsignedInt(byteBuffer.get()));
				/*
				 * Scale the value so that it is between 0 and 1, as this makes the BigDecimal math during training
				 * orders of magnitude more efficient
				 */
				pixels[j] = pixels[j].divide(MAX_PIXEL_VALUE, MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
			}

			images[i] = pixels;
		}

		return images;
	}

	protected BigDecimal[][] loadLabels(byte[] labelsBytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(labelsBytes);

		// Skip the first four bytes, as it's a magic number
		byteBuffer.getInt();

		// The second four bytes is the number of items
		int numberOfItems = byteBuffer.getInt();

		BigDecimal[][] labels = new BigDecimal[numberOfItems][network.getOutputLayer().getNeurons().length];

		for (int i = 0; i < numberOfItems; i++) {
			int label = Byte.toUnsignedInt(byteBuffer.get());

			for (int j = 0; j < network.getOutputLayer().getNeurons().length; j++) {
				labels[i][j] = (label == j) ? BigDecimal.ONE : BigDecimal.ZERO;
			}
		}

		return labels;
	}

	protected static DataSet shuffleArrays(BigDecimal[][] imagesArray, BigDecimal[][] labelsArray) {
		if (imagesArray.length != labelsArray.length) {
			throw new IllegalArgumentException("The images array length of " + imagesArray.length
					+ " does not match the labels array length of " + labelsArray.length
					+ ".  Unable to shuffle arrays.");
		}

		BigDecimal[][] shuffledImagesArray = new BigDecimal[imagesArray.length][];
		BigDecimal[][] shuffledLabelsArray = new BigDecimal[labelsArray.length][];

		int arrayLength = imagesArray.length;

		for (int i = 0; i < arrayLength; i++) {
			int randomIndex = ThreadLocalRandom.current().nextInt(imagesArray.length);

			shuffledImagesArray[i] = imagesArray[randomIndex];
			shuffledLabelsArray[i] = labelsArray[randomIndex];
		}

		return new DataSet(shuffledImagesArray, shuffledLabelsArray);
	}

	@Override
	public DataSet generateTrainingSamples(int count) {
		return generate(count, trainingImages, trainingLabels);
	}

	@Override
	public DataSet generateTestSamples(int count) {
		return generate(count, testImages, testLabels);
	}

	protected DataSet generate(int count, BigDecimal[][] images, BigDecimal[][] labels) {
		if (count > images.length) {
			throw new IllegalArgumentException("The number of samples to generate (" + count
					+ ") exceeds the maximum number of samples available (" + images.length + ").");
		}

		int inputLayerSize = network.getInputLayer().getNeurons().length - (network.getInputLayer().hasBias() ? 1 : 0);
		int outputLayerSize = network.getOutputLayer().getNeurons().length;

		BigDecimal[][] inputs = new BigDecimal[count][inputLayerSize];
		BigDecimal[][] outputs = new BigDecimal[count][outputLayerSize];

		for (int i = 0; i < count; i++) {
			inputs[i] = images[i];
			outputs[i] = labels[i];
		}

		return new DataSet(inputs, outputs);
	}
}
