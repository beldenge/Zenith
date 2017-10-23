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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.neural.io.ProcessedTextFileParser;
import com.ciphertool.zenith.neural.model.DataSet;

@Component
@Validated
@ConfigurationProperties
@Profile("zodiac408")
public class Zodiac408SampleGenerator implements SampleGenerator {
	private static Logger			log				= LoggerFactory.getLogger(Zodiac408SampleGenerator.class);

	private static final int		ALPHABET_SIZE	= 26;

	@Min(1)
	@Value("${network.layers.input}")
	private int						inputLayerNeurons;

	@Value("${network.testSamples.count}")
	private int						testSampleCount;

	@NotBlank
	@Value("${task.zodiac408.directory.trainingTextDirectory.valid}")
	private String					validTrainingTextDirectory;

	@Value("${task.zodiac408.directory.trainingTextDirectory.invalid}")
	private String					invalidTrainingTextDirectory;

	@Autowired
	private ProcessedTextFileParser	fileParser;

	private BigDecimal[][]			englishTrainingSamples;
	private BigDecimal[][]			nonEnglishTrainingSamples;
	private BigDecimal[][]			englishTestSamples;

	@PostConstruct
	public void init() {
		log.info("Starting training text import...");

		Path validTrainingTextDirectoryPath = Paths.get(validTrainingTextDirectory);

		if (!Files.isDirectory(validTrainingTextDirectoryPath)) {
			throw new IllegalArgumentException(
					"Property \"task.zodiac408.directory.trainingTextDirectory.valid\" must be a directory.");
		}

		Path invalidTrainingTextDirectoryPath = null;

		if (invalidTrainingTextDirectory != null && !invalidTrainingTextDirectory.isEmpty()) {
			invalidTrainingTextDirectoryPath = Paths.get(invalidTrainingTextDirectory);

			if (!Files.isDirectory(invalidTrainingTextDirectoryPath)) {
				throw new IllegalArgumentException(
						"Property \"task.zodiac408.directory.trainingTextDirectory.invalid\" must be a directory.");
			}
		}

		int inputLayerSize = inputLayerNeurons;

		// Load English training data

		long start = System.currentTimeMillis();

		List<Future<List<BigDecimal[]>>> futures = parseFiles(validTrainingTextDirectoryPath, inputLayerSize, 4);

		List<BigDecimal[]> englishParagraphs = new ArrayList<>();

		for (Future<List<BigDecimal[]>> future : futures) {
			try {
				englishParagraphs.addAll(future.get());
			} catch (InterruptedException | ExecutionException e) {
				log.error("Caught Exception while waiting for ParseFileTask ", e);
			}
		}

		BigDecimal[][] englishSamples = new BigDecimal[englishParagraphs.size()][inputLayerSize];

		for (int i = 0; i < englishParagraphs.size(); i++) {
			englishSamples[i] = englishParagraphs.get(i);
		}

		englishSamples = shuffleArray(englishSamples);

		List<BigDecimal[]> englishSamplesList = new ArrayList<>(Arrays.asList(englishSamples));

		englishTestSamples = new BigDecimal[testSampleCount / 2][inputLayerSize];

		for (int i = 0; i < testSampleCount / 2; i++) {
			englishTestSamples[i] = englishSamplesList.remove(englishSamplesList.size() - 1);
		}

		englishTrainingSamples = new BigDecimal[englishSamplesList.size()][inputLayerSize];

		int samplesLeft = englishSamplesList.size();

		for (int i = 0; i < samplesLeft; i++) {
			englishTrainingSamples[i] = englishSamplesList.remove(englishSamplesList.size() - 1);
		}

		log.info("Finished importing {} samples from English training text in {}ms.", englishSamples.length, (System.currentTimeMillis()
				- start));

		// Load non-English training data (if provided)

		if (invalidTrainingTextDirectoryPath == null) {
			return;
		}

		start = System.currentTimeMillis();

		futures = parseFiles(invalidTrainingTextDirectoryPath, inputLayerSize, 1);

		List<BigDecimal[]> nonEnglishParagraphs = new ArrayList<>();

		for (Future<List<BigDecimal[]>> future : futures) {
			try {
				nonEnglishParagraphs.addAll(future.get());
			} catch (InterruptedException | ExecutionException e) {
				log.error("Caught Exception while waiting for ParseFileTask ", e);
			}
		}

		BigDecimal[][] nonEnglishSamples = new BigDecimal[nonEnglishParagraphs.size()][inputLayerSize];

		for (int i = 0; i < nonEnglishParagraphs.size(); i++) {
			nonEnglishSamples[i] = nonEnglishParagraphs.get(i);
		}

		nonEnglishSamples = shuffleArray(nonEnglishSamples);

		List<BigDecimal[]> nonEnglishSamplesList = new ArrayList<>(Arrays.asList(nonEnglishSamples));

		nonEnglishTrainingSamples = new BigDecimal[nonEnglishSamplesList.size()][inputLayerSize];

		for (int i = 0; i < nonEnglishSamplesList.size(); i++) {
			nonEnglishTrainingSamples[i] = nonEnglishSamplesList.get(i);
		}

		log.info("Finished importing {} samples from non-English training text in {}ms.", nonEnglishSamples.length, (System.currentTimeMillis()
				- start));
	}

	protected static BigDecimal[][] shuffleArray(BigDecimal[][] arrayToShuffle) {
		BigDecimal[][] shuffledArray = new BigDecimal[arrayToShuffle.length][];

		int arrayLength = arrayToShuffle.length;

		List<BigDecimal[]> arrayList = new ArrayList<>(Arrays.asList(arrayToShuffle));

		for (int i = 0; i < arrayLength; i++) {
			int randomIndex = ThreadLocalRandom.current().nextInt(arrayList.size());

			shuffledArray[i] = arrayList.remove(randomIndex);
		}

		return shuffledArray;
	}

	@Override
	public DataSet generateTrainingSamples(int count) {
		englishTrainingSamples = shuffleArray(englishTrainingSamples);

		return generate(count, englishTrainingSamples);
	}

	@Override
	public DataSet generateTestSamples(int count) {
		englishTestSamples = shuffleArray(englishTestSamples);

		return generate(count, englishTestSamples);
	}

	protected DataSet generate(int count, BigDecimal[][] samplesToUse) {
		if (count > samplesToUse.length * 2) {
			throw new IllegalArgumentException("The number of samples to generate (" + count
					+ ") exceeds the maximum number of samples available (" + samplesToUse.length * 2 + ").");
		}

		BigDecimal[][] samples = new BigDecimal[count][inputLayerNeurons];
		BigDecimal[][] outputs = new BigDecimal[count][2];

		boolean even = true;
		for (int i = 0; i < count; i++) {
			if (even) {
				samples[i] = samplesToUse[i / 2];
				outputs[i] = new BigDecimal[] { BigDecimal.ONE, BigDecimal.ZERO };
			} else {
				samples[i] = generateInvalidSample(i / 2);
				outputs[i] = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ONE };
			}

			even = !even;
		}

		return new DataSet(samples, outputs);
	}

	protected BigDecimal[] generateInvalidSample(int i) {
		if (nonEnglishTrainingSamples != null) {
			if (i > nonEnglishTrainingSamples.length - 1) {
				throw new IllegalArgumentException(
						"The invalid training samples file was specified, but the number of training samples exceeds the total available of "
								+ nonEnglishTrainingSamples.length + ".");
			}

			return nonEnglishTrainingSamples[i];
		}

		int inputLayerSize = inputLayerNeurons;

		BigDecimal[] randomSample = new BigDecimal[inputLayerSize];

		for (int j = 0; j < inputLayerSize; j++) {
			randomSample[j] = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(ALPHABET_SIZE)
					+ 1).divide(BigDecimal.valueOf(ALPHABET_SIZE), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
		}

		return randomSample;
	}

	protected List<Future<List<BigDecimal[]>>> parseFiles(Path path, int inputLayerSize, int stepLimit) {
		List<Future<List<BigDecimal[]>>> tasks = new ArrayList<Future<List<BigDecimal[]>>>();
		Future<List<BigDecimal[]>> task;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					tasks.addAll(parseFiles(entry, inputLayerSize, stepLimit));
				} else {
					task = fileParser.parse(entry, inputLayerSize, stepLimit);
					tasks.add(task);
				}
			}
		} catch (IOException ioe) {
			log.error("Unable to parse files.", ioe);
		}

		return tasks;
	}
}
