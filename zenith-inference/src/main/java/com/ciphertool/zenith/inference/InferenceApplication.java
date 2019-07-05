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

import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.optimizer.SolutionOptimizer;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication(scanBasePackages = {
        "com.ciphertool.zenith.inference",
        "com.ciphertool.zenith.model.dao"
})
public class InferenceApplication implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Configuration
    @ComponentScan({
            "com.ciphertool.zenith.genetic.algorithms.crossover.impl",
            "com.ciphertool.zenith.genetic.algorithms.mutation.impl",
            "com.ciphertool.zenith.genetic.algorithms",
            "com.ciphertool.zenith.genetic.algorithms.selection",
            "com.ciphertool.zenith.genetic.population"
    })
    @ConditionalOnProperty(value = "decipherment.optimizer", havingValue = "GeneticAlgorithmSolutionOptimizer")
    public class GeneticAlgorithmConfiguration {
    }

    @Value("${task-executor.pool-size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int corePoolSize;

    @Value("${task-executor.pool-size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int maxPoolSize;

    @Value("${task-executor.queue-capacity}")
    private int queueCapacity;

    @Value("${markov.letter.order}")
    private int markovOrder;

    @Value("${cipher.name}")
    private String cipherName;

    @Value("${decipherment.optimizer}")
    private String optimizerName;

    @Autowired
    private LetterNGramDao letterNGramDao;

    @Autowired
    private List<SolutionOptimizer> optimizers;

    private SolutionOptimizer solutionOptimizer;

    public static void main(String[] args) {
        SpringApplication.run(InferenceApplication.class, args).close();
    }

    @Override
    public void run(String... arg0) {
        List<String> existentOptimizers = optimizers.stream()
                .map(optimizer -> optimizer.getClass().getSimpleName())
                .collect(Collectors.toList());

        for (SolutionOptimizer optimizer : optimizers) {
            if (optimizer.getClass().getSimpleName().equals(optimizerName)) {
                solutionOptimizer = optimizer;
                break;
            }
        }

        if (solutionOptimizer == null) {
            log.error("The SolutionOptimizer with name {} does not exist.  Please use a name from the following: {}", optimizerName, existentOptimizers);
            throw new IllegalArgumentException("The SolutionOptimizer with name " + optimizerName + " does not exist.");
        }

        solutionOptimizer.optimize();
    }

    @Bean
    public Cipher cipher(CipherDao cipherDao) {
        return cipherDao.findByCipherName(cipherName);
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

    @Bean
    @Primary
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
