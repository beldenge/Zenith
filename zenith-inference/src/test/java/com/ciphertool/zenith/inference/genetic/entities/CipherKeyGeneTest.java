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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CipherKeyGeneTest {
    @Test
    public void given_nullInput_when_constructing_then_throwsIllegalArgumentException() {
        CipherKeyChromosome chromosome = new CipherKeyChromosome(null, new Cipher("test", 1, 1), 1);

        assertThrows(IllegalArgumentException.class, () -> new CipherKeyGene(chromosome, null));
        assertThrows(IllegalArgumentException.class, () -> new CipherKeyGene(chromosome, ""));
    }

    @Test
    public void given_validInput_when_settingValueMarksEvaluationNeededWhenChanged_then_matchesExpectations() {
        Genome genome = new Genome(false, null, null);
        CipherKeyChromosome chromosome = new CipherKeyChromosome(genome, new Cipher("test", 1, 1), 1);
        CipherKeyGene gene = new CipherKeyGene(chromosome, "a");

        assertFalse(genome.isEvaluationNeeded());
        gene.setValue("a");
        assertFalse(genome.isEvaluationNeeded());

        gene.setValue("b");
        assertTrue(genome.isEvaluationNeeded());
    }

    @Test
    public void given_nullInput_when_settingValueWithNullInitialValueDoesNotThrow_then_returnsTrue() {
        Genome genome = new Genome(false, null, null);
        CipherKeyChromosome chromosome = new CipherKeyChromosome(genome, new Cipher("test", 1, 1), 1);
        CipherKeyGene gene = new CipherKeyGene();
        gene.setChromosome(chromosome);

        gene.setValue("a");

        assertTrue(genome.isEvaluationNeeded());
        assertEquals("a", gene.getValue());
    }

    @Test
    public void given_validInput_when_equalsAndHashCodeBasedOnValue_then_comparesAsExpected() {
        CipherKeyGene left = new CipherKeyGene(null, "a");
        CipherKeyGene right = new CipherKeyGene(null, "a");
        CipherKeyGene different = new CipherKeyGene(null, "b");

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertNotEquals(left, different);
        assertNotNull(left);
    }
}