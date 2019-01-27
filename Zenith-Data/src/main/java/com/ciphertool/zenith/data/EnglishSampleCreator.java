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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Validated
@ConfigurationProperties
public class EnglishSampleCreator {
    private static Logger log	= LoggerFactory.getLogger(EnglishSampleCreator.class);

    private static final int TRUE_ENGLISH = 99;

    @NotBlank
    @Value("${task.zodiac408.sourceDirectory}")
    private String					validTrainingTextDirectory;

    @Min(1)
    @Value("${training.sequenceLength}")
    private int sequenceLength = 1;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private FileExporter fileExporter;

    public void createSamples() {
        Path validTrainingTextDirectoryPath = Paths.get(validTrainingTextDirectory);

        if (!Files.isDirectory(validTrainingTextDirectoryPath)) {
            throw new IllegalArgumentException(
                    "Property \"task.zodiac408.sourceDirectory\" must be a directory.");
        }

        long start = System.currentTimeMillis();

        List<CompletableFuture<Void>> futures = parseFiles(validTrainingTextDirectoryPath);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();

        log.info("Finished processing source directory in {}ms.", (System.currentTimeMillis() - start));
    }

    protected List<CompletableFuture<Void>> parseFiles(Path path) {
        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        CompletableFuture<Void> task;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    tasks.addAll(parseFiles(entry));
                } else {
                    task = CompletableFuture.runAsync(() -> fileExporter.parse(TRUE_ENGLISH, entry, sequenceLength), taskExecutor);
                    tasks.add(task);
                }
            }
        } catch (IOException ioe) {
            log.error("Unable to parse files.", ioe);
        }

        return tasks;
    }
}
