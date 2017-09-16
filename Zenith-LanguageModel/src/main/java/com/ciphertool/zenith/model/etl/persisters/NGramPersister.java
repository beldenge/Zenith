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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.etl.importers.LetterNGramMarkovImporter;
import com.ciphertool.zenith.model.markov.ListMarkovModel;
import com.ciphertool.zenith.model.markov.MarkovModel;

@Component
public class NGramPersister {
	private Logger						log	= LoggerFactory.getLogger(getClass());

	@Autowired
	private TaskExecutor				taskExecutor;

	@Autowired
	private LetterNGramMarkovImporter	letterNGramMarkovImporter;

	@Autowired
	private LetterNGramDao				letterNGramDao;

	@Autowired
	private LetterNGramDao				maskedNGramDao;

	@Value("${letter.ngrams.with.spaces.enabled}")
	private boolean						letterNGramsWithSpacesEnabled;

	@Value("${letter.ngrams.without.spaces.enabled}")
	private boolean						letterNGramsWithoutSpacesEnabled;

	@Value("${masked.ngrams.with.spaces.enabled}")
	private boolean						maskedNGramsWithSpacesEnabled;

	@Value("${masked.ngrams.without.spaces.enabled}")
	private boolean						maskedNGramsWithoutSpacesEnabled;

	@Value("${mongodb.parallelScan.batchSize}")
	private int							batchSize;

	public void persistNGrams() {
		int order = letterNGramMarkovImporter.getOrder();

		if (letterNGramsWithSpacesEnabled) {
			persistLetterNGrams(order, false, true);
		}

		if (letterNGramsWithoutSpacesEnabled) {
			persistLetterNGrams(order, false, false);
		}

		if (maskedNGramsWithSpacesEnabled) {
			persistLetterNGrams(order, true, true);
		}

		if (maskedNGramsWithoutSpacesEnabled) {
			persistLetterNGrams(order, true, false);
		}
	}

	protected void persistLetterNGrams(int order, boolean maskLetterTypes, boolean includeWordBoundaries) {
		long startDeleteWithoutSpaces = System.currentTimeMillis();

		log.info("Deleting all existing" + (maskLetterTypes ? " masked" : "") + " n-grams with"
				+ (includeWordBoundaries ? "" : "out") + " spaces.");

		(maskLetterTypes ? maskedNGramDao : letterNGramDao).deleteAll(order, includeWordBoundaries);

		log.info("Completed deletion of" + (maskLetterTypes ? " masked" : "") + " n-grams with"
				+ (includeWordBoundaries ? "" : "out") + " spaces in {}ms.", (System.currentTimeMillis()
						- startDeleteWithoutSpaces));

		MarkovModel markovModel = letterNGramMarkovImporter.importCorpus(maskLetterTypes, includeWordBoundaries);

		long count = markovModel.size();

		log.info("Total" + (maskLetterTypes ? " masked" : "") + " nodes with" + (includeWordBoundaries ? "" : "out")
				+ " spaces: {}", count);

		long startAddWithoutSpaces = System.currentTimeMillis();

		log.info("Starting persistence of" + (maskLetterTypes ? " masked" : "") + " n-grams with"
				+ (includeWordBoundaries ? "" : "out") + " spaces.");

		// List<FutureTask<Void>> futures = new ArrayList<FutureTask<Void>>(26);
		// FutureTask<Void> task;
		//
		// for (Map.Entry<Character, TreeNGram> entry : ((TreeMarkovModel)
		// markovModel).getRootNode().getTransitions().entrySet()) {
		// if (entry.getValue() != null) {
		// task = new FutureTask<Void>(new PersistNodesTask(entry.getValue(), maskLetterTypes,
		// includeWordBoundaries));
		// futures.add(task);
		// this.taskExecutor.execute(task);
		// }
		// }
		//
		// for (FutureTask<Void> future : futures) {
		// try {
		// future.get();
		// } catch (InterruptedException ie) {
		// log.error("Caught InterruptedException while waiting for PersistNodesTask ", ie);
		// } catch (ExecutionException ee) {
		// log.error("Caught ExecutionException while waiting for PersistNodesTask ", ee);
		// }
		// }

		AtomicInteger counter = new AtomicInteger();

		((ListMarkovModel) markovModel).getNodeMap().values().stream().collect(Collectors.groupingBy(c -> counter.getAndIncrement()
				/ batchSize)).values().parallelStream().forEach(chunk -> (maskLetterTypes ? maskedNGramDao : letterNGramDao).addAll(order, chunk, includeWordBoundaries));

		log.info("Completed persistence of" + (maskLetterTypes ? " masked" : "") + " n-grams with"
				+ (includeWordBoundaries ? "" : "out") + " spaces in {}ms.", (System.currentTimeMillis()
						- startAddWithoutSpaces));
	}

	// protected List<TreeNGram> persistNodes(TreeNGram node, boolean maskLetterTypes, boolean includeWordBoundaries) {
	// List<TreeNGram> nGrams = new ArrayList<>();
	//
	// nGrams.add(node);
	//
	// for (Map.Entry<Character, TreeNGram> entry : node.getTransitions().entrySet()) {
	// nGrams.addAll(persistNodes(entry.getValue(), maskLetterTypes, includeWordBoundaries));
	//
	// if (nGrams.size() >= batchSize) {
	// (maskLetterTypes ? maskedNGramDao : letterNGramDao).addAll(entry.getValue().getOrder(), nGrams,
	// includeWordBoundaries);
	//
	// nGrams = new ArrayList<>();
	// }
	// }
	//
	// return nGrams;
	// }
	//
	// /**
	// * A concurrent task for computing the conditional probability of a Markov node.
	// */
	// protected class PersistNodesTask implements Callable<Void> {
	// private TreeNGram node;
	// private boolean maskLetterTypes;
	// private boolean includeWordBoundaries;
	//
	// /**
	// * @param node
	// * the root node
	// * @param maskLetterTypes
	// * whether to mask letter types (vowels and consonants)
	// * @param includeWordBoundaries
	// * whether to include word boundaries
	// */
	// public PersistNodesTask(TreeNGram node, boolean maskLetterTypes, boolean includeWordBoundaries) {
	// this.node = node;
	// this.maskLetterTypes = maskLetterTypes;
	// this.includeWordBoundaries = includeWordBoundaries;
	// }
	//
	// @Override
	// public Void call() throws Exception {
	// List<TreeNGram> nGrams = persistNodes(node, maskLetterTypes, includeWordBoundaries);
	//
	// (maskLetterTypes ? maskedNGramDao : letterNGramDao).addAll(node.getOrder(), nGrams, includeWordBoundaries);
	//
	// return null;
	// }
	// }
}
