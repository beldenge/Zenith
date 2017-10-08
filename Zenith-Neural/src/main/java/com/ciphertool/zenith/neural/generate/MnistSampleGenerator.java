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
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.neural.model.DataSet;
import com.ciphertool.zenith.neural.model.NeuralNetwork;

@Component
@Profile("mnist")
public class MnistSampleGenerator implements SampleGenerator {
	private static Logger	log	= LoggerFactory.getLogger(MnistSampleGenerator.class);

	@Value("${task.mnist.directory.trainingImages}")
	private String			trainingImagesFile;

	@Value("${task.mnist.directory.trainingLabels}")
	private String			trainingLabelsFile;

	@Value("${task.mnist.directory.testImages}")
	private String			testImagesFile;

	@Value("${task.mnist.directory.testLabels}")
	private String			testLabelsFile;

	@Autowired
	private NeuralNetwork	network;

	@PostConstruct
	public void init() {
		Path trainingImagesPath = Paths.get(trainingImagesFile);

		byte[] trainingImagesBytes;

		try {
			trainingImagesBytes = Files.readAllBytes(trainingImagesPath);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to read training images file.", ioe);
		}

		int[][] trainingImages = loadImages(trainingImagesBytes);

		for (int i = 0; i < trainingImages.length; i++) {
			log.info("Test image " + (i + 1) + ": " + Arrays.toString(trainingImages[i]));
		}

		Path trainingLabelsPath = Paths.get(trainingLabelsFile);

		byte[] trainingLabelsBytes;

		try {
			trainingLabelsBytes = Files.readAllBytes(trainingLabelsPath);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to read training labels file.", ioe);
		}

		int[] trainingLabels = loadLabels(trainingLabelsBytes);

		for (int i = 0; i < trainingLabels.length; i++) {
			log.info("Test image " + (i + 1) + ": " + trainingLabels[i]);
		}

		Path testImagesPath = Paths.get(testImagesFile);

		byte[] testImagesBytes;

		try {
			testImagesBytes = Files.readAllBytes(testImagesPath);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to read test images file.", ioe);
		}

		int[][] testImages = loadImages(testImagesBytes);

		for (int i = 0; i < testImages.length; i++) {
			log.info("Test image " + (i + 1) + ": " + Arrays.toString(testImages[i]));
		}

		Path testLabelsPath = Paths.get(testLabelsFile);

		byte[] testLabelsBytes;

		try {
			testLabelsBytes = Files.readAllBytes(testLabelsPath);
		} catch (IOException ioe) {
			throw new IllegalStateException("Unable to read test labels file.", ioe);
		}

		int[] testLabels = loadLabels(testLabelsBytes);

		for (int i = 0; i < testLabels.length; i++) {
			log.info("Test label " + (i + 1) + ": " + testLabels[i]);
		}
	}

	protected int[][] loadImages(byte[] imagesBytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(imagesBytes);

		// Skip the first four bytes, as it's a magic number
		byteBuffer.getInt();

		// The second four bytes is the number of items
		int numberOfItems = byteBuffer.getInt();

		// The third and fourth sets of four bytes are the number of rows and columns, respectively
		int numberOfRows = byteBuffer.getInt();
		int numberOfColumns = byteBuffer.getInt();
		int totalPixels = numberOfRows * numberOfColumns;

		int[][] images = new int[numberOfItems][totalPixels];

		for (int i = 0; i < numberOfItems; i++) {
			int[] pixels = new int[totalPixels];

			for (int j = 0; j < totalPixels; j++) {
				pixels[j] = Byte.toUnsignedInt(byteBuffer.get());
			}

			images[i] = pixels;
		}

		return images;
	}

	protected int[] loadLabels(byte[] labelsBytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(labelsBytes);

		// Skip the first four bytes, as it's a magic number
		byteBuffer.getInt();

		// The second four bytes is the number of items
		int numberOfItems = byteBuffer.getInt();

		int[] labels = new int[numberOfItems];

		for (int i = 0; i < numberOfItems; i++) {
			labels[i] = Byte.toUnsignedInt(byteBuffer.get());
		}

		return labels;
	}

	@Override
	public DataSet generateTrainingSamples(int count) {
		return null;
	}

	@Override
	public DataSet generateTestSamples(int count) {
		return null;
	}
}
