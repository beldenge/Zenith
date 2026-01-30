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

import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyGene;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class BiasedCipherKeyBreederTest {
    @Test
    public void given_validInput_when_breedUsesBiasedBucket_then_returnsTrue() {
        BiasedCipherKeyBreeder breeder = new BiasedCipherKeyBreeder();
        ArrayMarkovModel markovModel = new ArrayMarkovModel(1, 1f);
        TreeNGram node = new TreeNGram("a");
        node.setCount(10L);
        markovModel.addNode(node);

        ReflectionTestUtils.setField(breeder, "letterMarkovModel", markovModel);

        Cipher cipher = new Cipher("test", 1, 3);
        cipher.setCiphertext(Arrays.asList("A", "B", "A"));

        breeder.init(cipher, Collections.emptyList(), null);

        Population population = mock(Population.class);
        Genome genome = breeder.breed(population);

        assertEquals(1, genome.getChromosomes().size());
        CipherKeyChromosome chromosome = (CipherKeyChromosome) genome.getChromosomes().get(0);
        assertEquals(2, chromosome.getGenes().size());

        for (String key : chromosome.getGenes().keySet()) {
            CipherKeyGene gene = (CipherKeyGene) chromosome.getGenes().get(key);
            assertEquals("a", gene.getValue());
        }

        assertTrue(chromosome.getGenes().containsKey("A"));
        assertTrue(chromosome.getGenes().containsKey("B"));
    }
}