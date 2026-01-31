/*
 * Copyright 2017-2026 George Belden
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

package com.ciphertool.zenith.model.etl.transformers;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Component
public class BlogAuthorshipCorpusTransformer extends CorpusTransformer {
    private static final String INPUT_EXT = ".xml";
    private static final String OUTPUT_EXT = ".txt";

    private static final XmlMapper XML_MAPPER = new XmlMapper();

    @Value("${corpus.blog.input.directory}")
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

                Blog value = XML_MAPPER.readValue(content, Blog.class);

                List<String> posts = value.getEntries().stream()
                        .map(Entry::getPost)
                        .collect(Collectors.toList());

                for (String post : posts) {
                    String cleanedPost = post.replaceAll("urlLink", "");
                    String sentence = cleanedPost.replaceAll("\\s+", " ");
                    sentenceLength = sentence.split("\\s+").length;

                    if (sentenceLength > 1) {
                        sb.append(sentence + "\n");

                        wordCount += sentenceLength;
                    }
                }
            } catch (IOException ioe) {
                log.error("Unable to parse file: " + this.path.toString(), ioe);
            }

            String relativeFilename = this.path.subpath(Paths.get(corpusInputDirectory).getNameCount(), this.path.getNameCount()).toString();

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

    @NoArgsConstructor
    @Getter
    public static class Blog {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "entry")
        private List<Entry> entries;
    }

    @NoArgsConstructor
    @Getter
    public static class Entry {
        private String date;

        @JacksonXmlCData
        private String post;
    }
}
