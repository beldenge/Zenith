package com.ciphertool.zenith.model.markov;

import com.ciphertool.zenith.model.entities.TreeNGram;

import java.util.*;

public class MapMarkovModel {
    private int order;
    private double unknownLetterNGramProbability;
    private double unknownLetterNGramLogProbability;
    private Map<String, TreeNGram> nGramMap;
    private List<TreeNGram> firstOrderNodes = new ArrayList<>();

    public MapMarkovModel(int order, int capacity) {
        this.order = order;
        // A lower load factor seems to result in slightly faster lookups at a greater memory cost
        this.nGramMap = new HashMap<>(capacity, 0.1f);
    }

    public long getTotalNGramCount() {
        return firstOrderNodes.stream()
                .mapToLong(TreeNGram::getCount)
                .sum();
    }

    public int getMapSize() {
        return nGramMap.size();
    }

    public List<TreeNGram> getFirstOrderNodes() {
        return Collections.unmodifiableList(firstOrderNodes);
    }

    public void addNode(TreeNGram nodeToAdd) {
        if (nodeToAdd.getCumulativeString().length() == 1) {
            firstOrderNodes.add(nodeToAdd);
        } else {
            nGramMap.put(nodeToAdd.getCumulativeString(), nodeToAdd);
        }
    }

    public TreeNGram findExact(String nGram) {
        return nGramMap.get(nGram);
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
