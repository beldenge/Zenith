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

package com.ciphertool.zenith.inference.genetic.dao;

import com.ciphertool.zenith.genetic.dao.GeneDao;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyGene;
import com.ciphertool.zenith.inference.probability.LetterProbability;
import com.ciphertool.zenith.inference.util.LetterUtils;
import com.ciphertool.zenith.math.selection.RouletteSampler;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class CipherKeyGeneDao implements GeneDao {
    private static List<LetterProbability> letterUnigramProbabilities = new ArrayList<>();

    private RouletteSampler<LetterProbability> rouletteSampler = new RouletteSampler<>();

    @Autowired
    private ArrayMarkovModel letterMarkovModel;

    @PostConstruct
    public void init() {
        double probability;
        for (TreeNGram node : letterMarkovModel.getFirstOrderNodes()) {
            probability = ((double) node.getCount()) / letterMarkovModel.getTotalNGramCount();

            letterUnigramProbabilities.add(new LetterProbability(node.getCumulativeString().charAt(0), probability));
        }

        Collections.sort(letterUnigramProbabilities);
        rouletteSampler.reIndex(letterUnigramProbabilities);
    }

    @Override
    public Gene findRandomGene(Chromosome chromosome) {
        return new CipherKeyGene(chromosome, String.valueOf(LetterUtils.getRandomLetter()));
    }

    @Override
    public Gene findProbabilisticGene(Chromosome chromosome) {
        // Pick a plaintext at random according to the language model
        String nextPlaintext = letterUnigramProbabilities.get(rouletteSampler.getNextIndex()).getValue().toString();

        return new CipherKeyGene(chromosome, nextPlaintext);
    }
}