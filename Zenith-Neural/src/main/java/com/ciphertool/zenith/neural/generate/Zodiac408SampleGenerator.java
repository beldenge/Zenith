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
import java.util.Map;
import java.util.concurrent.*;

import javax.validation.constraints.Min;

import com.ciphertool.zenith.math.sampling.RouletteSampler;
import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;
import com.ciphertool.zenith.model.probability.LetterProbability;
import com.ciphertool.zenith.neural.generate.zodiac408.EnglishParagraph;
import com.ciphertool.zenith.neural.generate.zodiac408.EnglishParagraphDao;
import com.ciphertool.zenith.neural.generate.zodiac408.EnglishParagraphSequenceDao;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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

	private static final int	CHAR_TO_NUMERIC_OFFSET	= 9;

	private static final RouletteSampler RANDOM_LETTER_SAMPLER = new RouletteSampler();
	private static final List<LetterProbability> RANDOM_LETTER_PROBABILITIES = new ArrayList<>(ModelConstants.LOWERCASE_LETTERS.size());

	static {
		for (Character letter : ModelConstants.LOWERCASE_LETTERS) {
			RANDOM_LETTER_PROBABILITIES.add(new LetterProbability(letter, MathConstants.SINGLE_LETTER_RANDOM_PROBABILITY));
		}
	}

	private static final BigDecimal RANDOM_LETTER_TOTAL_PROBABILITY = RANDOM_LETTER_SAMPLER.reIndex(RANDOM_LETTER_PROBABILITIES);

	@Min(1)
	@Value("${network.layers.input}")
	private int						inputLayerNeurons;

	@Min(2)
	@Value("${network.layers.output}")
	private int						outputLayerNeurons;

	@NotBlank
	@Value("${task.zodiac408.sourceDirectory}")
	private String					validTrainingTextDirectory;

	@Value("${network.testSamples.count}")
	private int						testSampleCount;

	@Value("${network.trainingSamples.count}")
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

		List<CompletableFuture<Void>> futures = parseFiles(validTrainingTextDirectoryPath, inputLayerNeurons);

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();

		log.info("Finished processing source directory in {}ms.", (System.currentTimeMillis() - start));
	}

	// TODO: cache these calculations for crying out loud
	protected BigDecimal charToBigDecimal(char c) {
		int numericValue = Character.getNumericValue(c) - CHAR_TO_NUMERIC_OFFSET;

		return BigDecimal.valueOf(numericValue).divide(BigDecimal.valueOf(ModelConstants.LOWERCASE_LETTERS.size()), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
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
		if (!intialized) {
			init();
		}

		return shuffleDataSet(generate(count));
	}

	@Override
	public DataSet generateTrainingSample() {
		if (!intialized) {
			init();
		}

		return generateOne();
	}

	@Override
	public DataSet generateTestSamples(int count) {
		if (!intialized) {
			init();
		}

		return generate(count);
	}

	@Override
	public DataSet generateTestSample() {
		if (!intialized) {
			init();
		}

		return generateOne();
	}

	protected static DataSet shuffleDataSet(DataSet dataSetToShuffle) {
		BigDecimal[][] shuffledInputs = new BigDecimal[dataSetToShuffle.getInputs().length][];
		BigDecimal[][] shuffledOutputs = new BigDecimal[dataSetToShuffle.getOutputs().length][];

		int arrayLength = dataSetToShuffle.getInputs().length;

		List<BigDecimal[]> inputsArrayList = new ArrayList<>(Arrays.asList(dataSetToShuffle.getInputs()));
		List<BigDecimal[]> outputsArrayList = new ArrayList<>(Arrays.asList(dataSetToShuffle.getOutputs()));

		for (int i = 0; i < arrayLength; i++) {
			int randomIndex = ThreadLocalRandom.current().nextInt(inputsArrayList.size());

			shuffledInputs[i] = inputsArrayList.remove(randomIndex);
			shuffledOutputs[i] = outputsArrayList.remove(randomIndex);
		}

		return new DataSet(shuffledInputs, shuffledOutputs);
	}

	protected DataSet generate(int count) {
		BigDecimal[][] samples = new BigDecimal[count * outputLayerNeurons][inputLayerNeurons];
		BigDecimal[][] outputs = new BigDecimal[count * outputLayerNeurons][outputLayerNeurons];

		for (int i = 0; i < count; i++) {
			DataSet next = generateOne();

			int sampleSize = next.getInputs().length;

			for (int j = 0; j < sampleSize; j ++) {
				int index = (i * sampleSize) + j;

				samples[index] = next.getInputs()[j];
				outputs[index] = next.getOutputs()[j];
			}
		}

		return new DataSet(samples, outputs);
	}

	public DataSet generateOne() {
		BigDecimal[][] samples = new BigDecimal[outputLayerNeurons][inputLayerNeurons];
		BigDecimal[][] outputs = new BigDecimal[outputLayerNeurons][outputLayerNeurons];

		// Generate a random sample
		samples[0] = generateRandomSample();
		outputs[0] = new BigDecimal[outputLayerNeurons];
		outputs[0][0] = BigDecimal.ONE;

		for (int i = 1; i < outputLayerNeurons; i ++) {
			outputs[0][i] = BigDecimal.ZERO;
		}

		// Generate probabilistic samples based on Markov model
		for (int i = 1; i < outputLayerNeurons - 1; i ++) {
			samples[i] = generateMarkovModelSample(i);
			outputs[i] = new BigDecimal[outputLayerNeurons];

			for (int j = 0; j < outputLayerNeurons; j ++) {
				outputs[i][j] = (i == j) ? BigDecimal.ONE : BigDecimal.ZERO;
			}
		}

		// Generate a sample from known English paragraphs
		int i = outputLayerNeurons - 1;

		samples[i] = getRandomParagraph();
		outputs[i] = new BigDecimal[outputLayerNeurons];
		outputs[i][outputLayerNeurons - 1] = BigDecimal.ONE;

		for (int j = 0; j < outputLayerNeurons - 1; j ++) {
			outputs[i][j] = BigDecimal.ZERO;
		}

		return new DataSet(samples, outputs);
	}

	protected BigDecimal[] getRandomParagraph() {
		Long randomIndex = ThreadLocalRandom.current().nextLong(englishParagraphCount);

		EnglishParagraph nextParagraph = englishParagraphDao.findBySequence(randomIndex);

		char[] nextSample = nextParagraph.getParagraph().substring(0, inputLayerNeurons).toCharArray();

		log.debug("Random paragraph: {}", String.valueOf(nextSample));

		BigDecimal[] numericSample = new BigDecimal[inputLayerNeurons];

		for (int j = 0; j < nextSample.length; j++) {
			numericSample[j] = charToBigDecimal(nextSample[j]);
		}

		return numericSample;
	}

	protected BigDecimal[] generateMarkovModelSample(int markovOrder) {
		BigDecimal[] sample = new BigDecimal[inputLayerNeurons];

		TreeNGram rootNode = letterMarkovModel.getRootNode();
		TreeNGram match;

		StringBuffer sb = null;

		if (log.isDebugEnabled()) {
			sb = new StringBuffer();
		}

		String root = "";
		for (int i = 0; i < inputLayerNeurons; i++) {
			match = (root.isEmpty() || markovOrder == 1) ? rootNode : letterMarkovModel.findLongest(root);

			LetterProbability chosen = sampleNextTransitionFromDistribution(match, markovOrder);

			char nextSymbol = chosen.getValue();

			if (log.isDebugEnabled()) {
				sb.append(nextSymbol);
			}

			sample[i] = charToBigDecimal(nextSymbol);

			root = ((root.isEmpty() || root.length() < markovOrder - 1) ? root : root.substring(1)) + nextSymbol;
		}

		log.debug("Random sample of order {}: {}", markovOrder, sb.toString());

		return sample;
	}

	protected static LetterProbability sampleNextTransitionFromDistribution(TreeNGram match, int markovOrder) {
		if (match.getTransitions().isEmpty()) {
			return RANDOM_LETTER_PROBABILITIES.get(RANDOM_LETTER_SAMPLER.getNextIndex(RANDOM_LETTER_PROBABILITIES, RANDOM_LETTER_TOTAL_PROBABILITY));
		}

		RouletteSampler sampler = new RouletteSampler();

		List<LetterProbability> probabilities = new ArrayList<>(ModelConstants.LOWERCASE_LETTERS.size());

		for (Map.Entry<Character, TreeNGram> entry : match.getTransitions().entrySet()) {
			LetterProbability probability = new LetterProbability(entry.getKey(), entry.getValue().getConditionalProbability());

			probabilities.add(probability);
		}

		BigDecimal totalProbability = sampler.reIndex(probabilities);

		int nextIndex = sampler.getNextIndex(probabilities, totalProbability);

		return probabilities.get(nextIndex);
	}

	protected BigDecimal[] generateRandomSample() {
		int inputLayerSize = inputLayerNeurons;

		BigDecimal[] randomSample = new BigDecimal[inputLayerSize];

		StringBuffer sb = null;

		if (log.isDebugEnabled()) {
			sb = new StringBuffer();
		}

		for (int j = 0; j < inputLayerSize; j++) {
			char nextLetter = ModelConstants.LOWERCASE_LETTERS.get(ThreadLocalRandom.current().nextInt(ModelConstants.LOWERCASE_LETTERS.size()));

			if (log.isDebugEnabled()) {
				sb.append(nextLetter);
			}

			randomSample[j] = charToBigDecimal(nextLetter);
		}

		log.debug("Random sample: {}", sb.toString());

		return randomSample;
	}

	protected List<CompletableFuture<Void>> parseFiles(Path path, int inputLayerSize) {
		List<CompletableFuture<Void>> tasks = new ArrayList<>();
        CompletableFuture<Void> task;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					tasks.addAll(parseFiles(entry, inputLayerSize));
				} else {
					task = CompletableFuture.runAsync(() -> fileParser.parse(entry, inputLayerSize), taskExecutor);
					tasks.add(task);
				}
			}
		} catch (IOException ioe) {
			log.error("Unable to parse files.", ioe);
		}

		return tasks;
	}
}
