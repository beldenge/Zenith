/**
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

import com.ciphertool.zenith.api.model.DoubleResponse;
import com.ciphertool.zenith.api.model.IntResponse;
import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.statistics.CiphertextCycleCountEvaluator;
import com.ciphertool.zenith.inference.statistics.CiphertextMultiplicityEvaluator;
import com.ciphertool.zenith.inference.statistics.CiphertextRepeatingBigramEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/statistics/{cipherName}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class CipherStatisticsService {
    @Autowired
    private CipherDao cipherDao;

    @Autowired
    private CiphertextMultiplicityEvaluator multiplicityEvaluator;

    @Autowired
    private CiphertextRepeatingBigramEvaluator bigramEvaluator;

    @Autowired
    private CiphertextCycleCountEvaluator cycleCountEvaluator;

    private Cipher getCipher(String cipherName) {
        Cipher cipher = cipherDao.findByCipherName(cipherName);

        if (cipher == null) {
            throw new IllegalArgumentException("No cipher found for name " + cipherName + ".");
        }

        return cipher;
    }

    @GetMapping("/multiplicity")
    @ResponseBody
    public DoubleResponse getMultiplicity(@PathVariable String cipherName) {
        return new DoubleResponse(multiplicityEvaluator.evaluate(getCipher(cipherName)));
    }

    @GetMapping("/entropy")
    @ResponseBody
    public DoubleResponse getEntropy(@PathVariable String cipherName) {
        return new DoubleResponse(multiplicityEvaluator.evaluate(getCipher(cipherName)));
    }

    @GetMapping("/indexOfCoincidence")
    @ResponseBody
    public DoubleResponse getIndexOfCoincidence(@PathVariable String cipherName) {
        return new DoubleResponse(multiplicityEvaluator.evaluate(getCipher(cipherName)));
    }

    @GetMapping("/chiSquared")
    @ResponseBody
    public DoubleResponse getChiSquared(@PathVariable String cipherName) {
        return new DoubleResponse(multiplicityEvaluator.evaluate(getCipher(cipherName)));
    }

    @GetMapping("/bigramRepeats")
    @ResponseBody
    public IntResponse getBigramRepeats(@PathVariable String cipherName) {
        return new IntResponse(bigramEvaluator.evaluate(getCipher(cipherName)));
    }

    @GetMapping("/cycleScore")
    @ResponseBody
    public IntResponse getCycleScore(@PathVariable String cipherName) {
        return new IntResponse(cycleCountEvaluator.evaluateThreadSafe(getCipher(cipherName)));
    }
}
