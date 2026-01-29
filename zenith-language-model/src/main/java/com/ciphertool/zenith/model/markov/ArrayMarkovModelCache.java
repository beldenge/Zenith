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

package com.ciphertool.zenith.model.markov;

import com.ciphertool.zenith.model.entities.TreeNGram;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class ArrayMarkovModelCache {
    // 4-byte file signature used to verify the cache file is a Zenith Markov Model Cache ("ZMMC").
    private static final int MAGIC = 0x5A4D4D43;
    private static final int VERSION = 1;

    private ArrayMarkovModelCache() {}

    public static ArrayMarkovModel readIfValid(Path path, int expectedOrder, int expectedMaxNGramsToKeep) throws IOException {
        if (path == null || !Files.exists(path)) {
            return null;
        }

        try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            int magic = inputStream.readInt();
            if (magic != MAGIC) {
                return null;
            }

            int version = inputStream.readInt();
            if (version != VERSION) {
                return null;
            }

            int order = inputStream.readInt();
            int maxNGramsToKeep = inputStream.readInt();
            float unknownProbability = inputStream.readFloat();
            int totalNodes = inputStream.readInt();
            int firstOrderCount = inputStream.readInt();
            List<TreeNGram> firstOrderNodes = new ArrayList<>(firstOrderCount);

            if (order != expectedOrder || maxNGramsToKeep != expectedMaxNGramsToKeep) {
                return null;
            }

            for (int i = 0; i < firstOrderCount; i++) {
                char letter = inputStream.readChar();
                long count = inputStream.readLong();
                double logProbability = inputStream.readDouble();

                TreeNGram node = new TreeNGram(String.valueOf(letter));
                node.setCount(count);
                node.setLogProbability(logProbability);
                firstOrderNodes.add(node);
            }

            int arrayLength = inputStream.readInt();
            if (arrayLength != ArrayMarkovModel.NGRAM_ARRAY_LENGTH) {
                return null;
            }

            float[] nGramLogProbabilities = new float[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                nGramLogProbabilities[i] = inputStream.readFloat();
            }

            return new ArrayMarkovModel(order, unknownProbability, firstOrderNodes, nGramLogProbabilities, totalNodes);
        }
    }

    public static void write(Path path, ArrayMarkovModel model, int maxNGramsToKeep) throws IOException {
        if (path == null) {
            return;
        }

        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path tempPath = path.resolveSibling(path.getFileName().toString() + ".tmp");

        try (DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(tempPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)))) {
            outputStream.writeInt(MAGIC);
            outputStream.writeInt(VERSION);
            outputStream.writeInt(model.getOrder());
            outputStream.writeInt(maxNGramsToKeep);
            outputStream.writeFloat(model.getUnknownLetterNGramProbability());
            outputStream.writeInt(model.getTotalNodes());

            List<TreeNGram> firstOrderNodes = model.getFirstOrderNodes();
            outputStream.writeInt(firstOrderNodes.size());
            for (TreeNGram node : firstOrderNodes) {
                String cumulativeString = node.getCumulativeString();
                outputStream.writeChar(cumulativeString.charAt(0));
                outputStream.writeLong(node.getCount());
                outputStream.writeDouble(node.getLogProbability());
            }

            float[] nGramLogProbabilities = model.getNGramLogProbabilities();
            outputStream.writeInt(nGramLogProbabilities.length);
            for (float nGramLogProbability : nGramLogProbabilities) {
                outputStream.writeFloat(nGramLogProbability);
            }
        }

        try {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
