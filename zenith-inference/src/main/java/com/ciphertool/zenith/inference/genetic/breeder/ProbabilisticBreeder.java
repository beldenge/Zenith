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

 import com.ciphertool.zenith.genetic.Breeder;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyGene;
import com.ciphertool.zenith.inference.probability.LetterProbability;
import com.ciphertool.zenith.inference.selection.RouletteSampler;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProbabilisticBreeder implements Breeder {
	private Logger								log							= LoggerFactory.getLogger(getClass());

	private static List<LetterProbability>		letterUnigramProbabilities	= new ArrayList<>();
	private TreeMarkovModel letterMarkovModel;
	private Cipher cipher;
	private RouletteSampler<LetterProbability> rouletteSampler				= new RouletteSampler<>();
	private Double							totalProbability;
	private static final String[]				KEYS						= { "a", "anchor", "b", "backc", "backd",
			"backe", "backf", "backj", "backk", "backl", "backp", "backq", "backr", "backslash", "box", "boxdot",
			"carrot", "circledot", "d", "e", "f", "flipt", "forslash", "fullbox", "fullcircle", "fulltri", "g", "h",
			"horstrike", "i", "j", "k", "l", "lrbox", "m", "n", "o", "p", "pi", "plus", "q", "r", "s", "t", "tri",
			"tridot", "u", "v", "vertstrike", "w", "x", "y", "z", "zodiac" };

	@PostConstruct
	public void init() {
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
		totalProbability = rouletteSampler.reIndex(letterUnigramProbabilities);
	}

	@Override
	public Chromosome breed() {
		CipherKeyChromosome chromosome = new CipherKeyChromosome(cipher, KEYS.length);

		for (String ciphertext : KEYS) {
			// Pick a plaintext at random according to the language model
			String nextPlaintext = letterUnigramProbabilities.get(rouletteSampler.getNextIndex(letterUnigramProbabilities, totalProbability)).getValue().toString();

			CipherKeyGene newGene = new CipherKeyGene(chromosome, nextPlaintext);
			chromosome.putGene(ciphertext, newGene);
		}

		if (log.isDebugEnabled()) {
			log.debug(chromosome.toString());
		}

		return chromosome;
	}

	@Override
	public void setGeneticStructure(Object obj) {
		this.cipher = (Cipher) obj;
	}

	/**
	 * @param letterMarkovModel
	 *            the letterMarkovModel to set
	 */
	@Required
	public void setLetterMarkovModel(TreeMarkovModel letterMarkovModel) {
		this.letterMarkovModel = letterMarkovModel;
	}
}
