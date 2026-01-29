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

public class UniformCrossoverOperatorTest {

    @Test
    public void testCrossover() {
        UniformCrossoverOperator operator = new UniformCrossoverOperator();

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
        
        when(clonedChromosome.getGenes()).thenReturn(genes1);

        // Mocking clone for genes
        when(gene1a.clone()).thenReturn(gene1a);
        when(gene1b.clone()).thenReturn(gene1b);
        when(gene2a.clone()).thenReturn(gene2a);
        when(gene2b.clone()).thenReturn(gene2b);

        parent1.addChromosome(chromosome1);
        parent2.addChromosome(chromosome2);

        Genome child = operator.crossover(parent1, parent2);

        assertNotNull(child);
        assertEquals(1, child.getChromosomes().size());
        verify(clonedChromosome, atLeastOnce()).replaceGene(any(), any());
    }

    @Test
    public void testCrossover_MissingKey() {
        UniformCrossoverOperator operator = new UniformCrossoverOperator();

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
        when(clonedChromosome.getGenes()).thenReturn(genes1);

        parent1.addChromosome(chromosome1);
        parent2.addChromosome(chromosome2);

        // We need to force a flip to true to trigger the exception
        // We can do this by using Reflection to set a mocked Coin
        Coin mockedCoin = mock(Coin.class);
        when(mockedCoin.flip()).thenReturn(true);
        org.springframework.test.util.ReflectionTestUtils.setField(operator, "coin", mockedCoin);

        assertThrows(IllegalStateException.class, () -> operator.crossover(parent1, parent2));
    }
}
