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
	private LetterNGramDao				maskedNGramDao;
	private boolean						letterNGramsWithSpacesEnabled;
	private boolean						letterNGramsWithoutSpacesEnabled;
	private boolean						maskedNGramsWithSpacesEnabled;
	private boolean						maskedNGramsWithoutSpacesEnabled;

	public void persistNGrams() {
		if (letterNGramsWithSpacesEnabled) {
			persistLetterNGramsWithSpaces();
		}

		if (letterNGramsWithoutSpacesEnabled) {
			persistLetterNGramsWithoutSpaces();
		}

		if (maskedNGramsWithSpacesEnabled) {
			persistMaskedNGramsWithSpaces();
		}

		if (maskedNGramsWithoutSpacesEnabled) {
			persistMaskedNGramsWithoutSpaces();
		}
	}

	protected void persistLetterNGramsWithSpaces() {
		long startDeleteWithSpaces = System.currentTimeMillis();

		log.info("Deleting all existing n-grams with spaces.");

		letterNGramDao.deleteAll(true);

		log.info("Completed deletion of n-grams with spaces in {}ms.", (System.currentTimeMillis()
				- startDeleteWithSpaces));

		MarkovModel markovModelWithSpaces = letterNGramMarkovImporter.importCorpus(false, true);

		long startAddWithSpaces = System.currentTimeMillis();

		log.info("Starting persistence of n-grams with spaces.");

		List<NGramIndexNode> nGramsWithSpaces = transformToList(markovModelWithSpaces.getRootNode());

		letterNGramDao.addAll(nGramsWithSpaces, true);

		log.info("Completed persistence of n-grams with spaces in {}ms.", (System.currentTimeMillis()
				- startAddWithSpaces));

		// Hopefully release these nodes for garbage collection
		nGramsWithSpaces.clear();
	}

	protected void persistLetterNGramsWithoutSpaces() {
		long startDeleteWithoutSpaces = System.currentTimeMillis();

		log.info("Deleting all existing n-grams without spaces.");

		letterNGramDao.deleteAll(false);

		log.info("Completed deletion of n-grams without spaces in {}ms.", (System.currentTimeMillis()
				- startDeleteWithoutSpaces));

		MarkovModel markovModelWithoutSpaces = letterNGramMarkovImporter.importCorpus(false, false);

		long startAddWithoutSpaces = System.currentTimeMillis();

		log.info("Starting persistence of n-grams without spaces.");

		List<NGramIndexNode> nGramsWithoutSpaces = transformToList(markovModelWithoutSpaces.getRootNode());

		letterNGramDao.addAll(nGramsWithoutSpaces, false);

		log.info("Completed persistence of n-grams without spaces in {}ms.", (System.currentTimeMillis()
				- startAddWithoutSpaces));

		// Hopefully release these nodes for garbage collection
		nGramsWithoutSpaces.clear();
	}

	protected void persistMaskedNGramsWithSpaces() {
		long startDeleteWithSpaces = System.currentTimeMillis();

		log.info("Deleting all existing masked n-grams with spaces.");

		maskedNGramDao.deleteAll(true);

		log.info("Completed deletion of masked n-grams with spaces in {}ms.", (System.currentTimeMillis()
				- startDeleteWithSpaces));

		MarkovModel markovModelWithSpaces = letterNGramMarkovImporter.importCorpus(true, true);

		long startAddWithSpaces = System.currentTimeMillis();

		log.info("Starting persistence of masked n-grams with spaces.");

		List<NGramIndexNode> nGramsWithSpaces = transformToList(markovModelWithSpaces.getRootNode());

		maskedNGramDao.addAll(nGramsWithSpaces, true);

		log.info("Completed persistence of masked n-grams with spaces in {}ms.", (System.currentTimeMillis()
				- startAddWithSpaces));

		// Hopefully release these nodes for garbage collection
		nGramsWithSpaces.clear();
	}

	protected void persistMaskedNGramsWithoutSpaces() {
		long startDeleteWithoutSpaces = System.currentTimeMillis();

		log.info("Deleting all existing masked n-grams without spaces.");

		maskedNGramDao.deleteAll(false);

		log.info("Completed deletion of masked n-grams without spaces in {}ms.", (System.currentTimeMillis()
				- startDeleteWithoutSpaces));

		MarkovModel markovModelWithoutSpaces = letterNGramMarkovImporter.importCorpus(true, false);

		long startAddWithoutSpaces = System.currentTimeMillis();

		log.info("Starting persistence of masked n-grams without spaces.");

		List<NGramIndexNode> nGramsWithoutSpaces = transformToList(markovModelWithoutSpaces.getRootNode());

		maskedNGramDao.addAll(nGramsWithoutSpaces, false);

		log.info("Completed persistence of masked n-grams without spaces in {}ms.", (System.currentTimeMillis()
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

	/**
	 * @param maskedNGramDao
	 *            the maskedNGramDao to set
	 */
	public void setMaskedNGramDao(LetterNGramDao maskedNGramDao) {
		this.maskedNGramDao = maskedNGramDao;
	}

	/**
	 * @param letterNGramsWithSpacesEnabled
	 *            the letterNGramsWithSpacesEnabled to set
	 */
	public void setLetterNGramsWithSpacesEnabled(boolean letterNGramsWithSpacesEnabled) {
		this.letterNGramsWithSpacesEnabled = letterNGramsWithSpacesEnabled;
	}

	/**
	 * @param letterNGramsWithoutSpacesEnabled
	 *            the letterNGramsWithoutSpacesEnabled to set
	 */
	public void setLetterNGramsWithoutSpacesEnabled(boolean letterNGramsWithoutSpacesEnabled) {
		this.letterNGramsWithoutSpacesEnabled = letterNGramsWithoutSpacesEnabled;
	}

	/**
	 * @param maskedNGramsWithSpacesEnabled
	 *            the maskedNGramsWithSpacesEnabled to set
	 */
	public void setMaskedNGramsWithSpacesEnabled(boolean maskedNGramsWithSpacesEnabled) {
		this.maskedNGramsWithSpacesEnabled = maskedNGramsWithSpacesEnabled;
	}

	/**
	 * @param maskedNGramsWithoutSpacesEnabled
	 *            the maskedNGramsWithoutSpacesEnabled to set
	 */
	public void setMaskedNGramsWithoutSpacesEnabled(boolean maskedNGramsWithoutSpacesEnabled) {
		this.maskedNGramsWithoutSpacesEnabled = maskedNGramsWithoutSpacesEnabled;
	}
}
