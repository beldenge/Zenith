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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.nevec.rjm.BigDecimalMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.task.TaskExecutor;

import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.inference.entities.Plaintext;
import com.ciphertool.zenith.inference.evaluator.KnownPlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.probability.BoundaryProbability;
import com.ciphertool.zenith.inference.probability.LetterProbability;
import com.ciphertool.zenith.inference.probability.SolutionProbability;
import com.ciphertool.zenith.math.MathCache;
import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.model.markov.MarkovModel;
import com.ciphertool.zenith.model.markov.NGramIndexNode;

public class BayesianDecipherManager {
	private Logger							log							= LoggerFactory.getLogger(getClass());

	private static final List<Character>	LOWERCASE_LETTERS			= Arrays.asList(new Character[] { 'a', 'b', 'c',
			'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
			'y', 'z' });

	private String							cipherName;
	private PlaintextEvaluator				plaintextEvaluator;
	private CipherDao						cipherDao;
	private Cipher							cipher;
	private MarkovModel						letterMarkovModel;
	private int								samplerIterations;
	private double							sourceModelPrior;
	private BigDecimal						alphaHyperparameter;
	private double							channelModelPrior;
	private BigDecimal						betaHyperparameter;
	private int								annealingTemperatureMax;
	private int								annealingTemperatureMin;
	private int								cipherKeySize;
	private static List<LetterProbability>	letterUnigramProbabilities	= new ArrayList<>();
	private BigDecimal						ciphertextProbability;
	private KnownPlaintextEvaluator			knownPlaintextEvaluator;
	private TaskExecutor					taskExecutor;
	private MathCache						bigDecimalFunctions;
	private Double							percentToReallocate;

	@PostConstruct
	public void setUp() {
		alphaHyperparameter = BigDecimal.valueOf(sourceModelPrior);
		betaHyperparameter = BigDecimal.valueOf(channelModelPrior);

		this.cipher = cipherDao.findByCipherName(cipherName);
		int totalCharacters = this.cipher.getCiphertextCharacters().size();
		int lastRowBegin = (this.cipher.getColumns() * (this.cipher.getRows() - 1));

		// Remove the last row altogether
		for (int i = lastRowBegin; i < totalCharacters; i++) {
			this.cipher.removeCiphertextCharacter(this.cipher.getCiphertextCharacters().get(lastRowBegin));
		}

		cipherKeySize = (int) cipher.getCiphertextCharacters().stream().map(c -> c.getValue()).distinct().count();
		// Use a uniform distribution for ciphertext probabilities
		ciphertextProbability = BigDecimal.ONE.divide(BigDecimal.valueOf(cipherKeySize), MathConstants.PREC_10_HALF_UP);

		for (Map.Entry<Character, NGramIndexNode> entry : letterMarkovModel.getRootNode().getTransitions().entrySet()) {
			letterUnigramProbabilities.add(new LetterProbability(entry.getKey(),
					entry.getValue().getTerminalInfo().getConditionalProbability()));
		}
	}

	public void run() {
		// Initialize the solution key
		CipherSolution initialSolution = new CipherSolution(cipher, cipherKeySize);

		RouletteSampler<LetterProbability> rouletteSampler = new RouletteSampler<>();
		Collections.sort(letterUnigramProbabilities);
		BigDecimal totalProbability = rouletteSampler.reIndex(letterUnigramProbabilities);
		Double wordBoundaryProbability = (double) 1.0 / (double) LanguageConstants.AVERAGE_WORD_SIZE;

		cipher.getCiphertextCharacters().stream().map(ciphertext -> ciphertext.getValue()).distinct().forEach(ciphertext -> {
			// Pick a plaintext at random according to the language model
			String nextPlaintext = letterUnigramProbabilities.get(rouletteSampler.getNextIndex(letterUnigramProbabilities, totalProbability)).getValue().toString();

			initialSolution.putMapping(ciphertext, new Plaintext(nextPlaintext));
		});

		// for (int i = 0; i < cipher.getCiphertextCharacters().size() - 1; i++) {
		// if (ThreadLocalRandom.current().nextDouble() < wordBoundaryProbability) {
		// initialSolution.addWordBoundary(i);
		// }
		// }

		PartialDerivation initialDerivation = computePartialDerivationProbability(null, 0, cipher.getCiphertextCharacters().size(), initialSolution);
		EvaluationResults initialPlaintextResults = plaintextEvaluator.evaluate(initialSolution);
		initialSolution.setGenerativeModelProbability(initialDerivation.getProductOfProbabilities());
		initialSolution.setGenerativeModelLogProbability(initialDerivation.getSumOfProbabilities());
		initialSolution.setLanguageModelProbability(initialPlaintextResults.getProbability());
		initialSolution.setLanguageModelLogProbability(initialPlaintextResults.getLogProbability());
		initialSolution.setProbability(initialDerivation.getProductOfProbabilities().multiply(initialSolution.getLanguageModelProbability(), MathConstants.PREC_10_HALF_UP));
		initialSolution.setLogProbability(initialDerivation.getSumOfProbabilities().add(initialSolution.getLanguageModelLogProbability(), MathConstants.PREC_10_HALF_UP));
		log.info(initialSolution.toString());

		BigDecimal maxTemp = BigDecimal.valueOf(annealingTemperatureMax);
		BigDecimal minTemp = BigDecimal.valueOf(annealingTemperatureMin);
		BigDecimal iterations = BigDecimal.valueOf(samplerIterations);
		BigDecimal temperature;

		CipherSolution next = initialSolution;
		CipherSolution maxBayes = null;
		int maxBayesIteration = 0;
		CipherSolution maxKnown = null;
		int maxKnownIteration = 0;

		log.info("Running Gibbs sampler for " + samplerIterations + " iterations.");
		long start = System.currentTimeMillis();
		long startLetterSampling;
		long letterSamplingElapsed;
		long startWordSampling;
		long wordSamplingElapsed;

		Double knownProximity = null;
		int i;
		for (i = 0; i < samplerIterations; i++) {
			long iterationStart = System.currentTimeMillis();

			/*
			 * Set temperature as a ratio of the max temperature to the number of iterations left, offset by the min
			 * temperature so as not to go below it
			 */
			temperature = maxTemp.subtract(minTemp, MathConstants.PREC_10_HALF_UP).multiply(iterations.subtract(BigDecimal.valueOf(i), MathConstants.PREC_10_HALF_UP).divide(iterations, MathConstants.PREC_10_HALF_UP), MathConstants.PREC_10_HALF_UP).add(minTemp, MathConstants.PREC_10_HALF_UP);

			startLetterSampling = System.currentTimeMillis();
			next = runGibbsLetterSampler(temperature, next);
			letterSamplingElapsed = (System.currentTimeMillis() - startLetterSampling);

			startWordSampling = System.currentTimeMillis();
			// next = runGibbsWordBoundarySampler(temperature, next);
			wordSamplingElapsed = (System.currentTimeMillis() - startWordSampling);

			if (knownPlaintextEvaluator != null) {
				knownProximity = knownPlaintextEvaluator.evaluate(next);
				next.setKnownSolutionProximity(BigDecimal.valueOf(knownProximity));

				if (maxKnown == null
						|| maxKnown.getKnownSolutionProximity().compareTo(BigDecimal.valueOf(knownProximity)) < 0) {
					maxKnown = next;
					maxKnownIteration = i + 1;
				}
			}

			if (maxBayes == null || maxBayes.getProbability().compareTo(next.getProbability()) < 0) {
				maxBayes = next;
				maxBayesIteration = i + 1;
			}

			log.info("Iteration " + (i + 1) + " complete.  [elapsed=" + (System.currentTimeMillis() - iterationStart)
					+ "ms, letterSampling=" + letterSamplingElapsed + "ms, wordSampling=" + wordSamplingElapsed
					+ "ms, temp=" + String.format("%1$,.2f", temperature) + "]");
			log.info(next.toString());
		}

		log.info("Gibbs sampling completed in " + (System.currentTimeMillis() - start) + "ms.  Average="
				+ ((double) (System.currentTimeMillis() - start) / (double) i) + "ms.");
		log.info("Best known found at iteration " + maxKnownIteration + ": " + maxKnown);
		log.info("Best probability found at iteration " + maxBayesIteration + ": " + maxBayes);
	}

	protected CipherSolution runGibbsLetterSampler(BigDecimal temperature, CipherSolution solution) {
		RouletteSampler<SolutionProbability> rouletteSampler = new RouletteSampler<>();
		CipherSolution proposal = null;
		BigDecimal totalProbability;

		// For each cipher symbol type, run the gibbs sampling
		for (Map.Entry<String, Plaintext> entry : solution.getMappings().entrySet()) {
			List<SolutionProbability> plaintextDistribution = computeDistribution(entry.getKey(), solution);

			BigDecimal massToReallocate = BigDecimal.valueOf(percentToReallocate);

			BigDecimal sumOfProbabilities = plaintextDistribution.stream().map(SolutionProbability::getProbability).reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

			// Reallocate some of the total probability mass to smooth out the distribution
			BigDecimal probabilityMassToReallocate = sumOfProbabilities.multiply(massToReallocate, MathConstants.PREC_10_HALF_UP).divide(BigDecimal.valueOf(plaintextDistribution.size()), MathConstants.PREC_10_HALF_UP);

			for (SolutionProbability solutionProbability : plaintextDistribution) {
				solutionProbability.setProbability(solutionProbability.getProbability().multiply(BigDecimal.ONE.subtract(massToReallocate), MathConstants.PREC_10_HALF_UP).add(probabilityMassToReallocate));
			}

			Collections.sort(plaintextDistribution);
			totalProbability = rouletteSampler.reIndex(plaintextDistribution);

			proposal = plaintextDistribution.get(rouletteSampler.getNextIndex(plaintextDistribution, totalProbability)).getValue();

			solution = selectNext(temperature, solution, proposal);
		}

		return solution;
	}

	/**
	 * A concurrent task for computing the letter probability during Gibbs sampling.
	 */
	protected class LetterProbabilityTask implements Callable<CipherSolution> {
		private CipherSolution		originalSolution;
		private Character			letter;
		private PartialDerivation	partialDerivation;
		private EvaluationResults	partialPlaintextResults;
		private int					affectedCount;
		private String				ciphertextKey;
		private CipherSolution		conditionalSolution;

		/**
		 * @param originalSolution
		 *            the original unmodified solution
		 * @param letter
		 *            the letter to sample for
		 * @param partialDerivation
		 *            the partial derivation
		 * @param partialPlaintextResults
		 *            the partial plaintext results
		 * @param affectedCount
		 *            the affected count
		 * @param ciphertextKey
		 *            the ciphertext key
		 * @param modified
		 *            the modified solution
		 */
		public LetterProbabilityTask(CipherSolution originalSolution, Character letter,
				PartialDerivation partialDerivation, EvaluationResults partialPlaintextResults, int affectedCount,
				String ciphertextKey, CipherSolution modified) {
			this.originalSolution = originalSolution;
			this.letter = letter;
			this.partialDerivation = partialDerivation;
			this.partialPlaintextResults = partialPlaintextResults;
			this.affectedCount = affectedCount;
			this.ciphertextKey = ciphertextKey;
			this.conditionalSolution = modified;
		}

		@Override
		public CipherSolution call() throws Exception {
			if (conditionalSolution.getMappings().get(ciphertextKey).equals(new Plaintext(letter.toString()))) {
				// No need to re-score the solution in this case
				return conditionalSolution;
			}

			conditionalSolution.replaceMapping(ciphertextKey, new Plaintext(letter.toString()));

			int start = conditionalSolution.getCipher().getCiphertextCharacters().size() - affectedCount;
			long startDerivation = System.currentTimeMillis();
			// PartialDerivation derivationProbability = computePartialDerivationProbability(partialDerivation, start,
			// conditionalSolution.getCipher().getCiphertextCharacters().size(), conditionalSolution);
			PartialDerivation derivationProbability = new PartialDerivation(BigDecimal.ONE, BigDecimal.ZERO);
			log.debug("Partial derivation took {}ms.", (System.currentTimeMillis() - startDerivation));

			/*
			 * We can't use the modified clone since its ciphertext was moved around, and we need to preserve word
			 * boundaries
			 */
			originalSolution.replaceMapping(ciphertextKey, new Plaintext(letter.toString()));

			long startPlaintext = System.currentTimeMillis();
			EvaluationResults remainingPlaintextResults = plaintextEvaluator.evaluate(ciphertextKey, true, originalSolution);
			log.debug("Partial plaintext took {}ms.", (System.currentTimeMillis() - startPlaintext));

			conditionalSolution.setGenerativeModelProbability(derivationProbability.getProductOfProbabilities());
			conditionalSolution.setGenerativeModelLogProbability(derivationProbability.getSumOfProbabilities());
			conditionalSolution.setLanguageModelProbability(partialPlaintextResults.getProbability().multiply(remainingPlaintextResults.getProbability(), MathConstants.PREC_10_HALF_UP));
			conditionalSolution.setLanguageModelLogProbability(partialPlaintextResults.getLogProbability().add(remainingPlaintextResults.getLogProbability(), MathConstants.PREC_10_HALF_UP));
			conditionalSolution.setProbability(derivationProbability.getProductOfProbabilities().multiply(conditionalSolution.getLanguageModelProbability(), MathConstants.PREC_10_HALF_UP));
			conditionalSolution.setLogProbability(derivationProbability.getSumOfProbabilities().add(conditionalSolution.getLanguageModelLogProbability(), MathConstants.PREC_10_HALF_UP));

			return conditionalSolution;
		}
	}

	protected List<SolutionProbability> computeDistribution(String ciphertextKey, CipherSolution solution) {
		List<SolutionProbability> plaintextDistribution = new ArrayList<>();
		BigDecimal sumOfProbabilities = BigDecimal.ZERO;

		EvaluationResults partialPlaintextResults = plaintextEvaluator.evaluate(ciphertextKey, false, solution);

		CipherSolution modified = solution.clone();
		int affectedCount = moveAffectedWindowsToEnd(ciphertextKey, modified);

		int end = solution.getCipher().getCiphertextCharacters().size() - affectedCount;
		// PartialDerivation partialDerivation = computePartialDerivationProbability(null, 0, end, modified);
		PartialDerivation partialDerivation = new PartialDerivation(BigDecimal.ONE, BigDecimal.ZERO);

		List<FutureTask<CipherSolution>> futures = new ArrayList<FutureTask<CipherSolution>>(26);
		FutureTask<CipherSolution> task;

		// Calculate the full conditional probability for each possible plaintext substitution
		for (Character letter : LOWERCASE_LETTERS) {
			task = new FutureTask<CipherSolution>(new LetterProbabilityTask(solution.clone(), letter, partialDerivation,
					partialPlaintextResults, affectedCount, ciphertextKey, modified.clone()));
			futures.add(task);
			this.taskExecutor.execute(task);
		}

		CipherSolution next;

		for (FutureTask<CipherSolution> future : futures) {
			try {
				next = future.get();

				// Reset to the original cipher, since it was modified by moveAffectedWindowsToEnd()
				next.setCipher(cipher);

				plaintextDistribution.add(new SolutionProbability(next, next.getProbability()));
				sumOfProbabilities = sumOfProbabilities.add(next.getProbability(), MathConstants.PREC_10_HALF_UP);
			} catch (InterruptedException ie) {
				log.error("Caught InterruptedException while waiting for LetterProbabilityTask ", ie);
			} catch (ExecutionException ee) {
				log.error("Caught ExecutionException while waiting for LetterProbabilityTask ", ee);
			}
		}

		// Normalize the probabilities
		// TODO: should we also somehow normalize the log probabilities?
		for (SolutionProbability solutionProbability : plaintextDistribution) {
			solutionProbability.setProbability(solutionProbability.getProbability().divide(sumOfProbabilities, MathConstants.PREC_10_HALF_UP));
		}

		return plaintextDistribution;
	}

	protected PartialDerivation computePartialDerivationProbability(PartialDerivation partialDerivation, int start, int end, CipherSolution derivation) {
		BigDecimal productOfProbabilities = (partialDerivation == null ? BigDecimal.ONE : partialDerivation.getProductOfProbabilities());
		BigDecimal sumOfProbabilities = (partialDerivation == null ? BigDecimal.ZERO : partialDerivation.getSumOfProbabilities());
		Map<String, Integer> unigramCounts = (partialDerivation == null ? new HashMap<>() : new HashMap<>(
				partialDerivation.getUnigramCounts()));
		Map<String, Integer> bigramCounts = (partialDerivation == null ? new HashMap<>() : new HashMap<>(
				partialDerivation.getBigramCounts()));
		Map<CiphertextMapping, Integer> ciphertextMappingCounts = (partialDerivation == null ? new HashMap<>() : new HashMap<>(
				partialDerivation.getCiphertextMappingCounts()));
		String lastCharacter = (partialDerivation == null ? null : partialDerivation.getLastCharacter());
		String ciphertext = null;
		EvaluationResults partialProbabilities;

		for (int i = start; i < end; i++) {
			ciphertext = derivation.getCipher().getCiphertextCharacters().get(i).getValue();

			partialProbabilities = computePosition(productOfProbabilities, sumOfProbabilities, unigramCounts, bigramCounts, ciphertextMappingCounts, lastCharacter, ciphertext, derivation);

			productOfProbabilities = partialProbabilities.getProbability();
			sumOfProbabilities = partialProbabilities.getLogProbability();

			lastCharacter = derivation.getMappings().get(ciphertext).getValue();
		}

		return new PartialDerivation(productOfProbabilities, sumOfProbabilities, unigramCounts, bigramCounts,
				ciphertextMappingCounts, lastCharacter);
	}

	protected EvaluationResults computePosition(BigDecimal productOfProbabilities, BigDecimal sumOfProbabilities, Map<String, Integer> unigramCounts, Map<String, Integer> bigramCounts, Map<CiphertextMapping, Integer> ciphertextMappingCounts, String lastCharacter, String ciphertext, CipherSolution derivation) {
		String currentCharacter = derivation.getMappings().get(ciphertext).getValue();

		CiphertextMapping ciphertextMapping = new CiphertextMapping(ciphertext, new Plaintext(currentCharacter));

		if (lastCharacter == null) {
			productOfProbabilities = productOfProbabilities.multiply(letterUnigramProbabilities.get(letterUnigramProbabilities.indexOf(new LetterProbability(
					currentCharacter.charAt(0),
					BigDecimal.ZERO))).getProbability(), MathConstants.PREC_10_HALF_UP).multiply(ciphertextProbability, MathConstants.PREC_10_HALF_UP);

			sumOfProbabilities = sumOfProbabilities.add(bigDecimalFunctions.log(letterUnigramProbabilities.get(letterUnigramProbabilities.indexOf(new LetterProbability(
					currentCharacter.charAt(0),
					BigDecimal.ZERO))).getProbability()), MathConstants.PREC_10_HALF_UP).add(ciphertextProbability, MathConstants.PREC_10_HALF_UP);
		} else {
			BigDecimal nGramPriorProbability = letterMarkovModel.findLongest(lastCharacter
					+ currentCharacter).getTerminalInfo().getConditionalProbability();
			// Any sufficient corpus should contain every possible bigram, so no need to check for unknowns
			BigDecimal unigramCount = unigramCounts.get(lastCharacter) == null ? BigDecimal.ZERO : BigDecimal.valueOf(unigramCounts.get(lastCharacter));
			BigDecimal nGramCount = bigramCounts.get(lastCharacter
					+ currentCharacter) == null ? BigDecimal.ZERO : BigDecimal.valueOf(bigramCounts.get(lastCharacter
							+ currentCharacter));

			BigDecimal numerator = alphaHyperparameter.multiply(nGramPriorProbability, MathConstants.PREC_10_HALF_UP).add(nGramCount, MathConstants.PREC_10_HALF_UP);
			BigDecimal denominator = alphaHyperparameter.add(unigramCount, MathConstants.PREC_10_HALF_UP);
			BigDecimal sourcePart = numerator.divide(denominator, MathConstants.PREC_10_HALF_UP);

			// Multiply by the source model probability
			productOfProbabilities = productOfProbabilities.multiply(sourcePart, MathConstants.PREC_10_HALF_UP);
			sumOfProbabilities = sumOfProbabilities.add(bigDecimalFunctions.log(sourcePart), MathConstants.PREC_10_HALF_UP);

			BigDecimal ciphertextMappingCount = ciphertextMappingCounts.get(ciphertextMapping) == null ? BigDecimal.ZERO : BigDecimal.valueOf(ciphertextMappingCounts.get(ciphertextMapping));
			unigramCount = unigramCounts.get(currentCharacter) == null ? BigDecimal.ZERO : BigDecimal.valueOf(unigramCounts.get(currentCharacter));

			numerator = betaHyperparameter.multiply(ciphertextProbability, MathConstants.PREC_10_HALF_UP).add(ciphertextMappingCount, MathConstants.PREC_10_HALF_UP);
			denominator = betaHyperparameter.add(unigramCount, MathConstants.PREC_10_HALF_UP);
			BigDecimal channelPart = numerator.divide(denominator, MathConstants.PREC_10_HALF_UP);

			// Multiply by the channel model probability
			productOfProbabilities = productOfProbabilities.multiply(channelPart, MathConstants.PREC_10_HALF_UP);
			sumOfProbabilities = sumOfProbabilities.add(bigDecimalFunctions.log(channelPart), MathConstants.PREC_10_HALF_UP);

			if (bigramCounts.get(lastCharacter + currentCharacter) == null) {
				bigramCounts.put(lastCharacter + currentCharacter, 0);
			}

			bigramCounts.put(lastCharacter + currentCharacter, bigramCounts.get(lastCharacter + currentCharacter) + 1);
		}

		if (ciphertextMappingCounts.get(ciphertextMapping) == null) {
			ciphertextMappingCounts.put(ciphertextMapping, 0);
		}

		ciphertextMappingCounts.put(ciphertextMapping, ciphertextMappingCounts.get(ciphertextMapping) + 1);

		if (unigramCounts.get(currentCharacter) == null) {
			unigramCounts.put(currentCharacter, 0);
		}

		unigramCounts.put(currentCharacter, unigramCounts.get(currentCharacter) + 1);

		return new EvaluationResults(productOfProbabilities, sumOfProbabilities);
	}

	protected CipherSolution runGibbsWordBoundarySampler(BigDecimal temperature, CipherSolution solution) {
		int nextBoundary;
		BigDecimal sumOfProbabilities = null;
		List<BoundaryProbability> boundaryProbabilities = null;
		boolean isAddBoundary = false;
		CipherSolution addProposal = null;
		CipherSolution removeProposal = null;
		CipherSolution proposal = null;
		EvaluationResults addPlaintextResults = null;
		EvaluationResults removePlaintextResults = null;
		BigDecimal totalProbability;
		RouletteSampler<BoundaryProbability> rouletteSampler = new RouletteSampler<>();

		for (int i = 0; i < cipher.getCiphertextCharacters().size() - 1; i++) {
			boundaryProbabilities = new ArrayList<>();
			nextBoundary = i;

			addProposal = solution.clone();
			if (!addProposal.getWordBoundaries().contains(nextBoundary)) {
				addProposal.addWordBoundary(nextBoundary);
				addPlaintextResults = plaintextEvaluator.evaluate(addProposal);
				addProposal.setLanguageModelProbability(addPlaintextResults.getProbability());
				addProposal.setLanguageModelLogProbability(addPlaintextResults.getLogProbability());
				addProposal.setProbability(addProposal.getGenerativeModelProbability().multiply(addProposal.getLanguageModelProbability(), MathConstants.PREC_10_HALF_UP));
				addProposal.setLogProbability(addProposal.getGenerativeModelLogProbability().add(addProposal.getLanguageModelLogProbability(), MathConstants.PREC_10_HALF_UP));
			}

			removeProposal = solution.clone();
			if (removeProposal.getWordBoundaries().contains(nextBoundary)) {
				removeProposal.removeWordBoundary(nextBoundary);
				removePlaintextResults = plaintextEvaluator.evaluate(removeProposal);
				removeProposal.setLanguageModelProbability(removePlaintextResults.getProbability());
				removeProposal.setLanguageModelLogProbability(removePlaintextResults.getLogProbability());
				removeProposal.setProbability(removeProposal.getGenerativeModelProbability().multiply(removeProposal.getLanguageModelProbability(), MathConstants.PREC_10_HALF_UP));
				removeProposal.setLogProbability(removeProposal.getGenerativeModelLogProbability().add(removeProposal.getLanguageModelLogProbability(), MathConstants.PREC_10_HALF_UP));
			}

			sumOfProbabilities = addProposal.getProbability().add(removeProposal.getProbability());

			boundaryProbabilities.add(new BoundaryProbability(true,
					addProposal.getProbability().divide(sumOfProbabilities, MathConstants.PREC_10_HALF_UP)));
			boundaryProbabilities.add(new BoundaryProbability(false,
					removeProposal.getProbability().divide(sumOfProbabilities, MathConstants.PREC_10_HALF_UP)));

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

	protected CipherSolution selectNext(BigDecimal temperature, CipherSolution solution, CipherSolution proposal) {
		BigDecimal acceptanceProbability = null;

		if (proposal.getLogProbability().compareTo(solution.getLogProbability()) > 0) {
			log.debug("Better solution found");
			return proposal;
		} else {
			// Need to convert to log probabilities in order for the acceptance probability calculation to be useful
			acceptanceProbability = BigDecimalMath.exp(solution.getLogProbability().subtract(proposal.getLogProbability(), MathConstants.PREC_10_HALF_UP).divide(temperature, MathConstants.PREC_10_HALF_UP).negate());

			log.debug("Acceptance probability: {}", acceptanceProbability);

			if (acceptanceProbability.compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalStateException(
						"Acceptance probability was calculated to be less than zero.  Please review the math as this should not happen.");
			}

			if (acceptanceProbability.compareTo(BigDecimal.ONE) > 0
					|| ThreadLocalRandom.current().nextDouble() < acceptanceProbability.doubleValue()) {
				return proposal;
			}
		}

		return solution;
	}

	protected int moveAffectedWindowsToEnd(String ciphertextKey, CipherSolution solution) {
		Cipher cloned = cipher.clone();
		solution.setCipher(cloned);

		if (ciphertextKey == null) {
			// Nothing to do
			return 0;
		}

		List<Ciphertext> allWindows = new ArrayList<>();

		Integer begin = 0;
		boolean affected = false;

		for (int i = 0; i < cloned.getCiphertextCharacters().size(); i++) {
			if (ciphertextKey.equals(cloned.getCiphertextCharacters().get(i).getValue())) {
				affected = true;
			}

			if (solution.getWordBoundaries().contains(i)) {
				if (affected) {
					for (int j = begin; j <= i; j++) {
						allWindows.add(cloned.getCiphertextCharacters().get(j));
					}
				}

				begin = i + 1;
				affected = false;
			}
		}

		if (affected) {
			for (int j = begin; j < cloned.getCiphertextCharacters().size(); j++) {
				allWindows.add(cloned.getCiphertextCharacters().get(j));
			}
		}

		for (int i = 0; i < allWindows.size(); i++) {
			// Remove it
			cloned.removeCiphertextCharacter(allWindows.get(i));

			// And add it to the end of the List
			cloned.addCiphertextCharacter(allWindows.get(i));
		}

		return allWindows.size();
	}

	/**
	 * @param plaintextEvaluator
	 *            the plaintextEvaluator to set
	 */
	@Required
	public void setPlaintextEvaluator(PlaintextEvaluator plaintextEvaluator) {
		this.plaintextEvaluator = plaintextEvaluator;
	}

	/**
	 * @param cipherName
	 *            the cipherName to set
	 */
	@Required
	public void setCipherName(String cipherName) {
		this.cipherName = cipherName;
	}

	/**
	 * @param cipherDao
	 *            the cipherDao to set
	 */
	@Required
	public void setCipherDao(CipherDao cipherDao) {
		this.cipherDao = cipherDao;
	}

	/**
	 * @param letterMarkovModel
	 *            the letterMarkovModel to set
	 */
	@Required
	public void setLetterMarkovModel(MarkovModel letterMarkovModel) {
		this.letterMarkovModel = letterMarkovModel;
	}

	/**
	 * @param samplerIterations
	 *            the samplerIterations to set
	 */
	@Required
	public void setSamplerIterations(int samplerIterations) {
		this.samplerIterations = samplerIterations;
	}

	/**
	 * @param sourceModelPrior
	 *            the sourceModelPrior to set
	 */
	@Required
	public void setSourceModelPrior(double sourceModelPrior) {
		this.sourceModelPrior = sourceModelPrior;
	}

	/**
	 * @param channelModelPrior
	 *            the channelModelPrior to set
	 */
	@Required
	public void setChannelModelPrior(double channelModelPrior) {
		this.channelModelPrior = channelModelPrior;
	}

	/**
	 * @param annealingTemperatureMax
	 *            the annealingTemperatureMax to set
	 */
	@Required
	public void setAnnealingTemperatureMax(int annealingTemperatureMax) {
		this.annealingTemperatureMax = annealingTemperatureMax;
	}

	/**
	 * @param annealingTemperatureMin
	 *            the annealingTemperatureMin to set
	 */
	@Required
	public void setAnnealingTemperatureMin(int annealingTemperatureMin) {
		this.annealingTemperatureMin = annealingTemperatureMin;
	}

	/**
	 * @param taskExecutor
	 *            the taskExecutor to set
	 */
	@Required
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * @param bigDecimalFunctions
	 *            the bigDecimalFunctions to set
	 */
	@Required
	public void setBigDecimalFunctions(MathCache bigDecimalFunctions) {
		this.bigDecimalFunctions = bigDecimalFunctions;
	}

	/**
	 * This is NOT required. We will not always know the solution. In fact, that should be the rare case.
	 * 
	 * @param knownPlaintextEvaluator
	 *            the knownPlaintextEvaluator to set
	 */
	public void setKnownPlaintextEvaluator(KnownPlaintextEvaluator knownPlaintextEvaluator) {
		this.knownPlaintextEvaluator = knownPlaintextEvaluator;
	}

	/**
	 * @param percentToReallocate
	 *            the percentToReallocate to set
	 */
	@Required
	public void setPercentToReallocate(Double percentToReallocate) {
		this.percentToReallocate = percentToReallocate;
	}
}
