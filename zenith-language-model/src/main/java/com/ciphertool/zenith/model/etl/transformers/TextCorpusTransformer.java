/**
 * Copyright 2017-2020 George Belden
 * <p>
 * This file is part of Zenith.
 * <p>
 * Zenith is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * Zenith is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.model.etl.transformers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Component
public class TextCorpusTransformer extends CorpusTransformer {
    private static final String INPUT_EXT = ".txt";

    @Value("${corpus.text.input.directory}")
    private String corpusInputDirectory;

    @Override
    protected String getInputFileExtension() {
        return INPUT_EXT;
    }

    @Override
    protected String getCorpusInputDirectory() {
        return corpusInputDirectory;
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
                log.error("Unable to parse file: {}", this.path.toString(), ioe);
            }

            String relativeFilename = this.path.subpath(Paths.get(corpusInputDirectory).getNameCount(), this.path.getNameCount()).toString();

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
