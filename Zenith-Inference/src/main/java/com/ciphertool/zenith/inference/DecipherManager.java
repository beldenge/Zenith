/**
 * Copyright 2017-2019 George Belden
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

package com.ciphertool.zenith.inference;

import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Plaintext;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.Zodiac408KnownPlaintextEvaluator;
import com.ciphertool.zenith.inference.probability.LetterProbability;
import com.ciphertool.zenith.inference.selection.RouletteSampler;
import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DecipherManager {
	private Logger				log						= LoggerFactory.getLogger(getClass());

	private static final double FIFTH_ROOT = 1d / 5d;

	@Value("${cipher.name}")
	private String cipherName;

	@Value("${decipherment.sampler.iterations}")
	private int	samplerIterations;

	@Value("${decipherment.annealing.temperature.max}")
	private int	annealingTemperatureMax;

	@Value("${decipherment.annealing.temperature.min}")
	private int	annealingTemperatureMin;

	@Value("${decipherment.sampler.iterateRandomly}")
	private Boolean	iterateRandomly;

	@Value("${markov.letter.order}")
	private int	markovOrder;

	@Value("${decipherment.useKnownEvaluator:false}")
	private boolean	useKnownEvaluator;

	@Value("${decipherment.epochs:1}")
	private int epochs;

	@Value("${decipherment.transposition.column-key:#{null}}")
	private String transpositionKey;

	@Value("${decipherment.remove-last-row:true}")
	private boolean removeLastRow;

	@Autowired
	private PlaintextEvaluator				plaintextEvaluator;

	@Autowired
	private CipherDao						cipherDao;

	@Autowired
	private LetterNGramDao					letterNGramDao;

	@Autowired(required = false)
	private Zodiac408KnownPlaintextEvaluator knownPlaintextEvaluator;

	public void run() {
		Cipher cipher = transformCipher(cipherDao.findByCipherName(cipherName));

		int cipherKeySize = (int) cipher.getCiphertextCharacters().stream().map(c -> c.getValue()).distinct().count();

		long startFindAll = System.currentTimeMillis();
		log.info("Beginning retrieval of all n-grams.");

		/*
		 * Begin setting up letter n-gram model
		 */
		List<TreeNGram> nGramNodes = letterNGramDao.findAll();

		log.info("Finished retrieving {} n-grams in {}ms.", nGramNodes.size(), (System.currentTimeMillis() - startFindAll));

		TreeMarkovModel letterMarkovModel = new TreeMarkovModel(this.markovOrder);

		long startAdding = System.currentTimeMillis();
		log.info("Adding nodes to the model.");

		nGramNodes.stream().forEach(letterMarkovModel::addNode);

		log.info("Finished adding nodes to the letter n-gram model in {}ms.", (System.currentTimeMillis() - startAdding));

		List<TreeNGram> firstOrderNodes = new ArrayList<>(letterMarkovModel.getRootNode().getTransitions().values());

		long totalNumberOfNgrams = firstOrderNodes.stream()
				.mapToLong(TreeNGram::getCount)
				.sum();

		Double unknownLetterNGramProbability = 1d / (double) totalNumberOfNgrams;
		letterMarkovModel.setUnknownLetterNGramProbability(unknownLetterNGramProbability);
		letterMarkovModel.setUnknownLetterNGramLogProbability(Math.log(unknownLetterNGramProbability));

		List<LetterProbability>	letterUnigramProbabilities	= new ArrayList<>(ModelConstants.LOWERCASE_LETTERS.size());

		Double probability;
		for (TreeNGram node : firstOrderNodes) {
			probability = (double) node.getCount() / (double) totalNumberOfNgrams;

			letterUnigramProbabilities.add(new LetterProbability(node.getCumulativeString().charAt(0), probability));

			log.info(node.getCumulativeString().charAt(0) + ": " + probability.toString());
		}

		log.info("unknownLetterNGramProbability: {}", letterMarkovModel.getUnknownLetterNGramProbability());

		Collections.sort(letterUnigramProbabilities);
		RouletteSampler<LetterProbability> unigramRouletteSampler = new RouletteSampler<>();
		double totalUnigramProbability = unigramRouletteSampler.reIndex(letterUnigramProbabilities);

		for (int epoch = 0; epoch < epochs; epoch ++) {
			CipherSolution initialSolution = generateInitialSolutionProposal(cipher, cipherKeySize, unigramRouletteSampler, letterUnigramProbabilities, totalUnigramProbability);

			log.info("Epoch {} of {}.  Running sampler for {} iterations.", (epoch + 1), epochs, samplerIterations);

			performEpoch(initialSolution, letterMarkovModel);
		}
	}

	private Cipher transformCipher(Cipher cipher) {
		Cipher transformed = cipher.clone();

		if (transpositionKey != null && !transpositionKey.isEmpty()) {
			if (transpositionKey.length() < 2 || transpositionKey.length() >= cipher.length()) {
				throw new IllegalArgumentException("The transposition key length of " + transpositionKey.length()
						+ " must be greater than one and less than the cipher length of " + cipher.length() + ".");
			}

			int next = 0;
			int[] columnIndices = new int[transpositionKey.length()];

			for (int i = 0; i < ModelConstants.LOWERCASE_LETTERS.size(); i ++) {
				char letter = ModelConstants.LOWERCASE_LETTERS.get(i);

				for (int j = 0; j < transpositionKey.length(); j++) {
					if (transpositionKey.charAt(j) == letter) {
						columnIndices[j] = next;
						next ++;
					}
				}
			}

			log.info("Transposition column key '{}' produced indices {}.", transpositionKey, columnIndices);

			int rows = cipher.length() / transpositionKey.length();

			int k = 0;
			for (int i = 0; i < transpositionKey.length(); i ++) {
				for (int j = 0; j < rows; j ++) {
					transformed.replaceCiphertextCharacter((j * transpositionKey.length()) + i, cipher.getCiphertextCharacters().get(k).clone());
					k ++;
				}
			}

			Cipher cloneAfterTranspose = transformed.clone();
			for (int i = 0; i < transpositionKey.length(); i ++) {
				for (int j = 0; j < rows; j ++) {
					transformed.replaceCiphertextCharacter((j * transpositionKey.length()) + i, cloneAfterTranspose.getCiphertextCharacters().get((j * transpositionKey.length()) + columnIndices[i]).clone());
				}
			}
		}

		if (removeLastRow) {
			int totalCharacters = transformed.getCiphertextCharacters().size();
			int lastRowBegin = (transformed.getColumns() * (transformed.getRows() - 1));

			// Remove the last row altogether
			for (int i = totalCharacters - 1; i >= lastRowBegin; i--) {
				transformed.removeCiphertextCharacter(transformed.getCiphertextCharacters().get(i));
			}

			transformed.setRows(transformed.getRows() - 1);
		}

		return transformed;
	}

	private CipherSolution generateInitialSolutionProposal(Cipher cipher, int cipherKeySize, RouletteSampler<LetterProbability> unigramRouletteSampler, List<LetterProbability>	letterUnigramProbabilities, double totalUnigramProbability) {
		CipherSolution solutionProposal = new CipherSolution(cipher, cipherKeySize);

		cipher.getCiphertextCharacters().stream()
				.map(ciphertext -> ciphertext.getValue())
				.distinct()
				.forEach(ciphertext -> {
					// Pick a plaintext at random according to the language model
					String nextPlaintext = letterUnigramProbabilities.get(unigramRouletteSampler.getNextIndex(letterUnigramProbabilities, totalUnigramProbability)).getValue().toString();

					solutionProposal.putMapping(ciphertext, new Plaintext(nextPlaintext));
				});

		return solutionProposal;
	}

	private void performEpoch(CipherSolution initialSolution, TreeMarkovModel letterMarkovModel) {
		plaintextEvaluator.evaluate(letterMarkovModel, initialSolution, null);

		if (useKnownEvaluator && knownPlaintextEvaluator != null) {
			initialSolution.setKnownSolutionProximity(knownPlaintextEvaluator.evaluate(initialSolution));
		}

		log.debug(initialSolution.toString());

		Double maxTemp = (double) annealingTemperatureMax;
		Double minTemp = (double) annealingTemperatureMin;
		Double iterations = (double) samplerIterations;
		Double temperature;
		CipherSolution next = initialSolution;
		CipherSolution maxBayes = initialSolution;
		int maxBayesIteration = 0;
		CipherSolution maxKnown = initialSolution;
		int maxKnownIteration = 0;
		long start = System.currentTimeMillis();
		long startLetterSampling;
		long letterSamplingElapsed;
		Double knownProximity;

		int i;
		for (i = 0; i < samplerIterations; i++) {
			long iterationStart = System.currentTimeMillis();

			/*
			 * Set temperature as a ratio of the max temperature to the number of iterations left, offset by the min
			 * temperature so as not to go below it
			 */
			temperature = ((maxTemp - minTemp) * ((iterations - (double) i) / iterations)) + minTemp;

			startLetterSampling = System.currentTimeMillis();
			next = runLetterSampler(temperature, next, letterMarkovModel);
			letterSamplingElapsed = (System.currentTimeMillis() - startLetterSampling);

			if (useKnownEvaluator && knownPlaintextEvaluator != null) {
				knownProximity = knownPlaintextEvaluator.evaluate(next);
				next.setKnownSolutionProximity(knownProximity);

				if (maxKnown.getKnownSolutionProximity() < knownProximity) {
					maxKnown = next;
					maxKnownIteration = i + 1;
				}
			}

			if (maxBayes.getLogProbability().compareTo(next.getLogProbability()) < 0) {
				maxBayes = next;
				maxBayesIteration = i + 1;
			}

			log.debug("Iteration {} complete.  [elapsed={}ms, letterSampling={}ms, temp={}]", (i + 1), (System.currentTimeMillis() - iterationStart), letterSamplingElapsed, String.format("%1$,.2f", temperature));
			log.debug(next.toString());
		}

		log.info("Letter sampling completed in " + (System.currentTimeMillis() - start) + "ms.  Average=" + ((double) (System.currentTimeMillis() - start) / (double) i) + "ms.");

		if (useKnownEvaluator && knownPlaintextEvaluator != null) {
			log.info("Best known found at iteration " + maxKnownIteration + ": " + maxKnown);
			log.info("Mappings for best known:");

			for (Map.Entry<String, Plaintext> entry : maxKnown.getMappings().entrySet()) {
				log.info(entry.getKey() + ": " + entry.getValue().getValue());
			}
		}

		log.info("Best probability found at iteration " + maxBayesIteration + ": " + maxBayes);
		log.info("Mappings for best probability:");

		for (Map.Entry<String, Plaintext> entry : maxBayes.getMappings().entrySet()) {
			log.info(entry.getKey() + ": " + entry.getValue().getValue());
		}
	}

	private CipherSolution runLetterSampler(Double temperature, CipherSolution solution, TreeMarkovModel letterMarkovModel) {
		CipherSolution proposal;

		List<Map.Entry<String, Plaintext>> mappingList = new ArrayList<>();
		mappingList.addAll(solution.getMappings().entrySet());

		Map.Entry<String, Plaintext> nextEntry;

		// For each cipher symbol type, run the letter sampling
		for (int i = 0; i < solution.getMappings().size(); i++) {
			proposal = solution.clone();

			nextEntry = iterateRandomly ? mappingList.remove(ThreadLocalRandom.current().nextInt(mappingList.size())) : mappingList.get(i);

			String letter = ModelConstants.LOWERCASE_LETTERS.get(ThreadLocalRandom.current().nextInt(ModelConstants.LOWERCASE_LETTERS.size())).toString();

			proposal.replaceMapping(nextEntry.getKey(), new Plaintext(letter));

			plaintextEvaluator.evaluate(letterMarkovModel, proposal, nextEntry.getKey());

			solution = selectNext(temperature, solution, proposal);
		}

		return solution;
	}

	private CipherSolution selectNext(Double temperature, CipherSolution solution, CipherSolution proposal) {
		Double acceptanceProbability;

		Double solutionCoincidence = solution.computeIndexOfCoincidence();
		Double proposalCoincidence = proposal.computeIndexOfCoincidence();
		Double solutionScore = solution.getLogProbability() * Math.pow(solutionCoincidence, FIFTH_ROOT);
		Double proposalScore = proposal.getLogProbability() * Math.pow(proposalCoincidence, FIFTH_ROOT);

		if (proposalScore.compareTo(solutionScore) >= 0) {
			log.debug("Better solution found");
			return proposal;
		} else {
			// Need to convert to log probabilities in order for the acceptance probability calculation to be useful
			acceptanceProbability = Math.exp(((solutionScore - proposalScore) / temperature) * -1d);

			log.debug("Acceptance probability: {}", acceptanceProbability);

			if (acceptanceProbability < 0d) {
				throw new IllegalStateException("Acceptance probability was calculated to be less than zero.  Please review the math as this should not happen.");
			}

			if (acceptanceProbability > 1d || ThreadLocalRandom.current().nextDouble() < acceptanceProbability.doubleValue()) {
				return proposal;
			}
		}

		return solution;
	}
}
