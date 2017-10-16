/**
 * Copyright 2017 George Belden
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

package com.ciphertool.zenith.neural;

import java.math.BigDecimal;
import java.util.Arrays;

import javax.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;

import com.ciphertool.zenith.neural.generate.SampleGenerator;
import com.ciphertool.zenith.neural.model.DataSet;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.ProblemType;
import com.ciphertool.zenith.neural.predict.Predictor;
import com.ciphertool.zenith.neural.train.SupervisedTrainer;

@EnableAsync
@Validated
@ConfigurationProperties
@SpringBootApplication
public class NeuralNetworkApplication implements CommandLineRunner {
	private static Logger			log							= LoggerFactory.getLogger(NeuralNetworkApplication.class);

	private static final BigDecimal	ACCEPTABLE_MARGIN_OF_ERROR	= BigDecimal.valueOf(0.01);

	@Value("${taskExecutor.poolSize.override:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
	private int						corePoolSize;

	@Value("${taskExecutor.poolSize.override:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
	private int						maxPoolSize;

	@Min(1)
	@Value("${taskExecutor.queueCapacity}")
	private int						queueCapacity;

	@Min(1)
	@Value("${network.trainingSamples.count}")
	private int						numberOfSamples;

	@Min(1)
	@Value("${network.testSamples.count}")
	private int						numberOfTests;

	@Min(1)
	@Value("${network.epochs}")
	private int						epochs						= 1;

	@Autowired
	private NeuralNetwork			network;

	@Autowired
	private ThreadPoolTaskExecutor	taskExecutor;

	@Autowired
	private SampleGenerator			generator;

	@Autowired
	private SupervisedTrainer		trainer;

	@Autowired
	private Predictor				predictor;

	/**
	 * Main entry point for the application.
	 * 
	 * @param args
	 *            the optional, unused command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(NeuralNetworkApplication.class, args);
	}

	@Override
	public void run(String... arg0) throws Exception {
		log.info("TaskExecutor core pool size: {}", taskExecutor.getCorePoolSize());
		log.info("TaskExecutor max pool size: {}", taskExecutor.getMaxPoolSize());

		log.info("Training network...");
		long start = System.currentTimeMillis();

		for (int i = 1; i <= epochs; i++) {
			log.info("Generating " + numberOfSamples + " training samples...");
			long startGeneration = System.currentTimeMillis();
			DataSet trainingData = generator.generateTrainingSamples(numberOfSamples);
			log.info("Finished sample generation in " + (System.currentTimeMillis() - startGeneration) + "ms.");

			trainer.train(trainingData.getInputs(), trainingData.getOutputs());
			log.info("Completed epoch {}", i);
		}

		log.info("Finished training in " + (System.currentTimeMillis() - start) + "ms.");

		log.info("Generating " + numberOfTests + " test samples...");
		start = System.currentTimeMillis();
		DataSet testData = generator.generateTestSamples(numberOfTests);
		log.info("Finished in " + (System.currentTimeMillis() - start) + "ms.");

		log.info("Testing predictions...");
		start = System.currentTimeMillis();
		BigDecimal[][] predictions = predictor.predict(testData.getInputs());
		log.info("Finished in " + (System.currentTimeMillis() - start) + "ms.");

		int correctCount = 0;
		int bestProbabilityCount = 0;
		boolean wasIncorrect;
		for (int i = 0; i < predictions.length; i++) {
			wasIncorrect = false;

			BigDecimal[] inputs = testData.getInputs()[i];

			log.debug("Inputs: {}", Arrays.toString(inputs));

			BigDecimal highestProbability = BigDecimal.ZERO;
			int indexOfHighestProbability = -1;

			for (int j = 0; j < predictions[i].length; j++) {
				BigDecimal prediction = predictions[i][j];
				BigDecimal expected = testData.getOutputs()[i][j];

				log.debug("Expected: {}, Prediction: {}", expected, prediction);

				if (network.getProblemType() == ProblemType.CLASSIFICATION) {
					if (highestProbability.compareTo(prediction) < 0) {
						highestProbability = prediction;
						indexOfHighestProbability = j;
					}
				}

				// We can't test the exact values of 1 and 0 since the output from the network is a decimal value
				if (!wasIncorrect && prediction.subtract(expected).abs().compareTo(ACCEPTABLE_MARGIN_OF_ERROR) > 0) {
					wasIncorrect = true;
				}
			}

			if (network.getProblemType() == ProblemType.CLASSIFICATION
					&& BigDecimal.ONE.equals(testData.getOutputs()[i][indexOfHighestProbability])) {
				bestProbabilityCount++;
			}

			if (!wasIncorrect) {
				correctCount++;
			}
		}

		log.info("Neural network achieved " + correctCount + " correct out of " + numberOfTests + " total.");
		log.info("Percentage correct: " + (int) ((((double) correctCount / (double) numberOfTests) * 100.0) + 0.5));

		if (network.getProblemType() == ProblemType.CLASSIFICATION) {
			log.info("Classification achieved " + bestProbabilityCount + " most probable out of " + numberOfTests
					+ " total.");
			log.info("Percentage most probable: " + (int) ((((double) bestProbabilityCount / (double) numberOfTests)
					* 100.0) + 0.5));
		}

		/*
		 * FIXME: This is creating way too huge of a file because I need to change the way synapses link to neurons --
		 * as of now there is recursive duplication
		 */
		// network.saveToFile();
	}

	@Bean
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

		taskExecutor.setCorePoolSize(corePoolSize);
		taskExecutor.setMaxPoolSize(maxPoolSize);
		taskExecutor.setQueueCapacity(queueCapacity);
		taskExecutor.setKeepAliveSeconds(5);
		taskExecutor.setAllowCoreThreadTimeOut(true);

		return taskExecutor;
	}
}