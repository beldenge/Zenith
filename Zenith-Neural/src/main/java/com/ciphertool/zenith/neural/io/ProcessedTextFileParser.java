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
import java.nio.file.Files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component
public class ProcessedTextFileParser {
	private static Logger		log						= LoggerFactory.getLogger(ProcessedTextFileParser.class);

	private static final String	NON_ALPHA				= "[^a-zA-Z]";

	@NotBlank
	@Value("${task.zodiac408.samplesFile}")
	private String					samplesFile;

	private Path samplesFilePath;

	@Async
	public Future<Void> parse(Path path, int sampleSize) throws IOException {
		if (samplesFilePath == null) {
			samplesFilePath = Paths.get(samplesFile);
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

		StringBuilder sample = new StringBuilder();

		for (int i = 0; i < sentencesToAdd.size(); i++) {
			sample.append(sentencesToAdd.get(i));

			if (sample.length() >= sampleSize) {
				writeToFile(sample.toString().substring(0, sampleSize));

				sample.delete(0, sample.length());
			}
		}

		log.info("Completed parsing file {} in {}ms.", path.toString(), (System.currentTimeMillis() - start));

		return new AsyncResult<>(null);
	}

	protected synchronized void writeToFile(String s) throws IOException {
		Files.write(samplesFilePath, (s + "\n").getBytes(), StandardOpenOption.APPEND);
	}
}
