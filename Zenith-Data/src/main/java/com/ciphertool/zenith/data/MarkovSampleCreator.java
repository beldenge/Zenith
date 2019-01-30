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

package com.ciphertool.zenith.data;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.math.sampling.RouletteSampler;
import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;
import com.ciphertool.zenith.model.probability.LetterProbability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Validated
@ConfigurationProperties
public class MarkovSampleCreator implements SampleCreator {
    private static Logger log	= LoggerFactory.getLogger(MarkovSampleCreator.class);

    private static final int NUM_LETTERS = ModelConstants.LOWERCASE_LETTERS.size();

    private static final RouletteSampler RANDOM_LETTER_SAMPLER = new RouletteSampler();
    private static final List<LetterProbability> RANDOM_LETTER_PROBABILITIES = new ArrayList<>(NUM_LETTERS);

    static {
        for (Character letter : ModelConstants.LOWERCASE_LETTERS) {
            RANDOM_LETTER_PROBABILITIES.add(new LetterProbability(letter, MathConstants.SINGLE_LETTER_RANDOM_PROBABILITY));
        }
    }

    private static final Float RANDOM_LETTER_TOTAL_PROBABILITY = RANDOM_LETTER_SAMPLER.reIndex(RANDOM_LETTER_PROBABILITIES);

    @Min(1)
    @Value("${task.markovOrder:1}")
    private int markovOrder;

    @Min(1)
    @Value("${task.samplesToCreate}")
    private Integer samplesToCreate;

    @Min(1)
    @Value("${training.sequenceLength}")
    private int sequenceLength;

    @Autowired
    private LetterNGramDao letterNGramDao;

    @Autowired
    private RecordWriter writer;

    private TreeMarkovModel letterMarkovModel;

    @PostConstruct
    public void init() {
        if (markovOrder > 0) {
            letterMarkovModel = new TreeMarkovModel(markovOrder);

            List<TreeNGram> nodes = letterNGramDao.findAll(1, false);

            // TODO: try parallel stream here
            nodes.stream().forEach(letterMarkovModel::addNode);
        }
    }

    @Override
    public void createSamples() {
        for (int i = 0; i < samplesToCreate; i ++) {
            for (int j = 0; j < markovOrder; j ++) {
                generateMarkovModelSample(j + 1);
            }
        }
    }

    protected void generateMarkovModelSample(int markovOrder) {
        TreeNGram rootNode = letterMarkovModel.getRootNode();
        TreeNGram match;

        StringBuffer sb = new StringBuffer();

        String root = "";
        for (int i = 0; i < sequenceLength; i++) {
            match = (root.isEmpty() || markovOrder == 1) ? rootNode : letterMarkovModel.findLongest(root);

            LetterProbability chosen = sampleNextTransitionFromDistribution(match);

            char nextSymbol = chosen.getValue();

            sb.append(nextSymbol);

            root = ((root.isEmpty() || root.length() < markovOrder - 1) ? root : root.substring(1)) + nextSymbol;
        }

        log.debug("Random sample of order {}: {}", markovOrder, sb.toString());

        try {
            writer.write(markovOrder, sb.toString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static LetterProbability sampleNextTransitionFromDistribution(TreeNGram match) {
        if (match.getTransitions().isEmpty()) {
            return RANDOM_LETTER_PROBABILITIES.get(RANDOM_LETTER_SAMPLER.getNextIndex(RANDOM_LETTER_PROBABILITIES, RANDOM_LETTER_TOTAL_PROBABILITY));
        }

        RouletteSampler sampler = new RouletteSampler();

        List<LetterProbability> probabilities = new ArrayList<>(NUM_LETTERS);

        for (Map.Entry<Character, TreeNGram> entry : match.getTransitions().entrySet()) {
            LetterProbability probability = new LetterProbability(entry.getKey(), entry.getValue().getConditionalProbability());

            probabilities.add(probability);
        }

        Float totalProbability = sampler.reIndex(probabilities);

        int nextIndex = sampler.getNextIndex(probabilities, totalProbability);

        return probabilities.get(nextIndex);
    }
}
