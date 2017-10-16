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

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.neural.model.DataSet;
import com.ciphertool.zenith.neural.model.NeuralNetwork;

@Component
@Profile("zodiac408")
public class Zodiac408SampleGenerator implements SampleGenerator {
	private static Logger			log				= LoggerFactory.getLogger(Zodiac408SampleGenerator.class);

	private static final String		EXTENSION		= ".txt";
	private static final int		ALPHABET_SIZE	= 26;

	@Value("${network.testSamples.count}")
	private int						testSampleCount;

	@NotBlank
	@Value("${task.zodiac408.directory.trainingTextDirectory}")
	private String					trainingTextDirectory;

	@Autowired
	private NeuralNetwork			network;

	@Autowired
	private ProcessedTextFileParser	fileParser;

	private BigDecimal[][]			englishTrainingSamples;
	private BigDecimal[][]			englishTestSamples;

	@PostConstruct
	public void init() {
		log.info("Starting training text import...");

		Path trainingTextDirectoryPath = Paths.get(trainingTextDirectory);

		int inputLayerSize = network.getInputLayer().getNeurons().length - (network.getInputLayer().hasBias() ? 1 : 0);

		long start = System.currentTimeMillis();

		List<Future<List<BigDecimal[]>>> futures = parseFiles(trainingTextDirectoryPath, inputLayerSize);

		List<BigDecimal[]> paragraphs = new ArrayList<>();

		for (Future<List<BigDecimal[]>> future : futures) {
			try {
				paragraphs.addAll(future.get());
			} catch (InterruptedException | ExecutionException e) {
				log.error("Caught Exception while waiting for ParseFileTask ", e);
			}
		}

		BigDecimal[][] samples = new BigDecimal[paragraphs.size()][inputLayerSize];

		for (int i = 0; i < paragraphs.size(); i++) {
			samples[i] = paragraphs.get(i);
		}

		samples = shuffleArray(samples);

		List<BigDecimal[]> samplesList = Arrays.asList(samples);

		englishTestSamples = new BigDecimal[testSampleCount / 2][inputLayerSize];

		for (int i = 0; i < testSampleCount / 2; i++) {
			englishTestSamples[i] = samplesList.remove(samplesList.size() - 1);
		}

		englishTrainingSamples = new BigDecimal[samplesList.size()][inputLayerSize];

		for (int i = 0; i < samplesList.size(); i++) {
			englishTrainingSamples[i] = samplesList.remove(samplesList.size() - 1);
		}

		log.info("Finished importing training text in " + (System.currentTimeMillis() - start) + "ms");
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
		return generate(count, englishTrainingSamples);
	}

	@Override
	public DataSet generateTestSamples(int count) {
		return generate(count, englishTestSamples);
	}

	protected DataSet generate(int count, BigDecimal[][] samplesToUse) {
		if (count > samplesToUse.length * 2) {
			throw new IllegalArgumentException("The number of samples to generate (" + count
					+ ") exceeds the maximum number of samples available (" + samplesToUse.length * 2 + ").");
		}

		samplesToUse = shuffleArray(samplesToUse);

		int inputLayerSize = network.getInputLayer().getNeurons().length - (network.getInputLayer().hasBias() ? 1 : 0);

		BigDecimal[][] samples = new BigDecimal[count][inputLayerSize];
		BigDecimal[][] outputs = new BigDecimal[count][2];

		boolean even = true;
		for (int i = 0; i < count; i++) {
			if (even) {
				samples[i] = samplesToUse[i / 2];
				outputs[i] = new BigDecimal[] { BigDecimal.ONE, BigDecimal.ZERO };
			} else {
				samples[i] = generateRandomSample();
				outputs[i] = new BigDecimal[] { BigDecimal.ONE, BigDecimal.ZERO };
			}

			even = !even;
		}

		return new DataSet(samples, outputs);
	}

	protected BigDecimal[] generateRandomSample() {
		int inputLayerSize = network.getInputLayer().getNeurons().length - (network.getInputLayer().hasBias() ? 1 : 0);

		BigDecimal[] randomSample = new BigDecimal[inputLayerSize];

		for (int i = 0; i < inputLayerSize; i++) {
			randomSample[i] = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(ALPHABET_SIZE) + 1);
		}

		return randomSample;
	}

	protected List<Future<List<BigDecimal[]>>> parseFiles(Path path, int inputLayerSize) {
		List<Future<List<BigDecimal[]>>> tasks = new ArrayList<Future<List<BigDecimal[]>>>();
		Future<List<BigDecimal[]>> task;
		String filename;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					tasks.addAll(parseFiles(entry, inputLayerSize));
				} else {
					filename = entry.toString();
					String ext = filename.substring(filename.lastIndexOf('.'));

					if (!ext.equals(EXTENSION)) {
						log.info("Skipping file with unexpected file extension: " + filename);

						continue;
					}

					task = fileParser.parse(entry, inputLayerSize);
					tasks.add(task);
				}
			}
		} catch (IOException ioe) {
			log.error("Unable to parse files due to:" + ioe.getMessage(), ioe);
		}

		return tasks;
	}
}
