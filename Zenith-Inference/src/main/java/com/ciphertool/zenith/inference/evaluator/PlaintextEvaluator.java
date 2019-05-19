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

package com.ciphertool.zenith.inference.evaluator;

import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlaintextEvaluator {
	private Logger		log	= LoggerFactory.getLogger(getClass());

	public void evaluate(TreeMarkovModel letterMarkovModel, CipherSolution solution, String ciphertextKey) {
		long startLetter = System.currentTimeMillis();

		evaluateLetterNGrams(letterMarkovModel, solution, ciphertextKey);

		log.debug("Letter N-Grams took {}ms.", (System.currentTimeMillis() - startLetter));
	}

	public void evaluateLetterNGrams(TreeMarkovModel letterMarkovModel, CipherSolution solution, String ciphertextKey) {
		int order = letterMarkovModel.getOrder();

		Double probability;

		StringBuilder sb = new StringBuilder();
		sb.append(solution.asSingleLineString());

		if (ciphertextKey != null) {
			List<Integer> ciphertextIndices = new ArrayList<>();
			for (int i = 0; i < solution.getCipher().getCiphertextCharacters().size(); i++) {
				if (ciphertextKey.equals(solution.getCipher().getCiphertextCharacters().get(i).getValue())) {
					ciphertextIndices.add(i);
				}
			}

			Integer lastIndex = null;
			for (Integer ciphertextIndex : ciphertextIndices) {
				int start = Math.max(0, ciphertextIndex - (order - 1));
				int end = Math.min(sb.length() - order, ciphertextIndex + 1);

				if (lastIndex != null) {
					start = Math.max(start, lastIndex);
				}

				for (int i = start; i < end; i++) {
					probability = computeNGramProbability(letterMarkovModel, sb.substring(i, i + order));

					solution.replaceLogProbability(i, Math.log(probability));
				}

				lastIndex = end;
			}
		} else {
			for (int i = 0; i < sb.length() - order; i++) {
				probability = computeNGramProbability(letterMarkovModel, sb.substring(i, i + order));

				solution.addLogProbability(Math.log(probability));
			}
		}
	}

	private Double computeNGramProbability(TreeMarkovModel letterMarkovModel, String ngram) {
		Double probability;
		TreeNGram match = letterMarkovModel.findExact(ngram);

		if (match != null) {
			probability = match.getProbability();
			log.debug("Letter N-Gram Match={}, Probability={}", match.getCumulativeString(), probability);
		} else {
			probability = letterMarkovModel.getUnknownLetterNGramProbability();
			log.debug("No Letter N-Gram Match");
		}

		return probability;
	}
}