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
import java.util.HashMap;
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
import com.ciphertool.zenith.model.dto.ParseResults;
import com.ciphertool.zenith.model.markov.MarkovModel;
import com.ciphertool.zenith.model.markov.NGramIndexNode;
import com.ciphertool.zenith.model.markov.UnidirectionalMarkovModel;
import com.ciphertool.zenith.model.markov.UnidirectionalNGramIndexNode;

public class LetterNGramMarkovImporter implements MarkovImporter {
	private static Logger		log					= LoggerFactory.getLogger(LetterNGramMarkovImporter.class);

	private static final String	EXTENSION			= ".txt";
	private static final String	NON_ALPHA			= "[^a-zA-Z]";
	private static final String	NON_ALPHA_OR_SPACE	= "[^a-zA-Z ]";

	private String				corpusDirectory;
	private TaskExecutor		taskExecutor;
	private MarkovModel			letterMarkovModel;
	private int					order;

	@Override
	public MarkovModel importCorpus(boolean maskLetterTypes, boolean includeWordBoundaries) {
		letterMarkovModel = new UnidirectionalMarkovModel(this.order);

		long start = System.currentTimeMillis();

		log.info("Starting corpus text import...");

		List<FutureTask<ParseResults>> futures = parseFiles(Paths.get(this.corpusDirectory), maskLetterTypes, includeWordBoundaries);
		ParseResults parseResults;
		long total = 0L;
		long unique = 0L;
		Map<Integer, Long> levelTotals = new HashMap<>();

		for (int i = 1; i <= order; i++) {
			levelTotals.put(i, 0L);
		}

		for (FutureTask<ParseResults> future : futures) {
			try {
				parseResults = future.get();
				total += parseResults.getTotal();
				unique += parseResults.getUnique();

				for (int i = 1; i <= order; i++) {
					levelTotals.put(i, levelTotals.get(i) + parseResults.getLevelTotals().get(i));
				}
			} catch (InterruptedException ie) {
				log.error("Caught InterruptedException while waiting for ParseFileTask ", ie);
			} catch (ExecutionException ee) {
				log.error("Caught ExecutionException while waiting for ParseFileTask ", ee);
			}
		}

		log.info("Imported " + unique + " distinct letter N-Grams out of " + total + " total in "
				+ (System.currentTimeMillis() - start) + "ms");

		((UnidirectionalNGramIndexNode) this.letterMarkovModel.getRootNode()).setCount(total);

		postProcess(true, false, includeWordBoundaries);

		for (int i = 1; i <= order; i++) {
			normalize(this.letterMarkovModel, i, levelTotals.get(i));
		}

		for (Map.Entry<Character, NGramIndexNode> entry : this.letterMarkovModel.getRootNode().getTransitions().entrySet()) {
			log.info(entry.getKey().toString() + ": "
					+ entry.getValue().getProbability().toString().substring(0, Math.min(7, entry.getValue().getProbability().toString().length())));
		}

		return this.letterMarkovModel;
	}

	public void postProcess(boolean computeConditionalProbability, boolean linkChildren, boolean includeWordBoundaries) {
		long start = System.currentTimeMillis();

		log.info("Starting Markov model post-processing...");

		Map<Character, NGramIndexNode> initialTransitions = this.letterMarkovModel.getRootNode().getTransitions();

		List<FutureTask<Void>> futures = new ArrayList<FutureTask<Void>>(26);
		FutureTask<Void> task;

		if (computeConditionalProbability) {
			for (Map.Entry<Character, NGramIndexNode> entry : initialTransitions.entrySet()) {
				if (entry.getValue() != null) {
					task = new FutureTask<Void>(new ComputeConditionalTask(entry.getValue(),
							this.letterMarkovModel.getRootNode().getCount()));
					futures.add(task);
					this.taskExecutor.execute(task);
				}
			}

			for (FutureTask<Void> future : futures) {
				try {
					future.get();
				} catch (InterruptedException ie) {
					log.error("Caught InterruptedException while waiting for NormalizeTask ", ie);
				} catch (ExecutionException ee) {
					log.error("Caught ExecutionException while waiting for NormalizeTask ", ee);
				}
			}
		}

		if (linkChildren) {
			futures = new ArrayList<FutureTask<Void>>(26);

			for (Map.Entry<Character, NGramIndexNode> entry : initialTransitions.entrySet()) {
				if (entry.getValue() != null) {
					task = new FutureTask<Void>(new LinkChildTask(entry.getKey(), entry.getValue(),
							includeWordBoundaries));
					futures.add(task);
					this.taskExecutor.execute(task);
				}
			}

			for (FutureTask<Void> future : futures) {
				try {
					future.get();
				} catch (InterruptedException ie) {
					log.error("Caught InterruptedException while waiting for LinkChildTask ", ie);
				} catch (ExecutionException ee) {
					log.error("Caught ExecutionException while waiting for LinkChildTask ", ee);
				}
			}
		}

		log.info("Time elapsed: " + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * A concurrent task for computing the conditional probability of a Markov node.
	 */
	protected class ComputeConditionalTask implements Callable<Void> {
		private NGramIndexNode	node;
		private long			parentCount;

		/**
		 * @param node
		 *            the NGramIndexNode to set
		 * @param parentCount
		 *            the parentCount to set
		 */
		public ComputeConditionalTask(NGramIndexNode node, long parentCount) {
			this.node = node;
			this.parentCount = parentCount;
		}

		@Override
		public Void call() throws Exception {
			computeConditionalProbability(this.node, this.parentCount);

			return null;
		}
	}

	protected void computeConditionalProbability(NGramIndexNode node, long parentCount) {
		((UnidirectionalNGramIndexNode) node).setConditionalProbability(BigDecimal.valueOf(node.getCount()).divide(BigDecimal.valueOf(parentCount), MathConstants.PREC_10_HALF_UP));

		Map<Character, NGramIndexNode> transitions = node.getTransitions();

		if (transitions == null || transitions.isEmpty()) {
			return;
		}

		for (Map.Entry<Character, NGramIndexNode> entry : transitions.entrySet()) {
			computeConditionalProbability(entry.getValue(), node.getCount());
		}
	}

	protected void linkChild(NGramIndexNode node, String nGram, boolean includeWordBoundaries) {
		Map<Character, NGramIndexNode> transitions = node.getTransitions();

		if (nGram.length() == order) {
			for (Character letter : (includeWordBoundaries ? ModelConstants.LOWERCASE_LETTERS_AND_SPACE : ModelConstants.LOWERCASE_LETTERS)) {
				NGramIndexNode match = this.letterMarkovModel.find(nGram.substring(1) + letter.toString());

				if (match != null) {
					node.putChild(letter, match);
				}
			}

			return;
		}

		for (Map.Entry<Character, NGramIndexNode> entry : transitions.entrySet()) {
			NGramIndexNode nextNode = entry.getValue();

			if (nextNode != null) {
				linkChild(nextNode, nGram + String.valueOf(entry.getKey()), includeWordBoundaries);
			}
		}
	}

	/**
	 * A concurrent task for linking leaf nodes in a Markov model.
	 */
	protected class LinkChildTask implements Callable<Void> {
		private Character		key;
		private NGramIndexNode	node;
		private boolean			includeWordBoundaries;

		/**
		 * @param key
		 *            the Character key to set
		 * @param node
		 *            the NGramIndexNode to set
		 * @param includeWordBoundaries
		 *            whether to include word boundaries
		 */
		public LinkChildTask(Character key, NGramIndexNode node, boolean includeWordBoundaries) {
			this.key = key;
			this.node = node;
			this.includeWordBoundaries = includeWordBoundaries;
		}

		@Override
		public Void call() throws Exception {
			linkChild(this.node, String.valueOf(this.key), includeWordBoundaries);

			return null;
		}
	}

	protected void normalize(MarkovModel markovModel, int level, long orderTotal) {
		List<FutureTask<Void>> futures = new ArrayList<FutureTask<Void>>(26);
		FutureTask<Void> task;

		for (Map.Entry<Character, NGramIndexNode> entry : markovModel.getRootNode().getTransitions().entrySet()) {
			if (entry.getValue() != null) {
				task = new FutureTask<Void>(new NormalizeTask(entry.getValue(), level, orderTotal));
				futures.add(task);
				this.taskExecutor.execute(task);
			}
		}

		for (FutureTask<Void> future : futures) {
			try {
				future.get();
			} catch (InterruptedException ie) {
				log.error("Caught InterruptedException while waiting for NormalizeTask ", ie);
			} catch (ExecutionException ee) {
				log.error("Caught ExecutionException while waiting for NormalizeTask ", ee);
			}
		}
	}

	/**
	 * A concurrent task for normalizing a Markov model node.
	 */
	protected class NormalizeTask implements Callable<Void> {
		private NGramIndexNode	node;
		private int				level;
		private long			total;

		/**
		 * @param node
		 *            the NGramIndexNode to set
		 * @param level
		 *            the level to set
		 * @param total
		 *            the total to set
		 */
		public NormalizeTask(NGramIndexNode node, int level, long total) {
			this.node = node;
			this.level = level;
			this.total = total;
		}

		@Override
		public Void call() throws Exception {
			normalizeTerminal(this.node, this.level, this.total);

			return null;
		}
	}

	protected void normalizeTerminal(NGramIndexNode node, int level, long total) {
		if (((UnidirectionalNGramIndexNode) node).getLevel() == level) {
			((UnidirectionalNGramIndexNode) node).setProbability(BigDecimal.valueOf(node.getCount()).divide(BigDecimal.valueOf(total), MathConstants.PREC_10_HALF_UP));

			return;
		}

		Map<Character, NGramIndexNode> transitions = node.getTransitions();

		if (transitions == null || transitions.isEmpty()) {
			return;
		}

		for (Map.Entry<Character, NGramIndexNode> entry : transitions.entrySet()) {
			normalizeTerminal(entry.getValue(), level, total);
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
			Map<Integer, Long> levelTotals = new HashMap<>();
			long unique = 0;

			for (int k = 1; k <= order; k++) {
				levelTotals.put(k, 0L);
			}

			try {
				String content = new String(Files.readAllBytes(this.path));
				String sentence;

				String[] sentences = content.split("(\n|\r|\r\n)+");

				for (int i = 0; i < sentences.length; i++) {
					sentence = (" " + sentences[i].replaceAll(NON_ALPHA_OR_SPACE, "").replaceAll("\\s+", " ").trim()
							+ " ").toLowerCase();

					if (!includeWordBoundaries) {
						sentence = sentence.replaceAll(NON_ALPHA, "");
					}

					if (sentence.trim().length() == 0) {
						continue;
					}

					for (int j = 0; j < sentence.length(); j++) {
						String nGramString = sentence.substring(j, j + Math.min(order, sentence.length() - j));

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

								unique += (letterMarkovModel.addNGram(maskedString) ? 1 : 0);
							}
						} else {
							unique += (letterMarkovModel.addNGram(nGramString) ? 1 : 0);
						}

						total++;

						for (int k = 1; k <= nGramString.length(); k++) {
							levelTotals.put(k, levelTotals.get(k) + 1);
						}
					}
				}
			} catch (IOException ioe) {
				log.error("Unable to parse file: " + this.path.toString(), ioe);
			}

			return new ParseResults(total, levelTotals, unique);
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
	 * @param order
	 *            the order to set
	 */
	@Required
	public void setOrder(int order) {
		this.order = order;
	}
}
