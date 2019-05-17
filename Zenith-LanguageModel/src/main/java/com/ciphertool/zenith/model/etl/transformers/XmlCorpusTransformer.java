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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ciphertool.zenith.model.etl.converters.NumberToWordsConverter;

@Component
public class XmlCorpusTransformer extends CorpusTransformer {
	private static final String		INPUT_EXT			= ".xml";
	private static final String		OUTPUT_EXT			= ".txt";
	private static final String		HEAD_TAG			= "head";
	private static final String		ITEM_TAG			= "item";
	private static final String		LABEL_TAG			= "label";
	private static final String		SENTENCE_TAG		= "s";
	private static final String		PUNC_TAG			= "c";
	private static final String		TYPE_ATTR			= "c5";
	private static final String		PUNC_ATTR_VALUE		= "PUN";
	private static final String		NUM_ATTR_VALUE		= "CRD";
	private static final String		PERCENT_ATTR_VALUE	= "UNC";
	private static final String		DOLLAR_ATTR_VALUE	= "NN0";
	private static final String		WORD_TAG			= "w";

	private static final String		NUMERIC				= "[0-9]+";
	private static final Pattern	NUMBER_PATTERN		= Pattern.compile(NUMERIC);

	private static final String		RANGE				= "[0-9]+–[0-9]+";
	private static final Pattern	RANGE_PATTERN		= Pattern.compile(RANGE);

	private static final String		PERCENT				= "[0-9]+%";
	private static final Pattern	PERCENT_PATTERN		= Pattern.compile(PERCENT);

	private static final String		DOLLAR				= "(\\$|£)[0-9]+";
	private static final Pattern	DOLLAR_PATTERN		= Pattern.compile(DOLLAR);

	@Value("${corpus.xml.input.directory}")
	private String					corpusDirectory;

	@Value("${corpus.output.directory}")
	private String					outputDirectory;

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
			StringBuilder sentenceSb;

			try {
				DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = docBuilder.parse(new File(this.path.toString()));
				doc.getDocumentElement().normalize();

				NodeList sentences = doc.getElementsByTagName(SENTENCE_TAG);
				NodeList wordsAndPunc;
				Node item;
				int number;
				String parentNodeName;

				for (int i = 0; i < sentences.getLength(); i++) {
					parentNodeName = sentences.item(i).getParentNode().getNodeName();

					if (HEAD_TAG.equals(parentNodeName) || ITEM_TAG.equals(parentNodeName)
							|| LABEL_TAG.equals(parentNodeName)) {
						continue;
					}

					wordsAndPunc = sentences.item(i).getChildNodes();
					sentenceSb = new StringBuilder();

					for (int j = 0; j < wordsAndPunc.getLength(); j++) {
						item = wordsAndPunc.item(j);

						if (PUNC_TAG.equals(item.getNodeName())
								&& PUNC_ATTR_VALUE.equals(item.getAttributes().getNamedItem(TYPE_ATTR).getTextContent())) {
							sentenceSb.append(" ");
						} else if (WORD_TAG.equals(item.getNodeName())
								&& NUM_ATTR_VALUE.equals(item.getAttributes().getNamedItem(TYPE_ATTR).getTextContent())
								&& RANGE_PATTERN.matcher(item.getTextContent().replace(",", "").trim()).matches()) {
							try {
								String[] numberStrings = item.getTextContent().split("–");

								for (int k = 0; k < numberStrings.length; k++) {
									number = Integer.parseInt(numberStrings[k].replace(",", "").trim());

									if (k > 0) {
										sentenceSb.append("to ");
									}

									sentenceSb.append(NumberToWordsConverter.convert(number) + " ");
								}
							} catch (NumberFormatException nfe) {
								log.debug("Unable to format number as integer: {}", item.getTextContent().replace(",", "").trim());
							}
						} else if (WORD_TAG.equals(item.getNodeName())
								&& PERCENT_ATTR_VALUE.equals(item.getAttributes().getNamedItem(TYPE_ATTR).getTextContent())
								&& PERCENT_PATTERN.matcher(item.getTextContent().replace(",", "").trim()).matches()) {
							try {
								/*
								 * If the number cannot be reduced to an integer, then it's not worth converting into
								 * words
								 */
								number = Integer.parseInt(item.getTextContent().replace(",", "").trim().substring(0, item.getTextContent().replace(",", "").trim().length()
										- 1));

								sentenceSb.append(NumberToWordsConverter.convert(number) + " percent ");
							} catch (NumberFormatException nfe) {
								log.debug("Unable to format number as integer: {}", item.getTextContent().replace(",", "").trim());
							}
						} else if (WORD_TAG.equals(item.getNodeName())
								&& DOLLAR_ATTR_VALUE.equals(item.getAttributes().getNamedItem(TYPE_ATTR).getTextContent())
								&& DOLLAR_PATTERN.matcher(item.getTextContent().replace(",", "").trim()).matches()) {
							try {
								/*
								 * If the number cannot be reduced to an integer, then it's not worth converting into
								 * words
								 */
								number = Integer.parseInt(item.getTextContent().replace(",", "").trim().substring(1));

								sentenceSb.append(NumberToWordsConverter.convert(number) + " dollars ");
							} catch (NumberFormatException nfe) {
								log.debug("Unable to format number as integer: {}", item.getTextContent().replace(",", "").trim());
							}
						} else if (WORD_TAG.equals(item.getNodeName())
								&& NUM_ATTR_VALUE.equals(item.getAttributes().getNamedItem(TYPE_ATTR).getTextContent())
								&& NUMBER_PATTERN.matcher(item.getTextContent().replace(",", "").trim()).matches()) {
							try {
								/*
								 * If the number cannot be reduced to an integer, then it's not worth converting into
								 * words
								 */
								number = Integer.parseInt(item.getTextContent().replace(",", "").trim());

								sentenceSb.append(NumberToWordsConverter.convert(number) + " ");
							} catch (NumberFormatException nfe) {
								log.debug("Unable to format number as integer: {}", item.getTextContent().replace(",", "").trim());
							}
						} else {
							sentenceSb.append(item.getTextContent().replace("'", "").replace("’", "").replace("‘", "").replace("-", " "));
						}

						if (WORD_TAG.equals(item.getNodeName())) {
							wordCount++;
						}
					}

					if (sentenceSb.toString().split("\\s+").length > 1) {
						sb.append(sentenceSb.toString() + "\n");
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
			String newFilename = oldFilename.substring(0, oldFilename.lastIndexOf(".")) + OUTPUT_EXT;

			Files.write(Paths.get(parentDir + "/" + newFilename), sb.toString().getBytes());

			return wordCount;
		}
	}
}
