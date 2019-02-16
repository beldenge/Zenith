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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Lazy
@Component
@Validated
@ConfigurationProperties
public class RecordWriter {
    @NotBlank
    @Value("${task.outputFileDirectory}")
    private String					outputFileDirectory;

    @NotNull
    @Min(1)
    @Value("${task.maxRecordsPerFile}")
    private Integer maxRecordsPerFile;

    private Path outputDirectory;

    private Map<Integer, FileWritingContext> fileContextPerOrder = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws IOException {
        outputDirectory = Paths.get(outputFileDirectory);

        // Files.createDirectory() throws FileAlreadyExistsException if the directory already exists
        Files.createDirectory(outputDirectory);
    }

    public synchronized void write(@NotNull Integer markovOrder, @NotBlank String sequence) throws IOException {
        if (!fileContextPerOrder.containsKey(markovOrder)) {
            fileContextPerOrder.put(markovOrder, new FileWritingContext(new AtomicInteger(0)));
        }

        if (fileContextPerOrder.get(markovOrder).getRecordsWritten().get() % maxRecordsPerFile == 0) {
            createNextFile(markovOrder);
        }

        String record = String.join(",", markovOrder.toString(), sequence) + System.lineSeparator();

        fileContextPerOrder.get(markovOrder).getWriter().append(record);
        fileContextPerOrder.get(markovOrder).getRecordsWritten().incrementAndGet();
    }

    private void createNextFile(int markovOrder) throws IOException {
        if (fileContextPerOrder.get(markovOrder).getWriter() != null) {
            fileContextPerOrder.get(markovOrder).getWriter().close();
        }

        String filename = Paths.get(outputDirectory.toString(), getFileName(markovOrder)).toString();
        fileContextPerOrder.get(markovOrder).setWriter(new BufferedWriter(new FileWriter(filename, true)));
    }

    private String getFileName(int markovOrder) {
        int recordsWritten = fileContextPerOrder.containsKey(markovOrder) ? fileContextPerOrder.get(markovOrder).getRecordsWritten().get() : 0;

        String recordOffsetPart = String.valueOf(recordsWritten - (recordsWritten % maxRecordsPerFile));

        return String.join("-", FileConstants.OUTPUT_FILE_PREFIX, String.valueOf(markovOrder), recordOffsetPart) + FileConstants.OUTPUT_FILE_EXTENSION;
    }

    @PreDestroy
    public void closeResources() throws IOException {
        for (FileWritingContext fileContext : fileContextPerOrder.values()) {
            fileContext.getWriter().close();
        }
    }
}
