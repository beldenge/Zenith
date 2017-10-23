/**
 * Copyright 2017 George Belden
 * 
 * This file is part of Zenith.
 * 
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.neural.io;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

import com.ciphertool.zenith.math.MathConstants;

@Component
public class ProcessedTextFileParser {
	private static Logger		log						= LoggerFactory.getLogger(ProcessedTextFileParser.class);

	private static final int	ALPHABET_SIZE			= 26;
	private static final int	CHAR_TO_NUMERIC_OFFSET	= 9;
	private static final String	NON_ALPHA				= "[^a-zA-Z]";

	@Async
	public Future<List<BigDecimal[]>> parse(Path path, int sampleSize, int stepLimit) {
		if (stepLimit < 1) {
			throw new IllegalArgumentException(
					"A step limit less than one will cause an infinit loop.  Unable to continue.");
		}

		long start = System.currentTimeMillis();

		List<String> sentencesToAdd = new ArrayList<>();

		try {
			String content = new String(Files.readAllBytes(path));

			String[] sentences = content.split("(\n|\r|\r\n)+");

			for (int i = 0; i < sentences.length; i++) {
				sentences[i] = (sentences[i].replaceAll(NON_ALPHA, "").trim()).toLowerCase();

				if (sentences[i].trim().length() == 0) {
					continue;
				}

				sentencesToAdd.add(sentences[i]);
			}
		} catch (IOException ioe) {
			log.error("Unable to parse file: " + path.toString(), ioe);
		}

		List<String> samples = new ArrayList<>();

		StringBuilder sample = new StringBuilder();

		for (int i = 0; i < sentencesToAdd.size(); i++) {
			sample.append(sentencesToAdd.get(i));

			if (sample.length() >= sampleSize) {
				samples.add(sample.toString().substring(0, sampleSize));

				sample.delete(0, sample.length());
			}
		}

		List<BigDecimal[]> numericSamples = new ArrayList<>(samples.size());
		char[] nextSample;

		// TODO: for now we are stepping by fours because it is taking up too much memory otherwise
		for (int i = 0; i < samples.size(); i += stepLimit) {
			nextSample = samples.get(i).toCharArray();

			BigDecimal[] numericSample = new BigDecimal[sampleSize];

			for (int j = 0; j < nextSample.length; j++) {
				numericSample[j] = charToBigDecimal(nextSample[j]).divide(BigDecimal.valueOf(ALPHABET_SIZE), MathConstants.PREC_10_HALF_UP).setScale(10, RoundingMode.UP);
			}

			numericSamples.add(numericSample);
		}

		log.info("Completed parsing file {} in {}ms.", path.toString(), (System.currentTimeMillis() - start));

		return new AsyncResult<>(numericSamples);
	}

	protected BigDecimal charToBigDecimal(char c) {
		int numericValue = Character.getNumericValue(c) - CHAR_TO_NUMERIC_OFFSET;

		return BigDecimal.valueOf(numericValue);
	}
}
