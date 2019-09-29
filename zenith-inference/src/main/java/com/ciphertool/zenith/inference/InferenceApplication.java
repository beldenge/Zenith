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
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.optimizer.SolutionOptimizer;
import com.ciphertool.zenith.inference.transformer.ciphertext.*;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.MapMarkovModel;
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

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication(scanBasePackages = {
        "com.ciphertool.zenith.inference",
        "com.ciphertool.zenith.model.dao",
        "com.ciphertool.zenith.model.archive"
})
public class InferenceApplication implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(getClass());

    private final static String CIPHER_TRANSFORMER_SUFFIX = CipherTransformer.class.getSimpleName();
    private final static String PLAINTEXT_TRANSFORMER_SUFFIX = PlaintextTransformer.class.getSimpleName();

    @Configuration
    @ComponentScan({
            "com.ciphertool.zenith.genetic.algorithms.crossover",
            "com.ciphertool.zenith.genetic.algorithms.mutation",
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

    @Min(1)
    @Value("${language-model.ngram.minimum-count:1}")
    private int minimumCount;

    @Value("${decipherment.transposition.iterations:1}")
    protected int transpositionIterations;

    @Value("${cipher.name}")
    private String cipherName;

    @Value("${decipherment.optimizer}")
    private String optimizerName;

    @Value("${decipherment.transformers.ciphertext}")
    private List<String> cipherTransformersToUse;

    @Value("${decipherment.transformers.plaintext}")
    private List<String> plaintextTransformersToUse;

    @Value("${decipherment.evaluator.plaintext}")
    private String plaintextEvaluatorName;

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
    public Cipher cipher(CipherDao cipherDao, List<CipherTransformer> cipherTransformers) {
        Cipher cipher =  cipherDao.findByCipherName(cipherName);

        if (cipherTransformers == null || cipherTransformers.isEmpty()) {
            return cipher;
        }

        List<CipherTransformer> toUse = new ArrayList<>(cipherTransformersToUse.size());
        List<String> existentCipherTransformers = cipherTransformers.stream()
                .map(transformer -> transformer.getClass().getSimpleName().replace(CIPHER_TRANSFORMER_SUFFIX, ""))
                .collect(Collectors.toList());

        for (String transformerName : cipherTransformersToUse) {
            String transformerNameBeforeParenthesis = transformerName.contains("(") ? transformerName.substring(0, transformerName.indexOf('(')) : transformerName;

            if (!existentCipherTransformers.contains(transformerNameBeforeParenthesis)) {
                log.error("The CipherTransformer with name {} does not exist.  Please use a name from the following: {}", transformerNameBeforeParenthesis, existentCipherTransformers);
                throw new IllegalArgumentException("The CipherTransformer with name " + transformerNameBeforeParenthesis + " does not exist.");
            }

            for (CipherTransformer cipherTransformer : cipherTransformers) {
                if (cipherTransformer.getClass().getSimpleName().replace(CIPHER_TRANSFORMER_SUFFIX, "").equals(transformerNameBeforeParenthesis)) {
                    if (transformerName.contains("(") && transformerName.endsWith(")")) {
                        String parameter = transformerName.substring(transformerName.indexOf('(') + 1, transformerName.length() - 1);

                        if (cipherTransformer instanceof TranspositionCipherTransformer) {
                            String transpositionKeyString = parameter;
                            TranspositionCipherTransformer nextTransformer = new TranspositionCipherTransformer(transpositionKeyString, transpositionIterations);
                            nextTransformer.init();
                            toUse.add(nextTransformer);
                        } else if (cipherTransformer instanceof UnwrapTranspositionCipherTransformer) {
                            String transpositionKeyString = parameter;
                            UnwrapTranspositionCipherTransformer nextTransformer = new UnwrapTranspositionCipherTransformer(transpositionKeyString, transpositionIterations);
                            nextTransformer.init();
                            toUse.add(nextTransformer);
                        } else if (cipherTransformer instanceof PeriodCipherTransformer) {
                            int period = Integer.parseInt(parameter);
                            toUse.add(new PeriodCipherTransformer(period));
                        } else if (cipherTransformer instanceof UnwrapPeriodCipherTransformer) {
                            int period = Integer.parseInt(parameter);
                            toUse.add(new UnwrapPeriodCipherTransformer(period));
                        } else {
                            throw new IllegalArgumentException("The CipherTransformer with name " + transformerNameBeforeParenthesis + " does not accept parameters.");
                        }
                    } else {
                        toUse.add(cipherTransformer);
                    }

                    break;
                }
            }
        }

        for (CipherTransformer cipherTransformer : toUse) {
            cipher = cipherTransformer.transform(cipher);
        }

        return cipher;
    }

    @Bean
    public MapMarkovModel letterMarkovModel(LetterNGramDao letterNGramDao) {
        long startFindAll = System.currentTimeMillis();
        log.info("Beginning retrieval of all n-grams.");

        /*
         * Begin setting up letter n-gram model
         */
        List<TreeNGram> nGramNodes = letterNGramDao.findAll();

        log.info("Finished retrieving {} n-grams in {}ms.", nGramNodes.size(), (System.currentTimeMillis() - startFindAll));

        MapMarkovModel letterMarkovModel = new MapMarkovModel(markovOrder);

        long startAdding = System.currentTimeMillis();
        log.info("Adding nodes to the model.");

        nGramNodes.stream()
                .filter(node -> node.getCount() >= minimumCount)
                .forEach(letterMarkovModel::addNode);

        log.info("Finished adding {} nodes to the letter n-gram model in {}ms.", letterMarkovModel.getMapSize(), (System.currentTimeMillis() - startAdding));

        Double unknownLetterNGramProbability = 1d / (double) letterMarkovModel.getTotalNumberOfNgrams();
        letterMarkovModel.setUnknownLetterNGramProbability(unknownLetterNGramProbability);
        letterMarkovModel.setUnknownLetterNGramLogProbability(Math.log(unknownLetterNGramProbability));

        return letterMarkovModel;
    }

    @Bean
    public List<PlaintextTransformer> activePlaintextTransformers(List<PlaintextTransformer> plaintextTransformers) {
        if (plaintextTransformers == null || plaintextTransformers.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<PlaintextTransformer> toUse = new ArrayList<>(plaintextTransformersToUse.size());
        List<String> existentPlaintextTransformers = plaintextTransformers.stream()
                .map(transformer -> transformer.getClass().getSimpleName().replace(PLAINTEXT_TRANSFORMER_SUFFIX, ""))
                .collect(Collectors.toList());

        for (String transformerName : plaintextTransformersToUse) {
            if (!existentPlaintextTransformers.contains(transformerName)) {
                log.error("The PlaintextTransformer with name {} does not exist.  Please use a name from the following: {}", transformerName, existentPlaintextTransformers);
                throw new IllegalArgumentException("The PlaintextTransformer with name " + transformerName + " does not exist.");
            }

            for (PlaintextTransformer plaintextTransformer : plaintextTransformers) {
                if (plaintextTransformer.getClass().getSimpleName().replace(PLAINTEXT_TRANSFORMER_SUFFIX, "").equals(transformerName)) {
                    toUse.add(plaintextTransformer);

                    break;
                }
            }
        }

        return toUse;
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
