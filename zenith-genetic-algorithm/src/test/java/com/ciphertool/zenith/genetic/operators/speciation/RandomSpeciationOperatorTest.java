package com.ciphertool.zenith.genetic.operators.speciation;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.genetic.population.StandardPopulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RandomSpeciationOperatorTest {

    private RandomSpeciationOperator operator;
    private GeneticAlgorithmStrategy strategy;

    @BeforeEach
    public void setUp() {
        operator = new RandomSpeciationOperator();
        strategy = mock(GeneticAlgorithmStrategy.class);
    }

    @Test
    public void given_validInput_when_divergeEvenSplit_then_returnsExpectedValue() {
        when(strategy.getSpeciationFactor()).thenReturn(2);

        StandardPopulation population = new StandardPopulation();
        for (int i = 0; i < 10; i++) {
            Genome genome = mock(Genome.class);
            when(genome.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
            population.addIndividual(genome);
        }

        List<Population> result = operator.diverge(strategy, population);

        assertEquals(2, result.size());
        assertEquals(5, result.get(0).size());
        assertEquals(5, result.get(1).size());
    }

    @Test
    public void given_validInput_when_divergeUnevenSplit_then_returnsExpectedValue() {
        when(strategy.getSpeciationFactor()).thenReturn(3);

        StandardPopulation population = new StandardPopulation();
        for (int i = 0; i < 10; i++) {
            Genome genome = mock(Genome.class);
            when(genome.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
            population.addIndividual(genome);
        }

        List<Population> result = operator.diverge(strategy, population);

        assertEquals(3, result.size());
        assertEquals(3, result.get(0).size());
        assertEquals(3, result.get(1).size());
        assertEquals(4, result.get(2).size());
    }

    @Test
    public void given_validInput_when_divergeSpeciationFactorEqualsPopulationSize_then_returnsExpectedValue() {
        when(strategy.getSpeciationFactor()).thenReturn(5);

        StandardPopulation population = new StandardPopulation();
        for (int i = 0; i < 5; i++) {
            Genome genome = mock(Genome.class);
            when(genome.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
            population.addIndividual(genome);
        }

        List<Population> result = operator.diverge(strategy, population);

        assertEquals(5, result.size());
        for (Population pop : result) {
            assertEquals(1, pop.size());
        }
    }

    @Test
    public void given_validInput_when_divergeZeroSpeciationFactor_then_throwsIllegalArgumentException() {
        when(strategy.getSpeciationFactor()).thenReturn(0);

        StandardPopulation population = new StandardPopulation();
        Genome genome = mock(Genome.class);
        when(genome.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
        population.addIndividual(genome);

        assertThrows(IllegalArgumentException.class, () -> operator.diverge(strategy, population));
    }

    @Test
    public void given_validInput_when_divergeNegativeSpeciationFactor_then_throwsIllegalArgumentException() {
        when(strategy.getSpeciationFactor()).thenReturn(-1);

        StandardPopulation population = new StandardPopulation();
        Genome genome = mock(Genome.class);
        when(genome.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
        population.addIndividual(genome);

        assertThrows(IllegalArgumentException.class, () -> operator.diverge(strategy, population));
    }

    @Test
    public void given_emptyInput_when_divergeEmptyPopulation_then_throwsIllegalArgumentException() {
        when(strategy.getSpeciationFactor()).thenReturn(2);

        StandardPopulation population = new StandardPopulation();

        assertThrows(IllegalArgumentException.class, () -> operator.diverge(strategy, population));
    }

    @Test
    public void given_validInput_when_divergeSpeciationFactorGreaterThanPopulationSize_then_throwsIllegalArgumentException() {
        when(strategy.getSpeciationFactor()).thenReturn(10);

        StandardPopulation population = new StandardPopulation();
        for (int i = 0; i < 5; i++) {
            Genome genome = mock(Genome.class);
            when(genome.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
            population.addIndividual(genome);
        }

        assertThrows(IllegalArgumentException.class, () -> operator.diverge(strategy, population));
    }

    @Test
    public void given_validInput_when_divergePreservesAllIndividuals_then_returnsExpectedValue() {
        when(strategy.getSpeciationFactor()).thenReturn(2);

        StandardPopulation population = new StandardPopulation();
        for (int i = 0; i < 6; i++) {
            Genome genome = mock(Genome.class);
            when(genome.getFitnesses()).thenReturn(new com.ciphertool.zenith.genetic.fitness.Fitness[0]);
            population.addIndividual(genome);
        }

        List<Population> result = operator.diverge(strategy, population);

        int totalIndividuals = result.stream().mapToInt(Population::size).sum();
        assertEquals(6, totalIndividuals);
    }
}
