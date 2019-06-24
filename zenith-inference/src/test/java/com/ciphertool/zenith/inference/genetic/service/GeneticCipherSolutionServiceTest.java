/**
 * Copyright 2015 George Belden
 * 
 * This file is part of DecipherEngine.
 * 
 * DecipherEngine is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DecipherEngine is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DecipherEngine. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.inference.genetic.service;

import com.ciphertool.zenith.genetic.ChromosomePrinter;
import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.algorithms.StandardGeneticAlgorithm;
import com.ciphertool.zenith.genetic.population.StandardPopulation;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

public class GeneticCipherSolutionServiceTest {
	private static ChromosomePrinter chromosomePrinterMock = mock(ChromosomePrinter.class);

	@Test
	public void testSetGeneticAlgorithm() {
		StandardGeneticAlgorithm StandardGeneticAlgorithmToSet = mock(StandardGeneticAlgorithm.class);
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();
		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithmToSet);

		Field standardGeneticAlgorithmField = ReflectionUtils.findField(GeneticCipherSolutionService.class, "geneticAlgorithm");
		ReflectionUtils.makeAccessible(standardGeneticAlgorithmField);
		StandardGeneticAlgorithm StandardGeneticAlgorithmFromObject = (StandardGeneticAlgorithm) ReflectionUtils.getField(standardGeneticAlgorithmField, geneticCipherSolutionService);

		assertSame(StandardGeneticAlgorithmToSet, StandardGeneticAlgorithmFromObject);
	}

	@Test
	public void testBegin() {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);

		StandardPopulation population = new StandardPopulation();
		population.setChromosomePrinter(chromosomePrinterMock);
		CipherKeyChromosome solutionChromosome = new CipherKeyChromosome();
		solutionChromosome.setEvaluationNeeded(false);
		population.addIndividual(solutionChromosome);
		when(StandardGeneticAlgorithm.getPopulation()).thenReturn(population);

		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		GeneticAlgorithmStrategy GeneticAlgorithmStrategy = new GeneticAlgorithmStrategy();

		// Before the algorithm begins, this will be false
		assertFalse(geneticCipherSolutionService.isRunning());
		geneticCipherSolutionService.begin(GeneticAlgorithmStrategy, false);
		/*
		 * After the algorithm ends, this will again be false. It is only true actually during the algorithm.
		 */
		assertFalse(geneticCipherSolutionService.isRunning());

		verify(StandardGeneticAlgorithm, times(1)).evolveAutonomously();

		verify(StandardGeneticAlgorithm, times(1)).setStrategy(same(GeneticAlgorithmStrategy));
	}

	@Test
	public void testBegin_DebugMode() throws InterruptedException {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);

		StandardPopulation population = new StandardPopulation();
		population.setChromosomePrinter(chromosomePrinterMock);
		CipherKeyChromosome solutionChromosome = new CipherKeyChromosome();
		solutionChromosome.setEvaluationNeeded(false);
		population.addIndividual(solutionChromosome);
		when(StandardGeneticAlgorithm.getPopulation()).thenReturn(population);

		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		GeneticAlgorithmStrategy GeneticAlgorithmStrategy = new GeneticAlgorithmStrategy();

		assertFalse(geneticCipherSolutionService.isRunning());
		geneticCipherSolutionService.begin(GeneticAlgorithmStrategy, true);
		assertTrue(geneticCipherSolutionService.isRunning());

		verify(StandardGeneticAlgorithm, times(1)).initialize();
		verify(StandardGeneticAlgorithm, times(1)).getPopulation();
		verify(StandardGeneticAlgorithm, times(1)).proceedWithNextGeneration();

		verify(StandardGeneticAlgorithm, times(1)).setStrategy(same(GeneticAlgorithmStrategy));
	}

	@Test
	public void testSetUp() {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);
		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		GeneticAlgorithmStrategy GeneticAlgorithmStrategy = new GeneticAlgorithmStrategy();
		geneticCipherSolutionService.setUp(GeneticAlgorithmStrategy);

		verify(StandardGeneticAlgorithm, times(1)).setStrategy(same(GeneticAlgorithmStrategy));
	}

	@Test
	public void testEndImmediately() {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);

		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		geneticCipherSolutionService.endImmediately(false);

		verify(StandardGeneticAlgorithm, times(1)).requestStop();
	}

	@Test
	public void testEndImmediately_DebugMode() {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();
		geneticCipherSolutionService.toggleRunning();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);
		StandardPopulation population = new StandardPopulation();
		population.setChromosomePrinter(chromosomePrinterMock);

		CipherKeyChromosome solutionChromosome = new CipherKeyChromosome();
		solutionChromosome.setEvaluationNeeded(false);
		population.addIndividual(solutionChromosome);
		when(StandardGeneticAlgorithm.getPopulation()).thenReturn(population);
		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		assertTrue(geneticCipherSolutionService.isRunning());
		geneticCipherSolutionService.endImmediately(true);
		assertFalse(geneticCipherSolutionService.isRunning());
	}

	@Test
	public void testResume() throws InterruptedException {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);

		StandardPopulation population = new StandardPopulation();
		population.setChromosomePrinter(chromosomePrinterMock);
		CipherKeyChromosome solutionChromosome = new CipherKeyChromosome();
		solutionChromosome.setEvaluationNeeded(false);
		population.addIndividual(solutionChromosome);
		when(StandardGeneticAlgorithm.getPopulation()).thenReturn(population);

		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		geneticCipherSolutionService.resume();

		verify(StandardGeneticAlgorithm, times(1)).getPopulation();
		verify(StandardGeneticAlgorithm, times(1)).proceedWithNextGeneration();
	}

	@Test
	public void testResume_ExceptionThrown() throws InterruptedException {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();
		geneticCipherSolutionService.toggleRunning();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);
		doThrow(IllegalStateException.class).when(StandardGeneticAlgorithm).proceedWithNextGeneration();

		StandardPopulation population = new StandardPopulation();
		population.setChromosomePrinter(chromosomePrinterMock);
		CipherKeyChromosome solutionChromosome = new CipherKeyChromosome();
		solutionChromosome.setEvaluationNeeded(false);
		population.addIndividual(solutionChromosome);
		when(StandardGeneticAlgorithm.getPopulation()).thenReturn(population);

		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		Field logField = ReflectionUtils.findField(GeneticCipherSolutionService.class, "log");
		Logger mockLogger = mock(Logger.class);
		ReflectionUtils.makeAccessible(logField);
		ReflectionUtils.setField(logField, geneticCipherSolutionService, mockLogger);

		doNothing().when(mockLogger).error(anyString(), any(Throwable.class));

		assertTrue(geneticCipherSolutionService.isRunning());
		geneticCipherSolutionService.resume();
		assertFalse(geneticCipherSolutionService.isRunning());

		verify(mockLogger, times(1)).error(anyString(), any(Throwable.class));
	}

	@Test
	public void testRunAlgorithmAutonomously() {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();
		geneticCipherSolutionService.toggleRunning();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);

		StandardPopulation population = new StandardPopulation();
		population.setChromosomePrinter(chromosomePrinterMock);
		CipherKeyChromosome solutionChromosome = new CipherKeyChromosome();
		solutionChromosome.setEvaluationNeeded(false);
		population.addIndividual(solutionChromosome);
		when(StandardGeneticAlgorithm.getPopulation()).thenReturn(population);

		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		assertTrue(geneticCipherSolutionService.isRunning());
		geneticCipherSolutionService.runAlgorithmAutonomously();
		assertFalse(geneticCipherSolutionService.isRunning());

		verify(StandardGeneticAlgorithm, times(1)).evolveAutonomously();
	}

	@Test
	public void testRunAlgorithmAutonomously_ExceptionThrown() {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();
		geneticCipherSolutionService.toggleRunning();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);

		StandardPopulation population = new StandardPopulation();
		population.setChromosomePrinter(chromosomePrinterMock);
		CipherKeyChromosome solutionChromosome = new CipherKeyChromosome();
		solutionChromosome.setEvaluationNeeded(false);
		population.addIndividual(solutionChromosome);
		when(StandardGeneticAlgorithm.getPopulation()).thenReturn(population);

		doThrow(new IllegalStateException()).when(StandardGeneticAlgorithm).evolveAutonomously();

		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		Field logField = ReflectionUtils.findField(GeneticCipherSolutionService.class, "log");
		Logger mockLogger = mock(Logger.class);
		ReflectionUtils.makeAccessible(logField);
		ReflectionUtils.setField(logField, geneticCipherSolutionService, mockLogger);

		doNothing().when(mockLogger).error(anyString(), any(Throwable.class));

		assertTrue(geneticCipherSolutionService.isRunning());
		geneticCipherSolutionService.runAlgorithmAutonomously();
		assertFalse(geneticCipherSolutionService.isRunning());

		verify(mockLogger, times(1)).error(anyString(), any(Throwable.class));

		verify(StandardGeneticAlgorithm, times(1)).evolveAutonomously();
	}

	@Test
	public void testRunAlgorithmStepwise() throws InterruptedException {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);

		StandardPopulation population = new StandardPopulation();
		population.setChromosomePrinter(chromosomePrinterMock);
		CipherKeyChromosome solutionChromosome = new CipherKeyChromosome();
		solutionChromosome.setEvaluationNeeded(false);
		population.addIndividual(solutionChromosome);
		when(StandardGeneticAlgorithm.getPopulation()).thenReturn(population);

		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		geneticCipherSolutionService.runAlgorithmStepwise();

		verify(StandardGeneticAlgorithm, times(1)).initialize();
		verify(StandardGeneticAlgorithm, times(1)).getPopulation();
		verify(StandardGeneticAlgorithm, times(1)).proceedWithNextGeneration();
	}

	@Test
	public void testRunAlgorithmStepwise_ExceptionThrown() throws InterruptedException {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();
		geneticCipherSolutionService.toggleRunning();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);
		doThrow(IllegalStateException.class).when(StandardGeneticAlgorithm).initialize();

		StandardPopulation population = new StandardPopulation();
		population.setChromosomePrinter(chromosomePrinterMock);
		CipherKeyChromosome solutionChromosome = new CipherKeyChromosome();
		solutionChromosome.setEvaluationNeeded(false);
		population.addIndividual(solutionChromosome);
		when(StandardGeneticAlgorithm.getPopulation()).thenReturn(population);

		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		Field logField = ReflectionUtils.findField(GeneticCipherSolutionService.class, "log");
		Logger mockLogger = mock(Logger.class);
		ReflectionUtils.makeAccessible(logField);
		ReflectionUtils.setField(logField, geneticCipherSolutionService, mockLogger);

		doNothing().when(mockLogger).error(anyString(), any(Throwable.class));

		assertTrue(geneticCipherSolutionService.isRunning());
		geneticCipherSolutionService.runAlgorithmStepwise();
		assertFalse(geneticCipherSolutionService.isRunning());

		verify(mockLogger, times(1)).error(anyString(), any(Throwable.class));
	}

	@Test
	public void testEnd() {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();
		geneticCipherSolutionService.toggleRunning();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);
		StandardPopulation population = new StandardPopulation();
		population.setChromosomePrinter(chromosomePrinterMock);

		CipherKeyChromosome solutionChromosome = new CipherKeyChromosome();
		solutionChromosome.setEvaluationNeeded(false);
		population.addIndividual(solutionChromosome);
		when(StandardGeneticAlgorithm.getPopulation()).thenReturn(population);
		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		assertTrue(geneticCipherSolutionService.isRunning());
		geneticCipherSolutionService.end();
		assertFalse(geneticCipherSolutionService.isRunning());
	}

	@Test
	public void testEnd_ExceptionThrown() {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();
		geneticCipherSolutionService.toggleRunning();

		StandardGeneticAlgorithm StandardGeneticAlgorithm = mock(StandardGeneticAlgorithm.class);
		when(StandardGeneticAlgorithm.getPopulation()).thenThrow(new IllegalStateException());
		geneticCipherSolutionService.setGeneticAlgorithm(StandardGeneticAlgorithm);

		Field logField = ReflectionUtils.findField(GeneticCipherSolutionService.class, "log");
		Logger mockLogger = mock(Logger.class);
		ReflectionUtils.makeAccessible(logField);
		ReflectionUtils.setField(logField, geneticCipherSolutionService, mockLogger);

		doNothing().when(mockLogger).error(anyString(), any(Throwable.class));

		assertTrue(geneticCipherSolutionService.isRunning());
		geneticCipherSolutionService.end();
		assertFalse(geneticCipherSolutionService.isRunning());

		verify(mockLogger, times(1)).error(anyString(), any(Throwable.class));
	}

	@Test
	public void testIsRunningAndToggleRunning() {
		GeneticCipherSolutionService geneticCipherSolutionService = new GeneticCipherSolutionService();

		assertFalse(geneticCipherSolutionService.isRunning());

		geneticCipherSolutionService.toggleRunning();

		assertTrue(geneticCipherSolutionService.isRunning());

		geneticCipherSolutionService.toggleRunning();

		assertFalse(geneticCipherSolutionService.isRunning());
	}
}
