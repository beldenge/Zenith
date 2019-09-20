package com.ciphertool.zenith.model.markov;

import com.ciphertool.zenith.model.entities.TreeNGram;

import java.util.*;

public class MapMarkovModel {
    private Integer order;
    private Double unknownLetterNGramProbability;
    private Double unknownLetterNGramLogProbability;
    private Map<String, TreeNGram> nGramMap = new HashMap<>();
    private List<TreeNGram> firstOrderNodes = new ArrayList<>();

    public MapMarkovModel(int order) {
        this.order = order;
    }

    public long getTotalNumberOfNgrams() {
        return firstOrderNodes.stream()
                .mapToLong(TreeNGram::getCount)
                .sum();
    }

    public List<TreeNGram> getFirstOrderNodes() {
        return Collections.unmodifiableList(firstOrderNodes);
    }

    public void addNode(TreeNGram nodeToAdd) {
        nGramMap.put(nodeToAdd.getCumulativeString(), nodeToAdd);

        if (nodeToAdd.getCumulativeString().length() == 1) {
            firstOrderNodes.add(nodeToAdd);
        }
    }

    public TreeNGram findExact(String nGram) {
        return nGramMap.get(nGram);
    }

    public Integer getOrder() {
        return order;
    }

    public Double getUnknownLetterNGramProbability() {
        return unknownLetterNGramProbability;
    }

    public void setUnknownLetterNGramProbability(Double unknownLetterNGramProbability) {
        this.unknownLetterNGramProbability = unknownLetterNGramProbability;
    }

    public Double getUnknownLetterNGramLogProbability() {
        return unknownLetterNGramLogProbability;
    }

    public void setUnknownLetterNGramLogProbability(Double unknownLetterNGramLogProbability) {
        this.unknownLetterNGramLogProbability = unknownLetterNGramLogProbability;
    }
}
