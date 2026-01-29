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

package com.ciphertool.zenith.inference.genetic.breeder;

import com.ciphertool.zenith.genetic.dao.GeneDao;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyGene;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProbabilisticCipherKeyBreederTest {
    @Test
    public void testBreedUsesDistinctCipherKeys() {
        ProbabilisticCipherKeyBreeder breeder = new ProbabilisticCipherKeyBreeder();
        GeneDao geneDao = mock(GeneDao.class);
        ReflectionTestUtils.setField(breeder, "geneDao", geneDao);

        Cipher cipher = new Cipher("test", 1, 4);
        cipher.setCiphertext(Arrays.asList("A", "B", "A", "C"));

        breeder.init(cipher, Collections.emptyList(), null);

        when(geneDao.findProbabilisticGene(any())).thenAnswer(invocation -> {
            CipherKeyChromosome chromosome = (CipherKeyChromosome) invocation.getArgument(0);
            return new CipherKeyGene(chromosome, "y");
        });

        Population population = mock(Population.class);
        Genome genome = breeder.breed(population);

        assertEquals(1, genome.getChromosomes().size());
        CipherKeyChromosome chromosome = (CipherKeyChromosome) genome.getChromosomes().get(0);
        assertEquals(3, chromosome.getGenes().size());
        assertTrue(chromosome.getGenes().containsKey("A"));
        assertTrue(chromosome.getGenes().containsKey("B"));
        assertTrue(chromosome.getGenes().containsKey("C"));

        verify(geneDao, times(3)).findProbabilisticGene(any());
    }
}