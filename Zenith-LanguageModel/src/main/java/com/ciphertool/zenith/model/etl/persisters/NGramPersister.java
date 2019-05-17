/**
 * Copyright 2017-2019 George Belden
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.etl.importers.LetterNGramMarkovImporter;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;

@Component
public class NGramPersister {
	private Logger						log	= LoggerFactory.getLogger(getClass());

	@Autowired
	private TaskExecutor				taskExecutor;

	@Autowired
	private LetterNGramMarkovImporter	letterNGramMarkovImporter;

	@Autowired
	private LetterNGramDao				letterNGramDao;

	@Value("${letter.ngrams.with.spaces.enabled}")
	private boolean						letterNGramsWithSpacesEnabled;

	@Value("${letter.ngrams.without.spaces.enabled}")
	private boolean						letterNGramsWithoutSpacesEnabled;

	@Value("${mongodb.parallelScan.batchSize}")
	private int							batchSize;

	public void persistNGrams() {
		int order = letterNGramMarkovImporter.getOrder();

		if (letterNGramsWithSpacesEnabled) {
			persistLetterNGrams(order, true);
		}

		if (letterNGramsWithoutSpacesEnabled) {
			persistLetterNGrams(order, false);
		}
	}

	protected void persistLetterNGrams(int order, boolean includeWordBoundaries) {
		long startDeleteWithoutSpaces = System.currentTimeMillis();

		log.info("Deleting all existing n-grams with" + (includeWordBoundaries ? "" : "out") + " spaces.");

		letterNGramDao.deleteAll(includeWordBoundaries);

		log.info("Completed deletion of n-grams with" + (includeWordBoundaries ? "" : "out")
				+ " spaces in {}ms.", (System.currentTimeMillis() - startDeleteWithoutSpaces));

		TreeMarkovModel markovModel = letterNGramMarkovImporter.importCorpus(includeWordBoundaries);

		long count = markovModel.size();

		log.info("Total nodes with" + (includeWordBoundaries ? "" : "out") + " spaces: {}", count);

		long startAdd = System.currentTimeMillis();

		log.info("Starting persistence of n-grams with" + (includeWordBoundaries ? "" : "out") + " spaces.");

		List<FutureTask<Void>> futures = new ArrayList<FutureTask<Void>>(26);
		FutureTask<Void> task;

		for (Map.Entry<Character, TreeNGram> entry : ((TreeMarkovModel) markovModel).getRootNode().getTransitions().entrySet()) {
			if (entry.getValue() != null) {
				task = new FutureTask<Void>(new PersistNodesTask(entry.getValue(), includeWordBoundaries));
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

		log.info("Completed persistence of n-grams with" + (includeWordBoundaries ? "" : "out")
				+ " spaces in {}ms.", (System.currentTimeMillis() - startAdd));
	}

	protected List<TreeNGram> persistNodes(TreeNGram node, boolean includeWordBoundaries) {
		List<TreeNGram> nGrams = new ArrayList<>();

		nGrams.add(node);

		for (Map.Entry<Character, TreeNGram> entry : node.getTransitions().entrySet()) {
			nGrams.addAll(persistNodes(entry.getValue(), includeWordBoundaries));

			if (nGrams.size() >= batchSize) {
				letterNGramDao.addAll(nGrams, includeWordBoundaries);

				nGrams = new ArrayList<>();
			}
		}

		return nGrams;
	}

	/**
	 * A concurrent task for computing the conditional probability of a Markov node.
	 */
	protected class PersistNodesTask implements Callable<Void> {
		private TreeNGram	node;
		private boolean		includeWordBoundaries;

		/**
		 * @param node
		 *            the root node
		 * @param includeWordBoundaries
		 *            whether to include word boundaries
		 */
		public PersistNodesTask(TreeNGram node, boolean includeWordBoundaries) {
			this.node = node;
			this.includeWordBoundaries = includeWordBoundaries;
		}

		@Override
		public Void call() throws Exception {
			List<TreeNGram> nGrams = persistNodes(node, includeWordBoundaries);

			letterNGramDao.addAll(nGrams, includeWordBoundaries);

			return null;
		}
	}
}
