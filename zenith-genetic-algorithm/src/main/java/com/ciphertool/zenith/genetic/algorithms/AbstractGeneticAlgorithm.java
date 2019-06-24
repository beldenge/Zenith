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

package com.ciphertool.zenith.genetic.algorithms;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.algorithms.crossover.CrossoverAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.mutation.MutationAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.mutation.NonUniformMutationAlgorithm;
import com.ciphertool.zenith.genetic.algorithms.mutation.UniformMutationAlgorithm;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.statistics.ExecutionStatistics;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import com.ciphertool.zenith.genetic.statistics.PerformanceStatistics;
import com.ciphertool.zenith.genetic.population.Population;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.task.TaskExecutor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractGeneticAlgorithm {
	protected Logger					log					= LoggerFactory.getLogger(getClass());

	protected Population population;
	protected GeneticAlgorithmStrategy strategy;
	protected Boolean					stopRequested		= false;
	protected Integer					generationCount		= 0;
	protected ExecutionStatistics executionStatistics;
	@SuppressWarnings("rawtypes")
	protected MutationAlgorithm mutationAlgorithm;
	protected AtomicInteger				mutations			= new AtomicInteger(0);
	@SuppressWarnings("rawtypes")
	protected CrossoverAlgorithm crossoverAlgorithm;
	protected TaskExecutor				taskExecutor;
	protected Double					majorEvaluationPercentage;
	protected Integer					majorEvaluationStepSize;

	protected class SelectionResult {
		private Chromosome mom;
		private Chromosome	dad;

		/**
		 * @param mom
		 *            the mom Chromosome to set
		 * @param dad
		 *            the dad Chromosome to set
		 */
		public SelectionResult(Chromosome mom, Chromosome dad) {
			this.mom = mom;
			this.dad = dad;
		}

		/**
		 * @return the mom Chromosome
		 */
		public Chromosome getMom() {
			return mom;
		}

		/**
		 * @return the dad Chromosome
		 */
		public Chromosome getDad() {
			return dad;
		}
	}

	public void spawnInitialPopulation() throws InterruptedException {
		GenerationStatistics generationStatistics = new GenerationStatistics(this.executionStatistics,
				this.generationCount);

		long start = System.currentTimeMillis();

		this.population.clearIndividuals();

		this.population.breed();

		long startEntropyCalculation = System.currentTimeMillis();
		BigDecimal entropy = this.population.calculateEntropy();
		generationStatistics.setEntropy(entropy);
		generationStatistics.getPerformanceStatistics().setEntropyMillis(System.currentTimeMillis() - startEntropyCalculation);

		long startEvaluation = System.currentTimeMillis();
		this.population.evaluateFitness(generationStatistics);
		generationStatistics.getPerformanceStatistics().setEvaluationMillis(System.currentTimeMillis() - startEvaluation);

		long executionTime = System.currentTimeMillis() - start;
		generationStatistics.getPerformanceStatistics().setTotalMillis(executionTime);

		log.info("Took " + executionTime + "ms to spawn initial population of size " + this.population.size());

		log.info(generationStatistics.toString());
	}

	public void evolveAutonomously() {
		try {
			initialize();

			do {
				proceedWithNextGeneration();
			} while (!this.stopRequested && (this.strategy.getMaxGenerations() < 0
					|| this.generationCount < this.strategy.getMaxGenerations()));
		} catch (InterruptedException ie) {
			log.info(ie.getMessage());

			this.population.recoverFromBackup();
		}

		finish();
	}

	public void initialize() throws InterruptedException {
		validateParameters();

		this.generationCount = 0;

		this.stopRequested = false;
		this.population.setStopRequested(false);

		Date startDate = new Date();
		this.executionStatistics = new ExecutionStatistics(startDate, this.strategy);

		this.spawnInitialPopulation();
	}

	protected void validateParameters() {
		List<String> validationErrors = new ArrayList<String>();

		if (strategy.getGeneticStructure() == null) {
			validationErrors.add("Parameter 'geneticStructure' cannot be null.");
		}

		if (strategy.getPopulationSize() == null || strategy.getPopulationSize() <= 0) {
			validationErrors.add("Parameter 'populationSize' must be greater than zero.");
		}

		if (strategy.getMutationRate() == null || strategy.getMutationRate() < 0) {
			validationErrors.add("Parameter 'mutationRate' must be greater than or equal to zero.");
		}

		if (strategy.getMaxMutationsPerIndividual() == null || strategy.getMaxMutationsPerIndividual() < 0) {
			validationErrors.add("Parameter 'maxMutationsPerIndividual' must be greater than or equal to zero.");
		}

		if (strategy.getMaxGenerations() == null || strategy.getMaxGenerations() == 0) {
			validationErrors.add("Parameter 'maxGenerations' cannot be null and must not equal zero.");
		}

		if (strategy.getCrossoverAlgorithm() == null) {
			validationErrors.add("Parameter 'crossoverAlgorithm' cannot be null.");
		}

		if (strategy.getFitnessEvaluator() == null) {
			validationErrors.add("Parameter 'fitnessEvaluator' cannot be null.");
		}

		if (strategy.getMutationAlgorithm() == null) {
			validationErrors.add("Parameter 'mutationAlgorithm' cannot be null.");
		}

		if (strategy.getSelector() == null) {
			validationErrors.add("Parameter 'selectorMethod' cannot be null.");
		}

		if (validationErrors.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("Unable to execute genetic algorithm because one or more of the required parameters are missing.  The validation errors are:");

			for (String validationError : validationErrors) {
				sb.append("\n\t-" + validationError);
			}

			throw new IllegalStateException(sb.toString());
		}
	}

	/**
	 * A concurrent task for performing a crossover of two parent Chromosomes, producing one child Chromosome.
	 */
	protected class MutationTask implements Callable<Void> {
		private Chromosome chromosome;

		public MutationTask(Chromosome chromosome) {
			this.chromosome = chromosome;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void call() throws Exception {
			/*
			 * Mutate a gene within the Chromosome. The original Chromosome has been cloned.
			 */
			if (mutationAlgorithm.mutateChromosome(chromosome)) {
				mutations.incrementAndGet();
			}

			return null;
		}
	}

	public void proceedWithNextGeneration() throws InterruptedException {
		this.population.backupIndividuals();

		this.generationCount++;

		GenerationStatistics generationStatistics = new GenerationStatistics(this.executionStatistics,
				this.generationCount);

		long generationStart = System.currentTimeMillis();

		int populationSizeBeforeGeneration = this.population.size();

		PerformanceStatistics performanceStats = new PerformanceStatistics();

		List<Chromosome> moms = new ArrayList<Chromosome>();
		List<Chromosome> dads = new ArrayList<Chromosome>();

		long startSelection = System.currentTimeMillis();
		this.population.reIndexSelector();
		select(populationSizeBeforeGeneration, moms, dads);
		performanceStats.setSelectionMillis(System.currentTimeMillis() - startSelection);

		long startCrossover = System.currentTimeMillis();
		generationStatistics.setNumberOfCrossovers(crossover(populationSizeBeforeGeneration, moms, dads));
		performanceStats.setCrossoverMillis(System.currentTimeMillis() - startCrossover);

		long startMutation = System.currentTimeMillis();
		generationStatistics.setNumberOfMutations(mutate(populationSizeBeforeGeneration));
		performanceStats.setMutationMillis(System.currentTimeMillis() - startMutation);

		long startEntropyCalculation = System.currentTimeMillis();
		BigDecimal entropy = this.population.calculateEntropy();
		generationStatistics.setEntropy(entropy);
		performanceStats.setEntropyMillis(System.currentTimeMillis() - startEntropyCalculation);

		long startEvaluation = System.currentTimeMillis();
		this.population.evaluateFitness(generationStatistics);
		performanceStats.setEvaluationMillis(System.currentTimeMillis() - startEvaluation);

		if (majorEvaluationStepSize > 0 && (this.generationCount % majorEvaluationStepSize) == 0) {
			long startMajorEvaluation = System.currentTimeMillis();
			this.population.performMajorEvaluation(generationStatistics, majorEvaluationPercentage);
			performanceStats.setMajorEvaluationMillis(System.currentTimeMillis() - startMajorEvaluation);
		}

		performanceStats.setTotalMillis(System.currentTimeMillis() - generationStart);
		generationStatistics.setPerformanceStatistics(performanceStats);

		log.info(generationStatistics.toString());

		this.executionStatistics.addGenerationStatistics(generationStatistics);
	}

	public void requestStop() {
		this.stopRequested = true;

		this.population.requestStop();
	}

	public void finish() {
		long totalExecutionTime = 0;

		for (GenerationStatistics generationStatistics : this.executionStatistics.getGenerationStatisticsList()) {
			if (generationStatistics.getGeneration() == 0) {
				// This is the initial spawning of the population, which will potentially skew the average
				continue;
			}

			totalExecutionTime += generationStatistics.getPerformanceStatistics().getTotalMillis();
		}

		long averageExecutionTime = 0;

		if (this.generationCount > 1) {
			/*
			 * We subtract 1 from the generation count because the zeroth generation is just the initial spawning of the
			 * population. And, we add one to the result because the remainder from division is truncated due to use of
			 * primitive type long, and we want to round up.
			 */
			averageExecutionTime = (totalExecutionTime / (this.generationCount - 1)) + 1;
		} else {
			averageExecutionTime = totalExecutionTime;
		}

		log.info("Average generation time is " + averageExecutionTime + "ms.");

		this.executionStatistics.setEndDateTime(new Date());

		// This needs to be reset to null in case the algorithm is re-run
		this.executionStatistics = null;
	}

	@SuppressWarnings({ "rawtypes" })
	public void setStrategy(GeneticAlgorithmStrategy geneticAlgorithmStrategy) {
		this.population.setGeneticStructure(geneticAlgorithmStrategy.getGeneticStructure());
		this.population.setFitnessEvaluator(geneticAlgorithmStrategy.getFitnessEvaluator());
		this.population.setKnownSolutionFitnessEvaluator(geneticAlgorithmStrategy.getKnownSolutionFitnessEvaluator());
		this.population.setCompareToKnownSolution(geneticAlgorithmStrategy.getCompareToKnownSolution());
		this.population.setTargetSize(geneticAlgorithmStrategy.getPopulationSize());
		this.population.setSelector(geneticAlgorithmStrategy.getSelector());

		this.crossoverAlgorithm = geneticAlgorithmStrategy.getCrossoverAlgorithm();

		this.mutationAlgorithm = geneticAlgorithmStrategy.getMutationAlgorithm();

		if (this.mutationAlgorithm instanceof UniformMutationAlgorithm) {
			((UniformMutationAlgorithm) this.mutationAlgorithm).setMutationRate(geneticAlgorithmStrategy.getMutationRate());
		}

		if (this.mutationAlgorithm instanceof NonUniformMutationAlgorithm) {
			((NonUniformMutationAlgorithm) this.mutationAlgorithm).setMaxMutationsPerChromosome(geneticAlgorithmStrategy.getMaxMutationsPerIndividual());
		}

		this.strategy = geneticAlgorithmStrategy;
	}

	/**
	 * @return the strategy
	 */
	public GeneticAlgorithmStrategy getStrategy() {
		return strategy;
	}

	public abstract void select(int initialPopulationSize, List<Chromosome> moms, List<Chromosome> dads) throws InterruptedException;

	public abstract int crossover(int pairsToCrossover, List<Chromosome> moms, List<Chromosome> dads) throws InterruptedException;

	public abstract int mutate(int populationSizeBeforeReproduction) throws InterruptedException;

	/**
	 * @param taskExecutor
	 *            the taskExecutor to set
	 */
	@Required
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * @param majorEvaluationPercentage
	 *            the majorEvaluationPercentage to set
	 */
	@Required
	public void setMajorEvaluationPercentage(Double majorEvaluationPercentage) {
		this.majorEvaluationPercentage = majorEvaluationPercentage;
	}

	/**
	 * @param majorEvaluationStepSize
	 *            the majorEvaluationStepSize to set
	 */
	@Required
	public void setMajorEvaluationStepSize(Integer majorEvaluationStepSize) {
		this.majorEvaluationStepSize = majorEvaluationStepSize;
	}

	/**
	 * @return the population
	 */
	public Population getPopulation() {
		return population;
	}

	/**
	 * @param population
	 *            the population to set
	 */
	@Required
	public void setPopulation(Population population) {
		this.population = population;
	}
}
