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

import com.ciphertool.zenith.api.model.CipherRequest;
import com.ciphertool.zenith.api.model.DoubleResponse;
import com.ciphertool.zenith.api.model.IntResponse;
import com.ciphertool.zenith.inference.statistics.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/uniqueSymbols")
    @ResponseBody
    @Cacheable(value = "uniqueSymbols", key = "#cipher.ciphertext")
    public DoubleResponse getUniqueSymbols(@RequestBody CipherRequest cipher) {
        return new DoubleResponse(uniqueSymbolsEvaluator.evaluate(cipher.asCipher()));
    }

    @PostMapping("/multiplicity")
    @ResponseBody
    @Cacheable(value = "multiplicities", key = "#cipher.ciphertext")
    public DoubleResponse getMultiplicity(@RequestBody CipherRequest cipher) {
        return new DoubleResponse(multiplicityEvaluator.evaluate(cipher.asCipher()));
    }

    @PostMapping("/entropy")
    @ResponseBody
    @Cacheable(value = "entropies", key = "#cipher.ciphertext")
    public DoubleResponse getEntropy(@RequestBody CipherRequest cipher) {
        return new DoubleResponse(entropyEvaluator.evaluate(cipher.asCipher()));
    }

    @PostMapping("/indexOfCoincidence")
    @ResponseBody
    @Cacheable(value = "indexesOfCoincidence", key = "#cipher.ciphertext")
    public DoubleResponse getIndexOfCoincidence(@RequestBody CipherRequest cipher) {
        return new DoubleResponse(indexOfCoincidenceEvaluator.evaluate(cipher.asCipher()));
    }

    @PostMapping("/bigramRepeats")
    @ResponseBody
    @Cacheable(value = "bigramRepeats", key = "#cipher.ciphertext")
    public IntResponse getBigramRepeats(@RequestBody CipherRequest cipher) {
        return new IntResponse(bigramEvaluator.evaluate(cipher.asCipher()));
    }

    @PostMapping("/cycleScore")
    @ResponseBody
    @Cacheable(value = "cycleScores", key = "#cipher.ciphertext")
    public IntResponse getCycleScore(@RequestBody CipherRequest cipher) {
        return new IntResponse(cycleCountEvaluator.evaluate(cipher.asCipher()));
    }
}
