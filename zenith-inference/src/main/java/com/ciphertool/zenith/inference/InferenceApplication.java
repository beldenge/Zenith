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

package com.ciphertool.zenith.inference;

import com.ciphertool.zenith.inference.optimizer.SimulatedAnnealingSolutionOptimizer;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(scanBasePackageClasses = { InferenceApplication.class, LetterNGramDao.class })
public class InferenceApplication implements CommandLineRunner {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Value("${markov.letter.order}")
	private int	markovOrder;

	@Autowired
	private LetterNGramDao letterNGramDao;

	@Autowired
	private SimulatedAnnealingSolutionOptimizer optimizer;

	public static void main(String[] args) {
		SpringApplication.run(InferenceApplication.class, args).close();
	}

	@Override
	public void run(String... arg0) {
		optimizer.optimize();
	}

	@Bean
	public TreeMarkovModel letterMarkovModel() {
		long startFindAll = System.currentTimeMillis();
		log.info("Beginning retrieval of all n-grams.");

		/*
		 * Begin setting up letter n-gram model
		 */
		List<TreeNGram> nGramNodes = letterNGramDao.findAll();

		log.info("Finished retrieving {} n-grams in {}ms.", nGramNodes.size(), (System.currentTimeMillis() - startFindAll));

		TreeMarkovModel letterMarkovModel = new TreeMarkovModel(markovOrder);

		long startAdding = System.currentTimeMillis();
		log.info("Adding nodes to the model.");

		nGramNodes.stream().forEach(letterMarkovModel::addNode);

		log.info("Finished adding nodes to the letter n-gram model in {}ms.", (System.currentTimeMillis() - startAdding));

		List<TreeNGram> firstOrderNodes = new ArrayList<>(letterMarkovModel.getRootNode().getTransitions().values());

		long totalNumberOfNgrams = firstOrderNodes.stream()
				.mapToLong(TreeNGram::getCount)
				.sum();

		letterMarkovModel.getRootNode().setCount(totalNumberOfNgrams);

		Double unknownLetterNGramProbability = 1d / (double) totalNumberOfNgrams;
		letterMarkovModel.setUnknownLetterNGramProbability(unknownLetterNGramProbability);
		letterMarkovModel.setUnknownLetterNGramLogProbability(Math.log(unknownLetterNGramProbability));

		return letterMarkovModel;
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		builder.customizers((restTemplate) -> {
			PoolingHttpClientConnectionManager connectionManager = new
					PoolingHttpClientConnectionManager();
			connectionManager.setMaxTotal(10);
			connectionManager.setDefaultMaxPerRoute(10);

			CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(connectionManager).build();

			HttpComponentsClientHttpRequestFactory httpReqFactory = new HttpComponentsClientHttpRequestFactory(httpclient);
			httpReqFactory.setReadTimeout(5000);
			httpReqFactory.setConnectionRequestTimeout(5000);
			httpReqFactory.setConnectTimeout(5000);

			restTemplate.setRequestFactory(httpReqFactory);
		});

		return builder.build();
	}
}
