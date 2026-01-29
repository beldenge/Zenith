package com.ciphertool.zenith.genetic.operators.algorithm;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.operators.crossover.CrossoverOperator;
import com.ciphertool.zenith.genetic.operators.mutation.MutationOperator;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.genetic.population.StandardPopulation;
import com.ciphertool.zenith.genetic.statistics.ExecutionStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DivergentGeneticAlgorithmTest {

    private DivergentGeneticAlgorithm divergentGeneticAlgorithm;
    private GeneticAlgorithmStrategy strategy;
    private Population population;

    @BeforeEach
    public void setUp() {
        divergentGeneticAlgorithm = new DivergentGeneticAlgorithm();
        strategy = mock(GeneticAlgorithmStrategy.class);
        population = mock(StandardPopulation.class);
        when(strategy.getPopulation()).thenReturn(population);
        when(strategy.getTaskExecutor()).thenReturn(new SyncTaskExecutor());
        when(strategy.getPopulationSize()).thenReturn(10);
        when(strategy.getElitism()).thenReturn(2);
        
        ReflectionTestUtils.setField(divergentGeneticAlgorithm, "calculateEntropy", false);
    }

    @Test
    public void testProceedWithNextGeneration() {
        ExecutionStatistics executionStatistics = mock(ExecutionStatistics.class);
        
        List<Parents> parentsList = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            parentsList.add(new Parents(mock(Genome.class), mock(Genome.class)));
        }
        when(population.select()).thenReturn(parentsList);
        when(population.size()).thenReturn(10);
        List<Genome> individuals = new ArrayList<>();
        for (int i = 0; i < 10; i++) individuals.add(mock(Genome.class));
        when(population.getIndividuals()).thenReturn(individuals);

        CrossoverOperator crossoverOperator = mock(CrossoverOperator.class);
        when(strategy.getCrossoverOperator()).thenReturn(crossoverOperator);
        when(crossoverOperator.crossover(any(), any())).thenReturn(mock(Genome.class));

        MutationOperator mutationOperator = mock(MutationOperator.class);
        when(strategy.getMutationOperator()).thenReturn(mutationOperator);
        when(mutationOperator.mutateChromosomes(any(), any())).thenReturn(true);

        divergentGeneticAlgorithm.proceedWithNextGeneration(strategy, executionStatistics, 1);

        verify(population).select();
        verify(crossoverOperator, times(8)).crossover(any(), any());
        verify(mutationOperator, times(8)).mutateChromosomes(any(), any());
        verify(population).evaluateFitness(any());
        verify(executionStatistics).addGenerationStatistics(any());
    }

    @Test
    public void testCrossover() {
        List<Parents> parentsList = new ArrayList<>();
        parentsList.add(new Parents(mock(Genome.class), mock(Genome.class)));
        parentsList.add(new Parents(mock(Genome.class), mock(Genome.class)));
        parentsList.add(new Parents(mock(Genome.class), mock(Genome.class)));
        parentsList.add(new Parents(mock(Genome.class), mock(Genome.class)));
        parentsList.add(new Parents(mock(Genome.class), mock(Genome.class)));
        parentsList.add(new Parents(mock(Genome.class), mock(Genome.class)));
        parentsList.add(new Parents(mock(Genome.class), mock(Genome.class)));
        parentsList.add(new Parents(mock(Genome.class), mock(Genome.class)));

        CrossoverOperator crossoverOperator = mock(CrossoverOperator.class);
        when(strategy.getCrossoverOperator()).thenReturn(crossoverOperator);
        when(crossoverOperator.crossover(any(), any())).thenReturn(mock(Genome.class));
        when(population.size()).thenReturn(10);

        List<Genome> children = divergentGeneticAlgorithm.crossover(strategy, parentsList);

        assertEquals(8, children.size());
    }

    @Test
    public void testCrossover_Failure() {
        List<Parents> parentsList = new ArrayList<>();
        parentsList.add(new Parents(mock(Genome.class), mock(Genome.class)));

        CrossoverOperator crossoverOperator = mock(CrossoverOperator.class);
        when(strategy.getCrossoverOperator()).thenReturn(crossoverOperator);
        when(crossoverOperator.crossover(any(), any())).thenReturn(mock(Genome.class));
        when(population.size()).thenReturn(10);

        assertThrows(IllegalStateException.class, () -> divergentGeneticAlgorithm.crossover(strategy, parentsList));
    }
}
