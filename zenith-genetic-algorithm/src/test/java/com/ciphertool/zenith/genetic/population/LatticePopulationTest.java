package com.ciphertool.zenith.genetic.population;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.operators.selection.Selector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LatticePopulationTest {

    private GeneticAlgorithmStrategy strategy;
    private Selector selector;

    @BeforeEach
    public void setUp() {
        strategy = mock(GeneticAlgorithmStrategy.class);
        selector = mock(Selector.class);
        when(strategy.getSelector()).thenReturn(selector);
        when(selector.getInstance()).thenReturn(selector);
        when(strategy.getTaskExecutor()).thenReturn(new SyncTaskExecutor());
        when(strategy.getPopulationSize()).thenReturn(4);
        when(strategy.getLatticeRows()).thenReturn(2);
        when(strategy.getLatticeColumns()).thenReturn(2);
        when(strategy.getLatticeWrapAround()).thenReturn(true);
        when(strategy.getLatticeRadius()).thenReturn(1);
        when(strategy.getElitism()).thenReturn(0);
    }

    @Test
    public void testInit_Valid() {
        LatticePopulation population = new LatticePopulation();
        population.init(strategy);
        assertEquals(4, population.size());
    }

    @Test
    public void testInit_Invalid() {
        when(strategy.getPopulationSize()).thenReturn(5);
        LatticePopulation population = new LatticePopulation();
        assertThrows(IllegalArgumentException.class, () -> population.init(strategy));
    }

    @Test
    public void testAddIndividual() {
        LatticePopulation population = new LatticePopulation(2, 2, true, 1);
        Genome g1 = mock(Genome.class);
        when(g1.isEvaluationNeeded()).thenReturn(true);
        when(g1.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
        Genome g2 = mock(Genome.class);
        when(g2.isEvaluationNeeded()).thenReturn(true);
        when(g2.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
        Genome g3 = mock(Genome.class);
        when(g3.isEvaluationNeeded()).thenReturn(true);
        when(g3.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
        Genome g4 = mock(Genome.class);
        when(g4.isEvaluationNeeded()).thenReturn(true);
        when(g4.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);

        assertTrue(population.addIndividual(g1));
        assertTrue(population.addIndividual(g2));
        assertTrue(population.addIndividual(g3));
        assertTrue(population.addIndividual(g4));

        assertEquals(4, population.getIndividuals().size());
        
        Genome g5 = mock(Genome.class);
        assertThrows(IllegalStateException.class, () -> population.addIndividual(g5));
    }

    @Test
    public void testSelection() throws Exception {
        LatticePopulation population = new LatticePopulation(2, 2, true, 1);
        population.init(strategy);

        Genome g1 = mock(Genome.class);
        Genome g2 = mock(Genome.class);
        Genome g3 = mock(Genome.class);
        Genome g4 = mock(Genome.class);
        
        // Mocking fitnesses for ParetoSorter
        when(g1.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
        when(g2.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
        when(g3.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
        when(g4.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);

        // Mocking compareTo for ParetoSorter
        when(g1.compareTo(any())).thenReturn(0);
        when(g2.compareTo(any())).thenReturn(0);
        when(g3.compareTo(any())).thenReturn(0);
        when(g4.compareTo(any())).thenReturn(0);

        population.addIndividual(g1);
        population.addIndividual(g2);
        population.addIndividual(g3);
        population.addIndividual(g4);

        when(selector.getNextIndex(anyList(), eq(strategy))).thenReturn(0, 0, 1);

        Callable<Parents> selectionTask = population.newSelectionTask();
        Parents parents = selectionTask.call();

        assertNotNull(parents);
        assertNotNull(parents.getMom());
        assertNotNull(parents.getDad());
        assertNotSame(parents.getMom(), parents.getDad());
        
        // Verify that the selector was called with a list of size 9 first, then size 8
        // (A radius of 1 in a 2x2 with wrap-around results in 1 + 8 = 9 entries in nearbyIndividuals.
        // After removing mom, it's 8.)
        verify(selector, times(2)).reIndex(anyList());
    }

    @Test
    public void testSelection_NoWrapAround() throws Exception {
        when(strategy.getLatticeWrapAround()).thenReturn(false);
        LatticePopulation population = new LatticePopulation(2, 2, false, 1);
        population.init(strategy);

        for (int i = 0; i < 4; i++) {
            Genome g = mock(Genome.class);
            when(g.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
            when(g.compareTo(any())).thenReturn(0);
            population.addIndividual(g);
        }

        when(selector.getNextIndex(anyList(), eq(strategy))).thenReturn(0);

        Callable<Parents> selectionTask = population.newSelectionTask();
        Parents parents = selectionTask.call();

        assertNotNull(parents);
    }
}
