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

package com.ciphertool.zenith.model.entities;

import com.opencsv.bean.CsvBindByPosition;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TreeNGram {
    private static final Pattern LOWERCASE_LETTERS_AND_SPACE = Pattern.compile("[a-z \\.]");

    @CsvBindByPosition(position = 0, required = true)
    protected String cumulativeString;

    @CsvBindByPosition(position = 1, required = true)
    protected int order;

    @CsvBindByPosition(position = 2, required = true)
    protected long count = 0L;

    @CsvBindByPosition(position = 3)
    protected double probability;

    @CsvBindByPosition(position = 4)
    protected double logProbability;

    @CsvBindByPosition(position = 5, required = true)
    protected double conditionalProbability;

    @CsvBindByPosition(position = 6, required = true)
    protected double logConditionalProbability;

    private Map<Character, TreeNGram> transitions = new HashMap<>(1);

    // Needed for de-serialization
    public TreeNGram() {
    }

    public TreeNGram(String nGramString) {
        this.cumulativeString = nGramString;

        this.order = nGramString.length();
    }

    public void increment() {
        this.count += 1L;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getProbability() {
        return this.probability;
    }

    /**
     * All current usages of this method are thread-safe, but since it's used in a multi-threaded way, this is a
     * defensive measure in case future usage changes are not thread-safe.
     */
    public synchronized void setProbability(double probability) {
        this.probability = probability;
    }

    public double getConditionalProbability() {
        return conditionalProbability;
    }

    public double getLogProbability() {
        return logProbability;
    }

    public void setLogProbability(double logProbability) {
        this.logProbability = logProbability;
    }

    public double getLogConditionalProbability() {
        return logConditionalProbability;
    }

    public void setLogConditionalProbability(double logConditionalProbability) {
        this.logConditionalProbability = logConditionalProbability;
    }

    /**
     * All current usages of this method are thread-safe, but since it's used in a multi-threaded way, this is a
     * defensive measure in case future usage changes are not thread-safe.
     */
    public synchronized void setConditionalProbability(double conditionalProbability) {
        this.conditionalProbability = conditionalProbability;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getCumulativeString() {
        return cumulativeString;
    }

    public void setCumulativeString(String cumulativeString) {
        this.cumulativeString = cumulativeString;
    }

    public boolean containsChild(char c) {
        return this.getTransitions().containsKey(c);
    }

    public TreeNGram getChild(char c) {
        return this.getTransitions().get(c);
    }

    public synchronized boolean addOrIncrementChildAsync(String nGramString, int order) {
        char firstLetter = nGramString.charAt(order - 1);

        TreeNGram child = this.getChild(firstLetter);

        boolean isNew = false;

        if (child == null) {
            this.putChild(firstLetter, new TreeNGram(nGramString.substring(0, order)));

            child = this.getChild(firstLetter);

            isNew = true;
        }

        child.increment();

        return isNew;
    }

    public synchronized TreeNGram addExistingNodeAsync(TreeNGram nodeToAdd, int order) {
        char firstLetter = nodeToAdd.cumulativeString.charAt(order - 1);

        TreeNGram child = this.getChild(firstLetter);

        if (order == nodeToAdd.cumulativeString.length()) {
            if (child == null) {
                this.putChild(firstLetter, nodeToAdd);
            } else {
                child.count = nodeToAdd.count;
                child.conditionalProbability = nodeToAdd.conditionalProbability;
                child.probability = nodeToAdd.probability;
                child.logProbability = nodeToAdd.logProbability;
                child.logConditionalProbability = nodeToAdd.logConditionalProbability;
            }

            return null;
        } else if (child == null) {
            this.putChild(firstLetter, new TreeNGram(nodeToAdd.cumulativeString.substring(0, order)));
        }

        return this.getChild(firstLetter);
    }

    public TreeNGram putChild(char c, TreeNGram child) {
        if (!LOWERCASE_LETTERS_AND_SPACE.matcher(String.valueOf(c)).matches()) {
            throw new IllegalArgumentException(
                    "Attempted to add a character to the Markov Model which is outside the range of "
                            + LOWERCASE_LETTERS_AND_SPACE);
        }

        return this.getTransitions().put(c, child);
    }

    public Map<Character, TreeNGram> getTransitions() {
        return this.transitions;
    }
}
