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

import com.ciphertool.zenith.api.model.CipherRequest;
import com.ciphertool.zenith.api.model.CiphertextTransformationRequest;
import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.inference.transformer.ciphertext.CiphertextTransformationManager;
import com.ciphertool.zenith.inference.transformer.ciphertext.TransformationStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CipherControllerTest {

    @Mock
    private CipherDao cipherDao;

    @Mock
    private CiphertextTransformationManager ciphertextTransformationManager;

    @InjectMocks
    private CipherController controller;

    private Cipher testCipher;

    @BeforeEach
    void setUp() {
        testCipher = new Cipher("TestCipher", 2, 3);
        testCipher.addCiphertextCharacter(new Ciphertext("A"));
        testCipher.addCiphertextCharacter(new Ciphertext("B"));
        testCipher.addCiphertextCharacter(new Ciphertext("C"));
        testCipher.addCiphertextCharacter(new Ciphertext("D"));
        testCipher.addCiphertextCharacter(new Ciphertext("E"));
        testCipher.addCiphertextCharacter(new Ciphertext("F"));
    }

    @Test
    void ciphers_returnsCiphersFromDao() {
        List<Cipher> expectedCiphers = Arrays.asList(testCipher);
        when(cipherDao.findAll()).thenReturn(expectedCiphers);

        List<Cipher> result = controller.ciphers();

        assertEquals(expectedCiphers, result);
        verify(cipherDao).findAll();
    }

    @Test
    void transformCipher_transformsCipherWithSteps() {
        CipherRequest cipherRequest = new CipherRequest();
        cipherRequest.setName("TestCipher");
        cipherRequest.setRows(2);
        cipherRequest.setColumns(3);
        cipherRequest.setCiphertext(Arrays.asList("A", "B", "C", "D", "E", "F"));

        List<TransformationStep> steps = Arrays.asList(new TransformationStep("TestTransformer", null));

        CiphertextTransformationRequest request = new CiphertextTransformationRequest();
        request.setCipher(cipherRequest);
        request.setSteps(steps);

        Cipher transformedCipher = new Cipher("Transformed", 2, 3);
        when(ciphertextTransformationManager.transform(any(Cipher.class), eq(steps)))
                .thenReturn(transformedCipher);

        Cipher result = controller.transformCipher(request);

        assertEquals(transformedCipher, result);
        verify(ciphertextTransformationManager).transform(any(Cipher.class), eq(steps));
    }

    @Test
    void name_returnsCipherName() {
        assertEquals("TestCipher", controller.name(testCipher));
    }

    @Test
    void rows_returnsCipherRows() {
        assertEquals(2, controller.rows(testCipher));
    }

    @Test
    void columns_returnsCipherColumns() {
        assertEquals(3, controller.columns(testCipher));
    }

    @Test
    void readOnly_returnsCipherReadOnly() {
        assertFalse(controller.readOnly(testCipher));
    }

    @Test
    void ciphertext_returnsCiphertextAsStringList() {
        List<String> result = controller.ciphertext(testCipher);

        assertEquals(6, result.size());
        assertEquals("A", result.get(0));
        assertEquals("F", result.get(5));
    }

    @Test
    void knownSolutionKey_returnsKnownSolutionKey() {
        Map<String, String> key = new HashMap<>();
        key.put("A", "X");
        testCipher.setKnownSolutionKey(key);

        Map<String, String> result = controller.knownSolutionKey(testCipher);

        assertEquals(key, result);
    }
}
