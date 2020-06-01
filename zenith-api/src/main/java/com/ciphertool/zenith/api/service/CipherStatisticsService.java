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

package com.ciphertool.zenith.api.service;

import com.ciphertool.zenith.api.model.*;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.statistics.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/statistics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class CipherStatisticsService {
    @Autowired
    private CiphertextUniqueSymbolsEvaluator uniqueSymbolsEvaluator;

    @Autowired
    private CiphertextMultiplicityEvaluator multiplicityEvaluator;

    @Autowired
    private CiphertextEntropyEvaluator entropyEvaluator;

    @Autowired
    private CiphertextIndexOfCoincidenceEvaluator indexOfCoincidenceEvaluator;

    @Autowired
    private CiphertextRepeatingBigramEvaluator bigramEvaluator;

    @Autowired
    private CiphertextCycleCountEvaluator cycleCountEvaluator;

    @Autowired
    private CiphertextNgramEvaluator ciphertextNgramEvaluator;

    @PostMapping("/uniqueSymbols")
    @ResponseBody
    @Cacheable(value = "uniqueSymbols", key = "#request.ciphertext")
    public DoubleResponse getUniqueSymbols(@RequestBody CipherRequest request) {
        return new DoubleResponse(uniqueSymbolsEvaluator.evaluate(request.asCipher()));
    }

    @PostMapping("/multiplicity")
    @ResponseBody
    @Cacheable(value = "multiplicities", key = "#request.ciphertext")
    public DoubleResponse getMultiplicity(@RequestBody CipherRequest request) {
        return new DoubleResponse(multiplicityEvaluator.evaluate(request.asCipher()));
    }

    @PostMapping("/entropy")
    @ResponseBody
    @Cacheable(value = "entropies", key = "#request.ciphertext")
    public DoubleResponse getEntropy(@RequestBody CipherRequest request) {
        return new DoubleResponse(entropyEvaluator.evaluate(request.asCipher()));
    }

    @PostMapping("/indexOfCoincidence")
    @ResponseBody
    @Cacheable(value = "indexesOfCoincidence", key = "#request.ciphertext")
    public DoubleResponse getIndexOfCoincidence(@RequestBody CipherRequest request) {
        return new DoubleResponse(indexOfCoincidenceEvaluator.evaluate(request.asCipher()));
    }

    @PostMapping("/bigramRepeats")
    @ResponseBody
    @Cacheable(value = "bigramRepeats", key = "#request.ciphertext")
    public IntResponse getBigramRepeats(@RequestBody CipherRequest request) {
        return new IntResponse(bigramEvaluator.evaluate(request.asCipher()));
    }

    @PostMapping("/cycleScore")
    @ResponseBody
    @Cacheable(value = "cycleScores", key = "#request.ciphertext")
    public IntResponse getCycleScore(@RequestBody CipherRequest request) {
        return new IntResponse(cycleCountEvaluator.evaluate(request.asCipher()));
    }

    @PostMapping("/ngrams")
    @ResponseBody
    @Cacheable(value = "ngrams", key = "#request.ciphertext")
    public NgramStatisticsResponse getNGramStatistics(@RequestBody CipherRequest request) {
        Cipher cipher = request.asCipher();

        Map<String, Integer> unigramCountMap = ciphertextNgramEvaluator.evaluate(cipher, 1);
        List<NgramCount> unigramCounts = new ArrayList<>(unigramCountMap.size());

        for (Map.Entry<String, Integer> entry : unigramCountMap.entrySet()) {
            unigramCounts.add(new NgramCount(entry.getKey(), entry.getValue()));
        }

        Map<String, Integer> bigramCountMap = ciphertextNgramEvaluator.evaluate(cipher, 2);
        List<NgramCount> bigramCounts = new ArrayList<>(bigramCountMap.size());

        for (Map.Entry<String, Integer> entry : bigramCountMap.entrySet()) {
            bigramCounts.add(new NgramCount(entry.getKey(), entry.getValue()));
        }

        Map<String, Integer> trigramCountMap = ciphertextNgramEvaluator.evaluate(cipher, 3);
        List<NgramCount> trigramCounts = new ArrayList<>(trigramCountMap.size());

        for (Map.Entry<String, Integer> entry : trigramCountMap.entrySet()) {
            trigramCounts.add(new NgramCount(entry.getKey(), entry.getValue()));
        }

        return new NgramStatisticsResponse(unigramCounts, bigramCounts, trigramCounts);
    }
}
