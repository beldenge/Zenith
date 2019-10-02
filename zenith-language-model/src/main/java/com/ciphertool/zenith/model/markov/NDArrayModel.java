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

package com.ciphertool.zenith.model.markov;

import com.ciphertool.zenith.model.entities.TreeNGram;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NDArrayModel {
    private static final int ASCII_OFFSET = 97;
    private AtomicInteger totalNodes = new AtomicInteger(0);
    private int order;
    private float unknownLetterNGramProbability;
    private float unknownLetterNGramLogProbability;
    private List<TreeNGram> firstOrderNodes = new ArrayList<>();
    private float[][][][][] nGramLogProbabilities = new float[26][26][26][26][26];

    public NDArrayModel(int order) {
        this.order = order;

        for (int i = 0; i < nGramLogProbabilities.length; i ++) {
            for (int j = 0; j < nGramLogProbabilities[i].length; j ++) {
                for (int k = 0; k < nGramLogProbabilities[j].length; k ++) {
                    for (int l = 0; l < nGramLogProbabilities[k].length; l ++) {
                        Arrays.fill(nGramLogProbabilities[i][j][k][l], -1f);
                    }
                }
            }
        }
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
        String ngramString = treeNGram.getCumulativeString();

        int i = ngramString.charAt(0) - ASCII_OFFSET;
        int j = ngramString.charAt(1) - ASCII_OFFSET;
        int k = ngramString.charAt(2) - ASCII_OFFSET;
        int l = ngramString.charAt(3) - ASCII_OFFSET;
        int m = ngramString.charAt(4) - ASCII_OFFSET;

        if(nGramLogProbabilities[i][j][k][l][m] != -1f) {
            throw new IllegalStateException("Unable to add the same ngram twice='" + ngramString + "'.");
        }

        totalNodes.incrementAndGet();
        nGramLogProbabilities[i][j][k][l][m] = (float) treeNGram.getLogProbability();
    }

    public float findExact(String nGram) {
        return nGramLogProbabilities[nGram.charAt(0) - ASCII_OFFSET][nGram.charAt(1) - ASCII_OFFSET][nGram.charAt(2) - ASCII_OFFSET][nGram.charAt(3) - ASCII_OFFSET][nGram.charAt(4) - ASCII_OFFSET];
    }

    public int getOrder() {
        return order;
    }

    public float getUnknownLetterNGramProbability() {
        return unknownLetterNGramProbability;
    }

    public void setUnknownLetterNGramProbability(float unknownLetterNGramProbability) {
        this.unknownLetterNGramProbability = unknownLetterNGramProbability;
    }

    public float getUnknownLetterNGramLogProbability() {
        return unknownLetterNGramLogProbability;
    }

    public void setUnknownLetterNGramLogProbability(float unknownLetterNGramLogProbability) {
        this.unknownLetterNGramLogProbability = unknownLetterNGramLogProbability;
    }
}
