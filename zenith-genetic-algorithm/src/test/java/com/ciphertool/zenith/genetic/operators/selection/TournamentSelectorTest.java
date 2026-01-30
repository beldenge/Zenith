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

package com.ciphertool.zenith.genetic.operators.selection;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class TournamentSelectorTest {
    @Test
    public void given_emptyInput_when_gettingNextIndexEmptyReturnsMinusOne_then_returnsExpectedValue() {
        RandomSelector randomSelector = mock(RandomSelector.class);
        TournamentSelector selector = new TournamentSelector(randomSelector);

        int index = selector.getNextIndex(Collections.emptyList(), mock(GeneticAlgorithmStrategy.class));

        assertEquals(-1, index);
        verifyNoInteractions(randomSelector);
    }

    @Test
    public void given_validInput_when_gettingNextIndexSelectsBestWhenAccuracyIsCertain_then_returnsExpectedValue() {
        RandomSelector randomSelector = mock(RandomSelector.class);
        TournamentSelector selector = new TournamentSelector(randomSelector);

        Genome weakest = genomeWithFitness(1.0d);
        Genome middle = genomeWithFitness(3.0d);
        Genome strongest = genomeWithFitness(5.0d);

        List<Genome> individuals = Arrays.asList(weakest, strongest, middle);

        GeneticAlgorithmStrategy strategy = GeneticAlgorithmStrategy.builder()
                .tournamentSelectorAccuracy(1.0d)
                .tournamentSize(3)
                .build();

        when(randomSelector.getNextIndex(individuals, strategy)).thenReturn(0, 1, 2);

        int index = selector.getNextIndex(individuals, strategy);

        assertEquals(1, index);
    }

    private Genome genomeWithFitness(double value) {
        return new Genome(false, new com.ciphertool.zenith.genetic.fitness.Fitness[] { new MaximizingFitness(value) }, null);
    }
}
