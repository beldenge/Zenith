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

package com.ciphertool.zenith.model.markov;

import static org.mockito.Mockito.spy;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.math.sampling.RouletteSampler;
import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.probability.LetterProbability;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;

import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.etl.importers.LetterNGramMarkovImporter;

public class MarkovModelTest {
	private Logger								log		= LoggerFactory.getLogger(getClass());
	private static final int					ORDER	= 5;
	private static final char[] A_TO_Z = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

	private static LetterNGramMarkovImporter	importer;
	private static TreeMarkovModel				model;

	private static final RouletteSampler RANDOM_LETTER_SAMPLER = new RouletteSampler();
	private static final List<LetterProbability> RANDOM_LETTER_PROBABILITIES = new ArrayList<>(ModelConstants.LOWERCASE_LETTERS.size());

	static {
		for (Character letter : ModelConstants.LOWERCASE_LETTERS) {
			RANDOM_LETTER_PROBABILITIES.add(new LetterProbability(letter, MathConstants.SINGLE_LETTER_RANDOM_PROBABILITY));
		}
	}

	private static final BigDecimal RANDOM_LETTER_TOTAL_PROBABILITY = RANDOM_LETTER_SAMPLER.reIndex(RANDOM_LETTER_PROBABILITIES);

	//@BeforeClass
	public static void setUp() {
		ThreadPoolTaskExecutor taskExecutorSpy = spy(new ThreadPoolTaskExecutor());
		taskExecutorSpy.setCorePoolSize(8);
		taskExecutorSpy.setMaxPoolSize(8);
		taskExecutorSpy.setQueueCapacity(100000);
		taskExecutorSpy.setKeepAliveSeconds(1);
		taskExecutorSpy.setAllowCoreThreadTimeOut(true);
		taskExecutorSpy.initialize();

		importer = new LetterNGramMarkovImporter();

		Field corpusDirectoryField = ReflectionUtils.findField(LetterNGramMarkovImporter.class, "corpusDirectory");
		ReflectionUtils.makeAccessible(corpusDirectoryField);
		ReflectionUtils.setField(corpusDirectoryField, importer, "/Users/george/Desktop/zenith-transformed");

		Field taskExecutorField = ReflectionUtils.findField(LetterNGramMarkovImporter.class, "taskExecutor");
		ReflectionUtils.makeAccessible(taskExecutorField);
		ReflectionUtils.setField(taskExecutorField, importer, taskExecutorSpy);

		Field orderField = ReflectionUtils.findField(LetterNGramMarkovImporter.class, "order");
		ReflectionUtils.makeAccessible(orderField);
		ReflectionUtils.setField(orderField, importer, ORDER);

		model = importer.importCorpus(false);
	}

	//@Test
	public void generate() {
		log.info(generateMarkovModelSample(2));
	}

	protected String generateMarkovModelSample(int markovOrder) {
		TreeNGram rootNode = model.getRootNode();
		TreeNGram match;

		StringBuffer sb = new StringBuffer();
		String root = "";
		for (int i = 0; i < 100; i++) {
			match = (root.isEmpty() || markovOrder == 1) ? rootNode : model.findLongest(root);

			LetterProbability chosen = sampleNextTransitionFromDistribution(match, markovOrder);

			char nextSymbol = chosen.getValue();
			sb.append(nextSymbol);

			root = ((root.isEmpty() || root.length() < markovOrder - 1) ? root : root.substring(1)) + nextSymbol;
		}

		return sb.toString();
	}

	protected static LetterProbability sampleNextTransitionFromDistribution(TreeNGram match, int markovOrder) {
        if (match.getTransitions().isEmpty()) {
            return RANDOM_LETTER_PROBABILITIES.get(RANDOM_LETTER_SAMPLER.getNextIndex(RANDOM_LETTER_PROBABILITIES, RANDOM_LETTER_TOTAL_PROBABILITY));
        }

		RouletteSampler sampler = new RouletteSampler();

		List<LetterProbability> probabilities = new ArrayList<>(ModelConstants.LOWERCASE_LETTERS.size());

		for (Map.Entry<Character, TreeNGram> entry : match.getTransitions().entrySet()) {
			LetterProbability probability = new LetterProbability(entry.getKey(), entry.getValue().getConditionalProbability());

			probabilities.add(probability);
		}

		BigDecimal totalProbability = sampler.reIndex(probabilities);

		int nextIndex = sampler.getNextIndex(probabilities, totalProbability);

		return probabilities.get(nextIndex);
	}
}
