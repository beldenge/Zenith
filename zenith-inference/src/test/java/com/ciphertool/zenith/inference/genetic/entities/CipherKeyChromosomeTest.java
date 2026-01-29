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

package com.ciphertool.zenith.inference.genetic.entities;

import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.inference.entities.Cipher;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CipherKeyChromosomeTest {
    private Cipher buildCipher(String name) {
        Cipher cipher = new Cipher(name, 1, 2);
        cipher.setCiphertext(Arrays.asList("x", "y"));
        return cipher;
    }

    @Test
    public void testPutGeneSetsChromosomeAndMarksEvaluationNeeded() {
        Genome genome = new Genome(false, null, null);
        CipherKeyChromosome chromosome = new CipherKeyChromosome(genome, buildCipher("test"), 1);
        CipherKeyGene gene = new CipherKeyGene(null, "a");

        chromosome.putGene("x", gene);

        assertSame(chromosome, gene.getChromosome());
        assertTrue(genome.isEvaluationNeeded());
    }

    @Test
    public void testPutGeneRejectsNullOrDuplicate() {
        CipherKeyChromosome chromosome = new CipherKeyChromosome(null, buildCipher("test"), 1);

        assertThrows(IllegalArgumentException.class, () -> chromosome.putGene("x", null));

        chromosome.putGene("x", new CipherKeyGene(null, "a"));
        assertThrows(IllegalArgumentException.class, () -> chromosome.putGene("x", new CipherKeyGene(null, "b")));
    }

    @Test
    public void testReplaceGeneValidatesInputsAndMarksEvaluationNeeded() {
        Genome genome = new Genome(false, null, null);
        CipherKeyChromosome chromosome = new CipherKeyChromosome(genome, buildCipher("test"), 1);

        chromosome.putGene("x", new CipherKeyGene(null, "a"));
        genome.setEvaluationNeeded(false);

        chromosome.replaceGene("x", new CipherKeyGene(null, "a"));
        assertFalse(genome.isEvaluationNeeded());

        chromosome.replaceGene("x", new CipherKeyGene(null, "b"));
        assertTrue(genome.isEvaluationNeeded());
    }

    @Test
    public void testReplaceGeneRejectsNullAndMissingKey() {
        CipherKeyChromosome chromosome = new CipherKeyChromosome(null, buildCipher("test"), 1);

        assertThrows(IllegalArgumentException.class, () -> chromosome.replaceGene("x", new CipherKeyGene(null, "a")));
        assertThrows(IllegalArgumentException.class, () -> chromosome.replaceGene("x", null));
    }

    @Test
    public void testRemoveGeneRejectsMissingKey() {
        CipherKeyChromosome chromosome = new CipherKeyChromosome(null, buildCipher("test"), 1);
        assertThrows(IllegalArgumentException.class, () -> chromosome.removeGene("x"));
    }

    @Test
    public void testCloneCopiesGenes() {
        CipherKeyChromosome chromosome = new CipherKeyChromosome(null, buildCipher("test"), 2);
        CipherKeyGene gene = new CipherKeyGene(null, "a");
        chromosome.putGene("x", gene);

        CipherKeyChromosome clone = (CipherKeyChromosome) chromosome.clone();

        assertNotSame(chromosome, clone);
        assertEquals(chromosome.getGenes(), clone.getGenes());
        assertNotSame(chromosome.getGenes().get("x"), clone.getGenes().get("x"));
    }

    @Test
    public void testEqualsAndHashCodeIncludeCipher() {
        CipherKeyChromosome left = new CipherKeyChromosome(null, buildCipher("left"), 1);
        left.putGene("x", new CipherKeyGene(null, "a"));

        CipherKeyChromosome same = new CipherKeyChromosome(null, buildCipher("left"), 1);
        same.putGene("x", new CipherKeyGene(null, "a"));

        CipherKeyChromosome differentCipher = new CipherKeyChromosome(null, buildCipher("right"), 1);
        differentCipher.putGene("x", new CipherKeyGene(null, "a"));

        assertEquals(left, same);
        assertEquals(left.hashCode(), same.hashCode());
        assertNotEquals(left, differentCipher);
        assertNotNull(left);
    }
}