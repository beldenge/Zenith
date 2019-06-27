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

import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.transformer.TranspositionCipherTransformer;
import com.ciphertool.zenith.search.evaluator.CiphertextBigramEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class TranspositionSearcher {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${cipher.name}")
    private String cipherName;

    @Value("${decipherment.sampler.iterations}")
    private int samplerIterations;

    @Value("${decipherment.annealing.temperature.max}")
    private double annealingTemperatureMax;

    @Value("${decipherment.annealing.temperature.min}")
    private double annealingTemperatureMin;

    @Value("${decipherment.epochs:1}")
    private int epochs;

    @Value("${decipherment.transposition.key-length.min:2}")
    private int keyLengthMin;

    @Value("${decipherment.transposition.key-length.max}")
    private int keyLengthMax;

    @Autowired
    private CiphertextBigramEvaluator ciphertextEvaluator;

    @Autowired
    private CipherDao cipherDao;

    @Autowired
    private TranspositionCipherTransformer transpositionCipherTransformer;

    private Cipher cipher;

    @PostConstruct
    public void init() {
        if (keyLengthMin < 2) {
            throw new IllegalArgumentException("The minimum transposition key length must be greater than or equal to 2, but it was found to be " + keyLengthMin + ".");
        }

        if (keyLengthMin > keyLengthMax) {
            throw new IllegalArgumentException("The minimum transposition key length which was " + keyLengthMin + " must be less than or equal to the maximum key length which was " + keyLengthMax + ".");
        }

        cipher = cipherDao.findByCipherName(cipherName);

        if (keyLengthMax >= cipher.length()) {
            throw new IllegalArgumentException("The transposition key length must be less than the cipher length of " + cipher.length() + ".");
        }
    }

    public void run() {
        for (int keyLength = keyLengthMin; keyLength <= keyLengthMax; keyLength++) {
            for (int epoch = 0; epoch < epochs; epoch++) {
                CipherSolution cipherProposal = new CipherSolution();

                List<Integer> transpositionKeyIndicesSource = new ArrayList<>(keyLength);
                for (int i = 0; i < keyLength; i++) {
                    transpositionKeyIndicesSource.add(i);
                }

                List<Integer> transpositionKeyIndices = new ArrayList<>(keyLength);
                for (int i = 0; i < keyLength; i++) {
                    transpositionKeyIndices.add(transpositionKeyIndicesSource.remove(ThreadLocalRandom.current().nextInt(transpositionKeyIndicesSource.size())));
                }

                cipherProposal.setCipher(cipher);

                log.info("Epoch {} of {}.  Running sampler for {} iterations.", (epoch + 1), epochs, samplerIterations);

                performEpoch(cipherProposal, transpositionKeyIndices, keyLength);
            }
        }
    }

    private void performEpoch(CipherSolution initialCipher, List<Integer> transpositionKeyIndices, int keyLength) {
        ciphertextEvaluator.evaluate(initialCipher);

        log.debug("{}", transpositionKeyIndices);

        Double maxTemp = annealingTemperatureMax;
        Double minTemp = annealingTemperatureMin;
        Double iterations = (double) samplerIterations;
        Double temperature;
        CipherSolution next;
        CipherSolution maxProbability = initialCipher.clone();
        maxProbability.setCipher(transpositionCipherTransformer.transform(initialCipher.getCipher().clone(), transpositionKeyIndices));
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
            next = runSampler(temperature, initialCipher, transpositionKeyIndices);
            letterSamplingElapsed = (System.currentTimeMillis() - startSampling);

            if (maxProbability.getLogProbability() < next.getLogProbability()) {
                maxProbability = next;
                maxProbabilityIteration = i + 1;
                maxIndices = new ArrayList<>(transpositionKeyIndices);
            }

            if (log.isDebugEnabled()) {
                log.debug("Iteration {} complete.  [elapsed={}ms, letterSampling={}ms, temp={}]", (i + 1), (System.currentTimeMillis() - iterationStart), letterSamplingElapsed, String.format("%1$,.4f", temperature));
                log.debug("Indices: {}, Bigrams: {}, KeyLength: {}", transpositionKeyIndices, ciphertextEvaluator.evaluate(next), keyLength);
            }
        }

        log.info("Letter sampling completed in {}ms.  Average={}ms.", (System.currentTimeMillis() - start), ((double) (System.currentTimeMillis() - start) / (double) i));
        log.info("Best probability found at iteration {}", maxProbabilityIteration);
        log.info("Indices for best probability: {}, Bigrams: {}, KeyLength: {}", maxIndices, ciphertextEvaluator.evaluate(maxProbability), keyLength);
        log.debug("Cipher: {}", maxProbability.getCipher());
    }

    private CipherSolution runSampler(Double temperature, CipherSolution solution, List<Integer> transpositionKeyIndices) {
        CipherSolution proposal;
        CipherSolution best = solution;
        List<Integer> bestTranspositionKeyIndices = new ArrayList<>(transpositionKeyIndices);

        for (int i = 0; i < transpositionKeyIndices.size(); i++) {
            // Start at i + 1, as all previous swaps will have already been tried
            for (int j = i + 1; j < transpositionKeyIndices.size(); j++) {
                List<Integer> nextTranspositionKeyIndices = new ArrayList<>(transpositionKeyIndices);
                int firstValue = nextTranspositionKeyIndices.get(i);
                int secondValue = nextTranspositionKeyIndices.get(j);
                nextTranspositionKeyIndices.set(i, secondValue);
                nextTranspositionKeyIndices.set(j, firstValue);

                proposal = solution.clone();

                proposal.setCipher(transpositionCipherTransformer.transform(solution.getCipher(), nextTranspositionKeyIndices));

                ciphertextEvaluator.evaluate(proposal);

                best = selectNext(temperature, best, proposal);

                if (best == proposal) {
                    bestTranspositionKeyIndices.clear();
                    bestTranspositionKeyIndices.addAll(nextTranspositionKeyIndices);
                }
            }
        }

        transpositionKeyIndices.clear();
        transpositionKeyIndices.addAll(bestTranspositionKeyIndices);

        return best;
    }

    private CipherSolution selectNext(Double temperature, CipherSolution solution, CipherSolution proposal) {
        Double acceptanceProbability;

        Double solutionScore = solution.getLogProbability();
        Double proposalScore = proposal.getLogProbability();

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
