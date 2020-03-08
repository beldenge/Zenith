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

package com.ciphertool.zenith.inference;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.config.ApplicationConfiguration;
import com.ciphertool.zenith.inference.entities.config.GeneticAlgorithmConfiguration;
import com.ciphertool.zenith.inference.entities.config.SimulatedAnnealingConfiguration;
import com.ciphertool.zenith.inference.optimizer.GeneticAlgorithmSolutionOptimizer;
import com.ciphertool.zenith.inference.optimizer.SimulatedAnnealingSolutionOptimizer;
import com.ciphertool.zenith.inference.optimizer.SolutionOptimizer;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class InferenceApplication implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private Cipher cipher;

    @Autowired
    protected List<PlaintextTransformationStep> plaintextTransformationSteps;

    @Autowired
    private List<SolutionOptimizer> optimizers;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    private SolutionOptimizer solutionOptimizer;

    public static void main(String[] args) {
        SpringApplication.run(InferenceApplication.class, args).close();
    }

    @Override
    public void run(String... arg0) {
        List<String> existentOptimizers = optimizers.stream()
                .map(optimizer -> optimizer.getClass().getSimpleName())
                .collect(Collectors.toList());

        String optimizerName = applicationConfiguration.getSelectedOptimizer().getName();

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

        Map<String, Object> configuration = new HashMap<>();

        SimulatedAnnealingConfiguration simulatedAnnealingConfiguration = applicationConfiguration.getSimulatedAnnealingConfiguration();

        configuration.put(SimulatedAnnealingSolutionOptimizer.SAMPLER_ITERATIONS, simulatedAnnealingConfiguration.getSamplerIterations());
        configuration.put(SimulatedAnnealingSolutionOptimizer.ANNEALING_TEMPERATURE_MIN, simulatedAnnealingConfiguration.getAnnealingTemperatureMin());
        configuration.put(SimulatedAnnealingSolutionOptimizer.ANNEALING_TEMPERATURE_MAX, simulatedAnnealingConfiguration.getAnnealingTemperatureMax());

        GeneticAlgorithmConfiguration geneticAlgorithmConfiguration = applicationConfiguration.getGeneticAlgorithmConfiguration();

        configuration.put(GeneticAlgorithmSolutionOptimizer.POPULATION_SIZE, geneticAlgorithmConfiguration.getPopulationSize());
        configuration.put(GeneticAlgorithmSolutionOptimizer.NUMBER_OF_GENERATIONS, geneticAlgorithmConfiguration.getNumberOfGenerations());
        configuration.put(GeneticAlgorithmSolutionOptimizer.ELITISM, geneticAlgorithmConfiguration.getElitism());
        configuration.put(GeneticAlgorithmSolutionOptimizer.POPULATION_NAME, geneticAlgorithmConfiguration.getPopulationName());
        configuration.put(GeneticAlgorithmSolutionOptimizer.LATTICE_ROWS, geneticAlgorithmConfiguration.getLatticeRows());
        configuration.put(GeneticAlgorithmSolutionOptimizer.LATTICE_COLUMNS, geneticAlgorithmConfiguration.getLatticeColumns());
        configuration.put(GeneticAlgorithmSolutionOptimizer.LATTICE_WRAP_AROUND, geneticAlgorithmConfiguration.getLatticeWrapAround());
        configuration.put(GeneticAlgorithmSolutionOptimizer.LATTICE_RADIUS, geneticAlgorithmConfiguration.getLatticeRadius());
        configuration.put(GeneticAlgorithmSolutionOptimizer.BREEDER_NAME, geneticAlgorithmConfiguration.getBreederName());
        configuration.put(GeneticAlgorithmSolutionOptimizer.CROSSOVER_ALGORITHM_NAME, geneticAlgorithmConfiguration.getCrossoverAlgorithmName());
        configuration.put(GeneticAlgorithmSolutionOptimizer.MUTATION_ALGORITHM_NAME, geneticAlgorithmConfiguration.getMutationAlgorithmName());
        configuration.put(GeneticAlgorithmSolutionOptimizer.MUTATION_RATE, geneticAlgorithmConfiguration.getMutationRate());
        configuration.put(GeneticAlgorithmSolutionOptimizer.MAX_MUTATIONS_PER_INDIVIDUAL, geneticAlgorithmConfiguration.getMaxMutationsPerIndividual());
        configuration.put(GeneticAlgorithmSolutionOptimizer.SELECTOR_NAME, geneticAlgorithmConfiguration.getSelectorName());
        configuration.put(GeneticAlgorithmSolutionOptimizer.TOURNAMENT_SELECTOR_ACCURACY, geneticAlgorithmConfiguration.getTournamentSelectorAccuracy());
        configuration.put(GeneticAlgorithmSolutionOptimizer.TOURNAMENT_SIZE, geneticAlgorithmConfiguration.getTournamentSize());

        solutionOptimizer.optimize(cipher, applicationConfiguration.getEpochs(), configuration, plaintextTransformationSteps, null);
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
