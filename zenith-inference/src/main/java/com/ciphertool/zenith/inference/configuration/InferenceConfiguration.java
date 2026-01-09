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

package com.ciphertool.zenith.inference.configuration;

import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.FormComponentDto;
import com.ciphertool.zenith.inference.entities.config.ApplicationConfiguration;
import com.ciphertool.zenith.inference.transformer.ciphertext.CiphertextTransformationManager;
import com.ciphertool.zenith.inference.transformer.ciphertext.TransformationStep;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.dao.WordNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.entities.WordNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import com.ciphertool.zenith.model.markov.WordNGramModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@ComponentScan(basePackages = {
        "com.ciphertool.zenith.genetic.operators.crossover",
        "com.ciphertool.zenith.genetic.operators.mutation",
        "com.ciphertool.zenith.genetic.operators.selection",
        "com.ciphertool.zenith.genetic.operators.speciation",
        "com.ciphertool.zenith.genetic.operators.algorithm",
        "com.ciphertool.zenith.genetic.population",
        "com.ciphertool.zenith.model.dao",
        "com.ciphertool.zenith.model.archive"
})
public class InferenceConfiguration {
    private Logger log = LoggerFactory.getLogger(getClass());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CONFIG_FILE_NAME = "zenith.json";

    @Autowired
    private Validator validator;

    @Value("${task-executor.pool-size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int corePoolSize;

    @Value("${task-executor.pool-size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int maxPoolSize;

    @Value("${task-executor.queue-capacity}")
    private int queueCapacity;

    @Value("${markov.letter.order}")
    private int markovOrder;

    @Min(1)
    @Value("${language-model.max-ngrams-to-keep}")
    private int maxNGramsToKeep;

    @Value("${application.configuration.file-path}")
    private String configurationFilePath;

    @Value("${language-model.word-ngram.total-token-count}")
    private long wordGramTotalTokenCount;

    @Bean
    public ApplicationConfiguration configuration() {
        // First read configuration from the classpath
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        Resource[] resources;

        try {
            resources = resolver.getResources("classpath*:/config/" + CONFIG_FILE_NAME);
        } catch (IOException ioe) {
            throw new IllegalStateException("Unable to read configuration from classpath directory=config/", ioe);
        }

        ApplicationConfiguration applicationConfiguration = null;

        for (Resource resource : resources) {
            try (InputStream inputStream = resource.getInputStream()) {
                applicationConfiguration = OBJECT_MAPPER.readValue(inputStream, ApplicationConfiguration.class);
                validateInputWithInjectedValidator(applicationConfiguration);
                break;
            } catch (IOException e) {
                log.error("Unable to read application configuration from file: {}.", resource.getFilename(), e);
                throw new IllegalStateException(e);
            }
        }

        // Secondly, attempt to read configuration from the local directory on the filesystem
        File localConfigDirectory = new File(Paths.get(configurationFilePath).toAbsolutePath().toString());

        if (!localConfigDirectory.exists() || !localConfigDirectory.isDirectory()) {
            return applicationConfiguration;
        }

        for (File file : localConfigDirectory.listFiles()) {
            if (!file.getName().equals(CONFIG_FILE_NAME)) {
                log.debug("Skipping file in configuration directory due to invalid file name.  File={}", file.getName());
                continue;
            }

            try {
                applicationConfiguration = OBJECT_MAPPER.readValue(file, ApplicationConfiguration.class);
                validateInputWithInjectedValidator(applicationConfiguration);
                return applicationConfiguration;
            } catch (IOException e) {
                log.error("Unable to read application configuration from file: {}.", file.getPath(), e);
                throw new IllegalStateException(e);
            }
        }

        // We should only reach this point in the case where the configurationFilePath points to a directory which doesn't actually contain a configuration
        return applicationConfiguration;
    }

    private void validateInputWithInjectedValidator(ApplicationConfiguration configuration) {
        Set<ConstraintViolation<ApplicationConfiguration>> violations = validator.validate(configuration);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    @Bean
    public Cipher cipher(CipherDao cipherDao, CiphertextTransformationManager ciphertextTransformationManager, ApplicationConfiguration applicationConfiguration) {
        Cipher cipher = cipherDao.findByCipherName(applicationConfiguration.getSelectedCipher());

        if (CollectionUtils.isNotEmpty(applicationConfiguration.getAppliedCiphertextTransformers())) {
            List<TransformationStep> transformationSteps = new ArrayList<>(applicationConfiguration.getAppliedCiphertextTransformers().size());

            for (FormComponentDto transformer : applicationConfiguration.getAppliedCiphertextTransformers()) {
                transformationSteps.add(new TransformationStep(transformer.getName(), transformer.getForm() != null ? transformer.getForm().getModel() : null));
            }

            cipher = ciphertextTransformationManager.transform(cipher, transformationSteps);
        }

        return cipher;
    }

    @Bean
    public ArrayMarkovModel letterMarkovModel(LetterNGramDao letterNGramDao) {
        long startFindAll = System.currentTimeMillis();
        log.info("Beginning retrieval of all n-grams.");

        /*
         * Begin setting up letter n-gram model
         */
        List<TreeNGram> nGramNodes = letterNGramDao.findAll();

        log.info("Finished retrieving {} n-grams in {}ms.", nGramNodes.size(), (System.currentTimeMillis() - startFindAll));

        long totalNGramCount = nGramNodes.stream()
                .filter(node -> node.getOrder() == 1)
                .mapToLong(TreeNGram::getCount)
                .sum();

        ArrayMarkovModel letterMarkovModel = new ArrayMarkovModel(markovOrder, 1f / (float) totalNGramCount);

        long startAdding = System.currentTimeMillis();
        log.info("Adding nodes to the model.");

        nGramNodes.stream()
                .filter(node -> node.getOrder() == 1)
                .forEach(letterMarkovModel::addNode);

        nGramNodes.stream()
                .filter(node -> node.getOrder() == markovOrder)
                .sorted(Comparator.comparing(TreeNGram::getCount).reversed())
                .limit(maxNGramsToKeep)
                .forEach(letterMarkovModel::addNode);

        log.info("Finished adding {} nodes to the letter n-gram model in {}ms.", letterMarkovModel.getMapSize(), (System.currentTimeMillis() - startAdding));

        return letterMarkovModel;
    }

    @Bean
    public WordNGramModel wordUnigramModel(WordNGramDao wordNGramDao) {
        WordNGramModel wordUnigramModel = new WordNGramModel();

        List<WordNGram> wordUnigrams = wordNGramDao.findAllUnigrams();

        for (WordNGram wordUnigram : wordUnigrams) {
            wordUnigram.setOrder(1);

            if (wordUnigramModel.contains(wordUnigram.getNGram())) {
                log.debug("Unigram model already contains {}", wordUnigram.getNGram());
                wordUnigram.setCount(wordUnigramModel.getCount(wordUnigram.getNGram()) + wordUnigram.getCount());
            }

            wordUnigram.setLogProbability(Math.log10((double) wordUnigram.getCount() / (double) wordGramTotalTokenCount));
            wordUnigramModel.putWordNGram(wordUnigram.getNGram(), wordUnigram);
        }

        return wordUnigramModel;
    }

    @Bean
    public WordNGramModel wordBigramModel(WordNGramDao wordNGramDao, WordNGramModel wordUnigramModel) {
        WordNGramModel wordBigramModel = new WordNGramModel();

        List<WordNGram> wordBigrams = wordNGramDao.findAllBigrams();

        for (WordNGram wordBigram : wordBigrams) {
            wordBigram.setOrder(2);

            if (wordBigramModel.contains(wordBigram.getNGram())) {
                log.debug("Bigram model already contains {}", wordBigram.getNGram());
                wordBigram.setCount(wordBigramModel.getCount(wordBigram.getNGram()) + wordBigram.getCount());
            }

            String word1 = wordBigram.getNGram().split("\\s+")[0];
            double bigramProbability = Math.log10((double) wordBigram.getCount() / (double) wordGramTotalTokenCount);
            if (wordUnigramModel.contains(word1)) {
                wordBigram.setLogProbability(bigramProbability - wordUnigramModel.getLogProbability(word1));
            } else {
                wordBigram.setLogProbability(bigramProbability);
            }

            wordBigramModel.putWordNGram(wordBigram.getNGram(), wordBigram);
        }

        return wordBigramModel;
    }

    @Bean
    public List<TransformationStep> plaintextTransformationSteps(ApplicationConfiguration applicationConfiguration, List<PlaintextTransformer> plaintextTransformers) {
        List<TransformationStep> plaintextTransformationSteps = new ArrayList<>();

        List<String> existentPlaintextTransformers = plaintextTransformers.stream()
                .map(transformer -> transformer.getClass().getSimpleName())
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(applicationConfiguration.getAppliedPlaintextTransformers())) {
            for (FormComponentDto transformer : applicationConfiguration.getAppliedPlaintextTransformers()) {
                String transformerName = transformer.getName();

                if (!existentPlaintextTransformers.contains(transformer.getName())) {
                    log.error("The PlaintextTransformer with name {} does not exist.  Please use a name from the following: {}", transformerName, existentPlaintextTransformers);
                    throw new IllegalArgumentException("The PlaintextTransformer with name " + transformerName + " does not exist.");
                }

                plaintextTransformationSteps.add(new TransformationStep(transformer.getName(), transformer.getForm() != null ? transformer.getForm().getModel() : null));
            }
        }

        return plaintextTransformationSteps;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        builder.customizers((restTemplate) -> {
            PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setMaxConnTotal(10)
                    .setMaxConnPerRoute(10)
                    .build();

            RequestConfig requestConfig = RequestConfig.custom()
                    .setResponseTimeout(Timeout.ofMilliseconds(5000))
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(5000))
                    .build();

            HttpClient httpclient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfig)
                    .build();

            HttpComponentsClientHttpRequestFactory httpReqFactory = new HttpComponentsClientHttpRequestFactory(httpclient);

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

    @Bean("nestedGeneticAlgorithmTaskExecutor")
    public ThreadPoolTaskExecutor nestedGeneticAlgorithmTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setKeepAliveSeconds(5);
        taskExecutor.setAllowCoreThreadTimeOut(true);

        return taskExecutor;
    }
}
