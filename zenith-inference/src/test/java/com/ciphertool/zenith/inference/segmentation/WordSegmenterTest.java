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

package com.ciphertool.zenith.inference.segmentation;

import com.ciphertool.zenith.model.entities.WordNGram;
import com.ciphertool.zenith.model.markov.WordNGramModel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WordSegmenterTest {
    @Test
    public void given_emptyInput_when_scoreEmptyString_then_returnsNotNull() throws Exception {
        WordSegmenter segmenter = buildSegmenter(new WordNGramModel(), new WordNGramModel(), 1000L);

        Map.Entry<Double, String[]> result = segmenter.score("");

        assertNotNull(result);
        assertEquals(0d, result.getKey(), 0.0001d);
        assertEquals(0, result.getValue().length);
    }

    @Test
    public void given_validInput_when_scoreSingleWord_then_returnsNotNull() throws Exception {
        WordNGramModel unigramModel = new WordNGramModel();
        WordNGram unigram = new WordNGram();
        unigram.setNGram("a");
        unigram.setCount(5L);
        unigram.setLogProbability(-0.5d);
        unigramModel.putWordNGram("a", unigram);

        WordSegmenter segmenter = buildSegmenter(unigramModel, new WordNGramModel(), 1000L);

        Map.Entry<Double, String[]> result = segmenter.score("A");

        assertNotNull(result);
        assertEquals(-0.5d, result.getKey(), 0.0001d);
        assertEquals(1, result.getValue().length);
        assertEquals("a", result.getValue()[0]);
    }

    @Test
    public void given_validInput_when_scoreUsesUnseenProbability_then_returnsExpectedValue() throws Exception {
        WordSegmenter segmenter = buildSegmenter(new WordNGramModel(), new WordNGramModel(), 1000L);

        Map.Entry<Double, String[]> result = segmenter.score("A");

        assertEquals(-3.0d, result.getKey(), 0.0001d);
        assertEquals(1, result.getValue().length);
        assertEquals("a", result.getValue()[0]);
    }

    @Test
    public void given_validInput_when_scoreUsesBigramWhenPresent_then_returnsExpectedValue() throws Exception {
        WordNGramModel unigramModel = new WordNGramModel();
        WordNGram unigramA = new WordNGram();
        unigramA.setNGram("a");
        unigramA.setLogProbability(-0.1d);
        unigramModel.putWordNGram("a", unigramA);

        WordNGram unigramB = new WordNGram();
        unigramB.setNGram("b");
        unigramB.setLogProbability(-0.1d);
        unigramModel.putWordNGram("b", unigramB);

        WordNGramModel bigramModel = new WordNGramModel();
        WordNGram bigram = new WordNGram();
        bigram.setNGram("a b");
        bigram.setLogProbability(-0.05d);
        bigramModel.putWordNGram("a b", bigram);

        WordSegmenter segmenter = buildSegmenter(unigramModel, bigramModel, 1000L);

        Map.Entry<Double, String[]> result = segmenter.score("ab");

        assertEquals(-0.15d, result.getKey(), 0.0001d);
        assertEquals(2, result.getValue().length);
        assertEquals("a", result.getValue()[0]);
        assertEquals("b", result.getValue()[1]);
    }

    private static WordSegmenter buildSegmenter(WordNGramModel unigramModel, WordNGramModel bigramModel, long totalTokenCount) throws Exception {
        WordSegmenter segmenter = new WordSegmenter();

        setField(segmenter, "wordUnigramModel", unigramModel);
        setField(segmenter, "wordBigramModel", bigramModel);
        setField(segmenter, "wordGramTotalTokenCount", totalTokenCount);

        segmenter.init();

        return segmenter;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
