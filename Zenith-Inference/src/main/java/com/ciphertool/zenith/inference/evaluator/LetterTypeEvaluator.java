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

package com.ciphertool.zenith.inference.evaluator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.ciphertool.zenith.inference.dto.EvaluationResults;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.probability.WordProbability;
import com.ciphertool.zenith.math.MathCache;
import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.markov.MarkovModel;
import com.ciphertool.zenith.model.markov.NGramIndexNode;
import com.ciphertool.zenith.model.markov.UnidirectionalNGramIndexNode;

public class LetterTypeEvaluator {
	private Logger		log	= LoggerFactory.getLogger(getClass());

	private MathCache	bigDecimalFunctions;

	private Boolean		includeWordBoundaries;

	public EvaluationResults evaluate(MarkovModel letterMarkovModel, CipherSolution solution, String ciphertextKey) {
		if (ciphertextKey == null) {
			throw new IllegalArgumentException(
					"LetterTypeEvaluator does not support evaluations where the ciphertext key is not specified.");
		}

		BigDecimal interpolatedProbability = BigDecimal.ONE;
		BigDecimal interpolatedLogProbability = BigDecimal.ZERO;

		int order = letterMarkovModel.getOrder();

		long startLetter = System.currentTimeMillis();
		EvaluationResults letterNGramResults = evaluateLetterTypes(letterMarkovModel, solution, order, ciphertextKey);
		log.debug("Letter N-Grams took {}ms.", (System.currentTimeMillis() - startLetter));

		interpolatedProbability = letterNGramResults.getProbability();
		interpolatedLogProbability = letterNGramResults.getLogProbability();

		return new EvaluationResults(interpolatedProbability, interpolatedLogProbability);
	}

	public EvaluationResults evaluateLetterTypes(MarkovModel letterMarkovModel, CipherSolution solution, int order, String ciphertextKey) {
		List<WordProbability> words = transformToWordList(solution);

		if (words == null || words.isEmpty()) {
			throw new IllegalStateException(
					"Unable to evaluate n-grams because the list of words to concatenate is empty.");
		}

		BigDecimal probability = null;
		BigDecimal nGramProbability = BigDecimal.ONE;
		BigDecimal nGramLogProbability = BigDecimal.ZERO;
		NGramIndexNode match = null;

		StringBuilder sb = new StringBuilder();

		if (includeWordBoundaries) {
			sb.append(" ");
		}

		for (WordProbability word : words) {
			sb.append(word.getValue());

			if (includeWordBoundaries) {
				sb.append(" ");
			}
		}

		List<Integer> ciphertextIndices = new ArrayList<>();
		for (int i = 0; i < sb.length(); i++) {
			if (ciphertextKey.equals(solution.getCipher().getCiphertextCharacters().get(i).getValue())) {
				ciphertextIndices.add(i);
			}
		}

		String maskedNGram;
		for (Integer ciphertextIndex : ciphertextIndices) {
			for (int i = Math.max(0, ciphertextIndex - (order - 1)); i < Math.min(sb.length() - order, ciphertextIndex
					+ 1); i++) {
				maskedNGram = "";

				for (int j = i; j < i + order; j++) {
					if (j == ciphertextIndex) {
						maskedNGram += sb.charAt(j);
					} else if (sb.charAt(j) == ' ') {
						maskedNGram += " ";
					} else if (ModelConstants.LOWERCASE_VOWELS.contains(sb.charAt(j))) {
						maskedNGram += ModelConstants.VOWEL_SYMBOL;
					} else {
						maskedNGram += ModelConstants.CONSONANT_SYMBOL;
					}
				}

				match = letterMarkovModel.find(maskedNGram);

				if (match != null && ((UnidirectionalNGramIndexNode) match).getLevel() == order) {
					probability = match.getProbability();
					log.debug("Letter N-Gram Match={}, Probability={}", match.getCumulativeString(), probability);
				} else {
					probability = letterMarkovModel.getUnknownLetterNGramProbability();
					log.debug("No Letter N-Gram Match");
				}

				nGramProbability = nGramProbability.multiply(probability, MathConstants.PREC_10_HALF_UP);
				nGramLogProbability = nGramLogProbability.add(bigDecimalFunctions.log(probability), MathConstants.PREC_10_HALF_UP);
			}
		}

		return new EvaluationResults(nGramProbability, nGramLogProbability);
	}

	protected List<WordProbability> transformToWordList(CipherSolution solution) {
		String currentSolutionString = solution.asSingleLineString().substring(0, solution.getCipher().getCiphertextCharacters().size());

		List<WordProbability> words = new ArrayList<>();
		Integer begin = null;

		for (int i = 0; i < currentSolutionString.length(); i++) {
			if (i < (currentSolutionString.length() - 1) && solution.getWordBoundaries().contains(i)) {
				words.add(new WordProbability(begin, i, currentSolutionString.substring((begin == null ? 0 : begin
						+ 1), i + 1)));

				begin = i;
			}
		}

		words.add(new WordProbability(begin, null, currentSolutionString.substring((begin == null ? 0 : begin
				+ 1), currentSolutionString.length())));

		return words;
	}

	/**
	 * @param bigDecimalFunctions
	 *            the bigDecimalFunctions to set
	 */
	@Required
	public void setBigDecimalFunctions(MathCache bigDecimalFunctions) {
		this.bigDecimalFunctions = bigDecimalFunctions;
	}

	/**
	 * @param includeWordBoundaries
	 *            the includeWordBoundaries to set
	 */
	@Required
	public void setIncludeWordBoundaries(Boolean includeWordBoundaries) {
		this.includeWordBoundaries = includeWordBoundaries;
	}
}