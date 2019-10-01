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
    private double unknownLetterNGramProbability;
    private double unknownLetterNGramLogProbability;
    private List<TreeNGram> firstOrderNodes = new ArrayList<>();
    private double[][][][][] nGramLogProbabilities = new double[26][][][][];

    public NDArrayModel(int order) {
        this.order = order;
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

        int nextIndex = ngramString.charAt(0) - ASCII_OFFSET;
        if(nGramLogProbabilities[nextIndex] == null) {
            nGramLogProbabilities[nextIndex] = new double[26][][][];
        }

        double[][][][] secondLetter = nGramLogProbabilities[nextIndex];

        nextIndex = ngramString.charAt(1) - ASCII_OFFSET;
        if(secondLetter[nextIndex] == null) {
            secondLetter[nextIndex] = new double[26][][];
        }

        double[][][] thirdLetter = secondLetter[nextIndex];

        nextIndex = ngramString.charAt(2) - ASCII_OFFSET;
        if(thirdLetter[nextIndex] == null) {
            thirdLetter[nextIndex] = new double[26][];
        }

        double[][] fourthLetter = thirdLetter[nextIndex];

        nextIndex = ngramString.charAt(3) - ASCII_OFFSET;
        if(fourthLetter[nextIndex] == null) {
            fourthLetter[nextIndex] = new double[26];
            Arrays.fill(fourthLetter[nextIndex], -1d);
        }

        double[] fifthLetter = fourthLetter[nextIndex];

        nextIndex = ngramString.charAt(4) - ASCII_OFFSET;
        if(fifthLetter[nextIndex] != -1d) {
            throw new IllegalStateException("Unable to add the same ngram twice='" + ngramString + "'.");
        }

        totalNodes.incrementAndGet();
        fifthLetter[ngramString.charAt(4) - ASCII_OFFSET] = treeNGram.getLogProbability();
    }

    public Double findExact(String nGram) {
        int firstIndex = nGram.charAt(0) - ASCII_OFFSET;
        if(nGramLogProbabilities[firstIndex] == null) {
            return null;
        }

        int secondIndex = nGram.charAt(1) - ASCII_OFFSET;
        if(nGramLogProbabilities[firstIndex][secondIndex] == null) {
            return null;
        }

        int thirdIndex = nGram.charAt(2) - ASCII_OFFSET;
        if(nGramLogProbabilities[firstIndex][secondIndex][thirdIndex] == null) {
            return null;
        }

        int fourthIndex = nGram.charAt(3) - ASCII_OFFSET;
        if(nGramLogProbabilities[firstIndex][secondIndex][thirdIndex][fourthIndex] == null) {
            return null;
        }

        int fifthIndex = nGram.charAt(4) - ASCII_OFFSET;
        if(nGramLogProbabilities[firstIndex][secondIndex][thirdIndex][fourthIndex][fifthIndex] == -1d) {
            return null;
        }

        return nGramLogProbabilities[firstIndex][secondIndex][thirdIndex][fourthIndex][fifthIndex];
    }

    public int getOrder() {
        return order;
    }

    public double getUnknownLetterNGramProbability() {
        return unknownLetterNGramProbability;
    }

    public void setUnknownLetterNGramProbability(double unknownLetterNGramProbability) {
        this.unknownLetterNGramProbability = unknownLetterNGramProbability;
    }

    public double getUnknownLetterNGramLogProbability() {
        return unknownLetterNGramLogProbability;
    }

    public void setUnknownLetterNGramLogProbability(double unknownLetterNGramLogProbability) {
        this.unknownLetterNGramLogProbability = unknownLetterNGramLogProbability;
    }
}
