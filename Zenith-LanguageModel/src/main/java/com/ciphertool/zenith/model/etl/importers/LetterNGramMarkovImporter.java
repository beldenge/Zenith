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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.math.MathConstants;
import com.ciphertool.zenith.model.ModelConstants;
import com.ciphertool.zenith.model.dto.ParseResults;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;

@Component
public class LetterNGramMarkovImporter {
	private static Logger		log					= LoggerFactory.getLogger(LetterNGramMarkovImporter.class);

	private static final String	EXTENSION			= ".txt";
	private static final String	NON_ALPHA			= "[^a-zA-Z]";
	private static final String	NON_ALPHA_OR_SPACE	= "[^a-zA-Z ]";

	@Autowired
	private TaskExecutor		taskExecutor;

	@Value("${corpus.output.directory}")
	private String				corpusDirectory;

	@Value("${markov.letter.order}")
	private int					order;

	public TreeMarkovModel importCorpus(boolean includeWordBoundaries) {
		TreeMarkovModel letterMarkovModel = new TreeMarkovModel(this.order);

		long start = System.currentTimeMillis();

		log.info("Starting corpus text import...");

		Path corpusDirectoryPath = Paths.get(this.corpusDirectory);

		if (!Files.exists(corpusDirectoryPath)) {
			try {
				Files.createDirectories(corpusDirectoryPath);
			} catch (IOException ioe) {
				throw new IllegalStateException("Unable to create directory: " + this.corpusDirectory, ioe);
			}
		}

		List<FutureTask<ParseResults>> futures = parseFiles(corpusDirectoryPath, includeWordBoundaries, letterMarkovModel);
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

		smoothMissingTransitions(letterMarkovModel);

		computeConditionalProbabilityAsync(letterMarkovModel);

		letterMarkovModel.normalize(order, total, taskExecutor);

		return letterMarkovModel;
	}

	protected void smoothMissingTransitions(TreeMarkovModel letterMarkovModel) {
		long start = System.currentTimeMillis();

		log.info("Starting smoothing of missing transitions...");

		List<FutureTask<Void>> futures = new ArrayList<>(26);
		FutureTask<Void> task;

		for (Character letter : ModelConstants.LOWERCASE_LETTERS) {
			task = new FutureTask<>(new SmoothTransitionsTask(letterMarkovModel, letterMarkovModel.getRootNode(), "", letter));
			futures.add(task);
			this.taskExecutor.execute(task);
		}

		for (FutureTask<Void> future : futures) {
			try {
				future.get();
			} catch (InterruptedException ie) {
				log.error("Caught InterruptedException while waiting for SmoothTransitionsTask ", ie);
			} catch (ExecutionException ee) {
				log.error("Caught ExecutionException while waiting for SmoothTransitionsTask ", ee);
			}
		}

		log.info("Finished smoothing of missing transitions in {}ms", (System.currentTimeMillis() - start));
	}

	/**
	 * A concurrent task for smoothing out the probability distribution for missing transitions.
	 */
	protected class SmoothTransitionsTask implements Callable<Void> {
		private TreeMarkovModel model;
		private TreeNGram parentNode;
		private String nGram;
		private Character	letter;

		/**
		 * @param model
		 *            the TreeMarkovModel to set
		 * @param parentNode
		 *            the parent TreeNGram to set
		 * @param nGram
		 *            the nGram String to set
		 * @param letter
		 *            the letter to set
		 */
		public SmoothTransitionsTask(TreeMarkovModel model, TreeNGram parentNode, String nGram, Character letter) {
			this.model = model;
			this.parentNode = parentNode;
			this.nGram = nGram;
			this.letter = letter;
		}

		@Override
		public Void call() {
			smoothTransitions(this.model, this.parentNode, this.nGram, this.letter);

			return null;
		}

		protected void smoothTransitions(TreeMarkovModel model, TreeNGram parentNode, String nGram, Character letter) {
			String nextNGram = nGram + letter;
			if (!parentNode.getTransitions().containsKey(letter)) {
				model.addLetterTransition(nextNGram, MathConstants.SINGLE_LETTER_RANDOM_PROBABILITY);
			}

			// If there are zero transitions, then don't add any since they'll all have the same probability which would be a wasteful use of memory
			if (nextNGram.length() < order && !parentNode.getTransitions().get(letter).getTransitions().isEmpty()) {
				for (Character nextLetter : ModelConstants.LOWERCASE_LETTERS) {
					smoothTransitions(model, parentNode.getTransitions().get(letter), nextNGram, nextLetter);
				}
			}
		}
	}

	protected void computeConditionalProbabilityAsync(TreeMarkovModel letterMarkovModel) {
		long start = System.currentTimeMillis();

		log.info("Starting calculation of conditional probabilities...");

		Map<Character, TreeNGram> initialTransitions = letterMarkovModel.getRootNode().getTransitions();

		List<FutureTask<Void>> futures = new ArrayList<FutureTask<Void>>(26);
		FutureTask<Void> task;

		List<TreeNGram> firstOrderNodes = new ArrayList<>(letterMarkovModel.getRootNode().getTransitions().values());

		for (Map.Entry<Character, TreeNGram> entry : initialTransitions.entrySet()) {
			if (entry.getValue() != null) {
				task = new FutureTask<Void>(new ComputeConditionalTask(entry.getValue(), letterMarkovModel.getRootNode()));
				futures.add(task);
				this.taskExecutor.execute(task);
			}
		}

		for (FutureTask<Void> future : futures) {
			try {
				future.get();
			} catch (InterruptedException ie) {
				log.error("Caught InterruptedException while waiting for ComputeConditionalTask ", ie);
			} catch (ExecutionException ee) {
				log.error("Caught ExecutionException while waiting for ComputeConditionalTask ", ee);
			}
		}

		log.info("Finished calculating conditional probabilities in {}ms", (System.currentTimeMillis() - start));
	}

	/**
	 * A concurrent task for computing the conditional probability of a Markov node.
	 */
	protected class ComputeConditionalTask implements Callable<Void> {
		private TreeNGram	node;
		private TreeNGram	parentNode;

		/**
		 * @param node
		 *            the NGramIndexNode to set
		 * @param parentNode
		 *            the parentNode to set
		 */
		public ComputeConditionalTask(TreeNGram node, TreeNGram parentNode) {
			this.node = node;
			this.parentNode = parentNode;
		}

		@Override
		public Void call() {
			computeConditionalProbability(this.node, this.parentNode);

			return null;
		}

		protected void computeConditionalProbability(TreeNGram node, TreeNGram parentNode) {
			Double sum = parentNode.getTransitions().entrySet().stream().map(entry -> entry.getValue().getCount()).reduce(0.0, (a, b) -> a + b);

			node.setConditionalProbability(node.getCount() / sum);

			Map<Character, TreeNGram> transitions = node.getTransitions();

			if (transitions == null || transitions.isEmpty()) {
				return;
			}

			for (Map.Entry<Character, TreeNGram> entry : transitions.entrySet()) {
				computeConditionalProbability(entry.getValue(), node);
			}
		}
	}

	/**
	 * A concurrent task for parsing a file into a Markov model.
	 */
	protected class ParseFileTask implements Callable<ParseResults> {
		private Path			path;
		private boolean			includeWordBoundaries;
		private TreeMarkovModel	letterMarkovModel;

		/**
		 * @param path
		 *            the Path to set
		 * @param includeWordBoundaries
		 *            whether to include word boundaries
		 * @param letterMarkovModel
		 *            the TreeMarkovModel to use
		 */
		public ParseFileTask(Path path, boolean includeWordBoundaries, TreeMarkovModel letterMarkovModel) {
			this.path = path;
			this.includeWordBoundaries = includeWordBoundaries;
			this.letterMarkovModel = letterMarkovModel;
		}

		@Override
		public ParseResults call() {
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
								newSentence.append(ModelConstants.CONNECTED_LETTERS_PLACEHOLDER_CHAR);
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

						unique += (letterMarkovModel.addLetterTransition(nGramString, 1.0) ? 1 : 0);

						total++;
					}
				}
			} catch (IOException ioe) {
				log.error("Unable to parse file: " + this.path.toString(), ioe);
			}

			return new ParseResults(total, unique);
		}
	}

	protected List<FutureTask<ParseResults>> parseFiles(Path path, boolean includeWordBoundaries, TreeMarkovModel letterMarkovModel) {
		List<FutureTask<ParseResults>> tasks = new ArrayList<>();
		FutureTask<ParseResults> task;
		String filename;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					tasks.addAll(parseFiles(entry, includeWordBoundaries, letterMarkovModel));
				} else {
					filename = entry.toString();
					String ext = filename.substring(filename.lastIndexOf('.'));

					if (!ext.equals(EXTENSION)) {
						log.info("Skipping file with unexpected file extension: " + filename);

						continue;
					}

					task = new FutureTask<>(new ParseFileTask(entry, includeWordBoundaries,
							letterMarkovModel));
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
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}
}
