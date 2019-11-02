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
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.SolutionScorer;
import com.ciphertool.zenith.inference.printer.CipherSolutionPrinter;
import com.ciphertool.zenith.inference.probability.LetterProbability;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;
import com.ciphertool.zenith.inference.util.IndexOfCoincidenceEvaluator;
import com.ciphertool.zenith.math.selection.RouletteSampler;
import com.ciphertool.zenith.model.LanguageConstants;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

@Component
public class SimulatedAnnealingSolutionOptimizer implements SolutionOptimizer {
    private Logger log = LoggerFactory.getLogger(getClass());

    private static SplittableRandom RANDOM = new SplittableRandom();

    @Value("${simulated-annealing.sampler.iterations}")
    private int samplerIterations;

    @Value("${simulated-annealing.temperature.max}")
    private float annealingTemperatureMax;

    @Value("${simulated-annealing.temperature.min}")
    private float annealingTemperatureMin;

    @Value("${markov.letter.order}")
    private int markovOrder;

    @Value("${decipherment.epochs:1}")
    private int epochs;

    @Value("${decipherment.known-solution.correctness-threshold:0.9}")
    private float knownSolutionCorrectnessThreshold;

    @Autowired
    private SolutionScorer solutionScorer;

    @Autowired
    private IndexOfCoincidenceEvaluator indexOfCoincidenceEvaluator;

    @Autowired
    private ArrayMarkovModel letterMarkovModel;

    @Autowired(required = false)
    @Qualifier("activePlaintextTransformers")
    private List<PlaintextTransformer> plaintextTransformers;

    @Autowired
    private PlaintextEvaluator plaintextEvaluator;

    @Autowired
    private CipherSolutionPrinter cipherSolutionPrinter;

    @Override
    public CipherSolution optimize(Cipher cipher) {
        int cipherKeySize = (int) cipher.getCiphertextCharacters().stream()
                .map(c -> c.getValue())
                .distinct()
                .count();

        List<LetterProbability> letterUnigramProbabilities = new ArrayList<>(LanguageConstants.LOWERCASE_LETTERS_SIZE);

        double probability;
        for (TreeNGram node : letterMarkovModel.getFirstOrderNodes()) {
            probability = (double) node.getCount() / (double) letterMarkovModel.getTotalNGramCount();

            letterUnigramProbabilities.add(new LetterProbability(node.getCumulativeString().charAt(0), probability));

            log.info(node.getCumulativeString().charAt(0) + ": " + probability);
        }

        log.info("unknownLetterNGramProbability: {}", letterMarkovModel.getUnknownLetterNGramProbability());

        Collections.sort(letterUnigramProbabilities);
        RouletteSampler<LetterProbability> unigramRouletteSampler = new RouletteSampler<>();
        unigramRouletteSampler.reIndex(letterUnigramProbabilities);

        long totalElapsed = 0;
        int correctSolutions = 0;
        CipherSolution overallBest = null;

        int epoch = 0;
        for (; epoch < epochs; epoch++) {
            CipherSolution initialSolution = generateInitialSolutionProposal(cipher, cipherKeySize, unigramRouletteSampler, letterUnigramProbabilities);

            log.info("Epoch {} of {}.  Running sampler for {} iterations.", (epoch + 1), epochs, samplerIterations);

            String[] mappingKeys = new String[initialSolution.getMappings().size()];

            int mappingListIndex = 0;
            for (String key : initialSolution.getMappings().keySet()) {
                mappingKeys[mappingListIndex] = key;
                mappingListIndex ++;
            }

            long start = System.currentTimeMillis();

            CipherSolution best = performEpoch(cipher, initialSolution, mappingKeys);

            long elapsed = System.currentTimeMillis() - start;
            totalElapsed += elapsed;
            log.info("Epoch completed in {}ms.", elapsed);

            if (log.isInfoEnabled()) {
                cipherSolutionPrinter.print(best);
            }

            if (cipher.hasKnownSolution() && knownSolutionCorrectnessThreshold <= best.evaluateKnownSolution()) {
                correctSolutions ++;
            }

            overallBest = (overallBest == null) ? best : (best.getScore() > overallBest.getScore() ? best : overallBest);
        }

        if (cipher.hasKnownSolution()) {
            log.info("{} out of {} epochs ({}%) produced the correct solution.", correctSolutions, epochs, String.format("%1$,.2f", (correctSolutions / (double) epochs) * 100.0));
        }

        log.info("Average epoch time={}ms", ((float) totalElapsed / (float) epoch));

        return overallBest;
    }

    private CipherSolution generateInitialSolutionProposal(Cipher cipher, int cipherKeySize, RouletteSampler<LetterProbability> unigramRouletteSampler, List<LetterProbability> letterUnigramProbabilities) {
        CipherSolution solutionProposal = new CipherSolution(cipher, cipherKeySize);

        cipher.getCiphertextCharacters().stream()
                .map(ciphertext -> ciphertext.getValue())
                .distinct()
                .forEach(ciphertext -> {
                    // Pick a plaintext at random according to the language model
                    char nextPlaintext = letterUnigramProbabilities.get(unigramRouletteSampler.getNextIndex()).getValue();

                    solutionProposal.putMapping(ciphertext, nextPlaintext);
                });

        return solutionProposal;
    }

    private CipherSolution performEpoch(Cipher cipher, CipherSolution initialSolution, String[] mappingKeys) {
        String solutionString = initialSolution.asSingleLineString();
        if (plaintextTransformers != null) {
            for (PlaintextTransformer plaintextTransformer : plaintextTransformers) {
                solutionString = plaintextTransformer.transform(solutionString);
            }
        }

        plaintextEvaluator.evaluate(cipher, initialSolution, solutionString, null);
        initialSolution.setIndexOfCoincidence(indexOfCoincidenceEvaluator.evaluate(cipher, solutionString));
        initialSolution.setScore(solutionScorer.score(initialSolution));

        if (log.isDebugEnabled()) {
            cipherSolutionPrinter.print(initialSolution);
        }

        float temperature;
        CipherSolution next = initialSolution;
        long startLetterSampling;
        char[] solutionCharArray = next.asSingleLineString().toCharArray();

        int i;
        for (i = 0; i < samplerIterations; i++) {
            long iterationStart = System.currentTimeMillis();

            /*
             * Set temperature as a ratio of the max temperature to the number of iterations left, offset by the min
             * temperature so as not to go below it
             */
            temperature = ((annealingTemperatureMax - annealingTemperatureMin) * ((samplerIterations - (float) i) / samplerIterations)) + annealingTemperatureMin;

            startLetterSampling = System.currentTimeMillis();
            next = runLetterSampler(cipher, temperature, next, solutionCharArray, mappingKeys);

            if (log.isDebugEnabled()) {
                long now = System.currentTimeMillis();
                log.debug("Iteration {} complete.  [elapsed={}ms, letterSampling={}ms, temp={}]", (i + 1), (now - iterationStart), (now - startLetterSampling), String.format("%1$,.4f", temperature));
                cipherSolutionPrinter.print(next);
            }
        }

        return next;
    }

    private CipherSolution runLetterSampler(Cipher cipher, float temperature, CipherSolution solution, char[] solutionCharArray, String[] mappingKeys) {
        String nextKey;

        // For each cipher symbol type, run the letter sampling
        for (int i = 0; i < mappingKeys.length; i++) {
            nextKey = mappingKeys[i];

            char letter = LanguageConstants.LOWERCASE_LETTERS[RANDOM.nextInt(LanguageConstants.LOWERCASE_LETTERS_SIZE)];

            char originalMapping = solution.getMappings().get(nextKey);

            if (letter == originalMapping) {
                continue;
            }

            float originalScore = solution.getScore();
            float originalIndexOfCoincidence = solution.getIndexOfCoincidence();
            solution.replaceMapping(nextKey, letter);

            int[] cipherSymbolIndices = cipher.getCipherSymbolIndicesMap().get(nextKey);
            for (int j = 0; j < cipherSymbolIndices.length; j ++) {
                solutionCharArray[cipherSymbolIndices[j]] = letter;
            }

            String proposalString = new String(solutionCharArray);

            if (plaintextTransformers != null) {
                for (PlaintextTransformer plaintextTransformer : plaintextTransformers) {
                    proposalString = plaintextTransformer.transform(proposalString);
                }
            }

            float[][] logProbabilitiesUpdated = plaintextEvaluator.evaluate(cipher, solution, proposalString, nextKey);
            solution.setIndexOfCoincidence(indexOfCoincidenceEvaluator.evaluate(cipher, proposalString));
            solution.setScore(solutionScorer.score(solution));

            if (!selectNext(temperature, originalScore, solution.getScore())) {
                solution.setScore(originalScore);
                solution.setIndexOfCoincidence(originalIndexOfCoincidence);
                solution.replaceMapping(nextKey, originalMapping);

                for (int j = 0; j < logProbabilitiesUpdated[0].length; j ++) {
                    solution.replaceLogProbability((int) logProbabilitiesUpdated[0][j], logProbabilitiesUpdated[1][j]);
                }

                for (int j = 0; j < cipherSymbolIndices.length; j ++) {
                    solutionCharArray[cipherSymbolIndices[j]] = originalMapping;
                }
            }
        }

        return solution;
    }

    private boolean selectNext(float temperature, float solutionScore, float proposalScore) {
        if (proposalScore >= solutionScore) {
            return true;
        }

        // Need to convert to log probabilities in order for the acceptance probability calculation to be useful
        float acceptanceProbability = (float) Math.exp(((solutionScore - proposalScore) / temperature) * -1f);

        log.debug("Acceptance probability: {}", acceptanceProbability);

        if (acceptanceProbability < 0f) {
            throw new IllegalStateException("Acceptance probability was calculated to be less than zero.  Please review the math as this should not happen.");
        }

        if (acceptanceProbability > 1f || (float) RANDOM.nextDouble() < acceptanceProbability) {
            return true;
        }

        return false;
    }
}
