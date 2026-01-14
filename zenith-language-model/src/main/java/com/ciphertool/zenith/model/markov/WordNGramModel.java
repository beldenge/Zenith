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

import com.ciphertool.zenith.model.entities.WordNGram;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WordNGramModel {
    private Map<String, WordNGram> wordNGramMap = new HashMap<>();

    public Map<String, WordNGram> getWordNGramMap() {
        return Collections.unmodifiableMap(wordNGramMap);
    }

    public void putWordNGram(String key, WordNGram value) {
        this.wordNGramMap.put(key, value);
    }

    public boolean contains(String word) {
        return this.wordNGramMap.containsKey(word);
    }

    public double getLogProbability(String word) {
        return this.wordNGramMap.get(word).getLogProbability();
    }

    public long getCount(String word) {
        return this.wordNGramMap.get(word).getCount();
    }
}
