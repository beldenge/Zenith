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

package com.ciphertool.zenith.inference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.dto.EvaluationResults;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Plaintext;
import com.ciphertool.zenith.inference.evaluator.KnownPlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.NeuralNetworkPlaintextEvaluator;
import com.ciphertool.zenith.inference.probability.BoundaryProbability;
import com.ciphertool.zenith.model.probability.LetterProbability;
import com.ciphertool.zenith.inference.probability.SolutionProbability;
import com.ciphertool.zenith.math.sampling.RouletteSampler;
import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.model.ModelConstants;

@Component
public class BayesianDecipherManager {
	private Logger				log						= LoggerFactory.getLogger(getClass());
	private Logger				badPredictionLog		= LoggerFactory.getLogger("com.ciphertool.zenith.inference.badPredictionLog");

	private static Float[]	letterProbabilities		= new Float[ModelConstants.LOWERCASE_LETTERS.size()];
	private static Float[]	vowelProbabilities		= new Float[ModelConstants.LOWERCASE_VOWELS.size()];
	private static Float[]	consonantProbabilities	= new Float[ModelConstants.LOWERCASE_CONSONANTS.size()];

	static {
		Float numLetters = (float) ModelConstants.LOWERCASE_LETTERS.size();
		Float singleLetterRatio = 1.0f / numLetters;

		Float numVowels = (float) ModelConstants.LOWERCASE_VOWELS.size();
		Float singleVowelRatio = 1.0f / numVowels;

		Float numConsonants = (float) ModelConstants.LOWERCASE_CONSONANTS.size();
		Float singleConsonantRatio = 1.0f / numConsonants;

		for (int i = 0; i < letterProbabilities.length; i++) {
			letterProbabilities[i] = 0.0f;
		}

		for (int i = 0; i < vowelProbabilities.length; i++) {
			vowelProbabilities[i] = 0.0f;
		}

		for (int i = 0; i < consonantProbabilities.length; i++) {
			consonantProbabilities[i] = 0.0f;
		}

		for (int i = 1; i <= letterProbabilities.length; i++) {
			for (int j = letterProbabilities.length - 1; j >= letterProbabilities.length - i; j--) {
				letterProbabilities[j] = letterProbabilities[j] + (singleLetterRatio / (float) i);
			}
		}

		for (int i = 1; i <= vowelProbabilities.length; i++) {
			for (int j = vowelProbabilities.length - 1; j >= vowelProbabilities.length - i; j--) {
				vowelProbabilities[j] = vowelProbabilities[j] + (singleVowelRatio / (float) i);
			}
		}

		for (int i = 1; i <= consonantProbabilities.length; i++) {
			for (int j = consonantProbabilities.length - 1; j >= consonantProbabilities.length - i; j--) {
				consonantProbabilities[j] = consonantProbabilities[j] + (singleConsonantRatio / (float) i);
			}
		}
	}

	@Value("${cipher.name}")
	private String							cipherName;

	@Value("${bayes.sampler.iterations}")
	private int								samplerIterations;

	@Value("${bayes.annealing.temperature.max}")
	private int								annealingTemperatureMax;

	@Value("${bayes.annealing.temperature.min}")
	private int								annealingTemperatureMin;

	@Value("${bayes.sampler.iterateRandomly}")
	private Boolean							iterateRandomly;

	@Value("${markov.letter.include.word.boundaries}")
	private Boolean							includeWordBoundaries;

	@Value("${markov.letter.order}")
	private int								markovOrder;

	@Value("${markov.minimum.count}")
	private int								minimumCount;

	@Value("${bayes.sampler.letterTypeSampling.enabled}")
	private boolean							letterTypeSamplingEnabled;

	@Autowired
	private NeuralNetworkPlaintextEvaluator	plaintextEvaluator;

	@Autowired
	private CipherDao						cipherDao;

	@Autowired(required = false)
	private KnownPlaintextEvaluator			knownPlaintextEvaluator;

	@Autowired
	private TaskExecutor					taskExecutor;

	private Cipher							cipher;
	private int								cipherKeySize;
	private static List<LetterProbability>	letterUnigramProbabilities	= new ArrayList<>(26);

	/**
	 * @see <a href="https://en.wikipedia.org/wiki/Letter_frequency">https://en.wikipedia.org/wiki/Letter_frequency</a>
	 */
	static {
		letterUnigramProbabilities.add(new LetterProbability('a', 0.08167f));
		letterUnigramProbabilities.add(new LetterProbability('b', 0.01492f));
		letterUnigramProbabilities.add(new LetterProbability('c', 0.02782f));
		letterUnigramProbabilities.add(new LetterProbability('d', 0.04523f));
		letterUnigramProbabilities.add(new LetterProbability('e', 0.12702f));
		letterUnigramProbabilities.add(new LetterProbability('f', 0.02228f));
		letterUnigramProbabilities.add(new LetterProbability('g', 0.02015f));
		letterUnigramProbabilities.add(new LetterProbability('h', 0.06094f));
		letterUnigramProbabilities.add(new LetterProbability('i', 0.06996f));
		letterUnigramProbabilities.add(new LetterProbability('j', 0.00153f));
		letterUnigramProbabilities.add(new LetterProbability('k', 0.00772f));
		letterUnigramProbabilities.add(new LetterProbability('l', 0.04025f));
		letterUnigramProbabilities.add(new LetterProbability('m', 0.02406f));
		letterUnigramProbabilities.add(new LetterProbability('n', 0.06749f));
		letterUnigramProbabilities.add(new LetterProbability('o', 0.07507f));
		letterUnigramProbabilities.add(new LetterProbability('p', 0.01929f));
		letterUnigramProbabilities.add(new LetterProbability('q', 0.00095f));
		letterUnigramProbabilities.add(new LetterProbability('r', 0.05987f));
		letterUnigramProbabilities.add(new LetterProbability('s', 0.06327f));
		letterUnigramProbabilities.add(new LetterProbability('t', 0.09056f));
		letterUnigramProbabilities.add(new LetterProbability('u', 0.02758f));
		letterUnigramProbabilities.add(new LetterProbability('v', 0.00978f));
		letterUnigramProbabilities.add(new LetterProbability('w', 0.02360f));
		letterUnigramProbabilities.add(new LetterProbability('x', 0.00150f));
		letterUnigramProbabilities.add(new LetterProbability('y', 0.01974f));
		letterUnigramProbabilities.add(new LetterProbability('z', 0.00074f));

		Float sum = 0.0f;

		for (LetterProbability probability : letterUnigramProbabilities) {
			sum = sum + probability.getProbability();
		}

		for (LetterProbability probability : letterUnigramProbabilities) {
			probability.setProbability(probability.getProbability() / sum);
		}
	}

	@PostConstruct
	public void setUp() {
		this.cipher = cipherDao.findByCipherName(cipherName);
		int totalCharacters = this.cipher.getCiphertextCharacters().size();
		int lastRowBegin = (this.cipher.getColumns() * (this.cipher.getRows() - 1));

		// Remove the last row altogether
		for (int i = lastRowBegin; i < totalCharacters; i++) {
			this.cipher.removeCiphertextCharacter(this.cipher.getCiphertextCharacters().get(lastRowBegin));
		}

		cipherKeySize = (int) cipher.getCiphertextCharacters().stream().map(c -> c.getValue()).distinct().count();
	}

	public void run() {
		// Initialize the solution key
		CipherSolution initialSolution = new CipherSolution(cipher, cipherKeySize);

		RouletteSampler<LetterProbability> rouletteSampler = new RouletteSampler<>();
		Collections.sort(letterUnigramProbabilities);
		Float totalProbability = rouletteSampler.reIndex(letterUnigramProbabilities);

		cipher.getCiphertextCharacters().stream().map(ciphertext -> ciphertext.getValue()).distinct().forEach(ciphertext -> {
			// Pick a plaintext at random according to the language model
			String nextPlaintext = letterUnigramProbabilities.get(rouletteSampler.getNextIndex(letterUnigramProbabilities, totalProbability)).getValue().toString();

			initialSolution.putMapping(ciphertext, new Plaintext(nextPlaintext));
		});

		if (includeWordBoundaries) {
			Float wordBoundaryProbability = 1.0f / (float) LanguageConstants.AVERAGE_WORD_SIZE;

			for (int i = 0; i < cipher.getCiphertextCharacters().size() - 1; i++) {
				if (ThreadLocalRandom.current().nextFloat() < wordBoundaryProbability) {
					initialSolution.addWordBoundary(i);
				}
			}
		}

		EvaluationResults initialPlaintextResults = plaintextEvaluator.evaluate(initialSolution, null);
		initialSolution.setProbability(initialPlaintextResults.getProbability());
		initialSolution.setLogProbability(initialPlaintextResults.getLogProbability());

		if (knownPlaintextEvaluator != null) {
			initialSolution.setKnownSolutionProximity(knownPlaintextEvaluator.evaluate(initialSolution));
		}

		log.info(initialSolution.toString());

		Float maxTemp = (float) annealingTemperatureMax;
		Float minTemp = (float) annealingTemperatureMin;
		Float iterations = (float) samplerIterations;
		Float temperature;

		CipherSolution next = initialSolution;
		CipherSolution maxBayes = initialSolution;
		int maxBayesIteration = 0;
		CipherSolution maxKnown = initialSolution;
		int maxKnownIteration = 0;

		log.info("Running Gibbs sampler for " + samplerIterations + " iterations.");
		long start = System.currentTimeMillis();
		long startLetterSampling;
		long letterSamplingElapsed;
		long startWordSampling;
		long wordSamplingElapsed;

		Float knownProximity = null;
		int i;
		for (i = 0; i < samplerIterations; i++) {
			long iterationStart = System.currentTimeMillis();

			/*
			 * Set temperature as a ratio of the max temperature to the number of iterations left, offset by the min
			 * temperature so as not to go below it
			 */
			temperature = (((maxTemp - minTemp) * (iterations - (float) i)) / iterations) + minTemp;

			startLetterSampling = System.currentTimeMillis();
			next = runGibbsLetterSampler(temperature, next);
			letterSamplingElapsed = (System.currentTimeMillis() - startLetterSampling);

			EvaluationResults fullPlaintextResults = plaintextEvaluator.evaluate(next, null);
			next.setProbability(fullPlaintextResults.getProbability());
			next.setLogProbability(fullPlaintextResults.getLogProbability());

			startWordSampling = System.currentTimeMillis();
			if (includeWordBoundaries) {
				next = runGibbsWordBoundarySampler(temperature, next);
			}
			wordSamplingElapsed = (System.currentTimeMillis() - startWordSampling);

			if (knownPlaintextEvaluator != null) {
				knownProximity = knownPlaintextEvaluator.evaluate(next);
				next.setKnownSolutionProximity(knownProximity);

				if (next.getKnownSolutionProximity() < 0.05
						&& next.getProbability() > 0.99) {
					badPredictionLog.info(next.asSingleLineString());
				}

				if (maxKnown.getKnownSolutionProximity() < knownProximity) {
					maxKnown = next;
					maxKnownIteration = i + 1;
				}
			}

			if (maxBayes.getProbability().compareTo(next.getProbability()) < 0) {
				maxBayes = next;
				maxBayesIteration = i + 1;
			}

			log.info("Iteration " + (i + 1) + " complete.  [elapsed=" + (System.currentTimeMillis() - iterationStart)
					+ "ms, letterSampling=" + letterSamplingElapsed + "ms, wordSampling=" + wordSamplingElapsed
					+ "ms, temp=" + String.format("%1$,.2f", temperature) + "]");
			log.info(next.toString());
		}

		log.info("Gibbs sampling completed in " + (System.currentTimeMillis() - start) + "ms.  Average="
				+ ((float) (System.currentTimeMillis() - start) / (float) i) + "ms.");

		log.info("Best known found at iteration " + maxKnownIteration + ": " + maxKnown);
		log.info("Mappings for best known:");

		for (Map.Entry<String, Plaintext> entry : maxKnown.getMappings().entrySet()) {
			log.info(entry.getKey() + ": " + entry.getValue().getValue());
		}

		log.info("Best probability found at iteration " + maxBayesIteration + ": " + maxBayes);
		log.info("Mappings for best probability:");

		for (Map.Entry<String, Plaintext> entry : maxBayes.getMappings().entrySet()) {
			log.info(entry.getKey() + ": " + entry.getValue().getValue());
		}
	}

	protected CipherSolution runGibbsLetterSampler(Float temperature, CipherSolution solution) {
		RouletteSampler<SolutionProbability> rouletteSampler = new RouletteSampler<>();
		CipherSolution proposal = null;
		Float totalProbability;

		List<Map.Entry<String, Plaintext>> mappingList = new ArrayList<>();
		mappingList.addAll(solution.getMappings().entrySet());

		Map.Entry<String, Plaintext> nextEntry;
		CipherSolution original;

		// For each cipher symbol type, run the gibbs sampling
		for (int i = 0; i < solution.getMappings().size(); i++) {
			original = solution.clone();

			nextEntry = iterateRandomly ? mappingList.remove(ThreadLocalRandom.current().nextInt(mappingList.size())) : mappingList.get(i);

			EvaluationResults fullPlaintextResults = plaintextEvaluator.evaluate(original, nextEntry.getKey());
			original.setProbability(fullPlaintextResults.getProbability());
			original.setLogProbability(fullPlaintextResults.getLogProbability());

			List<SolutionProbability> plaintextDistribution = computeDistribution(nextEntry.getKey(), original);

			Collections.sort(plaintextDistribution);

			totalProbability = rouletteSampler.reIndex(plaintextDistribution);

			proposal = plaintextDistribution.get(rouletteSampler.getNextIndex(plaintextDistribution, totalProbability)).getValue();

			solution = selectNext(temperature, original, proposal);
		}

		return solution;
	}

	/**
	 * A concurrent task for computing the letter probability during Gibbs sampling.
	 */
	protected class LetterProbabilityTask implements Callable<CipherSolution> {
		private Character		letter;
		private String			ciphertextKey;
		private CipherSolution	conditionalSolution;

		/**
		 * @param conditionalSolution
		 *            the original unmodified solution
		 * @param letter
		 *            the letter to sample for
		 * @param ciphertextKey
		 *            the ciphertext key
		 */
		public LetterProbabilityTask(CipherSolution conditionalSolution, Character letter, String ciphertextKey) {
			this.letter = letter;
			this.ciphertextKey = ciphertextKey;
			this.conditionalSolution = conditionalSolution;
		}

		@Override
		public CipherSolution call() {
			if (conditionalSolution.getMappings().get(ciphertextKey).equals(new Plaintext(letter.toString()))) {
				// No need to re-score the solution in this case
				return conditionalSolution;
			}

			conditionalSolution.replaceMapping(ciphertextKey, new Plaintext(letter.toString()));

			long startPlaintext = System.currentTimeMillis();
			EvaluationResults remainingPlaintextResults = plaintextEvaluator.evaluate(conditionalSolution, ciphertextKey);
			log.debug("Partial plaintext took {}ms.", (System.currentTimeMillis() - startPlaintext));

			conditionalSolution.setProbability(remainingPlaintextResults.getProbability());
			conditionalSolution.setLogProbability(remainingPlaintextResults.getLogProbability());

			return conditionalSolution;
		}
	}

	protected List<SolutionProbability> computeDistribution(String ciphertextKey, CipherSolution solution) {
		List<SolutionProbability> plaintextDistribution = new ArrayList<>();
		Float sumOfProbabilities = 0.0f;

		List<FutureTask<CipherSolution>> futures = new ArrayList<FutureTask<CipherSolution>>(26);
		FutureTask<CipherSolution> task;

		// Calculate the full conditional probability for each possible plaintext substitution
		for (Character letter : ModelConstants.LOWERCASE_LETTERS) {
			task = new FutureTask<>(new LetterProbabilityTask(solution.clone(), letter, ciphertextKey));
			futures.add(task);
			this.taskExecutor.execute(task);
		}

		CipherSolution next;

		for (FutureTask<CipherSolution> future : futures) {
			try {
				next = future.get();

				plaintextDistribution.add(new SolutionProbability(next, next.getProbability()));
				sumOfProbabilities = sumOfProbabilities + next.getProbability();
			} catch (InterruptedException ie) {
				log.error("Caught InterruptedException while waiting for LetterProbabilityTask ", ie);
			} catch (ExecutionException ee) {
				log.error("Caught ExecutionException while waiting for LetterProbabilityTask ", ee);
			}
		}

		// Normalize the probabilities
		// TODO: should we also somehow normalize the log probabilities?
		for (SolutionProbability solutionProbability : plaintextDistribution) {
			solutionProbability.setProbability(solutionProbability.getProbability() / sumOfProbabilities);
		}

		return plaintextDistribution;
	}

	protected CipherSolution runGibbsWordBoundarySampler(Float temperature, CipherSolution solution) {
		int nextBoundary;
		Float sumOfProbabilities = null;
		List<BoundaryProbability> boundaryProbabilities = null;
		boolean isAddBoundary = false;
		CipherSolution addProposal = null;
		CipherSolution removeProposal = null;
		CipherSolution proposal = null;
		EvaluationResults addPlaintextResults = null;
		EvaluationResults removePlaintextResults = null;
		Float totalProbability;
		RouletteSampler<BoundaryProbability> rouletteSampler = new RouletteSampler<>();

		for (int i = 0; i < cipher.getCiphertextCharacters().size() - 1; i++) {
			boundaryProbabilities = new ArrayList<>();
			nextBoundary = i;

			addProposal = solution.clone();
			if (!addProposal.getWordBoundaries().contains(nextBoundary)) {
				addProposal.addWordBoundary(nextBoundary);
				addPlaintextResults = plaintextEvaluator.evaluate(addProposal, null);
				addProposal.setProbability(addPlaintextResults.getProbability());
				addProposal.setLogProbability(addPlaintextResults.getLogProbability());
			}

			removeProposal = solution.clone();
			if (removeProposal.getWordBoundaries().contains(nextBoundary)) {
				removeProposal.removeWordBoundary(nextBoundary);
				removePlaintextResults = plaintextEvaluator.evaluate(removeProposal, null);
				removeProposal.setProbability(removePlaintextResults.getProbability());
				removeProposal.setLogProbability(removePlaintextResults.getLogProbability());
			}

			sumOfProbabilities = addProposal.getProbability() + removeProposal.getProbability();

			boundaryProbabilities.add(new BoundaryProbability(true,
					addProposal.getProbability() / sumOfProbabilities));
			boundaryProbabilities.add(new BoundaryProbability(false,
					removeProposal.getProbability() / sumOfProbabilities));

			Collections.sort(boundaryProbabilities);
			totalProbability = rouletteSampler.reIndex(boundaryProbabilities);

			isAddBoundary = boundaryProbabilities.get(rouletteSampler.getNextIndex(boundaryProbabilities, totalProbability)).getValue();

			if (isAddBoundary) {
				proposal = addProposal;
			} else {
				proposal = removeProposal;
			}

			solution = selectNext(temperature, solution, proposal);
		}

		return solution;
	}

	protected CipherSolution selectNext(Float temperature, CipherSolution solution, CipherSolution proposal) {
		Float acceptanceProbability = null;

		if (proposal.getLogProbability().compareTo(solution.getLogProbability()) >= 0) {
			log.debug("Better solution found");
			return proposal;
		} else {
			// Need to convert to log probabilities in order for the acceptance probability calculation to be useful
			acceptanceProbability = (float) Math.exp(((solution.getLogProbability() - proposal.getLogProbability()) / temperature) * -1.0);

			log.debug("Acceptance probability: {}", acceptanceProbability);

			if (acceptanceProbability< 0.0) {
				throw new IllegalStateException(
						"Acceptance probability was calculated to be less than zero.  Please review the math as this should not happen.");
			}

			if (acceptanceProbability > 1.0
					|| ThreadLocalRandom.current().nextFloat() < acceptanceProbability) {
				return proposal;
			}
		}

		return solution;
	}
}
