package com.ciphertool.zenith.genetic.population;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.operators.selection.Selector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;

import java.lang.reflect.Method;
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
    public void given_validInput_when_initValid_then_returnsExpectedValue() {
        LatticePopulation population = new LatticePopulation();
        population.init(strategy);
        // size() returns actual count (0 after init, before individuals added)
        assertEquals(0, population.size());
    }

    @Test
    public void given_invalidInput_when_initInvalid_then_throwsIllegalArgumentException() {
        when(strategy.getPopulationSize()).thenReturn(5);
        LatticePopulation population = new LatticePopulation();
        assertThrows(IllegalArgumentException.class, () -> population.init(strategy));
    }

    @Test
    public void given_validInput_when_addingIndividual_then_throwsIllegalStateException() {
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
    public void given_validInput_when_selection_then_returnsNotNull() throws Exception {
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
        
        // Verify that the selector was called twice (once for mom selection, once for dad after removing mom)
        // With duplicate prevention, a 2x2 lattice with radius 1 and wrap-around has only 4 unique cells
        verify(selector, times(2)).reIndex(anyList());
    }

    @Test
    public void given_missingDependency_when_selectionNoWrapAround_then_returnsNotNull() throws Exception {
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

    @Test
    public void given_validInput_when_selectionLargeRadiusWithWrapAround_then_returnsNotNull() throws Exception {
        // Test that large radius works with wrap-around enabled
        // A 3x3 lattice with radius 5 should still work and select all 9 cells (with duplicates filtered)
        when(strategy.getLatticeWrapAround()).thenReturn(true);
        when(strategy.getLatticeRadius()).thenReturn(5);
        when(strategy.getLatticeRows()).thenReturn(3);
        when(strategy.getLatticeColumns()).thenReturn(3);
        when(strategy.getPopulationSize()).thenReturn(9);

        LatticePopulation population = new LatticePopulation(3, 3, true, 5);
        population.init(strategy);

        for (int i = 0; i < 9; i++) {
            Genome g = mock(Genome.class);
            when(g.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
            when(g.compareTo(any())).thenReturn(0);
            population.addIndividual(g);
        }

        when(selector.getNextIndex(anyList(), eq(strategy))).thenReturn(0, 1);

        Callable<Parents> selectionTask = population.newSelectionTask();
        Parents parents = selectionTask.call();

        assertNotNull(parents);
        assertNotNull(parents.getMom());
        assertNotNull(parents.getDad());
    }

    @Test
    public void given_validInput_when_initInitializesIndividualsArray_then_matchesExpectations() {
        LatticePopulation population = new LatticePopulation();
        population.init(strategy);

        // Should be able to add individuals without NPE
        Genome g = mock(Genome.class);
        when(g.isEvaluationNeeded()).thenReturn(true);
        when(g.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);

        assertDoesNotThrow(() -> population.addIndividual(g));
    }

    @Test
    public void given_validInput_when_initLargeSelectionRadiusAllowed_then_matchesExpectations() {
        when(strategy.getLatticeRadius()).thenReturn(10);
        when(strategy.getLatticeWrapAround()).thenReturn(true);
        // Large radius is allowed - wrap functions handle any offset via modulo

        LatticePopulation population = new LatticePopulation();
        assertDoesNotThrow(() -> population.init(strategy));
    }

    @Test
    public void given_validInput_when_wrapRowIndex_then_returnsExpectedValue() throws Exception {
        LatticePopulation population = new LatticePopulation(5, 5, true, 1);

        Method wrapRowIndex = LatticePopulation.class.getDeclaredMethod("wrapRowIndex", int.class);
        wrapRowIndex.setAccessible(true);

        // Values in range are unchanged
        assertEquals(0, wrapRowIndex.invoke(population, 0));
        assertEquals(2, wrapRowIndex.invoke(population, 2));
        assertEquals(4, wrapRowIndex.invoke(population, 4));

        // Negative values wrap to end of lattice
        assertEquals(4, wrapRowIndex.invoke(population, -1));
        assertEquals(3, wrapRowIndex.invoke(population, -2));
        assertEquals(0, wrapRowIndex.invoke(population, -5));

        // Large negative values (multiple wraps)
        assertEquals(4, wrapRowIndex.invoke(population, -6));
        assertEquals(3, wrapRowIndex.invoke(population, -12));

        // Values past end wrap to beginning
        assertEquals(0, wrapRowIndex.invoke(population, 5));
        assertEquals(1, wrapRowIndex.invoke(population, 6));

        // Large positive values (multiple wraps)
        assertEquals(0, wrapRowIndex.invoke(population, 10));
        assertEquals(2, wrapRowIndex.invoke(population, 12));
    }

    @Test
    public void given_validInput_when_wrapColumnIndex_then_returnsExpectedValue() throws Exception {
        LatticePopulation population = new LatticePopulation(5, 5, true, 1);

        Method wrapColumnIndex = LatticePopulation.class.getDeclaredMethod("wrapColumnIndex", int.class);
        wrapColumnIndex.setAccessible(true);

        // Values in range are unchanged
        assertEquals(0, wrapColumnIndex.invoke(population, 0));
        assertEquals(2, wrapColumnIndex.invoke(population, 2));
        assertEquals(4, wrapColumnIndex.invoke(population, 4));

        // Negative values wrap to end of lattice
        assertEquals(4, wrapColumnIndex.invoke(population, -1));
        assertEquals(3, wrapColumnIndex.invoke(population, -2));
        assertEquals(0, wrapColumnIndex.invoke(population, -5));

        // Large negative values (multiple wraps)
        assertEquals(4, wrapColumnIndex.invoke(population, -6));
        assertEquals(3, wrapColumnIndex.invoke(population, -12));

        // Values past end wrap to beginning
        assertEquals(0, wrapColumnIndex.invoke(population, 5));
        assertEquals(1, wrapColumnIndex.invoke(population, 6));

        // Large positive values (multiple wraps)
        assertEquals(0, wrapColumnIndex.invoke(population, 10));
        assertEquals(2, wrapColumnIndex.invoke(population, 12));
    }

    @Test
    public void given_validInput_when_clearingIndividualsResetsState_then_returnsExpectedValue() {
        LatticePopulation population = new LatticePopulation(2, 2, true, 1);

        Genome g1 = mock(Genome.class);
        when(g1.isEvaluationNeeded()).thenReturn(true);
        when(g1.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);

        population.addIndividual(g1);
        assertEquals(1, population.size());

        population.clearIndividuals();

        // After clear, should be able to add 4 individuals again
        for (int i = 0; i < 4; i++) {
            Genome g = mock(Genome.class);
            when(g.isEvaluationNeeded()).thenReturn(true);
            when(g.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
            population.addIndividual(g);
        }

        assertEquals(4, population.size());
    }

    @Test
    public void given_validInput_when_settingStrategyPreservesExistingIndividuals_then_returnsExpectedValue() {
        // This tests the speciation use case where getInstance() creates a population,
        // individuals are added, and then setStrategy() is called (instead of init())
        LatticePopulation population = new LatticePopulation(2, 2, true, 1);

        // Add some individuals (simulating speciation)
        Genome g1 = mock(Genome.class);
        when(g1.isEvaluationNeeded()).thenReturn(false);
        when(g1.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
        Genome g2 = mock(Genome.class);
        when(g2.isEvaluationNeeded()).thenReturn(false);
        when(g2.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);

        population.addIndividual(g1);
        population.addIndividual(g2);

        assertEquals(2, population.size());

        // setStrategy() should preserve existing individuals (unlike init() which reinitializes)
        population.setStrategy(strategy);

        // Verify individuals are preserved
        assertEquals(2, population.size());
    }
}
