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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ArrayMarkovModel {
    static final int NGRAM_ARRAY_LENGTH = 26 * 26 * 26 * 26 * 26;
    private static final int FOURTH_POWER = 26 * 26 * 26 * 26;
    private static final int THIRD_POWER = 26 * 26 * 26;
    private static final int SECOND_POWER = 26 * 26;
    private static final int ASCII_OFFSET = 97;
    private AtomicInteger totalNodes = new AtomicInteger(0);
    private int order;
    private float unknownLetterNGramProbability;
    private float unknownLetterNGramLogProbability;
    private List<TreeNGram> firstOrderNodes = new ArrayList<>();
    private float[] nGramLogProbabilities = new float[NGRAM_ARRAY_LENGTH];

    public ArrayMarkovModel(int order, float unknownLetterNGramProbability) {
        this.order = order;

        this.unknownLetterNGramProbability = unknownLetterNGramProbability;
        this.unknownLetterNGramLogProbability = (float) Math.log(unknownLetterNGramProbability);
        Arrays.fill(nGramLogProbabilities, this.unknownLetterNGramLogProbability);
    }

    ArrayMarkovModel(int order, float unknownLetterNGramProbability, List<TreeNGram> firstOrderNodes, float[] nGramLogProbabilities, int totalNodes) {
        this.order = order;
        this.unknownLetterNGramProbability = unknownLetterNGramProbability;
        this.unknownLetterNGramLogProbability = (float) Math.log(unknownLetterNGramProbability);

        if (nGramLogProbabilities.length != NGRAM_ARRAY_LENGTH) {
            throw new IllegalArgumentException("Expected nGramLogProbabilities size=" + NGRAM_ARRAY_LENGTH + " but was=" + nGramLogProbabilities.length);
        }

        this.firstOrderNodes = new ArrayList<>(firstOrderNodes);
        this.nGramLogProbabilities = Arrays.copyOf(nGramLogProbabilities, nGramLogProbabilities.length);
        this.totalNodes.set(totalNodes);
    }

    public long getTotalNGramCount() {
        return firstOrderNodes.stream()
                .mapToLong(TreeNGram::getCount)
                .sum();
    }

    public int getMapSize() {
        return totalNodes.get();
    }

    public List<TreeNGram> getFirstOrderNodes() {
        return Collections.unmodifiableList(firstOrderNodes);
    }

    int getTotalNodes() {
        return totalNodes.get();
    }

    float[] getNGramLogProbabilities() {
        return nGramLogProbabilities;
    }

    public void addNode(TreeNGram nodeToAdd) {
        if (nodeToAdd.getCumulativeString().length() == 1) {
            firstOrderNodes.add(nodeToAdd);
        } else {
            addToNDArray(nodeToAdd);
        }
    }

    private void addToNDArray(TreeNGram treeNGram) {
        String ngram = treeNGram.getCumulativeString();

        int arrayIndex = computeArrayIndex(ngram);

        if (nGramLogProbabilities[arrayIndex] != unknownLetterNGramLogProbability) {
            throw new IllegalStateException("Unable to add the same ngram twice='" + ngram + "'.");
        }

        totalNodes.incrementAndGet();
        nGramLogProbabilities[arrayIndex] = (float) treeNGram.getLogProbability();
    }

    public float findExact(String ngram) {
        return nGramLogProbabilities[computeArrayIndex(ngram)];
    }

    public int computeArrayIndex(String ngram) {
        int i = ((ngram.charAt(0) - ASCII_OFFSET) * FOURTH_POWER);
        int j = ((ngram.charAt(1) - ASCII_OFFSET) * THIRD_POWER);
        int k = ((ngram.charAt(2) - ASCII_OFFSET) * SECOND_POWER);
        int l = ((ngram.charAt(3) - ASCII_OFFSET) * 26);
        int m = (ngram.charAt(4) - ASCII_OFFSET);

        return i + j + k + l + m;
    }

    public int getOrder() {
        return order;
    }

    public float getUnknownLetterNGramProbability() {
        return unknownLetterNGramProbability;
    }

    public float getUnknownLetterNGramLogProbability() {
        return unknownLetterNGramLogProbability;
    }
}
