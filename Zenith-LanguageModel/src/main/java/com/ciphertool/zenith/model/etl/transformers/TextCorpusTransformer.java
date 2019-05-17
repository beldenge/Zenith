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

package com.ciphertool.zenith.model.etl.transformers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TextCorpusTransformer extends CorpusTransformer {
	private static final String	INPUT_EXT	= ".txt";

	@Value("${corpus.text.input.directory}")
	private String				corpusDirectory;

	@Value("${corpus.output.directory}")
	private String				outputDirectory;

	@Override
	public void transformCorpus() throws ParserConfigurationException {
		long start = System.currentTimeMillis();

		log.info("Starting corpus transformation...");

		List<FutureTask<Long>> futures = parseFiles(INPUT_EXT, Paths.get(this.corpusDirectory));

		long total = 0;

		for (FutureTask<Long> future : futures) {
			try {
				total += future.get();
			} catch (InterruptedException ie) {
				log.error("Caught InterruptedException while waiting for TransformFileTask ", ie);
			} catch (ExecutionException ee) {
				log.error("Caught ExecutionException while waiting for TransformFileTask ", ee);
			}
		}

		log.info("Transformed " + total + " words in " + (System.currentTimeMillis() - start) + "ms");
	}

	@Override
	protected TransformFileTask getTransformFileTask(Path entry) {
		return new TransformFileTask(entry);
	}

	/**
	 * A concurrent task for transforming an XML file to a flat text file.
	 */
	protected class TransformFileTask implements Callable<Long> {
		private Path path;

		/**
		 * @param path
		 *            the Path to set
		 */
		public TransformFileTask(Path path) {
			this.path = path;
		}

		@Override
		public Long call() throws Exception {
			log.debug("Transforming file {}", this.path.toString());

			long wordCount = 0L;
			StringBuilder sb = new StringBuilder();
			int sentenceLength;

			try {
				String content = new String(Files.readAllBytes(this.path));

				String[] sentences = content.replaceAll("\\s+", " ").split("(\\.|\\?|!)+( |\")");

				for (int i = 0; i < sentences.length; i++) {
					sentenceLength = sentences[i].split("\\s+").length;

					if (sentenceLength > 1) {
						sb.append(sentences[i] + "\n");

						wordCount += sentenceLength;
					}
				}
			} catch (IOException ioe) {
				log.error("Unable to parse file: " + this.path.toString(), ioe);
			}

			String relativeFilename = this.path.subpath(Paths.get(corpusDirectory).getNameCount(), this.path.getNameCount()).toString();

			Path parentDir = Paths.get(outputDirectory + "/" + relativeFilename).getParent();

			if (!Files.exists(parentDir)) {
				Files.createDirectories(parentDir);
			}

			String oldFilename = this.path.getFileName().toString();

			Files.write(Paths.get(parentDir + "/" + oldFilename), sb.toString().getBytes());

			return wordCount;
		}
	}
}
