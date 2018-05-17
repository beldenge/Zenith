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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

	private INDArray			trainingImages;
	private INDArray			trainingLabels;
	private INDArray			testImages;
	private INDArray			testLabels;

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

		for (int i = 0; i < trainingImages.size(0); i++) {
			log.debug("Test image {}: {}", i + 1, trainingImages.getRow(i));
			log.debug("Test label {}: {}", i + 1, trainingLabels.getRow(i));
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

		for (int i = 0; i < testImages.size(0); i++) {
			log.debug("Test image {}: {}", i + 1, testImages.getRow(i));
			log.debug("Test label {}: {}", i + 1, testLabels.getRow(i));
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

	protected INDArray loadImages(byte[] imagesBytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(imagesBytes);

		// Skip the first four bytes, as it's a magic number
		byteBuffer.getInt();

		// The second four bytes is the number of items
		int numberOfItems = byteBuffer.getInt();

		// The third and fourth sets of four bytes are the number of rows and columns, respectively
		int numberOfRows = byteBuffer.getInt();
		int numberOfColumns = byteBuffer.getInt();
		int totalPixels = numberOfRows * numberOfColumns;

		INDArray images = Nd4j.create(numberOfItems, totalPixels);

		for (int i = 0; i < numberOfItems; i++) {
			for (int j = 0; j < totalPixels; j++) {
				images.putScalar(i, j, (float) Byte.toUnsignedInt(byteBuffer.get()));
			}
		}

		/*
		 * Scale the value so that it is between 0 and 1, as this makes the Float math during training
		 * orders of magnitude more efficient
		 */
		images.divi(MAX_PIXEL_VALUE);

		return images;
	}

	protected INDArray loadLabels(byte[] labelsBytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(labelsBytes);

		// Skip the first four bytes, as it's a magic number
		byteBuffer.getInt();

		// The second four bytes is the number of items
		int numberOfItems = byteBuffer.getInt();

		INDArray labels = Nd4j.zeros(numberOfItems, outputLayerNeurons);

		for (int i = 0; i < numberOfItems; i++) {
			int label = Byte.toUnsignedInt(byteBuffer.get());

			labels.putScalar(i, label, 1.0f);
		}

		return labels;
	}

	@Override
	public DataSet generateTrainingSample() {
		INDArray inputs = Nd4j.create(inputLayerNeurons);
		INDArray outputs = Nd4j.create(outputLayerNeurons);

		int next = nextTrainingIndex.getAndIncrement();

		inputs.putRow(0, trainingImages.getRow(next));
		outputs.putRow(0, trainingLabels.getRow(next));

		return new DataSet(inputs, outputs);
	}

	@Override
	public DataSet generateTestSample() {
		INDArray inputs = Nd4j.create(inputLayerNeurons);
		INDArray outputs = Nd4j.create(outputLayerNeurons);

		int next = nextTestIndex.getAndIncrement();

		inputs.putRow(0, testImages.getRow(next));
		outputs.putRow(0, testLabels.getRow(next));

		return new DataSet(inputs, outputs);
	}
}
