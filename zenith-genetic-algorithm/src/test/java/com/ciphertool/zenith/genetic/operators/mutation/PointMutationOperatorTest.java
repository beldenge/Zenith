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

package com.ciphertool.zenith.genetic.operators.mutation;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.dao.GeneDao;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.mocks.MockChromosome;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PointMutationOperatorTest {
    @Test
    public void given_validInput_when_mutateChromosomesReplacesGenesWhenDifferent_then_replacesItem() {
        GeneDao geneDao = mock(GeneDao.class);
        PointMutationOperator operator = new PointMutationOperator();
        setGeneDao(operator, geneDao);

        MockChromosome chromosome = new MockChromosome();
        chromosome.putGene("k1", new SimpleGene("A"));
        chromosome.putGene("k2", new SimpleGene("B"));

        Genome genome = new Genome(false, null, null);
        genome.addChromosome(chromosome);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .mutationRate(1.0d)
                .build();

        when(geneDao.findRandomGene(any(Chromosome.class))).thenAnswer(invocation -> new SimpleGene("X"));

        boolean mutated = operator.mutateChromosomes(genome, strategy);

        assertTrue(mutated);
        chromosome.getGenes().values().forEach(gene -> assertEquals("X", gene.getValue()));
        verify(geneDao, times(2)).findRandomGene(any(Chromosome.class));
    }

    @Test
    public void given_validInput_when_mutateChromosomesDoesNotReplaceWhenSameGene_then_returnsFalse() {
        GeneDao geneDao = mock(GeneDao.class);
        PointMutationOperator operator = new PointMutationOperator();
        setGeneDao(operator, geneDao);

        MockChromosome chromosome = new MockChromosome();
        chromosome.putGene("k1", new SimpleGene("A"));

        Genome genome = new Genome(false, null, null);
        genome.addChromosome(chromosome);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .mutationRate(1.0d)
                .build();

        when(geneDao.findRandomGene(any(Chromosome.class))).thenReturn(new SimpleGene("A"));

        boolean mutated = operator.mutateChromosomes(genome, strategy);

        assertFalse(mutated);
        assertEquals("A", chromosome.getGenes().get("k1").getValue());
        verify(geneDao).findRandomGene(any(Chromosome.class));
    }

    private void setGeneDao(PointMutationOperator operator, GeneDao geneDao) {
        try {
            Field field = PointMutationOperator.class.getDeclaredField("geneDao");
            field.setAccessible(true);
            field.set(operator, geneDao);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new AssertionError("Unable to set geneDao for PointMutationOperator.", exception);
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
