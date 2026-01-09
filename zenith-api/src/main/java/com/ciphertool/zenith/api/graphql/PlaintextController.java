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

import com.ciphertool.zenith.api.model.PlaintextTransformationRequest;
import com.ciphertool.zenith.api.model.SolutionResponse;
import com.ciphertool.zenith.api.model.WordSegmentationResponse;
import com.ciphertool.zenith.inference.segmentation.WordSegmenter;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class PlaintextController {
    @Autowired
    private WordSegmenter wordSegmenter;

    @Autowired
    private PlaintextTransformationManager plaintextTransformationManager;

    @QueryMapping
    public WordSegmentationResponse segmentPlaintext(@Argument @NotBlank String plaintext) {
        Map.Entry<Double, String[]> segmentedPlaintext = this.wordSegmenter.score(plaintext);

        return new WordSegmentationResponse(segmentedPlaintext.getKey(), segmentedPlaintext.getValue());
    }

    @QueryMapping
    public SolutionResponse transformPlaintext(@Argument @Valid PlaintextTransformationRequest request) {
        String transformed = plaintextTransformationManager.transform(request.getPlaintext(), request.getPlaintextTransformers());

        return new SolutionResponse(transformed, null);
    }
}
