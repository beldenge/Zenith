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

package com.ciphertool.zenith.inference.printer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.transformer.ciphertext.TransformationStep;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationManager;
import com.ciphertool.zenith.inference.util.ChiSquaredEvaluator;
import com.ciphertool.zenith.inference.util.EntropyEvaluator;
import com.ciphertool.zenith.inference.util.IndexOfCoincidenceEvaluator;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CipherSolutionPrinterTest {
    @Test
    public void testPrintWithoutTransformations() {
        CipherSolutionPrinter printer = new CipherSolutionPrinter();
        PlaintextTransformationManager plaintextTransformationManager = mock(PlaintextTransformationManager.class);
        EntropyEvaluator entropyEvaluator = mock(EntropyEvaluator.class);
        IndexOfCoincidenceEvaluator indexOfCoincidenceEvaluator = mock(IndexOfCoincidenceEvaluator.class);
        ChiSquaredEvaluator chiSquaredEvaluator = mock(ChiSquaredEvaluator.class);

        ReflectionTestUtils.setField(printer, "plaintextTransformationManager", plaintextTransformationManager);
        ReflectionTestUtils.setField(printer, "entropyEvaluator", entropyEvaluator);
        ReflectionTestUtils.setField(printer, "indexOfCoincidenceEvaluator", indexOfCoincidenceEvaluator);
        ReflectionTestUtils.setField(printer, "chiSquaredEvaluator", chiSquaredEvaluator);

        Cipher cipher = new Cipher("test", 2, 2);
        cipher.setCiphertext(Arrays.asList("A", "B", "C", "D"));

        CipherSolution solution = new CipherSolution(cipher, 4);
        solution.putMapping("A", 'a');
        solution.putMapping("B", 'b');
        solution.putMapping("C", 'c');
        solution.putMapping("D", 'd');
        solution.setProbability(0.5f);
        solution.addLogProbability(0, 0.1f);
        solution.addLogProbability(1, 0.2f);
        solution.addLogProbability(2, 0.3f);
        solution.addLogProbability(3, 0.4f);
        solution.setScores(new Fitness[] { new MaximizingFitness(1.23d) });

        when(entropyEvaluator.evaluate(any(), eq(cipher), eq("abcd"))).thenReturn(1.1f);
        when(indexOfCoincidenceEvaluator.evaluate(any(), eq(cipher), eq("abcd"))).thenReturn(0.2f);
        when(chiSquaredEvaluator.evaluate(any(), eq(cipher), eq("abcd"))).thenReturn(0.3f);

        Logger logger = (Logger) LoggerFactory.getLogger(CipherSolutionPrinter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            printer.print(solution, Collections.emptyList());
        } finally {
            logger.detachAppender(appender);
        }

        verify(plaintextTransformationManager, never()).transform(any(), any());
        verify(entropyEvaluator).evaluate(any(), eq(cipher), eq("abcd"));
        verify(indexOfCoincidenceEvaluator).evaluate(any(), eq(cipher), eq("abcd"));
        verify(chiSquaredEvaluator).evaluate(any(), eq(cipher), eq("abcd"));

        assertFalse(appender.list.isEmpty());
        String message = appender.list.get(0).getFormattedMessage();
        assertTrue(message.contains("Solution [probability=0.5"));
        assertTrue(message.contains("logProbability=1.0"));
        assertTrue(message.contains("scores=[MaximizingFitness{value=1.23}]"));
        assertTrue(message.contains("indexOfCoincidence=0.2"));
        assertTrue(message.contains("entropy=1.1"));
        assertTrue(message.contains("chiSquared=0.3"));
        assertTrue(message.contains("|  A"));
        assertTrue(message.contains("|  B"));
        assertTrue(message.contains("|  C"));
        assertTrue(message.contains("|  D"));

        assertEquals(Level.INFO, appender.list.get(0).getLevel());
    }

    @Test
    public void testPrintWithTransformationsAndKnownSolution() {
        CipherSolutionPrinter printer = new CipherSolutionPrinter();
        PlaintextTransformationManager plaintextTransformationManager = mock(PlaintextTransformationManager.class);
        EntropyEvaluator entropyEvaluator = mock(EntropyEvaluator.class);
        IndexOfCoincidenceEvaluator indexOfCoincidenceEvaluator = mock(IndexOfCoincidenceEvaluator.class);
        ChiSquaredEvaluator chiSquaredEvaluator = mock(ChiSquaredEvaluator.class);

        ReflectionTestUtils.setField(printer, "plaintextTransformationManager", plaintextTransformationManager);
        ReflectionTestUtils.setField(printer, "entropyEvaluator", entropyEvaluator);
        ReflectionTestUtils.setField(printer, "indexOfCoincidenceEvaluator", indexOfCoincidenceEvaluator);
        ReflectionTestUtils.setField(printer, "chiSquaredEvaluator", chiSquaredEvaluator);

        Cipher cipher = new Cipher("known", 2, 2);
        cipher.setCiphertext(Arrays.asList("A", "B", "C", "D"));
        cipher.putKnownSolutionMapping("A", "w");
        cipher.putKnownSolutionMapping("B", "x");
        cipher.putKnownSolutionMapping("C", "y");
        cipher.putKnownSolutionMapping("D", "z");

        CipherSolution solution = new CipherSolution(cipher, 4);
        solution.putMapping("A", 'w');
        solution.putMapping("B", 'x');
        solution.putMapping("C", 'y');
        solution.putMapping("D", 'z');
        solution.setScores(new Fitness[] { new MaximizingFitness(2.0d) });

        List<TransformationStep> steps = Collections.singletonList(new TransformationStep("Dummy", Collections.emptyMap()));
        when(plaintextTransformationManager.transform(eq("wxyz"), eq(steps))).thenReturn("lmno");

        when(entropyEvaluator.evaluate(any(), eq(cipher), eq("lmno"))).thenReturn(2.0f);
        when(indexOfCoincidenceEvaluator.evaluate(any(), eq(cipher), eq("lmno"))).thenReturn(0.4f);
        when(chiSquaredEvaluator.evaluate(any(), eq(cipher), eq("lmno"))).thenReturn(0.8f);

        Logger logger = (Logger) LoggerFactory.getLogger(CipherSolutionPrinter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            printer.print(solution, steps);
        } finally {
            logger.detachAppender(appender);
        }

        verify(plaintextTransformationManager).transform(eq("wxyz"), eq(steps));
        verify(entropyEvaluator).evaluate(any(), eq(cipher), eq("lmno"));
        verify(indexOfCoincidenceEvaluator).evaluate(any(), eq(cipher), eq("lmno"));
        verify(chiSquaredEvaluator).evaluate(any(), eq(cipher), eq("lmno"));

        assertFalse(appender.list.isEmpty());
        String message = appender.list.get(0).getFormattedMessage();
        assertTrue(message.contains("proximity=100.00%"));
        assertTrue(message.contains("Mappings for best probability:"));
    }
}