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

import com.ciphertool.zenith.model.dao.LetterNGramDao;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
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
import com.ciphertool.zenith.neural.io.NetworkMapper;
import com.ciphertool.zenith.neural.model.DataSet;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.ProblemType;
import com.ciphertool.zenith.neural.predict.Predictor;
import com.ciphertool.zenith.neural.train.SupervisedTrainer;

@EnableAsync
@Validated
@ConfigurationProperties
@SpringBootApplication(scanBasePackageClasses = { NeuralNetworkApplication.class, LetterNGramDao.class })
public class NeuralNetworkApplication implements CommandLineRunner {
	private static Logger			log	= LoggerFactory.getLogger(NeuralNetworkApplication.class);

	@Value("${taskExecutor.poolSize.override:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
	private int						corePoolSize;

	@Value("${taskExecutor.poolSize.override:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
	private int						maxPoolSize;

	@Min(1)
	@Value("${taskExecutor.queueCapacity}")
	private int						queueCapacity;

	@Value("${network.testSamples.marginOfError:0.01}")
	private BigDecimal				marginOfErrorRegression;

	@Value("${network.trainingSamples.count}")
	private int						numberOfSamples;

	@Min(1)
	@Value("${network.testSamples.count}")
	private int						numberOfTests;

	@Min(1)
	@Value("${network.epochs:1}")
	private int						epochs;

	@Value("${network.input.fileName}")
	private String					inputFileName;

	@Value("${network.training.continue:false}")
	private boolean					continueTraining;

	@NotBlank
	@Value("${network.output.fileName}")
	private String					outputFileName;

	@Min(1)
	@Value("${network.batchSize}")
	private int						batchSize;

	@Min(1)
	@Value("${network.layers.input}")
	private int						inputLayerNeurons;

	@NotEmpty
	@Value("${network.layers.hidden}")
	private String[]				hiddenLayers;

	@Min(1)
	@Value("${network.layers.output}")
	private int						outputLayerNeurons;

	@Value("${network.bias.weight}")
	private BigDecimal				biasWeight;

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
	public void run(String... arg0) {
		log.info("TaskExecutor core pool size: {}", taskExecutor.getCorePoolSize());
		log.info("TaskExecutor max pool size: {}", taskExecutor.getMaxPoolSize());

		NeuralNetwork network;

		if (inputFileName != null && !inputFileName.isEmpty()) {
			network = new NeuralNetwork(NetworkMapper.loadFromFile(inputFileName));
		} else {
			network = new NeuralNetwork(inputLayerNeurons, hiddenLayers, outputLayerNeurons, biasWeight, batchSize);
		}

		long start = System.currentTimeMillis();

		if (inputFileName == null || inputFileName.isEmpty() || continueTraining) {
			log.info("Training network...");

			for (int i = 1; i <= epochs; i++) {
				log.info("Generating " + numberOfSamples + " training samples...");
				long startGeneration = System.currentTimeMillis();
				DataSet trainingData = generator.generateTrainingSamples(numberOfSamples);
				log.info("Finished sample generation in " + (System.currentTimeMillis() - startGeneration) + "ms.");

				trainer.train(network, batchSize, trainingData.getInputs(), trainingData.getOutputs());
				log.info("Completed epoch {}", i);
			}

			log.info("Finished training in " + (System.currentTimeMillis() - start) + "ms.");
		}

		log.info("Generating " + numberOfTests + " test samples...");
		start = System.currentTimeMillis();
		DataSet testData = generator.generateTestSamples(numberOfTests);
		log.info("Finished in " + (System.currentTimeMillis() - start) + "ms.");

		log.info("Testing predictions...");
		start = System.currentTimeMillis();
		BigDecimal[][] predictions = predictor.predict(network, testData.getInputs());
		log.info("Finished in " + (System.currentTimeMillis() - start) + "ms.");

		int correctCount = 0;
		int bestProbabilityCount = 0;
		boolean wasIncorrect;
		for (int i = 0; i < predictions.length; i++) {
			wasIncorrect = false;

			BigDecimal[] inputs = testData.getInputs()[i];

			log.info("Inputs: {}", Arrays.toString(inputs));

			BigDecimal highestProbability = BigDecimal.ZERO;
			int indexOfHighestProbability = -1;

			for (int j = 0; j < predictions[i].length; j++) {
				BigDecimal prediction = predictions[i][j];
				BigDecimal expected = testData.getOutputs()[i][j];

				log.info("Expected: {}, Prediction: {}", expected, prediction);

				if (network.getProblemType() == ProblemType.CLASSIFICATION) {
					if (highestProbability.compareTo(prediction) < 0) {
						highestProbability = prediction;
						indexOfHighestProbability = j;
					}
				}

				// We can't test the exact values of 1 and 0 since the output from the network is a decimal value
				if (!wasIncorrect && prediction.subtract(expected).abs().compareTo(marginOfErrorRegression) > 0) {
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

		log.info("Neural network achieved " + correctCount + " correct out of " + predictions.length + " total.");
		log.info("Percentage correct: " + (int) ((((double) correctCount / (double) predictions.length) * 100.0) + 0.5));

		if (network.getProblemType() == ProblemType.CLASSIFICATION) {
			log.info("Classification achieved " + bestProbabilityCount + " most probable out of " + predictions.length
					+ " total.");
			log.info("Percentage most probable: " + (int) ((((double) bestProbabilityCount / (double) predictions.length)
					* 100.0) + 0.5));
		}

		if (inputFileName == null || inputFileName.isEmpty() || continueTraining) {
			NetworkMapper.saveToFile(network, outputFileName);
		}
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