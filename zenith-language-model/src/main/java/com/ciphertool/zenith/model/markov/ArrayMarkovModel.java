/*
 * Copyright 2017-2020 George Belden
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
    private static final int FOURTH_POWER = 26 * 26 * 26 * 26;
    private static final int THIRD_POWER = 26 * 26 * 26;
    private static final int SECOND_POWER = 26 * 26;
    private static final int ASCII_OFFSET = 97;
    private AtomicInteger totalNodes = new AtomicInteger(0);
    private int order;
    private float unknownLetterNGramProbability;
    private float unknownLetterNGramLogProbability;
    private List<TreeNGram> firstOrderNodes = new ArrayList<>();
    private float[] nGramLogProbabilities = new float[26 * 26 * 26 * 26 * 26];

    public ArrayMarkovModel(int order, float unknownLetterNGramProbability) {
        this.order = order;

        this.unknownLetterNGramProbability = unknownLetterNGramProbability;
        this.unknownLetterNGramLogProbability = (float) Math.log(unknownLetterNGramProbability);
        Arrays.fill(nGramLogProbabilities, this.unknownLetterNGramLogProbability);
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
        // TODO: see if there is a way to do this with JavaCL for faster parallelism
        return nGramLogProbabilities[computeArrayIndex(ngram)];
    }

    public int computeArrayIndex(String ngram) {
        // TODO: see if doing this in a loop vs separate lines helps performance
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
