/**
 * Copyright 2015 George Belden
 * <p>
 * This file is part of DecipherEngine.
 * <p>
 * DecipherEngine is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * DecipherEngine is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * DecipherEngine. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.inference.optimizer;

import com.ciphertool.zenith.genetic.algorithms.StandardGeneticAlgorithm;
import com.ciphertool.zenith.genetic.population.StandardPopulation;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.printer.CipherSolutionPrinter;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class GeneticAlgorithmSolutionOptimizerTest {
    @Test
    public void testRunAlgorithmAutonomously() {
        GeneticAlgorithmSolutionOptimizer geneticAlgorithmSolutionOptimizer = new GeneticAlgorithmSolutionOptimizer();

        StandardGeneticAlgorithm standardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);

        StandardPopulation population = new StandardPopulation();
        CipherKeyChromosome solutionChromosome = new CipherKeyChromosome();
        solutionChromosome.setCipher(new Cipher());
        solutionChromosome.setEvaluationNeeded(false);
        population.addIndividual(solutionChromosome);
        when(standardGeneticAlgorithm.getPopulation()).thenReturn(population);

        Field geneticAlgorithmField = ReflectionUtils.findField(GeneticAlgorithmSolutionOptimizer.class, "geneticAlgorithm");
        ReflectionUtils.makeAccessible(geneticAlgorithmField);
        ReflectionUtils.setField(geneticAlgorithmField, geneticAlgorithmSolutionOptimizer, standardGeneticAlgorithm);

        Field epochsField = ReflectionUtils.findField(GeneticAlgorithmSolutionOptimizer.class, "epochs");
        ReflectionUtils.makeAccessible(epochsField);
        ReflectionUtils.setField(epochsField, geneticAlgorithmSolutionOptimizer, 1);

        CipherSolutionPrinter cipherSolutionPrinter = new CipherSolutionPrinter();
        Field plaintextTransformersField = ReflectionUtils.findField(CipherSolutionPrinter.class, "plaintextTransformers");
        ReflectionUtils.makeAccessible(plaintextTransformersField);
        ReflectionUtils.setField(plaintextTransformersField, cipherSolutionPrinter, Collections.EMPTY_LIST);

        Field cipherSolutionPrinterField = ReflectionUtils.findField(GeneticAlgorithmSolutionOptimizer.class, "cipherSolutionPrinter");
        ReflectionUtils.makeAccessible(cipherSolutionPrinterField);
        ReflectionUtils.setField(cipherSolutionPrinterField, geneticAlgorithmSolutionOptimizer, cipherSolutionPrinter);

        geneticAlgorithmSolutionOptimizer.optimize();

        verify(standardGeneticAlgorithm, times(1)).evolve();
    }

    @Test
    public void testRunAlgorithmAutonomously_ExceptionThrown() {
        GeneticAlgorithmSolutionOptimizer geneticAlgorithmSolutionOptimizer = new GeneticAlgorithmSolutionOptimizer();

        StandardGeneticAlgorithm standardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);

        StandardPopulation population = new StandardPopulation();
        CipherKeyChromosome solutionChromosome = new CipherKeyChromosome();
        solutionChromosome.setEvaluationNeeded(false);
        population.addIndividual(solutionChromosome);
        when(standardGeneticAlgorithm.getPopulation()).thenReturn(population);

        doThrow(new IllegalStateException()).when(standardGeneticAlgorithm).evolve();

        Field geneticAlgorithmField = ReflectionUtils.findField(GeneticAlgorithmSolutionOptimizer.class, "geneticAlgorithm");
        ReflectionUtils.makeAccessible(geneticAlgorithmField);
        ReflectionUtils.setField(geneticAlgorithmField, geneticAlgorithmSolutionOptimizer, standardGeneticAlgorithm);

        Field logField = ReflectionUtils.findField(GeneticAlgorithmSolutionOptimizer.class, "log");
        Logger mockLogger = mock(Logger.class);
        ReflectionUtils.makeAccessible(logField);
        ReflectionUtils.setField(logField, geneticAlgorithmSolutionOptimizer, mockLogger);

        Field epochsField = ReflectionUtils.findField(GeneticAlgorithmSolutionOptimizer.class, "epochs");
        ReflectionUtils.makeAccessible(epochsField);
        ReflectionUtils.setField(epochsField, geneticAlgorithmSolutionOptimizer, 1);

        boolean caught = false;
        try {
            geneticAlgorithmSolutionOptimizer.optimize();
        } catch (Exception e) {
            caught = true;
        }

        assertTrue(caught);

        verify(standardGeneticAlgorithm, times(1)).evolve();
    }
}
