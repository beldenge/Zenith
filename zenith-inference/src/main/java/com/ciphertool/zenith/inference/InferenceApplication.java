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
import com.ciphertool.zenith.inference.optimizer.AbstractSolutionOptimizer;
import com.ciphertool.zenith.inference.optimizer.GeneticAlgorithmSolutionOptimizer;
import com.ciphertool.zenith.inference.optimizer.SimulatedAnnealingSolutionOptimizer;
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

import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class InferenceApplication implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${decipherment.optimizer}")
    private String optimizerName;

    @Value("${decipherment.epochs:1}")
    private int epochs;

    @Value("${decipherment.known-solution.correctness-threshold:0.9}")
    private float knownSolutionCorrectnessThreshold;

    @Value("${simulated-annealing.sampler.iterations}")
    private int samplerIterations;

    @Value("${simulated-annealing.temperature.min}")
    private float annealingTemperatureMin;

    @Value("${simulated-annealing.temperature.max}")
    private float annealingTemperatureMax;

    @Value("${genetic-algorithm.population.size}")
    private int populationSize;

    @Value("${genetic-algorithm.number-of-generations}")
    private int numberOfGenerations;

    @Value("${genetic-algorithm.elitism}")
    private int elitism;

    @Value("${genetic-algorithm.mutation.rate}")
    private Double mutationRate;

    @Value("${genetic-algorithm.mutation.max-per-individual}")
    private int maxMutationsPerIndividual;

    @Value("${genetic-algorithm.population.type}")
    private String populationName;

    @Value("${genetic-algorithm.population.lattice.rows}")
    private int latticeRows;

    @Value("${genetic-algorithm.population.lattice.columns}")
    private int latticeColumns;

    @Value("${genetic-algorithm.population.lattice.wrap-around}")
    private boolean latticeWrapAround;

    @Min(1)
    @Value("${genetic-algorithm.population.lattice.selection-radius:1}")
    private int latticeSelectionRadius;

    @Value("${genetic-algorithm.breeder.implementation}")
    private String breederName;

    @Value("${genetic-algorithm.crossover.implementation}")
    private String crossoverAlgorithmName;

    @Value("${genetic-algorithm.mutation.implementation}")
    private String mutationAlgorithmName;

    @Value("${genetic-algorithm.selection.implementation}")
    private String selectorName;

    @Value("${genetic-algorithm.selection.tournament.accuracy}")
    private Double tournamentSelectionAccuracy;

    @Value("${genetic-algorithm.selection.tournament.size}")
    private int tournamentSize;

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

        Map<String, Object> configuration = new HashMap<>();
        configuration.put(AbstractSolutionOptimizer.KNOWN_SOLUTION_CORRECTNESS_THRESHOLD, knownSolutionCorrectnessThreshold);
        configuration.put(SimulatedAnnealingSolutionOptimizer.SAMPLER_ITERATIONS, samplerIterations);
        configuration.put(SimulatedAnnealingSolutionOptimizer.ANNEALING_TEMPERATURE_MIN, annealingTemperatureMin);
        configuration.put(SimulatedAnnealingSolutionOptimizer.ANNEALING_TEMPERATURE_MAX, annealingTemperatureMax);
        configuration.put(GeneticAlgorithmSolutionOptimizer.POPULATION_SIZE, populationSize);
        configuration.put(GeneticAlgorithmSolutionOptimizer.NUMBER_OF_GENERATIONS, numberOfGenerations);
        configuration.put(GeneticAlgorithmSolutionOptimizer.ELITISM, elitism);
        configuration.put(GeneticAlgorithmSolutionOptimizer.POPULATION_NAME, populationName);
        configuration.put(GeneticAlgorithmSolutionOptimizer.LATTICE_ROWS, latticeRows);
        configuration.put(GeneticAlgorithmSolutionOptimizer.LATTICE_COLUMNS, latticeColumns);
        configuration.put(GeneticAlgorithmSolutionOptimizer.LATTICE_WRAP_AROUND, latticeWrapAround);
        configuration.put(GeneticAlgorithmSolutionOptimizer.LATTICE_RADIUS, latticeSelectionRadius);
        configuration.put(GeneticAlgorithmSolutionOptimizer.BREEDER_NAME, breederName);
        configuration.put(GeneticAlgorithmSolutionOptimizer.CROSSOVER_ALGORITHM_NAME, crossoverAlgorithmName);
        configuration.put(GeneticAlgorithmSolutionOptimizer.MUTATION_ALGORITHM_NAME, mutationAlgorithmName);
        configuration.put(GeneticAlgorithmSolutionOptimizer.MUTATION_RATE, mutationRate);
        configuration.put(GeneticAlgorithmSolutionOptimizer.MAX_MUTATIONS_PER_INDIVIDUAL, maxMutationsPerIndividual);
        configuration.put(GeneticAlgorithmSolutionOptimizer.SELECTOR_NAME, selectorName);
        configuration.put(GeneticAlgorithmSolutionOptimizer.TOURNAMENT_SELECTOR_ACCURACY, tournamentSelectionAccuracy);
        configuration.put(GeneticAlgorithmSolutionOptimizer.TOURNAMENT_SIZE, tournamentSize);

        solutionOptimizer.optimize(cipher, epochs, configuration, null);
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
