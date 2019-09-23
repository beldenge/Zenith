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
import com.ciphertool.zenith.inference.printer.CipherSolutionPrinter;
import com.ciphertool.zenith.inference.probability.LetterProbability;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;
import com.ciphertool.zenith.math.selection.RouletteSampler;
import com.ciphertool.zenith.model.LanguageConstants;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.MapMarkovModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ConditionalOnProperty(value = "decipherment.optimizer", havingValue = "SimulatedAnnealingSolutionOptimizer")
public class SimulatedAnnealingSolutionOptimizer implements SolutionOptimizer {
    private Logger log = LoggerFactory.getLogger(getClass());

    private static SplittableRandom RANDOM = new SplittableRandom();

    @Value("${simulated-annealing.sampler.iterations}")
    private int samplerIterations;

    @Value("${simulated-annealing.temperature.max}")
    private double annealingTemperatureMax;

    @Value("${simulated-annealing.temperature.min}")
    private double annealingTemperatureMin;

    @Value("${simulated-annealing.sampler.iterate-randomly}")
    private Boolean iterateRandomly;

    @Value("${markov.letter.order}")
    private int markovOrder;

    @Value("${decipherment.epochs:1}")
    private int epochs;

    @Value("${decipherment.known-solution.correctness-threshold:0.9}")
    private double knownSolutionCorrectnessThreshold;

    @Autowired
    protected Cipher cipher;

    @Autowired
    private MapMarkovModel letterMarkovModel;

    @Autowired(required = false)
    @Qualifier("activePlaintextTransformers")
    private List<PlaintextTransformer> plaintextTransformers;

    @Autowired
    private PlaintextEvaluator plaintextEvaluator;

    @Autowired
    private CipherSolutionPrinter cipherSolutionPrinter;

    @Override
    public CipherSolution optimize() {
        int cipherKeySize = (int) cipher.getCiphertextCharacters().stream().map(c -> c.getValue()).distinct().count();

        List<TreeNGram> firstOrderNodes = new ArrayList<>(letterMarkovModel.getFirstOrderNodes());

        List<LetterProbability> letterUnigramProbabilities = new ArrayList<>(LanguageConstants.LOWERCASE_LETTERS_SIZE);

        Double probability;
        for (TreeNGram node : firstOrderNodes) {
            probability = (double) node.getCount() / (double) letterMarkovModel.getTotalNumberOfNgrams();

            letterUnigramProbabilities.add(new LetterProbability(node.getCumulativeString().charAt(0), probability));

            log.info(node.getCumulativeString().charAt(0) + ": " + probability.toString());
        }

        log.info("unknownLetterNGramProbability: {}", letterMarkovModel.getUnknownLetterNGramProbability());

        Collections.sort(letterUnigramProbabilities);
        RouletteSampler<LetterProbability> unigramRouletteSampler = new RouletteSampler<>();
        double totalUnigramProbability = unigramRouletteSampler.reIndex(letterUnigramProbabilities);

        int correctSolutions = 0;
        CipherSolution overallBest = null;

        for (int epoch = 0; epoch < epochs; epoch++) {
            CipherSolution initialSolution = generateInitialSolutionProposal(cipher, cipherKeySize, unigramRouletteSampler, letterUnigramProbabilities, totalUnigramProbability);

            log.info("Epoch {} of {}.  Running sampler for {} iterations.", (epoch + 1), epochs, samplerIterations);

            CipherSolution best = performEpoch(initialSolution);

            if (cipher.hasKnownSolution() && knownSolutionCorrectnessThreshold <= best.evaluateKnownSolution()) {
                correctSolutions ++;
            }

            overallBest = (overallBest == null) ? best : (best.getScore() > overallBest.getScore() ? best : overallBest);
        }

        if (cipher.hasKnownSolution()) {
            log.info("{} out of {} epochs ({}%) produced the correct solution.", correctSolutions, epochs, String.format("%1$,.2f", (correctSolutions / (double) epochs) * 100.0));
        }

        return overallBest;
    }

    private CipherSolution generateInitialSolutionProposal(Cipher cipher, int cipherKeySize, RouletteSampler<LetterProbability> unigramRouletteSampler, List<LetterProbability> letterUnigramProbabilities, double totalUnigramProbability) {
        CipherSolution solutionProposal = new CipherSolution(cipher, cipherKeySize);

        cipher.getCiphertextCharacters().stream()
                .map(ciphertext -> ciphertext.getValue())
                .distinct()
                .forEach(ciphertext -> {
                    // Pick a plaintext at random according to the language model
                    String nextPlaintext = letterUnigramProbabilities.get(unigramRouletteSampler.getNextIndex()).getValue().toString();

                    solutionProposal.putMapping(ciphertext, nextPlaintext);
                });

        return solutionProposal;
    }

    private CipherSolution performEpoch(CipherSolution initialSolution) {
        String solutionString = initialSolution.asSingleLineString();
        if (plaintextTransformers != null) {
            for (PlaintextTransformer plaintextTransformer : plaintextTransformers) {
                solutionString = plaintextTransformer.transform(solutionString);
            }
        }

        plaintextEvaluator.evaluate(initialSolution, solutionString, null);

        log.debug(initialSolution.toString());

        Double maxTemp = annealingTemperatureMax;
        Double minTemp = annealingTemperatureMin;
        Double iterations = (double) samplerIterations;
        Double temperature;
        CipherSolution next = initialSolution;
        CipherSolution maxProbability = initialSolution;
        int maxProbabilityIteration = 0;
        long start = System.currentTimeMillis();
        long startLetterSampling;
        long letterSamplingElapsed;

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

            if (maxProbability.getLogProbability() < next.getLogProbability()) {
                maxProbability = next;
                maxProbabilityIteration = i + 1;
            }

            if (log.isDebugEnabled()) {
                log.debug("Iteration {} complete.  [elapsed={}ms, letterSampling={}ms, temp={}]", (i + 1), (System.currentTimeMillis() - iterationStart), letterSamplingElapsed, String.format("%1$,.4f", temperature));
                cipherSolutionPrinter.print(next);
            }
        }

        long totalElapsed = System.currentTimeMillis() - start;
        log.info("Letter sampling completed in {}ms.  Average={}ms.", totalElapsed, ((double) totalElapsed / (double) i));

        log.info("Best probability found at iteration {}:", maxProbabilityIteration);
        if (log.isInfoEnabled()) {
            cipherSolutionPrinter.print(maxProbability);
        }
        log.info("Mappings for best probability:");

        for (Map.Entry<String, String> entry : maxProbability.getMappings().entrySet()) {
            log.info("{}: {}", entry.getKey(), entry.getValue());
        }

        return maxProbability;
    }

    private CipherSolution runLetterSampler(Double temperature, CipherSolution solution) {
        List<String> mappingList = new ArrayList<>();
        mappingList.addAll(solution.getMappings().keySet());

        String nextKey;

        // For each cipher symbol type, run the letter sampling
        for (int i = 0; i < solution.getMappings().size(); i++) {
            nextKey = iterateRandomly ? mappingList.remove(RANDOM.nextInt(mappingList.size())) : mappingList.get(i);

            String letter = LanguageConstants.LOWERCASE_LETTERS.get(RANDOM.nextInt(LanguageConstants.LOWERCASE_LETTERS_SIZE)).toString();

            String originalMapping = solution.getMappings().get(nextKey);

            if (letter == originalMapping) {
                continue;
            }

            Double originalScore = solution.getScore();
            Double originalIndexOfCoincidence = solution.getIndexOfCoincidence();
            solution.replaceMapping(nextKey, letter);

            String solutionString = solution.asSingleLineString();
            if (plaintextTransformers != null) {
                for (PlaintextTransformer plaintextTransformer : plaintextTransformers) {
                    solutionString = plaintextTransformer.transform(solutionString);
                }
            }

            Map<Integer, Double> logProbabilitiesUpdated = plaintextEvaluator.evaluate(solution, solutionString, nextKey);

            if (!selectNext(temperature, originalScore, solution.getScore())) {
                solution.setIndexOfCoincidence(originalIndexOfCoincidence);
                solution.replaceMapping(nextKey, originalMapping);

                for (Map.Entry<Integer, Double> entry : logProbabilitiesUpdated.entrySet()) {
                    solution.replaceLogProbability(entry.getKey(), entry.getValue());
                }
            }
        }

        return solution;
    }

    private boolean selectNext(Double temperature, Double solutionScore, Double proposalScore) {
        Double acceptanceProbability;

        if (proposalScore.compareTo(solutionScore) >= 0) {
            log.debug("Better solution found");
            return true;
        } else {
            // Need to convert to log probabilities in order for the acceptance probability calculation to be useful
            acceptanceProbability = Math.exp(((solutionScore - proposalScore) / temperature) * -1d);

            log.debug("Acceptance probability: {}", acceptanceProbability);

            if (acceptanceProbability < 0d) {
                throw new IllegalStateException("Acceptance probability was calculated to be less than zero.  Please review the math as this should not happen.");
            }

            if (acceptanceProbability > 1d || RANDOM.nextDouble() < acceptanceProbability.doubleValue()) {
                return true;
            }
        }

        return false;
    }
}
