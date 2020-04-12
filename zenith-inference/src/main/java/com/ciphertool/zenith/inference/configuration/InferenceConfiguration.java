/*
 * Copyright 2017-2020 George Belden
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
import com.ciphertool.zenith.inference.entities.ZenithTransformer;
import com.ciphertool.zenith.inference.entities.config.ApplicationConfiguration;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.transformer.ciphertext.CiphertextTransformationManager;
import com.ciphertool.zenith.inference.transformer.ciphertext.CiphertextTransformationStep;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.dao.WordNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.entities.WordNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import com.ciphertool.zenith.model.markov.WordNGramModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.validation.constraints.Min;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ComponentScan(basePackages = {
        "com.ciphertool.zenith.genetic.algorithms.crossover",
        "com.ciphertool.zenith.genetic.algorithms.mutation",
        "com.ciphertool.zenith.genetic.algorithms",
        "com.ciphertool.zenith.genetic.algorithms.selection",
        "com.ciphertool.zenith.genetic.population",
        "com.ciphertool.zenith.model.dao",
        "com.ciphertool.zenith.model.archive"
})
public class InferenceConfiguration {
    private Logger log = LoggerFactory.getLogger(getClass());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CONFIG_FILE_NAME = "zenith-config.json";

    @Value("${task-executor.pool-size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int corePoolSize;

    @Value("${task-executor.pool-size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int maxPoolSize;

    @Value("${task-executor.queue-capacity}")
    private int queueCapacity;

    @Value("${cipher.name}")
    private String cipherName;

    @Value("${markov.letter.order}")
    private int markovOrder;

    @Min(1)
    @Value("${language-model.max-ngrams-to-keep}")
    private int maxNGramsToKeep;

    @Value("${decipherment.evaluator.plaintext}")
    private String plaintextEvaluatorName;

    @Value("${application.configuration.file-path}")
    private String configurationFilePath;

    @Value("${language-model.word-ngram.total-token-count}")
    private long wordGramTotalTokenCount;

    @Bean
    public ApplicationConfiguration configuration() {
        // First read ciphers from the classpath
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
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
                break;
            } catch (IOException e) {
                log.error("Unable to read application configuration from file: {}.", resource.getFilename(), e);
                throw new IllegalStateException(e);
            }
        }

        // Secondly, attempt to read ciphers from the local directory on the filesystem
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
                return OBJECT_MAPPER.readValue(file, ApplicationConfiguration.class);
            } catch (IOException e) {
                log.error("Unable to read application configuration from file: {}.", file.getPath(), e);
                throw new IllegalStateException(e);
            }
        }

        // We should only reach this point in the case where the configurationFilePath points to a directory which doesn't actually contain a configuration
        return applicationConfiguration;
    }

    @Bean
    public Cipher cipher(CipherDao cipherDao, CiphertextTransformationManager ciphertextTransformationManager, ApplicationConfiguration applicationConfiguration) {
        Cipher cipher = cipherDao.findByCipherName(cipherName);

        List<CiphertextTransformationStep> transformationSteps = new ArrayList<>(applicationConfiguration.getAppliedCiphertextTransformers().size());

        for (ZenithTransformer transformer : applicationConfiguration.getAppliedCiphertextTransformers()) {
            transformationSteps.add(new CiphertextTransformationStep(transformer.getName(), transformer.getForm() != null ? transformer.getForm().getModel() : null));
        }

        return ciphertextTransformationManager.transform(cipher, transformationSteps);
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

        ArrayMarkovModel letterMarkovModel = new ArrayMarkovModel(markovOrder);

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

        float unknownLetterNGramProbability = 1f / (float) letterMarkovModel.getTotalNGramCount();
        letterMarkovModel.setUnknownLetterNGramProbability(unknownLetterNGramProbability);
        letterMarkovModel.setUnknownLetterNGramLogProbability((float) Math.log(unknownLetterNGramProbability));

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
    public PlaintextEvaluator plaintextEvaluator(List<PlaintextEvaluator> plaintextEvaluators) {
        for (PlaintextEvaluator evaluator : plaintextEvaluators) {
            if (evaluator.getClass().getSimpleName().equals(plaintextEvaluatorName)) {
                return evaluator;
            }
        }

        List<String> existentPlaintextEvaluators = plaintextEvaluators.stream()
                .map(evaluator -> evaluator.getClass().getSimpleName())
                .collect(Collectors.toList());

        log.error("The PlaintextEvaluator with name {} does not exist.  Please use a name from the following: {}", plaintextEvaluatorName, existentPlaintextEvaluators);
        throw new IllegalArgumentException("The PlaintextEvaluator with name " + plaintextEvaluatorName + " does not exist.");
    }

    @Bean
    public List<PlaintextTransformationStep> plaintextTransformationSteps(ApplicationConfiguration applicationConfiguration) {
        List<PlaintextTransformationStep> plaintextTransformationSteps = new ArrayList<>(applicationConfiguration.getAppliedPlaintextTransformers().size());

        for (ZenithTransformer transformer : applicationConfiguration.getAppliedPlaintextTransformers()) {
            plaintextTransformationSteps.add(new PlaintextTransformationStep(transformer.getName(), transformer.getForm() != null ? transformer.getForm().getModel() : null));
        }

        return plaintextTransformationSteps;
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
