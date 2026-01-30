package com.ciphertool.zenith.genetic.operators.speciation;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.population.LatticePopulation;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.genetic.population.StandardPopulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProximitySpeciationOperatorTest {

    private ProximitySpeciationOperator operator;
    private GeneticAlgorithmStrategy strategy;

    @BeforeEach
    public void setUp() {
        operator = new ProximitySpeciationOperator();
        strategy = mock(GeneticAlgorithmStrategy.class);
    }

    private LatticePopulation createPopulatedLattice(int rows, int columns) {
        LatticePopulation population = new LatticePopulation(rows, columns, false, 1);
        for (int i = 0; i < rows * columns; i++) {
            Genome genome = mock(Genome.class);
            when(genome.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
            when(genome.isEvaluationNeeded()).thenReturn(false);
            population.addIndividual(genome);
        }
        return population;
    }

    @Test
    public void given_validInput_when_divergeEvenSplit_then_returnsExpectedValue() {
        when(strategy.getSpeciationFactor()).thenReturn(2);

        LatticePopulation population = createPopulatedLattice(4, 4); // 16 individuals

        List<Population> result = operator.diverge(strategy, population);

        assertEquals(2, result.size());
        assertEquals(8, result.get(0).size());
        assertEquals(8, result.get(1).size());
    }

    @Test
    public void given_validInput_when_divergeUnevenSplit_then_returnsExpectedValue() {
        when(strategy.getSpeciationFactor()).thenReturn(3);

        LatticePopulation population = createPopulatedLattice(4, 4); // 16 individuals

        List<Population> result = operator.diverge(strategy, population);

        assertEquals(3, result.size());
        // 16 / 3 = 5 per slice, with remainder going to last
        assertEquals(5, result.get(0).size());
        assertEquals(5, result.get(1).size());
        assertEquals(6, result.get(2).size()); // Gets remainder
    }

    @Test
    public void given_validInput_when_divergeRequiresLatticePopulation_then_throwsIllegalStateException() {
        when(strategy.getSpeciationFactor()).thenReturn(2);

        StandardPopulation population = new StandardPopulation();
        Genome genome = mock(Genome.class);
        when(genome.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
        population.addIndividual(genome);

        assertThrows(IllegalStateException.class, () -> operator.diverge(strategy, population));
    }

    @Test
    public void given_validInput_when_divergeZeroSpeciationFactor_then_throwsIllegalArgumentException() {
        when(strategy.getSpeciationFactor()).thenReturn(0);

        LatticePopulation population = createPopulatedLattice(2, 2);

        assertThrows(IllegalArgumentException.class, () -> operator.diverge(strategy, population));
    }

    @Test
    public void given_validInput_when_divergeNegativeSpeciationFactor_then_throwsIllegalArgumentException() {
        when(strategy.getSpeciationFactor()).thenReturn(-1);

        LatticePopulation population = createPopulatedLattice(2, 2);

        assertThrows(IllegalArgumentException.class, () -> operator.diverge(strategy, population));
    }

    @Test
    public void given_emptyInput_when_divergeEmptyPopulation_then_throwsIllegalArgumentException() {
        when(strategy.getSpeciationFactor()).thenReturn(2);

        LatticePopulation population = new LatticePopulation(2, 2, false, 1);
        // Don't add any individuals

        assertThrows(IllegalArgumentException.class, () -> operator.diverge(strategy, population));
    }

    @Test
    public void given_validInput_when_divergeSpeciationFactorGreaterThanPopulationSize_then_throwsIllegalArgumentException() {
        when(strategy.getSpeciationFactor()).thenReturn(10);

        LatticePopulation population = createPopulatedLattice(2, 2); // 4 individuals

        assertThrows(IllegalArgumentException.class, () -> operator.diverge(strategy, population));
    }

    @Test
    public void given_validInput_when_divergeSpeciationFactorEqualsPopulationSize_then_comparesAsExpected() {
        when(strategy.getSpeciationFactor()).thenReturn(4);

        LatticePopulation population = createPopulatedLattice(2, 2); // 4 individuals

        List<Population> result = operator.diverge(strategy, population);

        assertEquals(4, result.size());
        for (Population pop : result) {
            assertEquals(1, pop.size());
        }
    }

    @Test
    public void given_validInput_when_divergePreservesAllIndividuals_then_returnsExpectedValue() {
        when(strategy.getSpeciationFactor()).thenReturn(2);

        LatticePopulation population = createPopulatedLattice(3, 3); // 9 individuals

        List<Population> result = operator.diverge(strategy, population);

        // Count total individuals across all new populations
        int totalIndividuals = result.stream().mapToInt(Population::size).sum();
        assertEquals(9, totalIndividuals);
    }

    @Test
    public void given_validInput_when_divergeReturnsLatticePopulations_then_returnsTrue() {
        when(strategy.getSpeciationFactor()).thenReturn(2);

        LatticePopulation population = createPopulatedLattice(4, 4);

        List<Population> result = operator.diverge(strategy, population);

        for (Population pop : result) {
            assertTrue(pop instanceof LatticePopulation, "Diverged populations should be LatticePopulation instances");
        }
    }
}
