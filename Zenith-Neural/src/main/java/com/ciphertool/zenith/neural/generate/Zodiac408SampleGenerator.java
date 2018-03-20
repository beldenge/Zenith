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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;

import com.ciphertool.zenith.math.sampling.RouletteSampler;
import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;
import com.ciphertool.zenith.model.probability.LetterProbability;
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

	private static final int	CHAR_TO_NUMERIC_OFFSET	= 9;

	@Min(1)
	@Value("${network.layers.input}")
	private int						inputLayerNeurons;

	@Min(2)
	@Value("${network.layers.output}")
	private int						outputLayerNeurons;

	@Value("${network.testSamples.count}")
	private int						testSampleCount;

	@Value("${network.trainingSamples.count}")
	private int						trainingSampleCount;

	@NotBlank
	@Value("${task.zodiac408.sourceDirectory}")
	private String					validTrainingTextDirectory;

	@NotBlank
	@Value("${task.zodiac408.samplesFile}")
	private String					samplesFile;

	@Autowired
	private LetterNGramDao letterNGramDao;

	@Autowired
	private ProcessedTextFileParser	fileParser;

	private TreeMarkovModel letterMarkovModel;
	private BigDecimal[][]			englishTrainingSamples;
	private BigDecimal[][]			englishTestSamples;

	private static RouletteSampler RANDOM_LETTER_SAMPLER = new RouletteSampler();
	private static List<LetterProbability> RANDOM_LETTER_PROBABILITIES = new ArrayList<>(ModelConstants.LOWERCASE_LETTERS.size());

	static {
		for (Character letter : ModelConstants.LOWERCASE_LETTERS) {
			RANDOM_LETTER_PROBABILITIES.add(new LetterProbability(letter, MathConstants.SINGLE_LETTER_RANDOM_PROBABILITY));
		}
	}

	private static BigDecimal RANDOM_LETTER_TOTAL_PROBABILITY = RANDOM_LETTER_SAMPLER.reIndex(RANDOM_LETTER_PROBABILITIES);

	@PostConstruct
	public void init() throws IOException {
		log.info("Starting training text import...");

		Path validTrainingTextDirectoryPath = Paths.get(validTrainingTextDirectory);

		if (!Files.isDirectory(validTrainingTextDirectoryPath)) {
			throw new IllegalArgumentException(
					"Property \"task.zodiac408.sourceDirectory\" must be a directory.");
		}

		Path samplesFilePath = Paths.get(samplesFile);

		if (Files.exists(samplesFilePath)) {
			if (!Files.isRegularFile(samplesFilePath)) {
				throw new IllegalArgumentException(
						"Property \"task.zodiac408.samplesFile\" must be a file.");
			}
		} else {
			Files.createFile(samplesFilePath);

			// Load English training data

			long start = System.currentTimeMillis();

			List<Future<Void>> futures = parseFiles(validTrainingTextDirectoryPath, inputLayerNeurons);

			for (Future<Void> future : futures) {
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					log.error("Caught Exception while waiting for ParseFileTask ", e);
				}
			}

			log.info("Finished processing source directory in {}ms.", (System.currentTimeMillis() - start));
		}

		List<BigDecimal[]> englishParagraphs = importSamples(samplesFilePath);

		if (englishParagraphs.size() < (testSampleCount + trainingSampleCount)) {
			throw new IllegalStateException("Requested " + testSampleCount + " test samples and " + trainingSampleCount + " training samples, but only " + englishParagraphs.size() + " total samples are available.");
		}

		int order = outputLayerNeurons - 2;

		if (order > 0) {
			letterMarkovModel = new TreeMarkovModel(outputLayerNeurons - 2);

			List<TreeNGram> nodes = letterNGramDao.findAll(1, false);

			// TODO: try parallel stream here
			nodes.stream().forEach(letterMarkovModel::addNode);
		}

		buildTrainingAndTestSets(englishParagraphs);
	}

	protected List<BigDecimal[]> importSamples(Path samplesFilePath) throws IOException {
		long start = System.currentTimeMillis();

		List<BigDecimal[]> numericSamples;

		try(Stream<String> lines = Files.lines(samplesFilePath)) {
			numericSamples = new ArrayList<>();

			lines.filter(line -> line != null && !line.isEmpty())
					// One in 10 chance of using a record to keep memory usage manageable
					.filter(line -> ThreadLocalRandom.current().nextInt(10) == 1)
					.forEach(line -> {
				char[] nextSample = line.substring(0, inputLayerNeurons).toCharArray();

				BigDecimal[] numericSample = new BigDecimal[inputLayerNeurons];

				for (int j = 0; j < nextSample.length; j++) {
					numericSample[j] = charToBigDecimal(nextSample[j]);
				}

				numericSamples.add(numericSample);
			});
		}

		log.info("Finished importing {} samples in {}ms.", numericSamples.size(), (System.currentTimeMillis() - start));

		return numericSamples;
	}

	protected void buildTrainingAndTestSets(List<BigDecimal[]> englishParagraphs) {
		BigDecimal[][] englishSamples = new BigDecimal[englishParagraphs.size()][inputLayerNeurons];

		for (int i = 0; i < englishParagraphs.size(); i++) {
			englishSamples[i] = englishParagraphs.get(i);
		}

		englishSamples = shuffleArray(englishSamples);

		List<BigDecimal[]> englishSamplesList = new ArrayList<>(Arrays.asList(englishSamples));

		englishTestSamples = new BigDecimal[testSampleCount][inputLayerNeurons];

		for (int i = 0; i < testSampleCount; i++) {
			englishTestSamples[i] = englishSamplesList.remove(englishSamplesList.size() - 1);
		}

		englishTrainingSamples = new BigDecimal[englishSamplesList.size()][inputLayerNeurons];

		int samplesLeft = englishSamplesList.size();

		for (int i = 0; i < samplesLeft; i++) {
			englishTrainingSamples[i] = englishSamplesList.remove(englishSamplesList.size() - 1);
		}
	}

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
		englishTrainingSamples = shuffleArray(englishTrainingSamples);

		return shuffleDataSet(generate(count, englishTrainingSamples));
	}

	@Override
	public DataSet generateTestSamples(int count) {
		englishTestSamples = shuffleArray(englishTestSamples);

		return generate(count, englishTestSamples);
	}

	protected DataSet generate(int count, BigDecimal[][] samplesToUse) {
		if (count > samplesToUse.length) {
			throw new IllegalArgumentException("The number of samples to generate (" + count
					+ ") exceeds the maximum number of samples available (" + samplesToUse.length + ").");
		}

		BigDecimal[][] samples = new BigDecimal[count * outputLayerNeurons][inputLayerNeurons];
		BigDecimal[][] outputs = new BigDecimal[count * outputLayerNeurons][outputLayerNeurons];

		for (int i = 0; i < count; i++) {
			samples[i] = generateRandomSample();
			outputs[i] = new BigDecimal[outputLayerNeurons];
			outputs[i][0] = BigDecimal.ONE;

			for (int j = 1; j < outputLayerNeurons; j ++) {
				outputs[i][j] = BigDecimal.ZERO;
			}
		}

		for (int h = 1; h < outputLayerNeurons - 1; h ++) {
			int endSampleSet = count + (count * h);
			for (int i = (count * h); i < endSampleSet; i++) {
				samples[i] = generateMarkovModelSample(h);
				outputs[i] = new BigDecimal[outputLayerNeurons];

				for (int j = 0; j < outputLayerNeurons; j ++) {
					outputs[i][j] = (h == j) ? BigDecimal.ONE : BigDecimal.ZERO;
				}
			}
		}

		int beginFinalSampleSet = count * (outputLayerNeurons - 1);
		for (int i = beginFinalSampleSet; i < count * outputLayerNeurons; i++) {
			samples[i] = samplesToUse[i - beginFinalSampleSet];
			outputs[i] = new BigDecimal[outputLayerNeurons];
			outputs[i][outputLayerNeurons - 1] = BigDecimal.ONE;

			for (int j = 0; j < outputLayerNeurons - 1; j ++) {
				outputs[i][j] = BigDecimal.ZERO;
			}
		}

		return new DataSet(samples, outputs);
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

	protected BigDecimal[] generateMarkovModelSample(int markovOrder) {
		BigDecimal[] sample = new BigDecimal[inputLayerNeurons];

		TreeNGram rootNode = letterMarkovModel.getRootNode();
		TreeNGram match;

		String root = "";
		for (int i = 0; i < inputLayerNeurons; i++) {
			match = (root.isEmpty() || markovOrder == 1) ? rootNode : letterMarkovModel.findLongest(root);

			LetterProbability chosen = sampleNextTransitionFromDistribution(match, markovOrder);

			char nextSymbol = chosen.getValue();
			sample[i] = charToBigDecimal(nextSymbol);

			root = ((root.isEmpty() || root.length() < markovOrder - 1) ? root : root.substring(1)) + nextSymbol;
		}

		return sample;
	}

	protected static LetterProbability sampleNextTransitionFromDistribution(TreeNGram match, int markovOrder) {
		if (match == null || match.getCumulativeString().length() < markovOrder) {
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

		for (int j = 0; j < inputLayerSize; j++) {
			randomSample[j] = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(ModelConstants.LOWERCASE_LETTERS.size())
					+ 1).divide(BigDecimal.valueOf(ModelConstants.LOWERCASE_LETTERS.size()), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
		}

		return randomSample;
	}

	protected List<Future<Void>> parseFiles(Path path, int inputLayerSize) {
		List<Future<Void>> tasks = new ArrayList<>();
		Future<Void> task;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					tasks.addAll(parseFiles(entry, inputLayerSize));
				} else {
					task = fileParser.parse(entry, inputLayerSize);
					tasks.add(task);
				}
			}
		} catch (IOException ioe) {
			log.error("Unable to parse files.", ioe);
		}

		return tasks;
	}
}
