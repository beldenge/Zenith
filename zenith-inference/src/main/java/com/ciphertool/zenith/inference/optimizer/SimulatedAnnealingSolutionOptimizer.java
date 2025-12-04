/*
 * Copyright 2017-2020 George Belden
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

import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.model.SolutionScore;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class SimulatedAnnealingSolutionOptimizer extends AbstractSolutionOptimizer {
    private Logger log = LoggerFactory.getLogger(getClass());

    private static ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    public static final String SAMPLER_ITERATIONS = "samplerIterations";
    public static final String ANNEALING_TEMPERATURE_MIN = "annealingTemperatureMin";
    public static final String ANNEALING_TEMPERATURE_MAX = "annealingTemperatureMax";

    @Autowired
    private ArrayMarkovModel letterMarkovModel;

    private char[] biasedLetterBucket;

    @PostConstruct
    public void init() {
        List<Character> biasedCharacterBucket = new ArrayList<>();

        // Instead of using a uniform distribution or one purely based on English, we flatten out the English letter unigram probabilities by the flatMassWeight
        // This seems to be a good balance for the letter sampler so that it slightly prefers more likely characters while still allowing for novel characters to be sampled
        float flatMassWeight = 0.8f;
        float flatMass = (1f / (float) letterMarkovModel.getFirstOrderNodes().size()) * flatMassWeight;

        for (TreeNGram node : letterMarkovModel.getFirstOrderNodes()) {
            float letterProbability = (float) node.getCount() / (float) letterMarkovModel.getTotalNGramCount();

            float scaledMass = letterProbability * (1f - flatMassWeight);

            int letterBias = (int) (1000f * (scaledMass + flatMass));

            for (int i = 0; i < letterBias; i ++) {
                biasedCharacterBucket.add(node.getCumulativeString().charAt(0));
            }
        }

        biasedLetterBucket = new char[biasedCharacterBucket.size()];
        for (int i = 0; i < biasedCharacterBucket.size(); i ++) {
            biasedLetterBucket[i] = biasedCharacterBucket.get(i);
        }
    }

    @Override
    public CipherSolution optimize(Cipher cipher, int epochs, Map<String, Object> configuration, List<PlaintextTransformationStep> plaintextTransformationSteps, PlaintextEvaluator plaintextEvaluator, OnEpochComplete onEpochComplete) {
        int samplerIterations = (int) configuration.get(SAMPLER_ITERATIONS);
        float annealingTemperatureMin = (float) configuration.get(ANNEALING_TEMPERATURE_MIN);
        float annealingTemperatureMax = (float) configuration.get(ANNEALING_TEMPERATURE_MAX);

        int cipherKeySize = (int) cipher.getCiphertextCharacters().stream()
                .map(c -> c.getValue())
                .distinct()
                .count();

        log.debug("unknownLetterNGramProbability: {}", letterMarkovModel.getUnknownLetterNGramProbability());

        Map<String, Object> precomputedCounterweightData = plaintextEvaluator.getPrecomputedCounterweightData(cipher);

        long totalElapsed = 0;
        int correctSolutions = 0;
        CipherSolution overallBest = null;

        int epoch = 0;
        for (; epoch < epochs; epoch++) {
            CipherSolution initialSolution = generateInitialSolutionProposal(cipher, cipherKeySize);

            log.info("Epoch {} of {}.  Running sampler for {} iterations.", (epoch + 1), epochs, samplerIterations);

            String[] mappingKeys = new String[initialSolution.getMappings().size()];

            int mappingListIndex = 0;
            for (String key : initialSolution.getMappings().keySet()) {
                mappingKeys[mappingListIndex] = key;
                mappingListIndex ++;
            }

            long start = System.currentTimeMillis();

            CipherSolution best = performEpoch(precomputedCounterweightData, cipher, initialSolution, mappingKeys, samplerIterations, annealingTemperatureMin, annealingTemperatureMax, plaintextTransformationSteps, plaintextEvaluator);

            long elapsed = System.currentTimeMillis() - start;
            totalElapsed += elapsed;
            log.info("Epoch completed in {}ms.", elapsed);

            if (log.isInfoEnabled()) {
                cipherSolutionPrinter.print(best, plaintextTransformationSteps);
            }

            if (cipher.hasKnownSolution() && knownSolutionCorrectnessThreshold <= best.evaluateKnownSolution()) {
                correctSolutions ++;
            }

            overallBest = (overallBest == null) ? best : (best.compareTo(overallBest) > 0 ? best : overallBest);

            if (onEpochComplete != null) {
                onEpochComplete.fire(epoch + 1);
            }
        }

        if (cipher.hasKnownSolution()) {
            log.info("{} out of {} epochs ({}%) produced the correct solution.", correctSolutions, epochs, String.format("%1$,.2f", (correctSolutions / (double) epochs) * 100.0));
        }

        log.info("Average epoch time={}ms", ((float) totalElapsed / (float) epoch));

        return overallBest;
    }

    private CipherSolution generateInitialSolutionProposal(Cipher cipher, int cipherKeySize) {
        CipherSolution solutionProposal = new CipherSolution(cipher, cipherKeySize);

        cipher.getCiphertextCharacters().stream()
                .map(ciphertext -> ciphertext.getValue())
                .distinct()
                .forEach(ciphertext -> {
                    solutionProposal.putMapping(ciphertext, biasedLetterBucket[RANDOM.nextInt(biasedLetterBucket.length)]);
                });

        return solutionProposal;
    }

    private CipherSolution performEpoch(Map<String, Object> precomputedCounterweightData, Cipher cipher, CipherSolution initialSolution, String[] mappingKeys, int samplerIterations, float annealingTemperatureMin, float annealingTemperatureMax, List<PlaintextTransformationStep> plaintextTransformationSteps, PlaintextEvaluator plaintextEvaluator) {
        String solutionString = initialSolution.asSingleLineString();

        if (CollectionUtils.isNotEmpty(plaintextTransformationSteps)) {
            solutionString = plaintextTransformationManager.transform(solutionString, plaintextTransformationSteps);
        }

        SolutionScore score = plaintextEvaluator.evaluate(precomputedCounterweightData, cipher, initialSolution, solutionString, null);
        initialSolution.setScores(score.getScores());

        if (log.isDebugEnabled()) {
            cipherSolutionPrinter.print(initialSolution, plaintextTransformationSteps);
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
            next = runLetterSampler(precomputedCounterweightData, cipher, temperature, next, solutionCharArray, mappingKeys, plaintextTransformationSteps, plaintextEvaluator);

            if (log.isDebugEnabled()) {
                long now = System.currentTimeMillis();
                log.debug("Iteration {} complete.  [elapsed={}ms, letterSampling={}ms, temp={}]", (i + 1), (now - iterationStart), (now - startLetterSampling), String.format("%1$,.4f", temperature));
                cipherSolutionPrinter.print(next, plaintextTransformationSteps);
            }
        }

        return next;
    }

    private CipherSolution runLetterSampler(Map<String, Object> precomputedCounterweightData,
                                            Cipher cipher,
                                            float temperature,
                                            CipherSolution solution,
                                            char[] solutionCharArray,
                                            String[] mappingKeys,
                                            List<PlaintextTransformationStep> plaintextTransformationSteps,
                                            PlaintextEvaluator plaintextEvaluator) {
        String nextKey;

        // For each cipher symbol type, run the letter sampling
        for (int i = 0; i < mappingKeys.length; i++) {
            nextKey = mappingKeys[i];

            char letter = biasedLetterBucket[RANDOM.nextInt(biasedLetterBucket.length)];

            char originalMapping = solution.getMappings().get(nextKey);

            if (letter == originalMapping) {
                continue;
            }

            // TODO: this needs to be refactored in order to support multi objective scoring functions
            Fitness[] originalScores = solution.getScores();
            solution.replaceMapping(nextKey, letter);

            int[] cipherSymbolIndices = cipher.getCipherSymbolIndicesMap().get(nextKey);
            for (int j = 0; j < cipherSymbolIndices.length; j ++) {
                solutionCharArray[cipherSymbolIndices[j]] = letter;
            }

            String proposalString = new String(solutionCharArray);

            if (CollectionUtils.isNotEmpty(plaintextTransformationSteps)) {
                proposalString = plaintextTransformationManager.transform(proposalString, plaintextTransformationSteps);
            }

            SolutionScore score = plaintextEvaluator.evaluate(precomputedCounterweightData, cipher, solution, proposalString, nextKey);
            solution.setScores(score.getScores());

            if (originalScores.length > 1) {
                throw new IllegalStateException("SimulatedAnnealing currently only supports single-objective scoring functions.");
            }

            // TODO: these next few lines need to be refactored in order to support multi objective scoring functions
            if (!selectNext(temperature, (float) originalScores[0].getValue(), (float) solution.getScores()[0].getValue())) {
                solution.setScores(new Fitness[] { originalScores[0] });
                solution.replaceMapping(nextKey, originalMapping);

                float[][] ngramProbabilitiesUpdated = score.getNgramProbabilitiesUpdated();
                for (int j = 0; j < ngramProbabilitiesUpdated[0].length; j ++) {
                    // The updated probabilities array is oversized as it's not feasible to predict the array length before building it
                    // So, once we hit an index that is equal to zero, we are guaranteed to be at the end of the populated part of the array
                    if (ngramProbabilitiesUpdated[1][j] == 0f) {
                        break;
                    }

                    solution.replaceLogProbability((int) ngramProbabilitiesUpdated[0][j], ngramProbabilitiesUpdated[1][j]);
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
