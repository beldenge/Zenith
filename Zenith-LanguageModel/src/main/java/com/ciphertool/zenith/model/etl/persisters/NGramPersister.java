/**
 * Copyright 2017 George Belden
 * 
 * This file is part of Zenith.
 * 
 * Zenith is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Zenith is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.model.etl.persisters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.etl.importers.LetterNGramMarkovImporter;
import com.ciphertool.zenith.model.markov.MarkovModel;
import com.ciphertool.zenith.model.markov.NGramIndexNode;

public class NGramPersister {
	private Logger						log	= LoggerFactory.getLogger(getClass());

	private LetterNGramMarkovImporter	letterNGramMarkovImporter;
	private LetterNGramDao				letterNGramDao;

	public void persistNGrams() {
		long startDeleteWithSpaces = System.currentTimeMillis();

		log.info("Deleting all existing n-grams with spaces.");

		letterNGramDao.deleteAll(true);

		log.info("Completed deletion of n-grams with spaces in {}ms.", (System.currentTimeMillis()
				- startDeleteWithSpaces));

		MarkovModel markovModelWithSpaces = letterNGramMarkovImporter.importCorpus(true);

		long startAddWithSpaces = System.currentTimeMillis();

		log.info("Starting persistence of n-grams with spaces.");

		List<NGramIndexNode> nGramsWithSpaces = transformToList(markovModelWithSpaces.getRootNode());

		letterNGramDao.addAll(nGramsWithSpaces, true);

		log.info("Completed persistence of n-grams with spaces in {}ms.", (System.currentTimeMillis()
				- startAddWithSpaces));

		// Hopefully release these nodes for garbage collection
		nGramsWithSpaces.clear();

		long startDeleteWithoutSpaces = System.currentTimeMillis();

		log.info("Deleting all existing n-grams without spaces.");

		letterNGramDao.deleteAll(false);

		log.info("Completed deletion of n-grams without spaces in {}ms.", (System.currentTimeMillis()
				- startDeleteWithoutSpaces));

		MarkovModel markovModelWithoutSpaces = letterNGramMarkovImporter.importCorpus(false);

		long startAddWithoutSpaces = System.currentTimeMillis();

		log.info("Starting persistence of n-grams without spaces.");

		List<NGramIndexNode> nGramsWithoutSpaces = transformToList(markovModelWithoutSpaces.getRootNode());

		letterNGramDao.addAll(nGramsWithoutSpaces, false);

		log.info("Completed persistence of n-grams without spaces in {}ms.", (System.currentTimeMillis()
				- startAddWithoutSpaces));

		// Hopefully release these nodes for garbage collection
		nGramsWithoutSpaces.clear();
	}

	protected List<NGramIndexNode> transformToList(NGramIndexNode node) {
		List<NGramIndexNode> nGrams = new ArrayList<>();

		nGrams.add(node);

		for (Map.Entry<Character, NGramIndexNode> entry : node.getTransitions().entrySet()) {
			nGrams.addAll(transformToList(entry.getValue()));
		}

		return nGrams;
	}

	/**
	 * @param letterNGramMarkovImporter
	 *            the letterNGramMarkovImporter to set
	 */
	@Required
	public void setLetterNGramMarkovImporter(LetterNGramMarkovImporter letterNGramMarkovImporter) {
		this.letterNGramMarkovImporter = letterNGramMarkovImporter;
	}

	/**
	 * @param letterNGramDao
	 *            the letterNGramDao to set
	 */
	@Required
	public void setLetterNGramDao(LetterNGramDao letterNGramDao) {
		this.letterNGramDao = letterNGramDao;
	}
}
