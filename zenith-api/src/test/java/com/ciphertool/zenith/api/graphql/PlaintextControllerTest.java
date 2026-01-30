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
import com.ciphertool.zenith.inference.transformer.ciphertext.TransformationStep;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaintextControllerTest {

    @Mock
    private WordSegmenter wordSegmenter;

    @Mock
    private PlaintextTransformationManager plaintextTransformationManager;

    @InjectMocks
    private PlaintextController controller;

    @Test
    void given_validInput_when_segmentPlaintextReturnsSegmentedWords_then_returnsExpectedValue() {
        String plaintext = "thequickbrownfox";
        String[] segments = {"the", "quick", "brown", "fox"};
        Map.Entry<Double, String[]> segmentResult = new AbstractMap.SimpleEntry<>(-15.5, segments);

        when(wordSegmenter.score(plaintext)).thenReturn(segmentResult);

        WordSegmentationResponse result = controller.segmentPlaintext(plaintext);

        assertEquals(-15.5, result.getProbability());
        assertArrayEquals(segments, result.getSegmentedPlaintext());
        verify(wordSegmenter).score(plaintext);
    }

    @Test
    void given_validInput_when_transformPlaintextAppliesTransformations_then_returnsNull() {
        PlaintextTransformationRequest request = new PlaintextTransformationRequest();
        request.setPlaintext("HELLO");
        List<TransformationStep> transformers = Arrays.asList(new TransformationStep("LowerCase", null));
        request.setPlaintextTransformers(transformers);

        when(plaintextTransformationManager.transform("HELLO", transformers)).thenReturn("hello");

        SolutionResponse result = controller.transformPlaintext(request);

        assertEquals("hello", result.getPlaintext());
        assertNull(result.getScores());
        verify(plaintextTransformationManager).transform("HELLO", transformers);
    }

    @Test
    void given_nullInput_when_transformPlaintextWithNullTransformersPassesNullToManager_then_returnsExpectedValue() {
        PlaintextTransformationRequest request = new PlaintextTransformationRequest();
        request.setPlaintext("TEST");
        request.setPlaintextTransformers(null);

        when(plaintextTransformationManager.transform("TEST", null)).thenReturn("TEST");

        SolutionResponse result = controller.transformPlaintext(request);

        assertEquals("TEST", result.getPlaintext());
        verify(plaintextTransformationManager).transform("TEST", null);
    }
}
