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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Validated
@ConfigurationProperties
public class RecordWriter {
    private static final String OUTPUT_FILE_PREFIX = "sequences-";
    private static final String OUTPUT_FILE_EXTENSION = ".csv";

    @NotBlank
    @Value("${task.outputFileDirectory}")
    private String					outputFileDirectory;

    @NotNull
    @Min(1)
    @Value("${task.maxRecordsPerFile}")
    private Integer maxRecordsPerFile;

    private Path outputDirectory;

    private AtomicInteger recordsWritten = new AtomicInteger(0);

    private String currentFileName = OUTPUT_FILE_PREFIX + "0" + OUTPUT_FILE_EXTENSION;

    @PostConstruct
    public void init() throws IOException {
        outputDirectory = Paths.get(outputFileDirectory);

        if (Files.exists(outputDirectory)) {
            throw new IllegalStateException("Output directory " + outputFileDirectory + " already exists, and the default behavior is to append.  Please move or delete this directory and retry.");
        }

        Files.createDirectory(outputDirectory);

        createNextFile();
    }

    public synchronized void write(@NotNull Integer markovOrder, @NotBlank String sequence) throws IOException {
        String record = markovOrder.toString() + "," + sequence + System.lineSeparator();

        Files.write(Paths.get(outputDirectory.toString(), currentFileName), record.getBytes(), StandardOpenOption.APPEND);

        recordsWritten.incrementAndGet();

        if (recordsWritten.get() % maxRecordsPerFile == 0) {
            createNextFile();
        }
    }

    private void createNextFile() throws IOException {
        currentFileName = OUTPUT_FILE_PREFIX + recordsWritten.get() + OUTPUT_FILE_EXTENSION;

        Path nextFile = Paths.get(outputDirectory.toString(), currentFileName);

        if (Files.exists(nextFile)) {
            throw new IllegalStateException("Output file " + nextFile.toString() + " already exists, and the default behavior is to append.  Please move or delete this file and retry.");
        }

        Files.createFile(nextFile);
    }
}
