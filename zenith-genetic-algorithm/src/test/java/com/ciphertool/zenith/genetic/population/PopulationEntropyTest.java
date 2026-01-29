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

package com.ciphertool.zenith.genetic.population;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.mocks.MockChromosome;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PopulationEntropyTest {
    @Test
    public void testCalculateEntropy() {
        Genome genome1 = new Genome(false, null, null);
        Genome genome2 = new Genome(false, null, null);

        MockChromosome chromosome1 = new MockChromosome();
        chromosome1.putGene("k1", new SimpleGene("A"));
        chromosome1.putGene("k2", new SimpleGene("A"));

        MockChromosome chromosome2 = new MockChromosome();
        chromosome2.putGene("k1", new SimpleGene("B"));
        chromosome2.putGene("k2", new SimpleGene("A"));

        genome1.addChromosome(chromosome1);
        genome2.addChromosome(chromosome2);

        Population population = new StubPopulation(List.of(genome1, genome2));

        BigDecimal entropy = population.calculateEntropy();

        assertEquals(0.5d, entropy.doubleValue(), 0.000001d);
    }

    private static class StubPopulation implements Population {
        private final List<Genome> individuals;

        private StubPopulation(List<Genome> individuals) {
            this.individuals = individuals;
        }

        @Override
        public Population getInstance() {
            return this;
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
            return false;
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
    }

    private static class SimpleGene implements Gene {
        private final String value;
        private Chromosome chromosome;

        private SimpleGene(String value) {
            this.value = value;
        }

        @Override
        public Gene clone() {
            return new SimpleGene(value);
        }

        @Override
        public void setChromosome(Chromosome chromosome) {
            this.chromosome = chromosome;
        }

        @Override
        public Chromosome getChromosome() {
            return chromosome;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SimpleGene other = (SimpleGene) obj;
            return value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}
