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

package com.ciphertool.zenith.inference.optimizer;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Plaintext;
import com.ciphertool.zenith.inference.probability.LetterProbability;
import com.ciphertool.zenith.math.selection.RouletteSampler;
import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ConditionalOnProperty(value = "decipherment.optimizer", havingValue = "SimulatedAnnealingSolutionOptimizer")
public class SimulatedAnnealingSolutionOptimizer extends AbstractSolutionOptimizer implements SolutionOptimizer {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${decipherment.sampler.iterations}")
    private int samplerIterations;

    @Value("${decipherment.annealing.temperature.max}")
    private double annealingTemperatureMax;

    @Value("${decipherment.annealing.temperature.min}")
    private double annealingTemperatureMin;

    @Value("${decipherment.sampler.iterate-randomly}")
    private Boolean iterateRandomly;

    @Value("${markov.letter.order}")
    private int markovOrder;

    @Value("${decipherment.epochs:1}")
    private int epochs;

    @Autowired
    private TreeMarkovModel letterMarkovModel;

    @Override
    public void optimize() {
        int cipherKeySize = (int) cipher.getCiphertextCharacters().stream().map(c -> c.getValue()).distinct().count();

        List<TreeNGram> firstOrderNodes = new ArrayList<>(letterMarkovModel.getRootNode().getTransitions().values());

        List<LetterProbability> letterUnigramProbabilities = new ArrayList<>(ModelConstants.LOWERCASE_LETTERS.size());

        Double probability;
        for (TreeNGram node : firstOrderNodes) {
            probability = (double) node.getCount() / (double) letterMarkovModel.getRootNode().getCount();

            letterUnigramProbabilities.add(new LetterProbability(node.getCumulativeString().charAt(0), probability));

            log.info(node.getCumulativeString().charAt(0) + ": " + probability.toString());
        }

        log.info("unknownLetterNGramProbability: {}", letterMarkovModel.getUnknownLetterNGramProbability());

        Collections.sort(letterUnigramProbabilities);
        RouletteSampler<LetterProbability> unigramRouletteSampler = new RouletteSampler<>();
        double totalUnigramProbability = unigramRouletteSampler.reIndex(letterUnigramProbabilities);

        for (int epoch = 0; epoch < epochs; epoch++) {
            CipherSolution initialSolution = generateInitialSolutionProposal(cipher, cipherKeySize, unigramRouletteSampler, letterUnigramProbabilities, totalUnigramProbability);

            log.info("Epoch {} of {}.  Running sampler for {} iterations.", (epoch + 1), epochs, samplerIterations);

            performEpoch(initialSolution);
        }
    }

    private CipherSolution generateInitialSolutionProposal(Cipher cipher, int cipherKeySize, RouletteSampler<LetterProbability> unigramRouletteSampler, List<LetterProbability> letterUnigramProbabilities, double totalUnigramProbability) {
        CipherSolution solutionProposal = new CipherSolution(cipher, cipherKeySize);

        cipher.getCiphertextCharacters().stream()
                .map(ciphertext -> ciphertext.getValue())
                .distinct()
                .forEach(ciphertext -> {
                    // Pick a plaintext at random according to the language model
                    String nextPlaintext = letterUnigramProbabilities.get(unigramRouletteSampler.getNextIndex()).getValue().toString();

                    solutionProposal.putMapping(ciphertext, new Plaintext(nextPlaintext));
                });

        return solutionProposal;
    }

    private void performEpoch(CipherSolution initialSolution) {
        plaintextEvaluator.evaluate(initialSolution, null);

        if (useKnownEvaluator && knownPlaintextEvaluator != null) {
            initialSolution.setKnownSolutionProximity(knownPlaintextEvaluator.evaluate(initialSolution));
        }

        log.debug(initialSolution.toString());

        Double maxTemp = annealingTemperatureMax;
        Double minTemp = annealingTemperatureMin;
        Double iterations = (double) samplerIterations;
        Double temperature;
        CipherSolution next = initialSolution;
        CipherSolution maxProbability = initialSolution;
        int maxProbabilityIteration = 0;
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
            next = runLetterSampler(temperature, next);
            letterSamplingElapsed = (System.currentTimeMillis() - startLetterSampling);

            if (useKnownEvaluator && knownPlaintextEvaluator != null) {
                knownProximity = knownPlaintextEvaluator.evaluate(next);
                next.setKnownSolutionProximity(knownProximity);

                if (maxKnown.getKnownSolutionProximity() < knownProximity) {
                    maxKnown = next;
                    maxKnownIteration = i + 1;
                }
            }

            if (maxProbability.getLogProbability() < next.getLogProbability()) {
                maxProbability = next;
                maxProbabilityIteration = i + 1;
            }

            if (log.isDebugEnabled()) {
                log.debug("Iteration {} complete.  [elapsed={}ms, letterSampling={}ms, temp={}]", (i + 1), (System.currentTimeMillis() - iterationStart), letterSamplingElapsed, String.format("%1$,.4f", temperature));
                log.debug(next.toString());
            }
        }

        log.info("Letter sampling completed in {}ms.  Average={}ms.", (System.currentTimeMillis() - start), ((double) (System.currentTimeMillis() - start) / (double) i));

        if (useKnownEvaluator && knownPlaintextEvaluator != null) {
            log.info("Best known found at iteration {}: {}", maxKnownIteration, maxKnown);
            log.info("Mappings for best known:");

            for (Map.Entry<String, Plaintext> entry : maxKnown.getMappings().entrySet()) {
                log.info("{}: {}", entry.getKey(), entry.getValue().getValue());
            }
        }

        log.info("Best probability found at iteration {}: {}", maxProbabilityIteration, maxProbability);
        log.info("Mappings for best probability:");

        for (Map.Entry<String, Plaintext> entry : maxProbability.getMappings().entrySet()) {
            log.info("{}: {}", entry.getKey(), entry.getValue().getValue());
        }
    }

    private CipherSolution runLetterSampler(Double temperature, CipherSolution solution) {
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

            plaintextEvaluator.evaluate(proposal, nextEntry.getKey());

            solution = selectNext(temperature, solution, proposal);
        }

        return solution;
    }

    private CipherSolution selectNext(Double temperature, CipherSolution solution, CipherSolution proposal) {
        Double acceptanceProbability;
        Double solutionScore = solution.getScore();
        Double proposalScore = proposal.getScore();

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
