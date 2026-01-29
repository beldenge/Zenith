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

package com.ciphertool.zenith.api.graphql;

import com.ciphertool.zenith.api.model.*;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.statistics.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    @Mock
    private CiphertextUniqueSymbolsEvaluator uniqueSymbolsEvaluator;

    @Mock
    private CiphertextMultiplicityEvaluator multiplicityEvaluator;

    @Mock
    private CiphertextEntropyEvaluator entropyEvaluator;

    @Mock
    private CiphertextIndexOfCoincidenceEvaluator indexOfCoincidenceEvaluator;

    @Mock
    private CiphertextRepeatingBigramEvaluator bigramEvaluator;

    @Mock
    private CiphertextCycleCountEvaluator cycleCountEvaluator;

    @Mock
    private CiphertextNgramEvaluator ciphertextNgramEvaluator;

    @InjectMocks
    private StatisticsController controller;

    private CipherRequest cipherRequest;

    @BeforeEach
    void setUp() {
        cipherRequest = new CipherRequest();
        cipherRequest.setName("TestCipher");
        cipherRequest.setRows(2);
        cipherRequest.setColumns(3);
        cipherRequest.setCiphertext(Arrays.asList("A", "B", "C", "D", "E", "F"));
    }

    @Test
    void uniqueSymbols_returnsEvaluatorResult() {
        when(uniqueSymbolsEvaluator.evaluate(any(Cipher.class))).thenReturn(6);

        DoubleResponse result = controller.uniqueSymbols(cipherRequest);

        assertEquals(6.0, result.getValue());
        verify(uniqueSymbolsEvaluator).evaluate(any(Cipher.class));
    }

    @Test
    void multiplicity_returnsEvaluatorResult() {
        when(multiplicityEvaluator.evaluate(any(Cipher.class))).thenReturn(1.5f);

        DoubleResponse result = controller.multiplicity(cipherRequest);

        assertEquals(1.5, result.getValue(), 0.001);
        verify(multiplicityEvaluator).evaluate(any(Cipher.class));
    }

    @Test
    void entropy_returnsEvaluatorResult() {
        when(entropyEvaluator.evaluate(any(Cipher.class))).thenReturn(2.58f);

        DoubleResponse result = controller.entropy(cipherRequest);

        assertEquals(2.58, result.getValue(), 0.001);
        verify(entropyEvaluator).evaluate(any(Cipher.class));
    }

    @Test
    void indexOfCoincidence_returnsEvaluatorResult() {
        when(indexOfCoincidenceEvaluator.evaluate(any(Cipher.class))).thenReturn(0.067f);

        DoubleResponse result = controller.indexOfCoincidence(cipherRequest);

        assertEquals(0.067, result.getValue(), 0.001);
        verify(indexOfCoincidenceEvaluator).evaluate(any(Cipher.class));
    }

    @Test
    void bigramRepeats_returnsEvaluatorResult() {
        when(bigramEvaluator.evaluate(any(Cipher.class))).thenReturn(5);

        IntResponse result = controller.bigramRepeats(cipherRequest);

        assertEquals(5, result.getValue());
        verify(bigramEvaluator).evaluate(any(Cipher.class));
    }

    @Test
    void cycleScore_returnsEvaluatorResult() {
        when(cycleCountEvaluator.evaluate(any(Cipher.class))).thenReturn(3);

        IntResponse result = controller.cycleScore(cipherRequest);

        assertEquals(3, result.getValue());
        verify(cycleCountEvaluator).evaluate(any(Cipher.class));
    }

    @Test
    void nGramStatistics_returnsAllThreeNGramCounts() {
        Map<String, Integer> firstNGrams = new LinkedHashMap<>();
        firstNGrams.put("A", 2);
        firstNGrams.put("B", 1);

        Map<String, Integer> secondNGrams = new LinkedHashMap<>();
        secondNGrams.put("AB", 1);

        Map<String, Integer> thirdNGrams = new LinkedHashMap<>();
        thirdNGrams.put("ABC", 1);

        when(ciphertextNgramEvaluator.evaluate(any(Cipher.class), eq(1))).thenReturn(firstNGrams);
        when(ciphertextNgramEvaluator.evaluate(any(Cipher.class), eq(2))).thenReturn(secondNGrams);
        when(ciphertextNgramEvaluator.evaluate(any(Cipher.class), eq(3))).thenReturn(thirdNGrams);

        NGramStatistics result = controller.nGramStatistics(cipherRequest, 0);

        assertEquals(2, result.getFirstNGramCounts().size());
        assertEquals(1, result.getSecondNGramCounts().size());
        assertEquals(1, result.getThirdNGramCounts().size());

        assertEquals("A", result.getFirstNGramCounts().get(0).getNgram());
        assertEquals(2, result.getFirstNGramCounts().get(0).getCount());
    }

    @Test
    void nGramStatistics_withNonZeroStatsPage_calculatesCorrectOffset() {
        Map<String, Integer> emptyMap = new LinkedHashMap<>();

        when(ciphertextNgramEvaluator.evaluate(any(Cipher.class), eq(4))).thenReturn(emptyMap);
        when(ciphertextNgramEvaluator.evaluate(any(Cipher.class), eq(5))).thenReturn(emptyMap);
        when(ciphertextNgramEvaluator.evaluate(any(Cipher.class), eq(6))).thenReturn(emptyMap);

        controller.nGramStatistics(cipherRequest, 1);

        verify(ciphertextNgramEvaluator).evaluate(any(Cipher.class), eq(4));
        verify(ciphertextNgramEvaluator).evaluate(any(Cipher.class), eq(5));
        verify(ciphertextNgramEvaluator).evaluate(any(Cipher.class), eq(6));
    }

    @Test
    void valueSchemaMapping_forDoubleResponse_returnsValue() {
        DoubleResponse response = new DoubleResponse(3.14);

        assertEquals(3.14, controller.value(response));
    }

    @Test
    void valueSchemaMapping_forIntResponse_returnsValue() {
        IntResponse response = new IntResponse(42);

        assertEquals(42, controller.value(response));
    }

    @Test
    void ngramSchemaMapping_returnsNgram() {
        NGramCount count = new NGramCount("ABC", 5);

        assertEquals("ABC", controller.ngram(count));
    }

    @Test
    void countSchemaMapping_returnsCount() {
        NGramCount count = new NGramCount("ABC", 5);

        assertEquals(5, controller.count(count));
    }

    @Test
    void firstNGramCountsSchemaMapping_returnsList() {
        List<NGramCount> counts = Arrays.asList(new NGramCount("A", 1));
        NGramStatistics stats = new NGramStatistics(counts, List.of(), List.of());

        assertEquals(counts, controller.firstNGramCounts(stats));
    }

    @Test
    void secondNGramCountsSchemaMapping_returnsList() {
        List<NGramCount> counts = Arrays.asList(new NGramCount("AB", 1));
        NGramStatistics stats = new NGramStatistics(List.of(), counts, List.of());

        assertEquals(counts, controller.secondNGramCounts(stats));
    }

    @Test
    void thirdNGramCountsSchemaMapping_returnsList() {
        List<NGramCount> counts = Arrays.asList(new NGramCount("ABC", 1));
        NGramStatistics stats = new NGramStatistics(List.of(), List.of(), counts);

        assertEquals(counts, controller.thirdNGramCounts(stats));
    }
}
