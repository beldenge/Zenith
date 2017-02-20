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

package com.ciphertool.zenith.model.etl.importers;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.dao.NGramCountSumDao;
import com.ciphertool.zenith.model.dto.ParseResults;
import com.ciphertool.zenith.model.entities.NGramCountSum;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ListMarkovModel;

public class LetterNGramMarkovImporter implements MarkovImporter {
	private static Logger		log					= LoggerFactory.getLogger(LetterNGramMarkovImporter.class);

	private static final String	EXTENSION			= ".txt";
	private static final String	NON_ALPHA			= "[^a-zA-Z]";
	private static final String	NON_ALPHA_OR_SPACE	= "[^a-zA-Z ]";

	private String				corpusDirectory;
	private TaskExecutor		taskExecutor;
	private ListMarkovModel		letterMarkovModel;
	private int					order;
	private NGramCountSumDao	nGramCountSumDao;
	private boolean				computeConditionalProbability;

	@Override
	public ListMarkovModel importCorpus(boolean maskLetterTypes, boolean includeWordBoundaries) {
		letterMarkovModel = new ListMarkovModel(this.order, null);

		long start = System.currentTimeMillis();

		log.info("Starting corpus text import...");

		List<FutureTask<ParseResults>> futures = parseFiles(Paths.get(this.corpusDirectory), maskLetterTypes, includeWordBoundaries);
		ParseResults parseResults;
		long total = 0L;
		long unique = 0L;

		for (FutureTask<ParseResults> future : futures) {
			try {
				parseResults = future.get();
				total += parseResults.getTotal();
				unique += parseResults.getUnique();
			} catch (InterruptedException ie) {
				log.error("Caught InterruptedException while waiting for ParseFileTask ", ie);
			} catch (ExecutionException ee) {
				log.error("Caught ExecutionException while waiting for ParseFileTask ", ee);
			}
		}

		log.info("Imported " + unique + " distinct letter N-Grams out of " + total + " total in "
				+ (System.currentTimeMillis() - start) + "ms");

		if (computeConditionalProbability) {
			// computeConditionalProbability();
		}

		nGramCountSumDao.delete(order, includeWordBoundaries, maskLetterTypes);

		this.letterMarkovModel.normalize(total);

		nGramCountSumDao.add(new NGramCountSum(order, includeWordBoundaries, maskLetterTypes, total));

		return this.letterMarkovModel;
	}

	// public void computeConditionalProbability() {
	// long start = System.currentTimeMillis();
	//
	// log.info("Starting calculation of conditional probabilities...");
	//
	// Map<Character, TreeNGram> initialTransitions = this.letterMarkovModel.getRootNode().getTransitions();
	//
	// List<FutureTask<Void>> futures = new ArrayList<FutureTask<Void>>(26);
	// FutureTask<Void> task;
	//
	// for (Map.Entry<Character, TreeNGram> entry : initialTransitions.entrySet()) {
	// if (entry.getValue() != null) {
	// task = new FutureTask<Void>(new ComputeConditionalTask(entry.getValue(),
	// this.letterMarkovModel.getRootNode().getCount()));
	// futures.add(task);
	// this.taskExecutor.execute(task);
	// }
	// }
	//
	// for (FutureTask<Void> future : futures) {
	// try {
	// future.get();
	// } catch (InterruptedException ie) {
	// log.error("Caught InterruptedException while waiting for NormalizeTask ", ie);
	// } catch (ExecutionException ee) {
	// log.error("Caught ExecutionException while waiting for NormalizeTask ", ee);
	// }
	// }
	//
	// log.info("Finished calculating conditional probabilities in {}ms", (System.currentTimeMillis() - start));
	// }

	/**
	 * A concurrent task for computing the conditional probability of a Markov node.
	 */
	protected class ComputeConditionalTask implements Callable<Void> {
		private TreeNGram	node;
		private long		parentCount;

		/**
		 * @param node
		 *            the NGramIndexNode to set
		 * @param parentCount
		 *            the parentCount to set
		 */
		public ComputeConditionalTask(TreeNGram node, long parentCount) {
			this.node = node;
			this.parentCount = parentCount;
		}

		@Override
		public Void call() throws Exception {
			computeConditionalProbability(this.node, this.parentCount);

			return null;
		}
	}

	protected void computeConditionalProbability(TreeNGram node, long parentCount) {
		node.setConditionalProbability(BigDecimal.valueOf(node.getCount()).divide(BigDecimal.valueOf(parentCount), MathConstants.PREC_10_HALF_UP));

		Map<Character, TreeNGram> transitions = node.getTransitions();

		if (transitions == null || transitions.isEmpty()) {
			return;
		}

		for (Map.Entry<Character, TreeNGram> entry : transitions.entrySet()) {
			computeConditionalProbability(entry.getValue(), node.getCount());
		}
	}

	/**
	 * A concurrent task for parsing a file into a Markov model.
	 */
	protected class ParseFileTask implements Callable<ParseResults> {
		private Path	path;
		private boolean	maskLetterTypes;
		private boolean	includeWordBoundaries;

		/**
		 * @param path
		 *            the Path to set
		 * @param includeWordBoundaries
		 *            whether to mask letter types
		 * @param includeWordBoundaries
		 *            whether to include word boundaries
		 */
		public ParseFileTask(Path path, boolean maskLetterTypes, boolean includeWordBoundaries) {
			this.path = path;
			this.maskLetterTypes = maskLetterTypes;
			this.includeWordBoundaries = includeWordBoundaries;
		}

		@Override
		public ParseResults call() throws Exception {
			log.debug("Importing file {}", this.path.toString());

			int order = letterMarkovModel.getOrder();
			long total = 0;
			long unique = 0;

			try {
				String content = new String(Files.readAllBytes(this.path));
				String sentence;

				String[] sentences = content.split("(\n|\r|\r\n)+");

				for (int i = 0; i < sentences.length; i++) {
					sentence = (" " + sentences[i].replaceAll(NON_ALPHA_OR_SPACE, "").replaceAll("\\s+", " ").trim()
							+ " ").toLowerCase();

					if (!includeWordBoundaries) {
						sentence = sentence.replaceAll(NON_ALPHA, "");
					} else {
						StringBuilder newSentence = new StringBuilder();
						for (int j = 0; j < sentence.length() - 1; j++) {
							newSentence.append(sentence.charAt(j));

							if (sentence.charAt(j) != ' ' && sentence.charAt(j + 1) != ' ') {
								newSentence.append('.');
							}
						}

						newSentence.append(sentence.charAt(sentence.length() - 1));

						sentence = newSentence.toString();
					}

					if (sentence.trim().length() == 0) {
						continue;
					}

					for (int j = 0; j < sentence.length() - order; j++) {
						String nGramString = sentence.substring(j, j + order);

						if (maskLetterTypes) {
							for (int k = 0; k < nGramString.length(); k++) {
								String maskedString = "";

								for (int l = 0; l < nGramString.length(); l++) {
									if (l == k) {
										maskedString += nGramString.charAt(l);
									} else if (nGramString.charAt(l) == ' ') {
										maskedString += " ";
									} else if (ModelConstants.LOWERCASE_VOWELS.contains(nGramString.charAt(l))) {
										maskedString += ModelConstants.VOWEL_SYMBOL;
									} else {
										maskedString += ModelConstants.CONSONANT_SYMBOL;
									}
								}

								unique += (letterMarkovModel.addLetterTransition(maskedString) ? 1 : 0);
							}
						} else {
							unique += (letterMarkovModel.addLetterTransition(nGramString) ? 1 : 0);
						}

						total++;
					}
				}
			} catch (IOException ioe) {
				log.error("Unable to parse file: " + this.path.toString(), ioe);
			}

			return new ParseResults(total, unique);
		}
	}

	protected List<FutureTask<ParseResults>> parseFiles(Path path, boolean maskLetterTypes, boolean includeWordBoundaries) {
		List<FutureTask<ParseResults>> tasks = new ArrayList<FutureTask<ParseResults>>();
		FutureTask<ParseResults> task;
		String filename;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					tasks.addAll(parseFiles(entry, maskLetterTypes, includeWordBoundaries));
				} else {
					filename = entry.toString();
					String ext = filename.substring(filename.lastIndexOf('.'));

					if (!ext.equals(EXTENSION)) {
						log.info("Skipping file with unexpected file extension: " + filename);

						continue;
					}

					task = new FutureTask<ParseResults>(new ParseFileTask(entry, maskLetterTypes,
							includeWordBoundaries));
					tasks.add(task);
					this.taskExecutor.execute(task);
				}
			}
		} catch (IOException ioe) {
			log.error("Unable to parse files due to:" + ioe.getMessage(), ioe);
		}

		return tasks;
	}

	/**
	 * @param taskExecutor
	 *            the taskExecutor to set
	 */
	@Required
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	@Required
	public void setCorpusDirectory(String corpusDirectory) {
		this.corpusDirectory = corpusDirectory;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @param order
	 *            the order to set
	 */
	@Required
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * @param nGramCountSumDao
	 *            the nGramCountSumDao to set
	 */
	@Required
	public void setnGramCountSumDao(NGramCountSumDao nGramCountSumDao) {
		this.nGramCountSumDao = nGramCountSumDao;
	}

	/**
	 * @param computeConditionalProbability
	 *            the computeConditionalProbability to set
	 */
	@Required
	public void setComputeConditionalProbability(boolean computeConditionalProbability) {
		this.computeConditionalProbability = computeConditionalProbability;
	}
}
