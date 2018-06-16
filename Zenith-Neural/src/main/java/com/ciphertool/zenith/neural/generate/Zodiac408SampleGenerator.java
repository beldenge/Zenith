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

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.math.sampling.RouletteSampler;
import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;
import com.ciphertool.zenith.model.probability.LetterProbability;
import com.ciphertool.zenith.neural.generate.zodiac408.EnglishParagraph;
import com.ciphertool.zenith.neural.generate.zodiac408.EnglishParagraphDao;
import com.ciphertool.zenith.neural.generate.zodiac408.EnglishParagraphSequenceDao;
import com.ciphertool.zenith.neural.io.ProcessedTextFileParser;
import com.ciphertool.zenith.neural.model.DataSet;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Validated
@ConfigurationProperties
@Profile("zodiac408")
public class Zodiac408SampleGenerator implements SampleGenerator {
	private static Logger			log				= LoggerFactory.getLogger(Zodiac408SampleGenerator.class);

	private static final int	CHAR_TO_NUMERIC_OFFSET	= 9;

	private static final int NUM_LETTERS = ModelConstants.LOWERCASE_LETTERS.size();

	private static final RouletteSampler RANDOM_LETTER_SAMPLER = new RouletteSampler();
	private static final List<LetterProbability> RANDOM_LETTER_PROBABILITIES = new ArrayList<>(NUM_LETTERS);

	static {
		for (Character letter : ModelConstants.LOWERCASE_LETTERS) {
			RANDOM_LETTER_PROBABILITIES.add(new LetterProbability(letter, MathConstants.SINGLE_LETTER_RANDOM_PROBABILITY));
		}
	}

	private static final Float RANDOM_LETTER_TOTAL_PROBABILITY = RANDOM_LETTER_SAMPLER.reIndex(RANDOM_LETTER_PROBABILITIES);

	private static final Map<Character, Float[]> SPARSE_CHARACTER_MAP = new HashMap<>(NUM_LETTERS);

	static {
		for (Character letter : ModelConstants.LOWERCASE_LETTERS) {
			SPARSE_CHARACTER_MAP.put(letter, charToFloatArray(letter));
		}
	}

	private static int UNIQUE_INPUT_COUNT;

	@Min(1)
	@Value("${network.inputLayerNeurons}")
	private int						inputLayerNeurons;

	@Min(2)
	@Value("${network.outputLayerNeurons}")
	private int						outputLayerNeurons;

	@NotBlank
	@Value("${task.zodiac408.sourceDirectory}")
	private String					validTrainingTextDirectory;

	@Value("${training.testSampleCount}")
	private int						testSampleCount;

	@Value("${training.trainingSampleCount}")
	private int						trainingSampleCount;

	@Autowired
	private ThreadPoolTaskExecutor 	taskExecutor;

	@Autowired
	private LetterNGramDao			 letterNGramDao;

	@Autowired
	private EnglishParagraphDao		 englishParagraphDao;

	@Autowired
	private EnglishParagraphSequenceDao englishParagraphSequenceDao;

	@Autowired
	private ProcessedTextFileParser	fileParser;

	private TreeMarkovModel 		letterMarkovModel;

	private long englishParagraphCount = 0L;

	private boolean intialized = false;

	protected static Float[] charToFloatArray(char c) {
		Float[] sparseRepresentation = new Float[NUM_LETTERS];

		int numericValue = Character.getNumericValue(c) - CHAR_TO_NUMERIC_OFFSET - 1;

		Arrays.fill(sparseRepresentation, 0.0f);

		sparseRepresentation[numericValue] = 1.0f;

		return sparseRepresentation;
	}

	@PostConstruct
	public void setUp() {
		if(inputLayerNeurons % NUM_LETTERS != 0) {
			throw new IllegalStateException("The 'network.layers.input' property must be evenly divisible by " +
					NUM_LETTERS + " of which the value " + inputLayerNeurons + " is not.");
		}

		UNIQUE_INPUT_COUNT = inputLayerNeurons / NUM_LETTERS;
	}

	@Override
	public void resetSamples(){
		// Nothing to do
	}

	public void init() {
		log.info("Starting training text import...");

		persistSamples();

		englishParagraphCount = englishParagraphDao.count();

		if (englishParagraphCount < (testSampleCount + trainingSampleCount)) {
			throw new IllegalStateException("Requested " + testSampleCount + " test samples and " + trainingSampleCount + " training samples, but only " + englishParagraphCount + " total samples are available.");
		}

		int order = outputLayerNeurons - 2;

		if (order > 0) {
			letterMarkovModel = new TreeMarkovModel(outputLayerNeurons - 2);

			List<TreeNGram> nodes = letterNGramDao.findAll(1, false);

			// TODO: try parallel stream here
			nodes.stream().forEach(letterMarkovModel::addNode);
		}

		intialized = true;
	}

	protected void persistSamples() {
		if (englishParagraphDao.exists()) {
			return;
		}

		Path validTrainingTextDirectoryPath = Paths.get(validTrainingTextDirectory);

		if (!Files.isDirectory(validTrainingTextDirectoryPath)) {
			throw new IllegalArgumentException(
					"Property \"task.zodiac408.sourceDirectory\" must be a directory.");
		}

		englishParagraphDao.reinitialize();
		englishParagraphSequenceDao.reinitialize();

		// Load English training data
		long start = System.currentTimeMillis();

		List<CompletableFuture<Void>> futures = parseFiles(validTrainingTextDirectoryPath);

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();

		log.info("Finished processing source directory in {}ms.", (System.currentTimeMillis() - start));
	}

	@Override
	public DataSet generateTrainingSample() {
		if (!intialized) {
			init();
		}

		return generateOne();
	}

	@Override
	public DataSet generateTestSample() {
		if (!intialized) {
			init();
		}

		return generateOne();
	}

	public DataSet generateOne() {
		INDArray samples = Nd4j.create(inputLayerNeurons);
		INDArray outputs = Nd4j.create(outputLayerNeurons);

		/*
		 * The idea is to "roll the dice" to determine what type of sample to generate, either a completely random
		 * sample, a sample based on the Markov model, or a sample from known English paragraphs.
		 */
		int sampleType = ThreadLocalRandom.current().nextInt(outputLayerNeurons);

		if (sampleType == 0) {
			// Generate a random sample
			samples.putRow(0, generateRandomSample());
			outputs.putScalar(0, 0, 1.0f);

			for (int i = 1; i < outputLayerNeurons; i++) {
				outputs.putScalar(0, i, 0.0f);
			}
		}
		else if (sampleType < outputLayerNeurons - 1) {
			// Generate probabilistic samples based on Markov model
			for (int i = 1; i < outputLayerNeurons - 1; i++) {
				if (i == sampleType) {
					samples.putRow(0, generateMarkovModelSample(i));

					for (int j = 0; j < outputLayerNeurons; j++) {
						outputs.putScalar(0, j, (i == j) ? 1.0f : 0.0f);
					}
				}
			}
		}
		else {
			// Generate a sample from known English paragraphs
			int i = outputLayerNeurons - 1;

			samples.putRow(0, getRandomParagraph());
			outputs.putScalar(0, outputLayerNeurons - 1, 1.0f);

			for (int j = 0; j < outputLayerNeurons - 1; j++) {
				outputs.putScalar(0, j, 0.0f);
			}
		}

		return new DataSet(samples, outputs);
	}

	protected INDArray getRandomParagraph() {
		Long randomIndex = ThreadLocalRandom.current().nextLong(1, englishParagraphCount + 1);

		EnglishParagraph nextParagraph = englishParagraphDao.findBySequence(randomIndex);

		char[] nextSample = nextParagraph.getParagraph().substring(0, UNIQUE_INPUT_COUNT).toCharArray();

		if (log.isDebugEnabled()) {
			log.debug("Random paragraph: {}", String.valueOf(nextSample));
		}

		INDArray numericSample = Nd4j.create(inputLayerNeurons);

		for (int j = 0; j < nextSample.length; j++) {
			Float[] sparseCoding = charToFloatArray(nextSample[j]);

			for (int k = 0; k < sparseCoding.length; k ++) {
				numericSample.putScalar((j * NUM_LETTERS) + k, sparseCoding[k]);
			}
		}

		return numericSample;
	}

	protected INDArray generateMarkovModelSample(int markovOrder) {
		INDArray sample = Nd4j.create(inputLayerNeurons);

		TreeNGram rootNode = letterMarkovModel.getRootNode();
		TreeNGram match;

		StringBuffer sb = null;

		if (log.isDebugEnabled()) {
			sb = new StringBuffer();
		}

		String root = "";
		for (int i = 0; i < UNIQUE_INPUT_COUNT; i++) {
			match = (root.isEmpty() || markovOrder == 1) ? rootNode : letterMarkovModel.findLongest(root);

			LetterProbability chosen = sampleNextTransitionFromDistribution(match);

			char nextSymbol = chosen.getValue();

			if (log.isDebugEnabled()) {
				sb.append(nextSymbol);
			}

			Float[] sparseCoding = charToFloatArray(nextSymbol);

			for (int k = 0; k < sparseCoding.length; k ++) {
				sample.putScalar((i * NUM_LETTERS) + k, sparseCoding[k]);
			}

			root = ((root.isEmpty() || root.length() < markovOrder - 1) ? root : root.substring(1)) + nextSymbol;
		}

		if (log.isDebugEnabled()) {
			log.debug("Random sample of order {}: {}", markovOrder, sb.toString());
		}

		return sample;
	}

	protected static LetterProbability sampleNextTransitionFromDistribution(TreeNGram match) {
		if (match.getTransitions().isEmpty()) {
			return RANDOM_LETTER_PROBABILITIES.get(RANDOM_LETTER_SAMPLER.getNextIndex(RANDOM_LETTER_PROBABILITIES, RANDOM_LETTER_TOTAL_PROBABILITY));
		}

		RouletteSampler sampler = new RouletteSampler();

		List<LetterProbability> probabilities = new ArrayList<>(NUM_LETTERS);

		for (Map.Entry<Character, TreeNGram> entry : match.getTransitions().entrySet()) {
			LetterProbability probability = new LetterProbability(entry.getKey(), entry.getValue().getConditionalProbability());

			probabilities.add(probability);
		}

		Float totalProbability = sampler.reIndex(probabilities);

		int nextIndex = sampler.getNextIndex(probabilities, totalProbability);

		return probabilities.get(nextIndex);
	}

	protected INDArray generateRandomSample() {
		INDArray randomSample = Nd4j.create(inputLayerNeurons);

		StringBuffer sb = null;

		if (log.isDebugEnabled()) {
			sb = new StringBuffer();
		}

		for (int j = 0; j < UNIQUE_INPUT_COUNT; j++) {
			char nextLetter = ModelConstants.LOWERCASE_LETTERS.get(ThreadLocalRandom.current().nextInt(NUM_LETTERS));

			if (log.isDebugEnabled()) {
				sb.append(nextLetter);
			}

			Float[] sparseCoding = charToFloatArray(nextLetter);

			for (int k = 0; k < sparseCoding.length; k ++) {
				randomSample.putScalar((j * NUM_LETTERS) + k, sparseCoding[k]);
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Random sample: {}", sb.toString());
		}

		return randomSample;
	}

	protected List<CompletableFuture<Void>> parseFiles(Path path) {
		List<CompletableFuture<Void>> tasks = new ArrayList<>();
        CompletableFuture<Void> task;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					tasks.addAll(parseFiles(entry));
				} else {
					task = CompletableFuture.runAsync(() -> fileParser.parse(entry, UNIQUE_INPUT_COUNT), taskExecutor);
					tasks.add(task);
				}
			}
		} catch (IOException ioe) {
			log.error("Unable to parse files.", ioe);
		}

		return tasks;
	}
}
