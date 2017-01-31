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
import com.ciphertool.zenith.inference.dto.EvaluationResults;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Plaintext;
import com.ciphertool.zenith.inference.evaluator.KnownPlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.probability.BoundaryProbability;
import com.ciphertool.zenith.inference.probability.LetterProbability;
import com.ciphertool.zenith.inference.probability.SolutionProbability;
import com.ciphertool.zenith.inference.selection.RouletteSampler;
import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
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
	private LetterNGramDao					letterNGramDao;
	private Cipher							cipher;
	private int								samplerIterations;
	private int								annealingTemperatureMax;
	private int								annealingTemperatureMin;
	private int								cipherKeySize;
	private static List<LetterProbability>	letterUnigramProbabilities	= new ArrayList<>();
	private KnownPlaintextEvaluator			knownPlaintextEvaluator;
	private TaskExecutor					taskExecutor;
	private Double							percentToReallocate;
	private Boolean							includeWordBoundaries;
	private int								markovOrder;
	private MarkovModel						letterMarkovModel;
	private int								minimumCount;

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

		long startFindAll = System.currentTimeMillis();
		log.info("Beginning retrieval of all n-grams with{} spaces.", (includeWordBoundaries ? "" : "out"));

		List<NGramIndexNode> nGramNodes = letterNGramDao.findAll(minimumCount, includeWordBoundaries);

		log.info("Finished retrieving {} n-grams with{} spaces in {}ms.", nGramNodes.size(), (includeWordBoundaries ? "" : "out"), (System.currentTimeMillis()
				- startFindAll));

		this.letterMarkovModel = new MarkovModel(this.markovOrder);

		long startCount = System.currentTimeMillis();
		log.info("Counting nodes with counts below the minimum of {}.", minimumCount);

		letterMarkovModel.setNumWithInsufficientCounts(letterNGramDao.countLessThan(minimumCount, includeWordBoundaries));

		log.info("Finished counting nodes below the minimum of {} in {}ms.", minimumCount, (System.currentTimeMillis()
				- startCount));

		long startAdding = System.currentTimeMillis();
		log.info("Adding nodes to the model.", minimumCount);

		for (NGramIndexNode nGramNode : nGramNodes) {
			this.letterMarkovModel.addNode(nGramNode);
		}

		log.info("Finished adding nodes to the model in {}ms.", (System.currentTimeMillis() - startAdding));

		long total = 0;
		for (Map.Entry<Character, NGramIndexNode> entry : letterMarkovModel.getRootNode().getTransitions().entrySet()) {
			if (!entry.getKey().equals(' ')) {
				total += entry.getValue().getCount();
			}
		}

		BigDecimal probability;
		for (Map.Entry<Character, NGramIndexNode> entry : letterMarkovModel.getRootNode().getTransitions().entrySet()) {
			if (!entry.getKey().equals(' ')) {
				probability = BigDecimal.valueOf(entry.getValue().getCount()).divide(BigDecimal.valueOf(total), MathConstants.PREC_10_HALF_UP);

				letterUnigramProbabilities.add(new LetterProbability(entry.getKey(), probability));
			}
		}

		log.info("unknownLetterNGramProbability: {}", this.letterMarkovModel.getUnknownLetterNGramProbability());
		log.info("Index of coincidence for English: {}", this.letterMarkovModel.getIndexOfCoincidence());
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

		if (includeWordBoundaries) {
			for (int i = 0; i < cipher.getCiphertextCharacters().size() - 1; i++) {
				if (ThreadLocalRandom.current().nextDouble() < wordBoundaryProbability) {
					initialSolution.addWordBoundary(i);
				}
			}
		}

		EvaluationResults initialPlaintextResults = plaintextEvaluator.evaluate(letterMarkovModel, initialSolution, null);
		initialSolution.setLanguageModelProbability(initialPlaintextResults.getProbability());
		initialSolution.setLanguageModelLogProbability(initialPlaintextResults.getLogProbability());
		initialSolution.setProbability(initialSolution.getLanguageModelProbability());
		initialSolution.setLogProbability(initialSolution.getLanguageModelLogProbability());

		if (knownPlaintextEvaluator != null) {
			initialSolution.setKnownSolutionProximity(BigDecimal.valueOf(knownPlaintextEvaluator.evaluate(initialSolution)));
		}

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
			if (includeWordBoundaries) {
				next = runGibbsWordBoundarySampler(temperature, next);
			}
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

			EvaluationResults fullPlaintextResults = plaintextEvaluator.evaluate(letterMarkovModel, proposal, null);
			proposal.setLanguageModelProbability(fullPlaintextResults.getProbability());
			proposal.setLanguageModelLogProbability(fullPlaintextResults.getLogProbability());
			proposal.setProbability(proposal.getLanguageModelProbability());
			proposal.setLogProbability(proposal.getLanguageModelLogProbability());

			solution = selectNext(temperature, solution, proposal);
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
		 * @param originalSolution
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
		public CipherSolution call() throws Exception {
			conditionalSolution.replaceMapping(ciphertextKey, new Plaintext(letter.toString()));

			long startPlaintext = System.currentTimeMillis();
			EvaluationResults remainingPlaintextResults = plaintextEvaluator.evaluate(letterMarkovModel, conditionalSolution, ciphertextKey);
			log.debug("Partial plaintext took {}ms.", (System.currentTimeMillis() - startPlaintext));

			conditionalSolution.setLanguageModelProbability(remainingPlaintextResults.getProbability());
			conditionalSolution.setLanguageModelLogProbability(remainingPlaintextResults.getLogProbability());
			conditionalSolution.setProbability(conditionalSolution.getLanguageModelProbability());
			conditionalSolution.setLogProbability(conditionalSolution.getLanguageModelLogProbability());

			return conditionalSolution;
		}
	}

	protected List<SolutionProbability> computeDistribution(String ciphertextKey, CipherSolution solution) {
		List<SolutionProbability> plaintextDistribution = new ArrayList<>();
		BigDecimal sumOfProbabilities = BigDecimal.ZERO;

		List<FutureTask<CipherSolution>> futures = new ArrayList<FutureTask<CipherSolution>>(26);
		FutureTask<CipherSolution> task;

		// Calculate the full conditional probability for each possible plaintext substitution
		for (Character letter : LOWERCASE_LETTERS) {
			task = new FutureTask<CipherSolution>(new LetterProbabilityTask(solution.clone(), letter, ciphertextKey));
			futures.add(task);
			this.taskExecutor.execute(task);
		}

		CipherSolution next;

		for (FutureTask<CipherSolution> future : futures) {
			try {
				next = future.get();

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
				addPlaintextResults = plaintextEvaluator.evaluate(letterMarkovModel, addProposal, null);
				addProposal.setLanguageModelProbability(addPlaintextResults.getProbability());
				addProposal.setLanguageModelLogProbability(addPlaintextResults.getLogProbability());
				addProposal.setProbability(addProposal.getLanguageModelProbability());
				addProposal.setLogProbability(addProposal.getLanguageModelLogProbability());
			}

			removeProposal = solution.clone();
			if (removeProposal.getWordBoundaries().contains(nextBoundary)) {
				removeProposal.removeWordBoundary(nextBoundary);
				removePlaintextResults = plaintextEvaluator.evaluate(letterMarkovModel, removeProposal, null);
				removeProposal.setLanguageModelProbability(removePlaintextResults.getProbability());
				removeProposal.setLanguageModelLogProbability(removePlaintextResults.getLogProbability());
				removeProposal.setProbability(removeProposal.getLanguageModelProbability());
				removeProposal.setLogProbability(removeProposal.getLanguageModelLogProbability());
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
	 * @param letterNGramDao
	 *            the letterNGramDao to set
	 */
	@Required
	public void setLetterNGramDao(LetterNGramDao letterNGramDao) {
		this.letterNGramDao = letterNGramDao;
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

	/**
	 * @param includeWordBoundaries
	 *            the includeWordBoundaries to set
	 */
	@Required
	public void setIncludeWordBoundaries(Boolean includeWordBoundaries) {
		this.includeWordBoundaries = includeWordBoundaries;
	}

	/**
	 * @param markovOrder
	 *            the markovOrder to set
	 */
	@Required
	public void setMarkovOrder(int markovOrder) {
		this.markovOrder = markovOrder;
	}

	/**
	 * @param minimumCount
	 *            the minimumCount to set
	 */
	@Required
	public void setMinimumCount(int minimumCount) {
		this.minimumCount = minimumCount;
	}
}
