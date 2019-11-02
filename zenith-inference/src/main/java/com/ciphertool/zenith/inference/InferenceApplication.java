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
import com.ciphertool.zenith.inference.transformer.ciphertext.*;
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
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class InferenceApplication implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(getClass());

    private final static String CIPHER_TRANSFORMER_SUFFIX = CipherTransformer.class.getSimpleName();

    @Value("${task-executor.pool-size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int corePoolSize;

    @Value("${task-executor.pool-size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int maxPoolSize;

    @Value("${task-executor.queue-capacity}")
    private int queueCapacity;

    @Value("${decipherment.transposition.iterations:1}")
    protected int transpositionIterations;

    @Value("${cipher.name}")
    private String cipherName;

    @Value("${decipherment.optimizer}")
    private String optimizerName;

    @Value("${decipherment.transformers.ciphertext}")
    private List<String> cipherTransformersToUse;

    @Autowired
    private Cipher cipher;

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

        solutionOptimizer.optimize(cipher);
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
