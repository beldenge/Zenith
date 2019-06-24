 /**
 * Copyright 2017-2019 George Belden
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

package com.ciphertool.zenith.inference.genetic.service;

 import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.algorithms.StandardGeneticAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class GeneticCipherSolutionService {
	private Logger				log		= LoggerFactory.getLogger(getClass());
	private boolean				running	= false;

	private StandardGeneticAlgorithm geneticAlgorithm;
	private long				start;

	public void begin(GeneticAlgorithmStrategy geneticAlgorithmStrategy, boolean debugMode) {
		toggleRunning();
		setUp(geneticAlgorithmStrategy);

		start = System.currentTimeMillis();

		if (debugMode) {
			runAlgorithmStepwise();
		} else {
			runAlgorithmAutonomously();
		}
	}

	protected void setUp(GeneticAlgorithmStrategy geneticAlgorithmStrategy) {
		geneticAlgorithm.setStrategy(geneticAlgorithmStrategy);
	}

	public void endImmediately(boolean inDebugMode) {
		if (inDebugMode) {
			end();
		} else {
			geneticAlgorithm.requestStop();
		}
	}

	public void resume() {
		try {
			/*
			 * Print the population every generation since this is debug mode, and print beforehand since it will all be
			 * printed again at the finish.
			 */
			this.geneticAlgorithm.getPopulation().printAscending();

			geneticAlgorithm.proceedWithNextGeneration();
		} catch (Throwable t) {
			log.error("Caught Throwable while running cipher solution service.  "
					+ "Cannot continue.  Performing tear-down tasks.", t);

			end();
		}
	}

	/**
	 * In this method we ALWAYS want to execute the inherited end() method
	 */
	protected void runAlgorithmAutonomously() {
		try {
			geneticAlgorithm.evolveAutonomously();
		} catch (Throwable t) {
			log.error("Caught Throwable while running cipher solution service.  "
					+ "Cannot continue.  Performing tear-down tasks.", t);
		} finally {
			end();
		}
	}

	/**
	 * In this method we only want to execute the inherited end() method if an exception is caught
	 */
	protected void runAlgorithmStepwise() {
		try {
			geneticAlgorithm.initialize();

			// Print the population every generation since this is debug mode.
			this.geneticAlgorithm.getPopulation().printAscending();

			geneticAlgorithm.proceedWithNextGeneration();
		} catch (Throwable t) {
			log.error("Caught Throwable while running cipher solution service.  "
					+ "Cannot continue.  Performing tear-down tasks.", t);

			end();
		}
	}

	protected void end() {
		try {
			// Print out summary information
			log.info("Total running time was " + (System.currentTimeMillis() - start) + "ms.");

			this.geneticAlgorithm.getPopulation().printAscending();
		} catch (Throwable t) {
			log.error("Caught Throwable while attempting to stop service.  " + "Performing tear-down tasks.", t);
		} finally {
		}

		toggleRunning();
	}

	public synchronized boolean isRunning() {
		return this.running;
	}

	protected synchronized void toggleRunning() {
		this.running = !this.running;
	}

	/**
	 * @param geneticAlgorithm
	 *            the geneticAlgorithm to set
	 */
	@Required
	public void setGeneticAlgorithm(StandardGeneticAlgorithm geneticAlgorithm) {
		this.geneticAlgorithm = geneticAlgorithm;
	}
}
