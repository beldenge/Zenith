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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Lazy
@Component
public class RecordShuffler {
    private static Logger log	= LoggerFactory.getLogger(RecordShuffler.class);

    @NotBlank
    @Value("${task.outputFileDirectory}")
    private String					sourceDirectory;

    @NotBlank
    @Value("${task.shuffledOutputDirectory}")
    private String					outputFileDirectory;

    @NotNull
    @Min(1)
    @Value("${task.maxRecordsPerFile}")
    private Integer maxRecordsPerFile;

    private Path sourceDirectoryPath;
    private Path outputDirectory;

    @Min(1)
    @Value("${task.markovOrder:1}")
    private int markovOrder;

    private Map<Integer, FileReadingContext> fileContextPerOrder = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws IOException {
        sourceDirectoryPath = Paths.get(sourceDirectory);

        if (!Files.isDirectory(sourceDirectoryPath)) {
            throw new IllegalArgumentException(
                    "Property \"task.sourceDirectory\" must be a directory.");
        }

        outputDirectory = Paths.get(outputFileDirectory);

        // Files.createDirectory() throws FileAlreadyExistsException if the directory already exists
        Files.createDirectory(outputDirectory);
    }

    public int shuffle() throws IOException {
        boolean done = false;
        BufferedWriter writer = null;
        int recordsWritten = 0;

        try {
            while (!done) {
                for (int i = 0; i < markovOrder + 2; i++) {
                    int markovOrderPart = (i == markovOrder + 1) ? 99 : i;

                    String line = read(markovOrderPart);

                    if (line == null) {
                        done = true;
                        break;
                    }

                    if (recordsWritten % maxRecordsPerFile == 0) {
                        if (writer != null) {
                            writer.close();
                        }

                        String filename = Paths.get(outputDirectory.toString(), String.join("-", FileConstants.OUTPUT_FILE_PREFIX, String.valueOf(recordsWritten)) + FileConstants.OUTPUT_FILE_EXTENSION).toString();
                        writer = new BufferedWriter(new FileWriter(filename, true));
                    }

                    writer.write(line + System.lineSeparator());
                    recordsWritten ++;
                }
            }
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }

        return recordsWritten;
    }

    public synchronized String read(Integer markovOrder) throws IOException {
        if (!fileContextPerOrder.containsKey(markovOrder)) {
            fileContextPerOrder.put(markovOrder, new FileReadingContext(new AtomicInteger(0)));
        }

        if (fileContextPerOrder.get(markovOrder).getRecordsRead().get() % maxRecordsPerFile == 0) {
            try {
                openNextFile(markovOrder);
            }
            catch(FileNotFoundException fnfe) {
                log.warn("Next file for markovOrder " + markovOrder + " at record count " + fileContextPerOrder.get(markovOrder).getRecordsRead().get() + " was not found.  Unable to continue, but this may be expected.");
                return null;
            }
        }

        fileContextPerOrder.get(markovOrder).getRecordsRead().incrementAndGet();

        return fileContextPerOrder.get(markovOrder).getReader().readLine();
    }

    private void openNextFile(int markovOrder) throws IOException {
        if (fileContextPerOrder.get(markovOrder).getReader() != null) {
            fileContextPerOrder.get(markovOrder).getReader().close();
        }

        String filename = Paths.get(sourceDirectoryPath.toString(), getFileName(markovOrder)).toString();
        fileContextPerOrder.get(markovOrder).setReader(new BufferedReader(new FileReader(filename)));
    }

    private String getFileName(int markovOrder) {
        int recordsRead = fileContextPerOrder.containsKey(markovOrder) ? fileContextPerOrder.get(markovOrder).getRecordsRead().get() : 0;

        String recordOffsetPart = String.valueOf(recordsRead - (recordsRead % maxRecordsPerFile));

        return String.join("-", FileConstants.OUTPUT_FILE_PREFIX, String.valueOf(markovOrder), recordOffsetPart) + FileConstants.OUTPUT_FILE_EXTENSION;
    }

    @PreDestroy
    public void closeResources() throws IOException {
        for (FileReadingContext fileContext : fileContextPerOrder.values()) {
            fileContext.getReader().close();
        }
    }
}
