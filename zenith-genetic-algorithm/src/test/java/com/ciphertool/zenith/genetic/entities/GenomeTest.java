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

package com.ciphertool.zenith.genetic.entities;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import com.ciphertool.zenith.genetic.population.AbstractPopulation;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GenomeTest {
    @Test
    public void testConstructorClonesFitnesses() {
        Fitness[] fitnesses = new Fitness[] { new MaximizingFitness(1.0d) };
        Genome genome = new Genome(true, fitnesses, new StubPopulation(1.0d));

        assertNotSame(fitnesses, genome.getFitnesses());
        assertNotSame(fitnesses[0], genome.getFitnesses()[0]);
    }

    @Test
    public void testSetFitnessesClearsEvaluationNeeded() {
        Genome genome = new Genome(true, null, new StubPopulation(1.0d));
        assertTrue(genome.isEvaluationNeeded());

        genome.setFitnesses(new Fitness[] { new MaximizingFitness(2.0d) });

        assertFalse(genome.isEvaluationNeeded());
    }

    @Test
    public void testGetProbabilitySingleObjective() {
        Fitness[] fitnesses = new Fitness[] { new MaximizingFitness(-1.0d) };
        Genome genome = new Genome(false, fitnesses, new StubPopulation(2.0d));

        double expected = AbstractPopulation.convertFromLogProbability(-1.0d) / 2.0d;

        assertEquals(expected, genome.getProbability(), 0.000001d);
    }

    @Test
    public void testGetProbabilityMultipleObjectivesThrows() {
        Fitness[] fitnesses = new Fitness[] { new MaximizingFitness(1.0d), new MaximizingFitness(2.0d) };
        Genome genome = new Genome(false, fitnesses, new StubPopulation(1.0d));

        assertThrows(IllegalStateException.class, genome::getProbability);
    }

    @Test
    public void testCompareToSingleObjective() {
        Genome higher = new Genome(false, new Fitness[] { new MaximizingFitness(2.0d) }, new StubPopulation(1.0d));
        Genome lower = new Genome(false, new Fitness[] { new MaximizingFitness(1.0d) }, new StubPopulation(1.0d));

        assertTrue(higher.compareTo(lower) > 0);
        assertTrue(lower.compareTo(higher) < 0);
    }

    @Test
    public void testCompareToMultiObjectiveDominates() {
        Genome dominant = new Genome(false, new Fitness[] { new MaximizingFitness(2.0d), new MaximizingFitness(3.0d) }, new StubPopulation(1.0d));
        Genome dominated = new Genome(false, new Fitness[] { new MaximizingFitness(1.0d), new MaximizingFitness(2.0d) }, new StubPopulation(1.0d));

        assertEquals(1, dominant.compareTo(dominated));
        assertEquals(-1, dominated.compareTo(dominant));
    }

    @Test
    public void testCompareToMultiObjectiveIncomparable() {
        Genome first = new Genome(false, new Fitness[] { new MaximizingFitness(2.0d), new MaximizingFitness(1.0d) }, new StubPopulation(1.0d));
        Genome second = new Genome(false, new Fitness[] { new MaximizingFitness(1.0d), new MaximizingFitness(2.0d) }, new StubPopulation(1.0d));

        assertEquals(0, first.compareTo(second));
        assertEquals(0, second.compareTo(first));
    }

    private static class StubPopulation implements Population {
        private final Double totalProbability;

        private StubPopulation(Double totalProbability) {
            this.totalProbability = totalProbability;
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
            return 0;
        }

        @Override
        public List<Genome> getIndividuals() {
            return Collections.emptyList();
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
            return totalProbability;
        }
    }
}
