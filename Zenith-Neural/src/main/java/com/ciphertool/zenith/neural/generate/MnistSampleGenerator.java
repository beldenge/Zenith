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
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Validated
@Component
@Profile("mnist")
@ConfigurationProperties
public class MnistSampleGenerator implements SampleGenerator {
	private static Logger			log				= LoggerFactory.getLogger(MnistSampleGenerator.class);

	private static final Float	MAX_PIXEL_VALUE	= 255.0f;

	@NotBlank
	@Value("${task.mnist.directory.trainingImages}")
	private String					trainingImagesFile;

	@NotBlank
	@Value("${task.mnist.directory.trainingLabels}")
	private String					trainingLabelsFile;

	@NotBlank
	@Value("${task.mnist.directory.testImages}")
	private String					testImagesFile;

	@NotBlank
	@Value("${task.mnist.directory.testLabels}")
	private String					testLabelsFile;

	@Min(1)
	@Value("${network.layers.input}")
	private int						inputLayerNeurons;

	@Min(1)
	@Value("${network.layers.output}")
	private int						outputLayerNeurons;

	private static AtomicInteger nextTrainingIndex = new AtomicInteger(0);
	private static AtomicInteger nextTestIndex = new AtomicInteger(0);

	private Float[][]			trainingImages;
	private Float[][]			trainingLabels;
	private Float[][]			testImages;
	private Float[][]			testLabels;

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

		for (int i = 0; i < testImages.length; i++) {
			log.debug("Test image {}: {}", i + 1, Arrays.toString(testImages[i]));
			log.debug("Test label {}: {}", i + 1, testLabels[i]);
		}
	}

	/*
	 * TODO: shuffle the sample sets on each call
	 */
	@Override
	public void resetSamples() {
		nextTrainingIndex = new AtomicInteger(0);
		nextTestIndex = new AtomicInteger(0);
	}

	protected Float[][] loadImages(byte[] imagesBytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(imagesBytes);

		// Skip the first four bytes, as it's a magic number
		byteBuffer.getInt();

		// The second four bytes is the number of items
		int numberOfItems = byteBuffer.getInt();

		// The third and fourth sets of four bytes are the number of rows and columns, respectively
		int numberOfRows = byteBuffer.getInt();
		int numberOfColumns = byteBuffer.getInt();
		int totalPixels = numberOfRows * numberOfColumns;

		Float[][] images = new Float[numberOfItems][totalPixels];

		for (int i = 0; i < numberOfItems; i++) {
			Float[] pixels = new Float[totalPixels];

			for (int j = 0; j < totalPixels; j++) {
				pixels[j] = (float) Byte.toUnsignedInt(byteBuffer.get());
				/*
				 * Scale the value so that it is between 0 and 1, as this makes the Float math during training
				 * orders of magnitude more efficient
				 */
				pixels[j] = pixels[j] / MAX_PIXEL_VALUE;
			}

			images[i] = pixels;
		}

		return images;
	}

	protected Float[][] loadLabels(byte[] labelsBytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(labelsBytes);

		// Skip the first four bytes, as it's a magic number
		byteBuffer.getInt();

		// The second four bytes is the number of items
		int numberOfItems = byteBuffer.getInt();

		Float[][] labels = new Float[numberOfItems][outputLayerNeurons];

		for (int i = 0; i < numberOfItems; i++) {
			int label = Byte.toUnsignedInt(byteBuffer.get());

			for (int j = 0; j < outputLayerNeurons; j++) {
				labels[i][j] = (label == j) ? 1.0f : 0.0f;
			}
		}

		return labels;
	}

	protected static DataSet shuffleArrays(Float[][] imagesArray, Float[][] labelsArray) {
		if (imagesArray.length != labelsArray.length) {
			throw new IllegalArgumentException("The images array length of " + imagesArray.length
					+ " does not match the labels array length of " + labelsArray.length
					+ ".  Unable to shuffle arrays.");
		}

		Float[][] shuffledImagesArray = new Float[imagesArray.length][];
		Float[][] shuffledLabelsArray = new Float[labelsArray.length][];

		int arrayLength = imagesArray.length;

		List<Float[]> imagesList = new ArrayList<>(Arrays.asList(imagesArray));
		List<Float[]> labelsList = new ArrayList<>(Arrays.asList(labelsArray));

		for (int i = 0; i < arrayLength; i++) {
			int randomIndex = ThreadLocalRandom.current().nextInt(imagesList.size());

			shuffledImagesArray[i] = imagesList.remove(randomIndex);
			shuffledLabelsArray[i] = labelsList.remove(randomIndex);
		}

		return new DataSet(shuffledImagesArray, shuffledLabelsArray);
	}

	@Override
	public DataSet generateTrainingSamples(int count) {
		if (count > testImages.length) {
			throw new IllegalArgumentException("The number of training samples to generate (" + count
					+ ") exceeds the maximum number of training samples available (" + testImages.length + ").");
		}

		nextTrainingIndex = new AtomicInteger(0);

		DataSet shuffledTrainingData = shuffleArrays(trainingImages, trainingLabels);

		trainingImages = shuffledTrainingData.getInputs();
		trainingLabels = shuffledTrainingData.getOutputs();

		Float[][] inputs = new Float[count][inputLayerNeurons];
		Float[][] outputs = new Float[count][outputLayerNeurons];

		for (int i = 0; i < count; i++) {
			DataSet next = generateTrainingSample();

			inputs[i] = next.getInputs()[0];
			outputs[i] = next.getOutputs()[0];
		}

		return new DataSet(inputs, outputs);
	}

	@Override
	public DataSet generateTrainingSample() {
		Float[][] inputs = new Float[1][inputLayerNeurons];
		Float[][] outputs = new Float[1][outputLayerNeurons];

		int next = nextTrainingIndex.getAndIncrement();

		inputs[0] = trainingImages[next];
		outputs[0] = trainingLabels[next];

		return new DataSet(inputs, outputs);
	}

	@Override
	public DataSet generateTestSamples(int count) {
		if (count > testImages.length) {
			throw new IllegalArgumentException("The number of test samples to generate (" + count
					+ ") exceeds the maximum number of test samples available (" + testImages.length + ").");
		}

		nextTestIndex = new AtomicInteger(0);

		DataSet shuffledTestData = shuffleArrays(testImages, testLabels);

		testImages = shuffledTestData.getInputs();
		testLabels = shuffledTestData.getOutputs();

		Float[][] inputs = new Float[count][inputLayerNeurons];
		Float[][] outputs = new Float[count][outputLayerNeurons];

		for (int i = 0; i < count; i++) {
			DataSet next = generateTestSample();

			inputs[i] = next.getInputs()[0];
			outputs[i] = next.getOutputs()[0];
		}

		return new DataSet(inputs, outputs);
	}

	@Override
	public DataSet generateTestSample() {
		Float[][] inputs = new Float[1][inputLayerNeurons];
		Float[][] outputs = new Float[1][outputLayerNeurons];

		int next = nextTestIndex.getAndIncrement();

		inputs[0] = testImages[next];
		outputs[0] = testLabels[next];

		return new DataSet(inputs, outputs);
	}
}
