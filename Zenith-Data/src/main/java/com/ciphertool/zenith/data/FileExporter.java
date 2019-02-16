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

package com.ciphertool.zenith.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileExporter {
    private static Logger log						= LoggerFactory.getLogger(FileExporter.class);

    private static final String	NON_ALPHA				= "[^a-zA-Z]";

    @Autowired
    private RecordWriter writer;

    public int parse(Integer markovOrder, Path inputPath, int sampleSize) {
        long start = System.currentTimeMillis();

        List<String> sentencesToAdd = new ArrayList<>();

        try {
            String content = new String(Files.readAllBytes(inputPath));

            String[] sentences = content.split("(\n|\r|\r\n)+");

            for (int i = 0; i < sentences.length; i++) {
                sentences[i] = (sentences[i].replaceAll(NON_ALPHA, "").trim()).toLowerCase();

                if (sentences[i].trim().length() == 0) {
                    continue;
                }

                sentencesToAdd.add(sentences[i]);
            }
        } catch (IOException ioe) {
            log.error("Unable to parse file: " + inputPath.toString(), ioe);
        }

        StringBuilder sample = new StringBuilder();
        int sequencesWritten = 0;

        for (int i = 0; i < sentencesToAdd.size(); i++) {
            sample.append(sentencesToAdd.get(i));

            if (sample.length() >= sampleSize) {
                String paragraph = sample.toString().substring(0, sampleSize);

                try {
                    log.debug("Random sample of order {}: {}", markovOrder, paragraph);

                    writer.write(markovOrder, paragraph);
                    sequencesWritten ++;
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }

                sample.delete(0, sample.length());
            }
        }

        log.info("Completed parsing file {} in {}ms.", inputPath.toString(), (System.currentTimeMillis() - start));
        return sequencesWritten;
    }
}
