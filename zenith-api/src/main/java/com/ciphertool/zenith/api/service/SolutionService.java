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

package com.ciphertool.zenith.api.service;

import com.ciphertool.zenith.api.model.*;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.inference.optimizer.AbstractSolutionOptimizer;
import com.ciphertool.zenith.inference.optimizer.GeneticAlgorithmSolutionOptimizer;
import com.ciphertool.zenith.inference.optimizer.SimulatedAnnealingSolutionOptimizer;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationManager;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class SolutionService {
    private static final String TYPE_HEADER_KEY = "type";

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private SimulatedAnnealingSolutionOptimizer simulatedAnnealingOptimizer;

    @Autowired
    private GeneticAlgorithmSolutionOptimizer geneticAlgorithmOptimizer;

    @Autowired
    private PlaintextTransformationManager plaintextTransformationManager;

    @MessageMapping("/solutions")
    public void solve(@Validated @RequestBody SolutionRequest request) {
        Cipher cipher = new Cipher(null, request.getRows(), request.getColumns());

        for (int i = 0; i < request.getCiphertext().length(); i ++) {
            cipher.addCiphertextCharacter(new Ciphertext(i, String.valueOf(request.getCiphertext().charAt(i))));
        }

        Map<String, Object> epochCompleteHeaders = new HashMap<>();
        epochCompleteHeaders.put(TYPE_HEADER_KEY, WebSocketResponseType.EPOCH_COMPLETE);

        CipherSolution cipherSolution;

        Map<String, Object> configuration = new HashMap<>();
        configuration.put(AbstractSolutionOptimizer.KNOWN_SOLUTION_CORRECTNESS_THRESHOLD, request.getKnownSolutionCorrectnessThreshold());

        if (request.getSimulatedAnnealingConfiguration() != null) {
            SimulatedAnnealingConfiguration simulatedAnnealingConfiguration = request.getSimulatedAnnealingConfiguration();
            configuration.put(SimulatedAnnealingSolutionOptimizer.SAMPLER_ITERATIONS, simulatedAnnealingConfiguration.getSamplerIterations());
            configuration.put(SimulatedAnnealingSolutionOptimizer.ANNEALING_TEMPERATURE_MIN, simulatedAnnealingConfiguration.getAnnealingTemperatureMin());
            configuration.put(SimulatedAnnealingSolutionOptimizer.ANNEALING_TEMPERATURE_MAX, simulatedAnnealingConfiguration.getAnnealingTemperatureMax());

            cipherSolution = simulatedAnnealingOptimizer.optimize(cipher, request.getEpochs(), configuration, (i) ->
                    template.convertAndSend("/topic/solutions", new EpochCompleteResponse(i, request.getEpochs()), epochCompleteHeaders)
            );
        } else if (request.getGeneticAlgorithmConfiguration() != null) {
            GeneticAlgorithmConfiguration geneticAlgorithmConfiguration = request.getGeneticAlgorithmConfiguration();
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

            cipherSolution = geneticAlgorithmOptimizer.optimize(cipher, request.getEpochs(), configuration, (i) ->
                    template.convertAndSend("/topic/solutions", new EpochCompleteResponse(i, request.getEpochs()), epochCompleteHeaders)
            );
        } else {
            throw new IllegalStateException("Neither simulated annealing nor genetic algorithm was chosen as the optimization strategy.  No other strategy is currently supported.");
        }

        Map<String, Object> solutionHeaders = new HashMap<>();
        solutionHeaders.put(TYPE_HEADER_KEY, WebSocketResponseType.SOLUTION);

        String solution = cipherSolution.asSingleLineString();
        List<SolutionRequestTransformer> transformers = request.getPlaintextTransformers();

        // FIXME: this needs to transform the plaintext within the optimizer, otherwise we are not optimizing on the right plaintext
        if (transformers != null && !transformers.isEmpty()) {
            List<PlaintextTransformationStep> steps = transformers.stream()
                    .map(SolutionRequestTransformer::asStep)
                    .collect(Collectors.toList());

            solution = plaintextTransformationManager.transform(solution, steps);
        }

        template.convertAndSend("/topic/solutions", new SolutionResponse(solution, Double.valueOf(cipherSolution.getScore())), solutionHeaders);
    }
}
