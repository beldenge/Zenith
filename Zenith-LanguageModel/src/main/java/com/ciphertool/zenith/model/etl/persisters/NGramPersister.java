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
import com.ciphertool.zenith.model.entities.NGramIndexNode;
import com.ciphertool.zenith.model.etl.importers.LetterNGramMarkovImporter;
import com.ciphertool.zenith.model.markov.MarkovModel;

public class NGramPersister {
	private Logger						log		= LoggerFactory.getLogger(getClass());

	private LetterNGramMarkovImporter	letterNGramMarkovImporter;
	private LetterNGramDao				letterNGramDao;
	private LetterNGramDao				maskedNGramDao;
	private boolean						letterNGramsWithSpacesEnabled;
	private boolean						letterNGramsWithoutSpacesEnabled;
	private boolean						maskedNGramsWithSpacesEnabled;
	private boolean						maskedNGramsWithoutSpacesEnabled;
	private int							batchSize;

	private List<NGramIndexNode>		nGrams	= new ArrayList<>();

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

		long count = countAll(markovModelWithSpaces.getRootNode());

		log.info("Total nodes with spaces: {}", count);

		long startAddWithSpaces = System.currentTimeMillis();

		log.info("Starting persistence of n-grams with spaces.");

		persistNodes(markovModelWithSpaces.getRootNode(), true);

		letterNGramDao.addAll(nGrams, true);

		nGrams = new ArrayList<>();

		log.info("Completed persistence of n-grams with spaces in {}ms.", (System.currentTimeMillis()
				- startAddWithSpaces));
	}

	protected void persistLetterNGramsWithoutSpaces() {
		long startDeleteWithoutSpaces = System.currentTimeMillis();

		log.info("Deleting all existing n-grams without spaces.");

		letterNGramDao.deleteAll(false);

		log.info("Completed deletion of n-grams without spaces in {}ms.", (System.currentTimeMillis()
				- startDeleteWithoutSpaces));

		MarkovModel markovModelWithoutSpaces = letterNGramMarkovImporter.importCorpus(false, false);

		long count = countAll(markovModelWithoutSpaces.getRootNode());

		log.info("Total nodes without spaces: {}", count);

		long startAddWithoutSpaces = System.currentTimeMillis();

		log.info("Starting persistence of n-grams without spaces.");

		persistNodes(markovModelWithoutSpaces.getRootNode(), false);

		letterNGramDao.addAll(nGrams, false);

		nGrams = new ArrayList<>();

		log.info("Completed persistence of n-grams without spaces in {}ms.", (System.currentTimeMillis()
				- startAddWithoutSpaces));
	}

	protected void persistMaskedNGramsWithSpaces() {
		long startDeleteWithSpaces = System.currentTimeMillis();

		log.info("Deleting all existing masked n-grams with spaces.");

		maskedNGramDao.deleteAll(true);

		log.info("Completed deletion of masked n-grams with spaces in {}ms.", (System.currentTimeMillis()
				- startDeleteWithSpaces));

		MarkovModel markovModelWithSpaces = letterNGramMarkovImporter.importCorpus(true, true);

		long count = countAll(markovModelWithSpaces.getRootNode());

		log.info("Total masked nodes with spaces: {}", count);

		long startAddWithSpaces = System.currentTimeMillis();

		log.info("Starting persistence of masked n-grams with spaces.");

		persistNodes(markovModelWithSpaces.getRootNode(), true);

		maskedNGramDao.addAll(nGrams, true);

		nGrams = new ArrayList<>();

		log.info("Completed persistence of masked n-grams with spaces in {}ms.", (System.currentTimeMillis()
				- startAddWithSpaces));
	}

	protected void persistMaskedNGramsWithoutSpaces() {
		long startDeleteWithoutSpaces = System.currentTimeMillis();

		log.info("Deleting all existing masked n-grams without spaces.");

		maskedNGramDao.deleteAll(false);

		log.info("Completed deletion of masked n-grams without spaces in {}ms.", (System.currentTimeMillis()
				- startDeleteWithoutSpaces));

		MarkovModel markovModelWithoutSpaces = letterNGramMarkovImporter.importCorpus(true, false);

		long count = countAll(markovModelWithoutSpaces.getRootNode());

		log.info("Total masked nodes without spaces: {}", count);

		long startAddWithoutSpaces = System.currentTimeMillis();

		log.info("Starting persistence of masked n-grams without spaces.");

		persistNodes(markovModelWithoutSpaces.getRootNode(), false);

		maskedNGramDao.addAll(nGrams, false);

		nGrams = new ArrayList<>();

		log.info("Completed persistence of masked n-grams without spaces in {}ms.", (System.currentTimeMillis()
				- startAddWithoutSpaces));
	}

	protected long countAll(NGramIndexNode node) {
		long sum = 1L;

		for (Map.Entry<Character, NGramIndexNode> entry : node.getTransitions().entrySet()) {
			sum += countAll(entry.getValue());
		}

		return sum;
	}

	protected void persistNodes(NGramIndexNode node, boolean includeWordBoundaries) {
		nGrams.add(node);

		if (nGrams.size() >= batchSize) {
			letterNGramDao.addAll(nGrams, includeWordBoundaries);

			nGrams = new ArrayList<>();
		}

		for (Map.Entry<Character, NGramIndexNode> entry : node.getTransitions().entrySet()) {
			persistNodes(entry.getValue(), includeWordBoundaries);
		}
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
	@Required
	public void setMaskedNGramDao(LetterNGramDao maskedNGramDao) {
		this.maskedNGramDao = maskedNGramDao;
	}

	/**
	 * @param letterNGramsWithSpacesEnabled
	 *            the letterNGramsWithSpacesEnabled to set
	 */
	@Required
	public void setLetterNGramsWithSpacesEnabled(boolean letterNGramsWithSpacesEnabled) {
		this.letterNGramsWithSpacesEnabled = letterNGramsWithSpacesEnabled;
	}

	/**
	 * @param letterNGramsWithoutSpacesEnabled
	 *            the letterNGramsWithoutSpacesEnabled to set
	 */
	@Required
	public void setLetterNGramsWithoutSpacesEnabled(boolean letterNGramsWithoutSpacesEnabled) {
		this.letterNGramsWithoutSpacesEnabled = letterNGramsWithoutSpacesEnabled;
	}

	/**
	 * @param maskedNGramsWithSpacesEnabled
	 *            the maskedNGramsWithSpacesEnabled to set
	 */
	@Required
	public void setMaskedNGramsWithSpacesEnabled(boolean maskedNGramsWithSpacesEnabled) {
		this.maskedNGramsWithSpacesEnabled = maskedNGramsWithSpacesEnabled;
	}

	/**
	 * @param maskedNGramsWithoutSpacesEnabled
	 *            the maskedNGramsWithoutSpacesEnabled to set
	 */
	@Required
	public void setMaskedNGramsWithoutSpacesEnabled(boolean maskedNGramsWithoutSpacesEnabled) {
		this.maskedNGramsWithoutSpacesEnabled = maskedNGramsWithoutSpacesEnabled;
	}

	/**
	 * @param batchSize
	 *            the batchSize to set
	 */
	@Required
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
}
