package com.ciphertool.zenith.genetic.operators.crossover;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.mocks.MockChromosome;
import com.ciphertool.zenith.genetic.util.Coin;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UniformCrossoverOperatorTest {

    @Test
    public void given_validInput_when_crossover_then_returnsNotNull() {
        UniformCrossoverOperator operator = new UniformCrossoverOperator();

        // Mock the coin to ensure deterministic behavior
        Coin mockedCoin = mock(Coin.class);
        when(mockedCoin.flip()).thenReturn(true);
        org.springframework.test.util.ReflectionTestUtils.setField(operator, "coin", mockedCoin);

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
        // With coin always returning true, both genes should be replaced
        verify(clonedChromosome, times(2)).replaceGene(any(), any());
    }

    @Test
    public void given_missingInput_when_crossoverMissingKey_then_throwsIllegalStateException() {
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

    @Test
    public void given_evaluatedParents_when_crossover_then_childAlwaysNeedsEvaluation() {
        // BUG FIX TEST: Verifies that children always need evaluation regardless of parent state.
        // Previously, children could inherit evaluationNeeded=false from evaluated parents,
        // causing them to skip fitness evaluation entirely.
        UniformCrossoverOperator operator = new UniformCrossoverOperator();

        Coin mockedCoin = mock(Coin.class);
        when(mockedCoin.flip()).thenReturn(false); // keep parent1's genes
        org.springframework.test.util.ReflectionTestUtils.setField(operator, "coin", mockedCoin);

        // Parent with evaluationNeeded=false (already evaluated)
        Genome parent1 = new Genome(false, null, null);
        Genome parent2 = new Genome(false, null, null);

        MockChromosome chromosome1 = new MockChromosome();
        chromosome1.putGene("k1", new SimpleGene("A"));

        MockChromosome chromosome2 = new MockChromosome();
        chromosome2.putGene("k1", new SimpleGene("B"));

        parent1.addChromosome(chromosome1);
        parent2.addChromosome(chromosome2);

        Genome child = operator.crossover(parent1, parent2);

        // Child must always need evaluation since it's a new individual
        assertTrue(child.isEvaluationNeeded(),
                "Child genome should always need evaluation after crossover, regardless of parent state");
    }

    @Test
    public void given_mutableChromosome_when_crossoverReplacesGenes_then_doesNotThrowConcurrentModification() {
        UniformCrossoverOperator operator = new UniformCrossoverOperator();

        Coin mockedCoin = mock(Coin.class);
        when(mockedCoin.flip()).thenReturn(true);
        org.springframework.test.util.ReflectionTestUtils.setField(operator, "coin", mockedCoin);

        Genome parent1 = new Genome(false, null, null);
        Genome parent2 = new Genome(false, null, null);

        MockChromosome chromosome1 = new MockChromosome();
        chromosome1.putGene("k1", new SimpleGene("A"));
        chromosome1.putGene("k2", new SimpleGene("B"));

        MockChromosome chromosome2 = new MockChromosome();
        chromosome2.putGene("k1", new SimpleGene("C"));
        chromosome2.putGene("k2", new SimpleGene("D"));

        parent1.addChromosome(chromosome1);
        parent2.addChromosome(chromosome2);

        Genome child = assertDoesNotThrow(() -> operator.crossover(parent1, parent2));

        assertNotNull(child);
        assertEquals(1, child.getChromosomes().size());

        MockChromosome childChromosome = (MockChromosome) child.getChromosomes().get(0);
        assertEquals("C", childChromosome.getGenes().get("k1").getValue());
        assertEquals("D", childChromosome.getGenes().get("k2").getValue());
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
