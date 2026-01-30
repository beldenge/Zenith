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

package com.ciphertool.zenith.inference.optimizer;

import com.ciphertool.zenith.genetic.Breeder;
import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.fitness.FitnessEvaluator;
import com.ciphertool.zenith.genetic.operators.crossover.CrossoverOperator;
import com.ciphertool.zenith.genetic.operators.mutation.MutationOperator;
import com.ciphertool.zenith.genetic.operators.selection.Selector;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import com.ciphertool.zenith.inference.configuration.GeneticAlgorithmInitialization;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.genetic.breeder.AbstractCipherKeyBreeder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class GeneticAlgorithmSolutionOptimizerTest {
    @Test
    public void given_validConfiguration_when_initializing_then_returnsExpectedComponents() throws Exception {
        GeneticAlgorithmSolutionOptimizer optimizer = buildOptimizer();
        Cipher cipher = buildCipher();
        PlaintextEvaluator plaintextEvaluator = mock(PlaintextEvaluator.class);
        Map<String, Object> config = buildConfig("TestPopulation", "TestBreeder", "TestCrossoverOperator", "TestMutationOperator", "TestSelector");

        GeneticAlgorithmInitialization initialization = optimizer.init(cipher, config, Collections.emptyList(), plaintextEvaluator);

        assertNotNull(initialization);
        assertNotNull(initialization.getPopulation());
        assertNotNull(initialization.getBreeder());
        assertNotNull(initialization.getCrossoverOperator());
        assertNotNull(initialization.getMutationOperator());
        assertNotNull(initialization.getSelector());
        assertNotNull(initialization.getFitnessEvaluator());
        assertEquals(TestPopulation.class, initialization.getPopulation().getClass());
        assertEquals(TestBreeder.class, initialization.getBreeder().getClass());
        assertEquals(TestCrossoverOperator.class, initialization.getCrossoverOperator().getClass());
        assertEquals(TestMutationOperator.class, initialization.getMutationOperator().getClass());
        assertEquals(TestSelector.class, initialization.getSelector().getClass());
        assertTrue(initialization.getFitnessEvaluator() instanceof FitnessEvaluator);
    }

    @Test
    public void given_unknownPopulation_when_initializing_then_throwsIllegalArgumentException() throws Exception {
        GeneticAlgorithmSolutionOptimizer optimizer = buildOptimizer();
        Cipher cipher = buildCipher();
        PlaintextEvaluator plaintextEvaluator = mock(PlaintextEvaluator.class);
        Map<String, Object> config = buildConfig("MissingPopulation", "TestBreeder", "TestCrossoverOperator", "TestMutationOperator", "TestSelector");

        assertThrows(IllegalArgumentException.class,
                () -> optimizer.init(cipher, config, Collections.emptyList(), plaintextEvaluator));
    }

    @Test
    public void given_unknownBreeder_when_initializing_then_throwsIllegalArgumentException() throws Exception {
        GeneticAlgorithmSolutionOptimizer optimizer = buildOptimizer();
        Cipher cipher = buildCipher();
        PlaintextEvaluator plaintextEvaluator = mock(PlaintextEvaluator.class);
        Map<String, Object> config = buildConfig("TestPopulation", "MissingBreeder", "TestCrossoverOperator", "TestMutationOperator", "TestSelector");

        assertThrows(IllegalArgumentException.class,
                () -> optimizer.init(cipher, config, Collections.emptyList(), plaintextEvaluator));
    }

    @Test
    public void given_unknownCrossoverOperator_when_initializing_then_throwsIllegalArgumentException() throws Exception {
        GeneticAlgorithmSolutionOptimizer optimizer = buildOptimizer();
        Cipher cipher = buildCipher();
        PlaintextEvaluator plaintextEvaluator = mock(PlaintextEvaluator.class);
        Map<String, Object> config = buildConfig("TestPopulation", "TestBreeder", "MissingCrossover", "TestMutationOperator", "TestSelector");

        assertThrows(IllegalArgumentException.class,
                () -> optimizer.init(cipher, config, Collections.emptyList(), plaintextEvaluator));
    }

    @Test
    public void given_unknownMutationOperator_when_initializing_then_throwsIllegalArgumentException() throws Exception {
        GeneticAlgorithmSolutionOptimizer optimizer = buildOptimizer();
        Cipher cipher = buildCipher();
        PlaintextEvaluator plaintextEvaluator = mock(PlaintextEvaluator.class);
        Map<String, Object> config = buildConfig("TestPopulation", "TestBreeder", "TestCrossoverOperator", "MissingMutation", "TestSelector");

        assertThrows(IllegalArgumentException.class,
                () -> optimizer.init(cipher, config, Collections.emptyList(), plaintextEvaluator));
    }

    @Test
    public void given_unknownSelector_when_initializing_then_throwsIllegalArgumentException() throws Exception {
        GeneticAlgorithmSolutionOptimizer optimizer = buildOptimizer();
        Cipher cipher = buildCipher();
        PlaintextEvaluator plaintextEvaluator = mock(PlaintextEvaluator.class);
        Map<String, Object> config = buildConfig("TestPopulation", "TestBreeder", "TestCrossoverOperator", "TestMutationOperator", "MissingSelector");

        assertThrows(IllegalArgumentException.class,
                () -> optimizer.init(cipher, config, Collections.emptyList(), plaintextEvaluator));
    }

    private GeneticAlgorithmSolutionOptimizer buildOptimizer() throws Exception {
        GeneticAlgorithmSolutionOptimizer optimizer = new GeneticAlgorithmSolutionOptimizer();

        setField(optimizer, "populations", Collections.singletonList(new TestPopulation()));
        setField(optimizer, "breeders", Collections.singletonList(new TestBreeder()));
        setField(optimizer, "crossoverOperators", Collections.singletonList(new TestCrossoverOperator()));
        setField(optimizer, "mutationOperators", Collections.singletonList(new TestMutationOperator()));
        setField(optimizer, "selectors", Collections.singletonList(new TestSelector()));
        setField(optimizer, "plaintextTransformationManager", mock(com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationManager.class));

        return optimizer;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> current = target.getClass();
        Field field = null;

        while (current != null) {
            try {
                field = current.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException ex) {
                current = current.getSuperclass();
            }
        }

        if (field == null) {
            throw new NoSuchFieldException(fieldName);
        }

        field.setAccessible(true);
        field.set(target, value);
    }

    private Map<String, Object> buildConfig(String populationName, String breederName, String crossoverOperatorName, String mutationOperatorName, String selectorName) {
        Map<String, Object> config = new HashMap<>();
        config.put(GeneticAlgorithmSolutionOptimizer.POPULATION_NAME, populationName);
        config.put(GeneticAlgorithmSolutionOptimizer.BREEDER_NAME, breederName);
        config.put(GeneticAlgorithmSolutionOptimizer.CROSSOVER_OPERATOR_NAME, crossoverOperatorName);
        config.put(GeneticAlgorithmSolutionOptimizer.MUTATION_OPERATOR_NAME, mutationOperatorName);
        config.put(GeneticAlgorithmSolutionOptimizer.SELECTOR_NAME, selectorName);
        return config;
    }

    private Cipher buildCipher() {
        Cipher cipher = new Cipher("test", 1, 3);
        cipher.setCiphertext(List.of("a", "b", "c"));
        return cipher;
    }

    private static class TestPopulation implements Population {
        private final List<Genome> individuals = new ArrayList<>();

        @Override
        public Population getInstance() {
            return new TestPopulation();
        }

        @Override
        public void init(GeneticAlgorithmStrategy strategy) {
        }

        @Override
        public void setStrategy(GeneticAlgorithmStrategy strategy) {
        }

        @Override
        public Genome evaluateFitness(GenerationStatistics generationStatistics) {
            return null;
        }

        @Override
        public List<Genome> breed(int numberToBreed) {
            return Collections.emptyList();
        }

        @Override
        public List<Parents> select() {
            return Collections.emptyList();
        }

        @Override
        public void clearIndividuals() {
            individuals.clear();
        }

        @Override
        public int size() {
            return individuals.size();
        }

        @Override
        public List<Genome> getIndividuals() {
            return individuals;
        }

        @Override
        public boolean addIndividual(Genome individual) {
            return individuals.add(individual);
        }

        @Override
        public void sortIndividuals() {
        }

        @Override
        public Double getTotalFitness() {
            return 0d;
        }

        @Override
        public Double getTotalProbability() {
            return 0d;
        }

        @Override
        public BigDecimal calculateEntropy() {
            return BigDecimal.ZERO;
        }
    }

    private static class TestBreeder extends AbstractCipherKeyBreeder {
        @Override
        public Genome breed(Population population) {
            return null;
        }
    }

    private static class TestCrossoverOperator implements CrossoverOperator {
        @Override
        public Genome crossover(Genome parentA, Genome parentB) {
            return null;
        }
    }

    private static class TestMutationOperator implements MutationOperator {
        @Override
        public boolean mutateChromosomes(Genome genome, GeneticAlgorithmStrategy strategy) {
            return false;
        }
    }

    private static class TestSelector implements Selector {
        @Override
        public Selector getInstance() {
            return new TestSelector();
        }

        @Override
        public void reIndex(List<Genome> individuals) {
        }

        @Override
        public int getNextIndex(List<Genome> individuals, GeneticAlgorithmStrategy strategy) {
            return 0;
        }
    }
}
