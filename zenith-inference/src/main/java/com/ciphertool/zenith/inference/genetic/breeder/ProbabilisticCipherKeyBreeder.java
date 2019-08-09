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

package com.ciphertool.zenith.inference.genetic.breeder;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyGene;
import com.ciphertool.zenith.inference.probability.LetterProbability;
import com.ciphertool.zenith.math.selection.RouletteSampler;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(value = "decipherment.optimizer", havingValue = "GeneticAlgorithmSolutionOptimizer")
public class ProbabilisticCipherKeyBreeder extends AbstractCipherKeyBreeder {
    private Logger log = LoggerFactory.getLogger(getClass());

    private static List<LetterProbability> letterUnigramProbabilities = new ArrayList<>();

    private RouletteSampler<LetterProbability> rouletteSampler = new RouletteSampler<>();

    @Autowired
    private TreeMarkovModel letterMarkovModel;

    @PostConstruct
    public void init() {
        super.init();

        Double total = 0d;
        for (Map.Entry<Character, TreeNGram> entry : letterMarkovModel.getRootNode().getTransitions().entrySet()) {
            if (!entry.getKey().equals(' ')) {
                total += entry.getValue().getCount();
            }
        }

        Double probability;
        for (Map.Entry<Character, TreeNGram> entry : letterMarkovModel.getRootNode().getTransitions().entrySet()) {
            if (!entry.getKey().equals(' ')) {
                probability = ((double) entry.getValue().getCount()) / total;

                letterUnigramProbabilities.add(new LetterProbability(entry.getKey(), probability));
            }
        }

        Collections.sort(letterUnigramProbabilities);
        rouletteSampler.reIndex(letterUnigramProbabilities);
    }

    @Override
    public Chromosome breed() {
        CipherKeyChromosome chromosome = new CipherKeyChromosome(cipher, keys.length);

        for (String ciphertext : keys) {
            // Pick a plaintext at random according to the language model
            String nextPlaintext = letterUnigramProbabilities.get(rouletteSampler.getNextIndex()).getValue().toString();

            CipherKeyGene newGene = new CipherKeyGene(chromosome, nextPlaintext);
            chromosome.putGene(ciphertext, newGene);
        }

        log.debug(chromosome.toString());

        return chromosome;
    }
}
