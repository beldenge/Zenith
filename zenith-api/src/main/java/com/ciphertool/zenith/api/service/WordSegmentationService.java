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

import com.ciphertool.zenith.api.model.WordSegmentationRequest;
import com.ciphertool.zenith.api.model.WordSegmentationResponse;
import com.ciphertool.zenith.inference.segmentation.WordSegmenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(value = "/api/segments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class WordSegmentationService {
    @Autowired
    private WordSegmenter wordSegmenter;

    @GetMapping
    @ResponseBody
    public WordSegmentationResponse findSegments(@RequestBody WordSegmentationRequest request) {
        Map.Entry<Double, String[]> segmentedPlaintext = this.wordSegmenter.score(request.getPlaintext());

        return new WordSegmentationResponse(segmentedPlaintext.getKey(), segmentedPlaintext.getValue());
    }
}
