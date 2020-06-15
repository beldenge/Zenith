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

import com.ciphertool.zenith.inference.entities.FormlyForm;
import com.ciphertool.zenith.inference.entities.config.ApplicationConfiguration;
import com.ciphertool.zenith.inference.entities.config.GeneticAlgorithmConfiguration;
import com.ciphertool.zenith.inference.entities.config.SimulatedAnnealingConfiguration;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.optimizer.GeneticAlgorithmSolutionOptimizer;
import com.ciphertool.zenith.inference.optimizer.SimulatedAnnealingSolutionOptimizer;
import com.ciphertool.zenith.inference.optimizer.SolutionOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigurationResolver {
    private static Logger log = LoggerFactory.getLogger(ConfigurationResolver.class);

    public static Map<String, Object> resolveConfiguration(ApplicationConfiguration applicationConfiguration) {
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
        configuration.put(GeneticAlgorithmSolutionOptimizer.ENABLE_FITNESS_SHARING, geneticAlgorithmConfiguration.isEnableFitnessSharing());

        return configuration;
    }

    public static SolutionOptimizer resolveSolutionOptimizer(ApplicationConfiguration applicationConfiguration, List<SolutionOptimizer> optimizers) {
        List<String> existentOptimizers = optimizers.stream()
                .map(optimizer -> optimizer.getClass().getSimpleName().replace(SolutionOptimizer.class.getSimpleName(), ""))
                .collect(Collectors.toList());

        String optimizerName = applicationConfiguration.getSelectedOptimizer().getName();

        for (SolutionOptimizer optimizer : optimizers) {
            if (optimizer.getClass().getSimpleName().replace(SolutionOptimizer.class.getSimpleName(), "").equals(optimizerName)) {
                return optimizer;
            }
        }

        log.error("The SolutionOptimizer with name {} does not exist.  Please use a name from the following: {}", optimizerName, existentOptimizers);
        throw new IllegalArgumentException("The SolutionOptimizer with name " + optimizerName + " does not exist.");
    }

    public static PlaintextEvaluator resolvePlaintextEvaluator(ApplicationConfiguration applicationConfiguration, List<PlaintextEvaluator> plaintextEvaluators) {
        List<String> existentPlaintextEvaluators = plaintextEvaluators.stream()
                .map(evaluator -> evaluator.getClass().getSimpleName().replace(PlaintextEvaluator.class.getSimpleName(), ""))
                .collect(Collectors.toList());

        String plaintextEvaluatorName = applicationConfiguration.getSelectedFitnessFunction().getName();
        FormlyForm form = applicationConfiguration.getSelectedFitnessFunction().getForm();

        for (PlaintextEvaluator evaluator : plaintextEvaluators) {
            if (evaluator.getClass().getSimpleName().replace(PlaintextEvaluator.class.getSimpleName(), "").equals(plaintextEvaluatorName)) {
                return evaluator.getInstance(form != null ? form.getModel() : null);
            }
        }

        log.error("The PlaintextEvaluator with name {} does not exist.  Please use a name from the following: {}", plaintextEvaluatorName, existentPlaintextEvaluators);
        throw new IllegalArgumentException("The PlaintextEvaluator with name " + plaintextEvaluatorName + " does not exist.");
    }
}
