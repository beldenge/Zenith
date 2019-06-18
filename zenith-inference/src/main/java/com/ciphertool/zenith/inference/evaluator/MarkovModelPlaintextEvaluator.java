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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(value="decipherment.evaluator.plaintext", havingValue = "MarkovModelPlaintextEvaluator")
public class MarkovModelPlaintextEvaluator implements PlaintextEvaluator {
	private Logger		log	= LoggerFactory.getLogger(getClass());

	@Autowired
	private TreeMarkovModel letterMarkovModel;

	@Override
	public void evaluate(CipherSolution solution, String ciphertextKey) {
		long startLetter = System.currentTimeMillis();

		evaluateLetterNGrams(solution, ciphertextKey);

		log.debug("Letter N-Grams took {}ms.", (System.currentTimeMillis() - startLetter));
	}

	protected void evaluateLetterNGrams(CipherSolution solution, String ciphertextKey) {
		int order = letterMarkovModel.getOrder();

		Double logProbability;

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
					logProbability = computeNGramLogProbability(sb.substring(i, i + order));

					solution.replaceLogProbability(i, logProbability);
				}

				lastIndex = end;
			}
		} else {
			solution.clearLogProbabilities();

			for (int i = 0; i < sb.length() - order; i++) {
				logProbability = computeNGramLogProbability(sb.substring(i, i + order));

				solution.addLogProbability(logProbability);
			}
		}
	}

	protected Double computeNGramLogProbability(String ngram) {
		Double logProbability;
		TreeNGram match = letterMarkovModel.findExact(ngram);

		if (match != null) {
			logProbability = match.getLogProbability();
			log.debug("Letter N-Gram Match={}, Probability={}", match.getCumulativeString(), logProbability);
		} else {
			logProbability = letterMarkovModel.getUnknownLetterNGramLogProbability();
			log.debug("No Letter N-Gram Match");
		}

		return logProbability;
	}
}