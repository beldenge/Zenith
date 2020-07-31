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

package com.ciphertool.zenith.api.service;

import com.ciphertool.zenith.api.model.SolutionRequest;
import com.ciphertool.zenith.api.model.SolutionRequestFitnessFunction;
import com.ciphertool.zenith.api.model.SolutionRequestTransformer;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.inference.entities.config.GeneticAlgorithmConfiguration;
import com.ciphertool.zenith.inference.entities.config.SimulatedAnnealingConfiguration;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.optimizer.GeneticAlgorithmSolutionOptimizer;
import com.ciphertool.zenith.inference.optimizer.OnEpochComplete;
import com.ciphertool.zenith.inference.optimizer.SimulatedAnnealingSolutionOptimizer;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractSolutionService {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${features.genetic-algorithm.enabled:true}")
    private boolean geneticAlgorithmEnabled;

    @Value("${features.epochs.max:-1}")
    private int maxEpochs;

    @Value("${features.simulated-annealing.max-iterations:-1}")
    private int simulatedAnnealingMaxIterations;

    @Autowired
    private SimulatedAnnealingSolutionOptimizer simulatedAnnealingOptimizer;

    @Autowired
    private GeneticAlgorithmSolutionOptimizer geneticAlgorithmOptimizer;

    @Autowired
    private List<PlaintextEvaluator> plaintextEvaluators;

    public abstract OnEpochComplete getCallback(SolutionRequest request);

    protected CipherSolution doSolve(SolutionRequest request) {
        if (maxEpochs > 0 && request.getEpochs() > maxEpochs) {
            throw new IllegalArgumentException("The requested number of epochs=" + request.getEpochs() + " exceeds the maximum supported=" + maxEpochs + ".");
        }

        Cipher cipher = new Cipher(null, request.getRows(), request.getColumns());

        String[] split = request.getCiphertext().split(" ");

        for (int i = 0; i < split.length; i ++) {
            cipher.addCiphertextCharacter(new Ciphertext(i, split[i]));
        }

        List<SolutionRequestTransformer> transformers = request.getPlaintextTransformers();

        List<PlaintextTransformationStep> steps = new ArrayList<>();
        if (transformers != null && !transformers.isEmpty()) {
            steps = transformers.stream()
                    .map(SolutionRequestTransformer::asStep)
                    .collect(Collectors.toList());
        }

        PlaintextEvaluator plaintextEvaluator = resolvePlaintextEvaluator(request.getFitnessFunction());

        CipherSolution cipherSolution;
        Map<String, Object> configuration = new HashMap<>();

        if (request.getSimulatedAnnealingConfiguration() != null) {
            SimulatedAnnealingConfiguration simulatedAnnealingConfiguration = request.getSimulatedAnnealingConfiguration();

            if (simulatedAnnealingMaxIterations > 0 && simulatedAnnealingConfiguration.getSamplerIterations() > simulatedAnnealingMaxIterations) {
                throw new IllegalArgumentException("The requested number of sampler iterations=" + simulatedAnnealingConfiguration.getSamplerIterations() + " exceeds the maximum supported=" + simulatedAnnealingMaxIterations + ".");
            }

            configuration.put(SimulatedAnnealingSolutionOptimizer.SAMPLER_ITERATIONS, simulatedAnnealingConfiguration.getSamplerIterations());
            configuration.put(SimulatedAnnealingSolutionOptimizer.ANNEALING_TEMPERATURE_MIN, simulatedAnnealingConfiguration.getAnnealingTemperatureMin());
            configuration.put(SimulatedAnnealingSolutionOptimizer.ANNEALING_TEMPERATURE_MAX, simulatedAnnealingConfiguration.getAnnealingTemperatureMax());

            cipherSolution = simulatedAnnealingOptimizer.optimize(cipher, request.getEpochs(), configuration, steps, plaintextEvaluator, getCallback(request));
        } else if (request.getGeneticAlgorithmConfiguration() != null) {
            if (!geneticAlgorithmEnabled) {
                throw new IllegalArgumentException("Genetic Algorithm Optimizer is currently disabled.");
            }

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
            configuration.put(GeneticAlgorithmSolutionOptimizer.CROSSOVER_OPERATOR_NAME, geneticAlgorithmConfiguration.getCrossoverOperatorName());
            configuration.put(GeneticAlgorithmSolutionOptimizer.MUTATION_OPERATOR_NAME, geneticAlgorithmConfiguration.getMutationOperatorName());
            configuration.put(GeneticAlgorithmSolutionOptimizer.MUTATION_RATE, geneticAlgorithmConfiguration.getMutationRate());
            configuration.put(GeneticAlgorithmSolutionOptimizer.MAX_MUTATIONS_PER_INDIVIDUAL, geneticAlgorithmConfiguration.getMaxMutationsPerIndividual());
            configuration.put(GeneticAlgorithmSolutionOptimizer.SELECTOR_NAME, geneticAlgorithmConfiguration.getSelectorName());
            configuration.put(GeneticAlgorithmSolutionOptimizer.TOURNAMENT_SELECTOR_ACCURACY, geneticAlgorithmConfiguration.getTournamentSelectorAccuracy());
            configuration.put(GeneticAlgorithmSolutionOptimizer.TOURNAMENT_SIZE, geneticAlgorithmConfiguration.getTournamentSize());
            configuration.put(GeneticAlgorithmSolutionOptimizer.MIN_POPULATIONS, geneticAlgorithmConfiguration.getMinPopulations());
            configuration.put(GeneticAlgorithmSolutionOptimizer.SPECIATION_EVENTS, geneticAlgorithmConfiguration.getSpeciationEvents());
            configuration.put(GeneticAlgorithmSolutionOptimizer.SPECIATION_FACTOR, geneticAlgorithmConfiguration.getSpeciationFactor());
            configuration.put(GeneticAlgorithmSolutionOptimizer.EXTINCTION_CYCLES, geneticAlgorithmConfiguration.getExtinctionCycles());


            cipherSolution = geneticAlgorithmOptimizer.optimize(cipher, request.getEpochs(), configuration, steps, plaintextEvaluator, getCallback(request));
        } else {
            throw new IllegalStateException("Neither simulated annealing nor genetic algorithm was chosen as the optimization strategy.  No other strategy is currently supported.");
        }

        return cipherSolution;
    }

    private PlaintextEvaluator resolvePlaintextEvaluator(SolutionRequestFitnessFunction requestFitnessFunction) {
        List<String> existentPlaintextEvaluators = plaintextEvaluators.stream()
                .map(evaluator -> evaluator.getClass().getSimpleName())
                .collect(Collectors.toList());

        String plaintextEvaluatorName = requestFitnessFunction.getFitnessFunctionName();

        PlaintextEvaluator plaintextEvaluator = null;

        for (PlaintextEvaluator evaluator : plaintextEvaluators) {
            if (evaluator.getClass().getSimpleName().replace(PlaintextEvaluator.class.getSimpleName(), "").equals(plaintextEvaluatorName)) {
                plaintextEvaluator = evaluator.getInstance(requestFitnessFunction.getData());
                break;
            }
        }

        if (plaintextEvaluator == null) {
            log.error("The PlaintextEvaluator with name {} does not exist.  Please use a name from the following: {}", plaintextEvaluatorName, existentPlaintextEvaluators);
            throw new IllegalArgumentException("The PlaintextEvaluator with name " + plaintextEvaluatorName + " does not exist.");
        }

        return plaintextEvaluator;
    }
}
