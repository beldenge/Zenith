package com.ciphertool.zenith.genetic.operators.crossover;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.Genome;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SinglePointCrossoverOperatorTest {

    @Test
    public void testCrossover() {
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

        // This should not hang anymore
        Genome child = operator.crossover(parent1, parent2);

        assertNotNull(child);
        assertEquals(1, child.getChromosomes().size());
        // Verify it finished
    }
}
