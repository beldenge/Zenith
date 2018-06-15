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

import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.neural.generate.SampleGenerator;
import com.ciphertool.zenith.neural.initialize.Initialization;
import com.ciphertool.zenith.neural.initialize.InitializationType;
import com.ciphertool.zenith.neural.model.NeuralNetwork;
import com.ciphertool.zenith.neural.model.ProblemType;
import com.ciphertool.zenith.neural.predict.PredictionStats;
import com.ciphertool.zenith.neural.predict.Predictor;
import com.ciphertool.zenith.neural.train.SupervisedTrainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@EnableAsync
@Validated
@ConfigurationProperties(prefix = "network")
@SpringBootApplication(scanBasePackageClasses = { NeuralNetworkApplication.class, LetterNGramDao.class, TaskExecutorConfiguration.class })
public class NeuralNetworkApplication implements CommandLineRunner {
	private static Logger			log	= LoggerFactory.getLogger(NeuralNetworkApplication.class);

	@Min(1)
	private int						epochs = 1;

	private String					inputFileName;

	@NotBlank
	private String					outputFileName;

	private boolean					continueTraining = true;

	@Min(1)
	private int						batchSize = 1;

	@Min(1)
	private int						inputLayerNeurons;

	@NotEmpty
	private String[]				hiddenLayers;

	@Min(1)
	private int						outputLayerNeurons;

	private Float					biasWeight;

	private String 					initializationType;

	@Autowired
	private ThreadPoolTaskExecutor	taskExecutor;

	@Autowired
	private SupervisedTrainer		trainer;

	@Autowired
	private Predictor				predictor;

	@Autowired
	private SampleGenerator 		generator;

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

		// FIXME: re-implement saving of network to file
		//if (inputFileName != null && !inputFileName.isEmpty()) {
		//	network = new NeuralNetwork(NetworkMapper.loadFromFile(inputFileName));
		//} else {
		network = new NeuralNetwork(inputLayerNeurons, hiddenLayers, outputLayerNeurons, biasWeight);

		Initialization initialization = InitializationType.valueOf(initializationType).getInitialization();
		initialization.initialize(network);
		//}

		long start = System.currentTimeMillis();

		if (inputFileName == null || inputFileName.isEmpty() || continueTraining) {
			log.info("Training network...");

			for (int i = 1; i <= epochs; i++) {
				trainer.train(network, batchSize);
				log.info("Completed epoch {}", i);
				generator.resetSamples();
			}

			log.info("Finished training in " + (System.currentTimeMillis() - start) + "ms.");
		}

		log.info("Testing predictions...");
		start = System.currentTimeMillis();
		PredictionStats predictionStats = predictor.predict(network);
		log.info("Finished in " + (System.currentTimeMillis() - start) + "ms.");

		log.info("Neural network achieved " + predictionStats.getCorrectCount() + " correct out of " + predictionStats.getTotalPredictions() + " total.");
		log.info("Percentage correct: " + (int) ((((float) predictionStats.getCorrectCount() / (float) predictionStats.getTotalPredictions()) * 100.0) + 0.5));

		if (network.getProblemType() == ProblemType.CLASSIFICATION) {
			log.info("Classification achieved " + predictionStats.getBestProbabilityCount() + " most probable out of " + predictionStats.getTotalPredictions()
					+ " total.");
			log.info("Percentage most probable: " + (int) ((((float) predictionStats.getBestProbabilityCount() / (float) predictionStats.getTotalPredictions())
					* 100.0) + 0.5));
		}
	}

	public void setEpochs(int epochs) {
		this.epochs = epochs;
	}

	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public void setContinueTraining(boolean continueTraining) {
		this.continueTraining = continueTraining;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public void setInputLayerNeurons(int inputLayerNeurons) {
		this.inputLayerNeurons = inputLayerNeurons;
	}

	public void setHiddenLayers(String[] hiddenLayers) {
		this.hiddenLayers = hiddenLayers;
	}

	public void setOutputLayerNeurons(int outputLayerNeurons) {
		this.outputLayerNeurons = outputLayerNeurons;
	}

	public void setBiasWeight(Float biasWeight) {
		this.biasWeight = biasWeight;
	}

	public void setInitializationType(String initializationType) {
		this.initializationType = initializationType;
	}
}