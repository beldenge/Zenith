package com.ciphertool.zenith.neural.generate;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component
public class ProcessedTextFileParser {
	private static Logger		log						= LoggerFactory.getLogger(ProcessedTextFileParser.class);

	private static final int	CHAR_TO_NUMERIC_OFFSET	= 9;
	private static final String	NON_ALPHA				= "[^a-zA-Z]";
	private static final String	NON_ALPHA_OR_SPACE		= "[^a-zA-Z ]";

	@Async
	public Future<List<BigDecimal[]>> parse(Path path, int sampleSize) {
		log.debug("Importing file {}", path.toString());

		List<String> sentencesToAdd = new ArrayList<>();

		try {
			String content = new String(Files.readAllBytes(path));

			String[] sentences = content.split("(\n|\r|\r\n)+");

			for (int i = 0; i < sentences.length; i++) {
				sentences[i] = (" " + sentences[i].replaceAll(NON_ALPHA_OR_SPACE, "").replaceAll("\\s+", " ").trim()
						+ " ").toLowerCase();

				sentences[i] = sentences[i].replaceAll(NON_ALPHA, "");

				if (sentences[i].trim().length() == 0) {
					continue;
				}

				sentencesToAdd.add(sentences[i]);
			}
		} catch (IOException ioe) {
			log.error("Unable to parse file: " + path.toString(), ioe);
		}

		List<String> samples = new ArrayList<>();

		for (int i = 0; i < sentencesToAdd.size(); i++) {
			StringBuilder sample = new StringBuilder();

			int j = 0;

			while (sample.length() < sampleSize || (i + j) > sentencesToAdd.size() - 1) {
				sample.append(sentencesToAdd.get(i + j));

				j++;
			}

			if (sample.length() >= sampleSize) {
				samples.add(sample.toString().substring(0, sampleSize));
			}
		}

		List<BigDecimal[]> numericSamples = new ArrayList<>(samples.size());

		for (int i = 0; i < samples.size(); i++) {
			char[] nextSample = samples.get(i).toCharArray();

			BigDecimal[] numericSample = new BigDecimal[sampleSize];

			for (int j = 0; j < nextSample.length; j++) {
				numericSample[j] = charToBigDecimal(nextSample[j]);
			}

			numericSamples.add(numericSample);
		}

		return new AsyncResult<>(numericSamples);
	}

	protected BigDecimal charToBigDecimal(char c) {
		int numericValue = Character.getNumericValue(c) - CHAR_TO_NUMERIC_OFFSET;

		return BigDecimal.valueOf(numericValue);
	}
}
