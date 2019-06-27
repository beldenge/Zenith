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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class TreeMarkovModel {
    private Logger log = LoggerFactory.getLogger(getClass());

    private TreeNGram rootNode = new TreeNGram("");
    private Integer order;
    private Double unknownLetterNGramProbability;
    private Double unknownLetterNGramLogProbability;

    public TreeMarkovModel(int order) {
        this.order = order;
    }

    public void addNode(TreeNGram nodeToAdd) {
        if (nodeToAdd.getCumulativeString() == null || nodeToAdd.getCumulativeString().length() == 0) {
            // This is the root node

            rootNode.setCount(nodeToAdd.getCount());
            rootNode.setConditionalProbability(nodeToAdd.getConditionalProbability());
            rootNode.setProbability(nodeToAdd.getProbability());
            rootNode.setLogConditionalProbability(nodeToAdd.getLogConditionalProbability());
            rootNode.setLogProbability(nodeToAdd.getLogProbability());

            return;
        }

        boolean succeeded = populateExistingNode(rootNode, nodeToAdd, 1);

        if (!succeeded) {
            throw new IllegalStateException("Could not add node to Markov Model: " + nodeToAdd);
        }
    }

    protected boolean populateExistingNode(TreeNGram parentNode, TreeNGram nodeToAdd, int order) {
        TreeNGram newChild = parentNode.addExistingNodeAsync(nodeToAdd, order);

        if (order < nodeToAdd.getCumulativeString().length()) {
            return populateExistingNode(newChild, nodeToAdd, order + 1);
        } else if (newChild != null) {
            return false;
        }

        return true;
    }

    public boolean addLetterTransition(String nGramString) {
        return populateLetterNode(rootNode, nGramString, 1);
    }

    protected boolean populateLetterNode(TreeNGram currentNode, String nGramString, Integer order) {
        boolean isNew = currentNode.addOrIncrementChildAsync(nGramString, order);

        if (order < nGramString.length()) {
            return populateLetterNode(currentNode.getChild(nGramString.charAt(order - 1)), nGramString, order + 1);
        }

        return isNew && order == this.order;
    }

    /**
     * @param nGram
     *            the N-Gram String to search by
     * @return the exact matching NGramIndexNode
     */
    public TreeNGram findExact(String nGram) {
        return findExactMatch(rootNode, nGram);
    }

    protected static TreeNGram findExactMatch(TreeNGram node, String nGramString) {
        TreeNGram nextNode = node.getChild(nGramString.charAt(0));

        if (nextNode == null) {
            return null;
        }

        if (nGramString.length() == 1) {
            return nextNode;
        }

        return findExactMatch(nextNode, nGramString.substring(1));
    }

    /**
     * @param nGram
     *            the N-Gram String to search by
     * @return the longest matching NGramIndexNode
     */
    public TreeNGram findLongest(String nGram) {
        return findLongestMatch(rootNode, nGram, null);
    }

    protected static TreeNGram findLongestMatch(TreeNGram node, String nGramString, TreeNGram longestMatch) {
        TreeNGram nextNode = node.getChild(nGramString.charAt(0));

        if (nextNode == null) {
            return longestMatch;
        }

        longestMatch = nextNode;

        if (nGramString.length() == 1) {
            return longestMatch;
        }

        return findLongestMatch(nextNode, nGramString.substring(1), longestMatch);
    }

    /**
     * @return the rootNode
     */
    public TreeNGram getRootNode() {
        return rootNode;
    }

    /**
     * @return the order
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * @return the unknownLetterNGramProbability
     */
    public Double getUnknownLetterNGramProbability() {
        return unknownLetterNGramProbability;
    }

    /**
     * @param unknownLetterNGramProbability
     *            the unknownLetterNGramProbability to set
     */
    public void setUnknownLetterNGramProbability(Double unknownLetterNGramProbability) {
        this.unknownLetterNGramProbability = unknownLetterNGramProbability;
    }

    public Double getUnknownLetterNGramLogProbability() {
        return unknownLetterNGramLogProbability;
    }

    public void setUnknownLetterNGramLogProbability(Double unknownLetterNGramLogProbability) {
        this.unknownLetterNGramLogProbability = unknownLetterNGramLogProbability;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Map<Character, TreeNGram> transitions = rootNode.getTransitions();

        for (Map.Entry<Character, TreeNGram> entry : transitions.entrySet()) {
            if (entry.getValue() != null) {
                appendTransitions("", entry.getKey(), entry.getValue(), sb);
            }
        }

        return sb.toString();
    }

    protected void appendTransitions(String parent, Character symbol, TreeNGram node, StringBuilder sb) {
        sb.append("\n[" + parent + "] ->" + symbol + " | " + node.getCount());

        Map<Character, TreeNGram> transitions = node.getTransitions();

        if (transitions == null || transitions.isEmpty()) {
            return;
        }

        for (Map.Entry<Character, TreeNGram> entry : transitions.entrySet()) {
            if (entry.getValue() != null) {
                appendTransitions(parent + entry.getKey(), entry.getKey(), entry.getValue(), sb);
            }
        }
    }

    public void normalize(int order, long orderTotal, TaskExecutor taskExecutor) {
        List<FutureTask<Void>> futures = new ArrayList<>(26);
        FutureTask<Void> task;

        for (Map.Entry<Character, TreeNGram> entry : this.getRootNode().getTransitions().entrySet()) {
            if (entry.getValue() != null) {
                task = new FutureTask<>(new NormalizeTask(entry.getValue(), order, orderTotal));
                futures.add(task);
                taskExecutor.execute(task);
            }
        }

        for (FutureTask<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ie) {
                log.error("Caught InterruptedException while waiting for NormalizeTask ", ie);
            } catch (ExecutionException ee) {
                log.error("Caught ExecutionException while waiting for NormalizeTask ", ee);
            }
        }
    }

    /**
     * A concurrent task for normalizing a Markov model node.
     */
    protected class NormalizeTask implements Callable<Void> {
        private TreeNGram node;
        private int order;
        private long total;

        /**
         * @param node
         *            the NGramIndexNode to set
         * @param order
         *            the order to set
         * @param total
         *            the order to set
         */
        public NormalizeTask(TreeNGram node, int order, long total) {
            this.node = node;
            this.order = order;
            this.total = total;
        }

        @Override
        public Void call() {
            normalizeTerminal(this.node, this.order, this.total);

            return null;
        }
    }

    protected void normalizeTerminal(TreeNGram node, int order, long total) {
        if (node.getCumulativeString().length() == order) {
            node.setProbability((double) node.getCount() / (double) total);
            node.setLogProbability(Math.log(node.getProbability()));

            return;
        }

        Map<Character, TreeNGram> transitions = node.getTransitions();

        if (transitions == null || transitions.isEmpty()) {
            return;
        }

        for (Map.Entry<Character, TreeNGram> entry : transitions.entrySet()) {
            normalizeTerminal(entry.getValue(), order, total);
        }
    }

    public long size() {
        return countAll(this.getRootNode());
    }

    protected long countAll(TreeNGram node) {
        long sum = 1L;

        for (Map.Entry<Character, TreeNGram> entry : node.getTransitions().entrySet()) {
            sum += countAll(entry.getValue());
        }

        return sum;
    }
}
