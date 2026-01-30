package com.ciphertool.zenith.genetic.operators.crossover;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.util.Coin;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SinglePointCrossoverOperatorTest {

    @Test
    public void given_validInput_when_crossover_then_returnsNotNull() {
        SinglePointCrossoverOperator operator = new SinglePointCrossoverOperator();

        Genome parent1 = new Genome(false, null, null);
        Genome parent2 = new Genome(false, null, null);

        Chromosome chromosome1 = mock(Chromosome.class);
        Chromosome chromosome2 = mock(Chromosome.class);

        Gene gene1a = mock(Gene.class);
        Gene gene1b = mock(Gene.class);
        Gene gene2a = mock(Gene.class);
        Gene gene2b = mock(Gene.class);

        Map<Object, Gene> genes1 = new LinkedHashMap<>();
        genes1.put("key1", gene1a);
        genes1.put("key2", gene1b);

        Map<Object, Gene> genes2 = new LinkedHashMap<>();
        genes2.put("key1", gene2a);
        genes2.put("key2", gene2b);

        when(chromosome1.getGenes()).thenReturn(genes1);
        when(chromosome2.getGenes()).thenReturn(genes2);

        Chromosome clonedChromosome = mock(Chromosome.class);
        when(chromosome1.clone()).thenReturn(clonedChromosome);
        when(chromosome2.clone()).thenReturn(clonedChromosome); // simplified

        // Mocking clone for genes
        when(gene1a.clone()).thenReturn(gene1a);
        when(gene1b.clone()).thenReturn(gene1b);
        when(gene2a.clone()).thenReturn(gene2a);
        when(gene2b.clone()).thenReturn(gene2b);

        parent1.addChromosome(chromosome1);
        parent2.addChromosome(chromosome2);

        java.util.Random mockedRandom = mock(java.util.Random.class);
        when(mockedRandom.nextInt(anyInt())).thenReturn(0);

        // This should not hang anymore
        Genome child = operator.crossover(parent1, parent2, 0.0, mockedRandom);

        assertNotNull(child);
        assertEquals(1, child.getChromosomes().size());

        // With randomIndex = 0, it should replaceGene for key1 (index 0)
        // Note: the operator uses dad.clone() then replaces genes from mom up to randomIndex
        verify(clonedChromosome).replaceGene(eq("key1"), any());
        verify(clonedChromosome, never()).replaceGene(eq("key2"), any());
    }

    @Test
    public void given_missingInput_when_crossoverMissingKey_then_throwsIllegalStateException() {
        SinglePointCrossoverOperator operator = new SinglePointCrossoverOperator();

        Genome parent1 = new Genome(false, null, null);
        Genome parent2 = new Genome(false, null, null);

        Chromosome chromosome1 = mock(Chromosome.class);
        Chromosome chromosome2 = mock(Chromosome.class);

        Map<Object, Gene> genes1 = new LinkedHashMap<>();
        genes1.put("key1", mock(Gene.class));

        Map<Object, Gene> genes2 = new LinkedHashMap<>();
        // parent2 is missing key1

        when(chromosome1.getGenes()).thenReturn(genes1);
        when(chromosome2.getGenes()).thenReturn(genes2);

        Chromosome clonedChromosome = mock(Chromosome.class);
        when(chromosome1.clone()).thenReturn(clonedChromosome);

        parent1.addChromosome(chromosome1);
        parent2.addChromosome(chromosome2);

        java.util.Random mockedRandom = mock(java.util.Random.class);
        when(mockedRandom.nextInt(anyInt())).thenReturn(0);

        // coinFlip=0.0 means parent1 is dad, parent2 is mom
        // Should throw because mom (parent2) is missing key1 that dad (parent1) has
        assertThrows(IllegalStateException.class, () -> operator.crossover(parent1, parent2, 0.0, mockedRandom));
    }

    @Test
    public void given_validInput_when_crossoverParentSwap_then_returnsNotNull() {
        SinglePointCrossoverOperator operator = new SinglePointCrossoverOperator();

        Genome parent1 = new Genome(false, null, null);
        Genome parent2 = new Genome(false, null, null);

        Chromosome chromosome1 = mock(Chromosome.class);
        Chromosome chromosome2 = mock(Chromosome.class);

        Gene gene1a = mock(Gene.class);
        Gene gene2a = mock(Gene.class);

        Map<Object, Gene> genes1 = new LinkedHashMap<>();
        genes1.put("key1", gene1a);

        Map<Object, Gene> genes2 = new LinkedHashMap<>();
        genes2.put("key1", gene2a);

        when(chromosome1.getGenes()).thenReturn(genes1);
        when(chromosome2.getGenes()).thenReturn(genes2);

        Chromosome clonedChromosome1 = mock(Chromosome.class);
        Chromosome clonedChromosome2 = mock(Chromosome.class);
        when(chromosome1.clone()).thenReturn(clonedChromosome1);
        when(chromosome2.clone()).thenReturn(clonedChromosome2);

        when(gene1a.clone()).thenReturn(gene1a);
        when(gene2a.clone()).thenReturn(gene2a);

        parent1.addChromosome(chromosome1);
        parent2.addChromosome(chromosome2);

        java.util.Random mockedRandom = mock(java.util.Random.class);
        when(mockedRandom.nextInt(anyInt())).thenReturn(0);

        // coinFlip=0.99 means firstGenome becomes dad (coin.flip(0.99) returns false, so dad = secondGenome? Let me check)
        // Actually coin.flip(0.99) = ((int)(0.99 * 2)) == 0 = (1 == 0) = false
        // So dad = secondGenome when coinFlip >= 0.5
        Genome child = operator.crossover(parent1, parent2, 0.99, mockedRandom);

        assertNotNull(child);
        // When coinFlip >= 0.5, secondGenome becomes dad, so chromosome2 should be cloned
        verify(chromosome2).clone();
        verify(chromosome1, never()).clone();
    }

    @Test
    public void given_validInput_when_crossoverAllGenesFromMom_then_returnsNotNull() {
        SinglePointCrossoverOperator operator = new SinglePointCrossoverOperator();

        Genome parent1 = new Genome(false, null, null);
        Genome parent2 = new Genome(false, null, null);

        Chromosome chromosome1 = mock(Chromosome.class);
        Chromosome chromosome2 = mock(Chromosome.class);

        Gene gene1a = mock(Gene.class);
        Gene gene1b = mock(Gene.class);
        Gene gene2a = mock(Gene.class);
        Gene gene2b = mock(Gene.class);

        Map<Object, Gene> genes1 = new LinkedHashMap<>();
        genes1.put("key1", gene1a);
        genes1.put("key2", gene1b);

        Map<Object, Gene> genes2 = new LinkedHashMap<>();
        genes2.put("key1", gene2a);
        genes2.put("key2", gene2b);

        when(chromosome1.getGenes()).thenReturn(genes1);
        when(chromosome2.getGenes()).thenReturn(genes2);

        Chromosome clonedChromosome = mock(Chromosome.class);
        when(chromosome1.clone()).thenReturn(clonedChromosome);

        when(gene2a.clone()).thenReturn(gene2a);
        when(gene2b.clone()).thenReturn(gene2b);

        parent1.addChromosome(chromosome1);
        parent2.addChromosome(chromosome2);

        java.util.Random mockedRandom = mock(java.util.Random.class);
        // randomIndex = 1 means replace genes at indices 0 and 1 (all genes)
        when(mockedRandom.nextInt(anyInt())).thenReturn(1);

        // coinFlip=0.0 means parent1 is dad
        Genome child = operator.crossover(parent1, parent2, 0.0, mockedRandom);

        assertNotNull(child);
        // Both genes should be replaced from mom
        verify(clonedChromosome).replaceGene(eq("key1"), any());
        verify(clonedChromosome).replaceGene(eq("key2"), any());
    }
}
