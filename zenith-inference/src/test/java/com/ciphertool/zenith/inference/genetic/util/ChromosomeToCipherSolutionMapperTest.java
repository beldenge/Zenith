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

package com.ciphertool.zenith.inference.genetic.util;

import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.fitness.MaximizingFitness;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyGene;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ChromosomeToCipherSolutionMapperTest {
    @Test
    public void given_validInput_when_mapCopiesMappingsAndClonesFitness_then_copiesState() {
        Cipher cipher = new Cipher("test", 1, 2);
        cipher.setCiphertext(Arrays.asList("x", "y"));

        Fitness[] fitnesses = new Fitness[] { new MaximizingFitness(1.23) };
        Genome genome = new Genome(false, fitnesses, null);

        CipherKeyChromosome chromosome = new CipherKeyChromosome(genome, cipher, 2);
        chromosome.putGene("x", new CipherKeyGene(null, "a"));
        chromosome.putGene("y", new CipherKeyGene(null, "b"));

        CipherSolution solution = ChromosomeToCipherSolutionMapper.map(chromosome);

        assertSame(cipher, solution.getCipher());
        assertEquals('a', solution.getMappings().get("x"));
        assertEquals('b', solution.getMappings().get("y"));

        assertNotNull(solution.getScores());
        assertEquals(1, solution.getScores().length);
        assertNotSame(fitnesses[0], solution.getScores()[0]);
        assertEquals(fitnesses[0].getValue(), solution.getScores()[0].getValue(), 0.0001);
    }
}