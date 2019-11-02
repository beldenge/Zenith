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

package com.ciphertool.zenith.search;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.transformer.ciphertext.UnwrapTranspositionCipherTransformer;
import com.ciphertool.zenith.search.evaluator.CiphertextCycleCountEvaluator;
import com.ciphertool.zenith.search.evaluator.CiphertextLanguageModelEvaluator;
import com.ciphertool.zenith.search.evaluator.CiphertextRepeatingBigramEvaluator;
import com.ciphertool.zenith.search.evaluator.CiphertextRowLevelEntropyEvaluator;
import com.ciphertool.zenith.search.model.EpochResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class TranspositionSearcher {
    private Logger log = LoggerFactory.getLogger(getClass());

    private static final int ARBITRARY_INITIAL_LIST_SIZE = 20;

    @Value("${mutation-search.simulated-annealing.sampler.iterations}")
    private int samplerIterations;

    @Value("${mutation-search.simulated-annealing.temperature.max}")
    private double annealingTemperatureMax;

    @Value("${mutation-search.simulated-annealing.temperature.min}")
    private double annealingTemperatureMin;

    @Value("${mutation-search.decipherment.epochs:1}")
    private int epochs;

    @Value("${decipherment.transposition.key-length.min:2}")
    private int keyLengthMin;

    @Value("${decipherment.transposition.key-length.max}")
    private int keyLengthMax;

    @Autowired
    private CiphertextRepeatingBigramEvaluator repeatingBigramEvaluator;

    @Autowired
    private CiphertextCycleCountEvaluator cycleCountEvaluator;

    @Autowired
    private CiphertextRowLevelEntropyEvaluator rowLevelEntropyEvaluator;

    @Autowired
    private CiphertextLanguageModelEvaluator languageModelEvaluator;

    @Autowired
    private UnwrapTranspositionCipherTransformer unwrapTranspositionCipherTransformer;

    @Autowired
    private Cipher cipher;

    @PostConstruct
    public void init() {
        if (keyLengthMin < 2) {
            throw new IllegalArgumentException("The minimum transposition key length must be greater than or equal to 2, but it was found to be " + keyLengthMin + ".");
        }

        if (keyLengthMin > keyLengthMax) {
            throw new IllegalArgumentException("The minimum transposition key length which was " + keyLengthMin + " must be less than or equal to the maximum key length which was " + keyLengthMax + ".");
        }

        if (keyLengthMax >= cipher.length()) {
            throw new IllegalArgumentException("The transposition key length must be less than the cipher length of " + cipher.length() + ".");
        }
    }

    public void run() {
        long start = System.currentTimeMillis();
        Map<Integer, List<EpochResults>> bestSolutionsPerKeyLength = new HashMap<>(keyLengthMax - keyLengthMin);

        for (int keyLength = keyLengthMin; keyLength <= keyLengthMax; keyLength++) {
            bestSolutionsPerKeyLength.put(keyLength, new ArrayList<>(epochs));

            for (int epoch = 1; epoch <= epochs; epoch++) {
                CipherSolution cipherProposal = new CipherSolution(cipher, ARBITRARY_INITIAL_LIST_SIZE);

                List<Integer> transpositionKeyIndicesSource = new ArrayList<>(keyLength);
                for (int i = 0; i < keyLength; i++) {
                    transpositionKeyIndicesSource.add(i);
                }

                List<Integer> transpositionKeyIndices = new ArrayList<>(keyLength);
                for (int i = 0; i < keyLength; i++) {
                    transpositionKeyIndices.add(transpositionKeyIndicesSource.remove(ThreadLocalRandom.current().nextInt(transpositionKeyIndicesSource.size())));
                }

                cipherProposal.setCipher(cipher);

                log.info("Epoch {} of {}.  Running sampler for {} iterations.", epoch, epochs, samplerIterations);

                EpochResults epochResults = performEpoch(epoch, cipherProposal, transpositionKeyIndices, keyLength);
                bestSolutionsPerKeyLength.get(keyLength).add(epochResults);
            }
        }

        log.info("Total time elapsed: {}ms.", (System.currentTimeMillis() - start));
        printSummaryResults(bestSolutionsPerKeyLength);
    }

    private EpochResults performEpoch(int epoch, CipherSolution initialCipher, List<Integer> transpositionKeyIndices, int keyLength) {
        log.debug("{}", transpositionKeyIndices);

        Double maxTemp = annealingTemperatureMax;
        Double minTemp = annealingTemperatureMin;
        Double iterations = (double) samplerIterations;
        Double temperature;
        CipherSolution next = initialCipher.clone();
        next.setCipher(unwrapTranspositionCipherTransformer.transform(cipher, transpositionKeyIndices));
        evaluate(next);
        CipherSolution maxProbability = next;
        List<Integer> maxIndices = new ArrayList<>(transpositionKeyIndices);
        int maxProbabilityIteration = 0;
        long start = System.currentTimeMillis();
        long startSampling;
        long letterSamplingElapsed;

        int i;
        for (i = 0; i < samplerIterations; i++) {
            long iterationStart = System.currentTimeMillis();

            /*
             * Set temperature as a ratio of the max temperature to the number of iterations left, offset by the min
             * temperature so as not to go below it
             */
            temperature = ((maxTemp - minTemp) * ((iterations - (double) i) / iterations)) + minTemp;

            startSampling = System.currentTimeMillis();
            next = runSampler(temperature, next, transpositionKeyIndices);
            letterSamplingElapsed = (System.currentTimeMillis() - startSampling);

            if (maxProbability.getLogProbability() < next.getLogProbability()) {
                maxProbability = next;
                maxProbabilityIteration = i + 1;
                maxIndices = new ArrayList<>(transpositionKeyIndices);
            }

            if (log.isDebugEnabled()) {
                log.debug("Iteration {} complete.  [elapsed={}ms, letterSampling={}ms, temp={}]", (i + 1), (System.currentTimeMillis() - iterationStart), letterSamplingElapsed, String.format("%1$,.4f", temperature));
                log.debug("Indices: {}, Score: {}, KeyLength: {}", transpositionKeyIndices, next.getLogProbability(), keyLength);
            }
        }

        log.info("Letter sampling completed in {}ms.  Average={}ms.", (System.currentTimeMillis() - start), ((double) (System.currentTimeMillis() - start) / (double) i));
        log.info("Best probability found at iteration {}", maxProbabilityIteration);
        log.info("Indices for best probability: {}, Score: {}, KeyLength: {}", maxIndices, maxProbability.getLogProbability(), keyLength);
        log.debug("Cipher: {}", maxProbability.getCipher());

        return new EpochResults(epoch, maxProbabilityIteration, maxProbability, maxIndices);
    }

    private CipherSolution runSampler(Double temperature, CipherSolution solution, List<Integer> transpositionKeyIndices) {
        CipherSolution proposal;
        CipherSolution best = solution;
        int first = ThreadLocalRandom.current().nextInt(transpositionKeyIndices.size());
        int second = ThreadLocalRandom.current().nextInt(transpositionKeyIndices.size());

        if (first == second) {
            return best;
        }

        List<Integer> nextTranspositionKeyIndices = new ArrayList<>(transpositionKeyIndices);
        int firstValue = nextTranspositionKeyIndices.get(first);
        int secondValue = nextTranspositionKeyIndices.get(second);
        nextTranspositionKeyIndices.set(first, secondValue);
        nextTranspositionKeyIndices.set(second, firstValue);

        proposal = solution.clone();

        proposal.setCipher(unwrapTranspositionCipherTransformer.transform(cipher, nextTranspositionKeyIndices));

        evaluate(proposal);

        best = selectNext(temperature, best, proposal);

        if (best == proposal) {
            transpositionKeyIndices.clear();
            transpositionKeyIndices.addAll(nextTranspositionKeyIndices);
        }

        return best;
    }

    private CipherSolution selectNext(Double temperature, CipherSolution solution, CipherSolution proposal) {
        Double acceptanceProbability;

        float solutionScore = solution.getLogProbability();
        float proposalScore = proposal.getLogProbability();

        if (proposalScore >= solutionScore) {
            log.debug("Better solution found");
            return proposal;
        }

        // Need to convert to log probabilities in order for the acceptance probability calculation to be useful
        acceptanceProbability = Math.exp(((solutionScore - proposalScore) / temperature) * -1d);

        log.info("Solution score: {}, Proposal Score: {}, Temperature: {}, Acceptance probability: {}", String.format("%1$.2f", solutionScore), String.format("%1$.2f", proposalScore), String.format("%1$,.2f", temperature), String.format("%1$,.4f", acceptanceProbability));

        if (acceptanceProbability < 0d) {
            throw new IllegalStateException("Acceptance probability was calculated to be less than zero.  Please review the math as this should not happen.");
        }

        if (acceptanceProbability > 1d || ThreadLocalRandom.current().nextDouble() < acceptanceProbability.doubleValue()) {
            return proposal;
        }

        return solution;
    }

    private double evaluate(CipherSolution cipherSolution) {
//        int repeatingBigramScore = repeatingBigramEvaluator.evaluate(cipherSolution);
        int cycleScore = cycleCountEvaluator.evaluate(cipher, cipherSolution);
//        double rowLevelEntropyPenalty = rowLevelEntropyEvaluator.evaluate(cipherSolution);
        float languageModelScore = languageModelEvaluator.evaluate(cipherSolution.getCipher());

        float scaledScore = languageModelScore + (cycleScore / 25f);

        cipherSolution.clearLogProbabilities();
        cipherSolution.addLogProbability(0, scaledScore);

        return scaledScore;
    }

    private void printSummaryResults(Map<Integer, List<EpochResults>> bestSolutionsPerKeyLength) {
        log.info("---------------------");
        log.info("---RESULTS SUMMARY---");
        log.info("---------------------");

        CipherSolution cipherProposal = new CipherSolution(cipher, ARBITRARY_INITIAL_LIST_SIZE);
        cipherProposal.setCipher(cipher);
        evaluate(cipherProposal);
        log.info("Original solution score: {}", cipherProposal.getLogProbability());

        int bestAverageKeyLength = -1;
        double bestAverageScore = Integer.MIN_VALUE;
        int bestBestKeyLength = -1;
        double bestBestScore = Integer.MIN_VALUE;

        for (Map.Entry<Integer, List<EpochResults>> entry : bestSolutionsPerKeyLength.entrySet()) {
            double averageScore = entry.getValue().stream()
                    .mapToDouble(epochResults -> epochResults.getBestSolution().getLogProbability())
                    .average()
                    .orElse(0d);

            double bestScore = entry.getValue().stream()
                    .mapToDouble(epochResults -> epochResults.getBestSolution().getLogProbability())
                    .max()
                    .orElse(0d);

            if (averageScore > bestAverageScore) {
                bestAverageScore = averageScore;
                bestAverageKeyLength = entry.getKey();
            }

            if (bestScore > bestBestScore) {
                bestBestScore = bestScore;
                bestBestKeyLength = entry.getKey();
            }

            log.info("For key length {}, average score was {} and best score was {}.", entry.getKey(), averageScore, bestScore);
        }

        log.info("Best average score found for key length: {}", bestAverageKeyLength);
        log.info("Best overall score found for key length: {}", bestBestKeyLength);
    }
}
