/*
 * Copyright 2017-2026 George Belden
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

package com.ciphertool.zenith.inference.genetic.breeder;

import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyGene;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class BiasedCipherKeyBreeder extends AbstractCipherKeyBreeder {
    private Logger log = LoggerFactory.getLogger(getClass());

    private static ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private char[] biasedLetterBucket;

    @Autowired
    private ArrayMarkovModel letterMarkovModel;

    @Override
    public void init(Cipher cipher, List<PlaintextTransformationStep> plaintextTransformationSteps, PlaintextEvaluator plaintextEvaluator) {
        super.init(cipher, plaintextTransformationSteps, plaintextEvaluator);

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
    public Genome breed(Population population) {
        Genome genome = new Genome(true, null, population);
        CipherKeyChromosome chromosome = new CipherKeyChromosome(genome, cipher, keys.length);

        for (String ciphertext : keys) {
            // Pick a plaintext at random according to the language model
            String nextPlaintext = String.valueOf(biasedLetterBucket[RANDOM.nextInt(biasedLetterBucket.length)]);

            chromosome.putGene(ciphertext, new CipherKeyGene(chromosome, nextPlaintext));
        }

        log.debug(chromosome.toString());

        genome.addChromosome(chromosome);

        return genome;
    }
}
