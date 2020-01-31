/**
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

package com.ciphertool.zenith.inference;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.optimizer.SolutionOptimizer;
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

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class InferenceApplication implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${decipherment.optimizer}")
    private String optimizerName;

    @Value("${decipherment.epochs:1}")
    private int epochs;

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

        solutionOptimizer.optimize(cipher, epochs);
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
