/**
 * Copyright 2017-2019 George Belden
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public abstract class CorpusTransformer {
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Value("${corpus.output.directory}")
    protected String outputDirectory;

    @Autowired
    protected TaskExecutor taskExecutor;

    public void transformCorpus() {
        long start = System.currentTimeMillis();

        log.info("Starting corpus transformation...");

        List<FutureTask<Long>> futures = parseFiles(getInputFileExtension(), Paths.get(getCorpusInputDirectory()));

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

        log.info("Transformed {} words in {}ms.", total, (System.currentTimeMillis() - start));
    }

    protected abstract String getInputFileExtension();
    protected abstract String getCorpusInputDirectory();
    protected abstract Callable<Long> getTransformFileTask(Path entry);

    protected List<FutureTask<Long>> parseFiles(String inputExt, Path path) {
        List<FutureTask<Long>> tasks = new ArrayList<>();
        FutureTask<Long> task;
        String filename;

        if (!Files.exists(path)) {
            log.info("Skipping directory as it does not exist: {}", path.toString());
            return Collections.emptyList();
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    tasks.addAll(parseFiles(inputExt, entry));
                } else {
                    filename = entry.toString();
                    String ext = filename.substring(filename.lastIndexOf('.'));

                    if (!ext.equals(inputExt)) {
                        log.info("Skipping file with unexpected file extension: {}", filename);

                        continue;
                    }

                    task = new FutureTask<>(getTransformFileTask(entry));
                    tasks.add(task);
                    this.taskExecutor.execute(task);
                }
            }
        } catch (IOException ioe) {
            log.error("Unable to parse files due to: {}", ioe.getMessage(), ioe);
        }

        return tasks;
    }
}
