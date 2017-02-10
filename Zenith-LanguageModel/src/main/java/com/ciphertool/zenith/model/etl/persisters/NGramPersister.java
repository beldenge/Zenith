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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.task.TaskExecutor;

import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.NGramIndexNode;
import com.ciphertool.zenith.model.etl.importers.LetterNGramMarkovImporter;
import com.ciphertool.zenith.model.markov.MarkovModel;

public class NGramPersister {
	private Logger						log	= LoggerFactory.getLogger(getClass());

	private LetterNGramMarkovImporter	letterNGramMarkovImporter;
	private LetterNGramDao				letterNGramDao;
	private LetterNGramDao				maskedNGramDao;
	private boolean						letterNGramsWithSpacesEnabled;
	private boolean						letterNGramsWithoutSpacesEnabled;
	private boolean						maskedNGramsWithSpacesEnabled;
	private boolean						maskedNGramsWithoutSpacesEnabled;
	private int							batchSize;
	private TaskExecutor				taskExecutor;

	public void persistNGrams() {
		if (letterNGramsWithSpacesEnabled) {
			persistLetterNGrams(false, true);
		}

		if (letterNGramsWithoutSpacesEnabled) {
			persistLetterNGrams(false, false);
		}

		if (maskedNGramsWithSpacesEnabled) {
			persistLetterNGrams(true, true);
		}

		if (maskedNGramsWithoutSpacesEnabled) {
			persistLetterNGrams(true, false);
		}
	}

	protected void persistLetterNGrams(boolean maskLetterTypes, boolean includeWordBoundaries) {
		long startDeleteWithoutSpaces = System.currentTimeMillis();

		log.info("Deleting all existing" + (maskLetterTypes ? " masked" : "") + " n-grams with"
				+ (includeWordBoundaries ? "" : "out") + " spaces.");

		(maskLetterTypes ? maskedNGramDao : letterNGramDao).deleteAll(includeWordBoundaries);

		log.info("Completed deletion of" + (maskLetterTypes ? " masked" : "") + " n-grams with"
				+ (includeWordBoundaries ? "" : "out") + " spaces in {}ms.", (System.currentTimeMillis()
						- startDeleteWithoutSpaces));

		MarkovModel markovModelWithoutSpaces = letterNGramMarkovImporter.importCorpus(maskLetterTypes, includeWordBoundaries);

		long count = countAll(markovModelWithoutSpaces.getRootNode());

		log.info("Total" + (maskLetterTypes ? " masked" : "") + " nodes with" + (includeWordBoundaries ? "" : "out")
				+ " spaces: {}", count);

		long startAddWithoutSpaces = System.currentTimeMillis();

		log.info("Starting persistence of" + (maskLetterTypes ? " masked" : "") + " n-grams with"
				+ (includeWordBoundaries ? "" : "out") + " spaces.");

		List<FutureTask<Void>> futures = new ArrayList<FutureTask<Void>>(26);
		FutureTask<Void> task;

		for (Map.Entry<Character, NGramIndexNode> entry : markovModelWithoutSpaces.getRootNode().getTransitions().entrySet()) {
			if (entry.getValue() != null) {
				task = new FutureTask<Void>(new PersistNodesTask(entry.getValue(), maskLetterTypes,
						includeWordBoundaries));
				futures.add(task);
				this.taskExecutor.execute(task);
			}
		}

		for (FutureTask<Void> future : futures) {
			try {
				future.get();
			} catch (InterruptedException ie) {
				log.error("Caught InterruptedException while waiting for PersistNodesTask ", ie);
			} catch (ExecutionException ee) {
				log.error("Caught ExecutionException while waiting for PersistNodesTask ", ee);
			}
		}

		// Don't forget to persist the root node itself
		List<NGramIndexNode> nGrams = new ArrayList<>();
		nGrams.add(markovModelWithoutSpaces.getRootNode());
		(maskLetterTypes ? maskedNGramDao : letterNGramDao).addAll(nGrams, includeWordBoundaries);

		log.info("Completed persistence of" + (maskLetterTypes ? " masked" : "") + " n-grams with"
				+ (includeWordBoundaries ? "" : "out") + " spaces in {}ms.", (System.currentTimeMillis()
						- startAddWithoutSpaces));
	}

	protected long countAll(NGramIndexNode node) {
		long sum = 1L;

		for (Map.Entry<Character, NGramIndexNode> entry : node.getTransitions().entrySet()) {
			sum += countAll(entry.getValue());
		}

		return sum;
	}

	protected List<NGramIndexNode> persistNodes(NGramIndexNode node, boolean maskLetterTypes, boolean includeWordBoundaries) {
		List<NGramIndexNode> nGrams = new ArrayList<>();

		nGrams.add(node);

		for (Map.Entry<Character, NGramIndexNode> entry : node.getTransitions().entrySet()) {
			nGrams.addAll(persistNodes(entry.getValue(), maskLetterTypes, includeWordBoundaries));

			if (nGrams.size() >= batchSize) {
				(maskLetterTypes ? maskedNGramDao : letterNGramDao).addAll(nGrams, includeWordBoundaries);

				nGrams = new ArrayList<>();
			}
		}

		return nGrams;
	}

	/**
	 * A concurrent task for computing the conditional probability of a Markov node.
	 */
	protected class PersistNodesTask implements Callable<Void> {
		private NGramIndexNode	node;
		private boolean			maskLetterTypes;
		private boolean			includeWordBoundaries;

		/**
		 * @param node
		 *            the root node
		 * @param maskLetterTypes
		 *            whether to mask letter types (vowels and consonants)
		 * @param includeWordBoundaries
		 *            whether to include word boundaries
		 */
		public PersistNodesTask(NGramIndexNode node, boolean maskLetterTypes, boolean includeWordBoundaries) {
			this.node = node;
			this.maskLetterTypes = maskLetterTypes;
			this.includeWordBoundaries = includeWordBoundaries;
		}

		@Override
		public Void call() throws Exception {
			List<NGramIndexNode> nGrams = persistNodes(node, maskLetterTypes, includeWordBoundaries);

			(maskLetterTypes ? maskedNGramDao : letterNGramDao).addAll(nGrams, includeWordBoundaries);

			return null;
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

	/**
	 * @param taskExecutor
	 *            the taskExecutor to set
	 */
	@Required
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
}
