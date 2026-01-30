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

package com.ciphertool.zenith.inference.genetic.dao;

import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyGene;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.ArrayMarkovModel;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CipherKeyGeneDaoTest {
    @Test
    public void given_singleLetterModel_when_findProbabilisticGene_then_returnsExpectedValue() throws Exception {
        CipherKeyGeneDao dao = buildDao();

        Cipher cipher = new Cipher("test", 1, 1);
        cipher.setCiphertext(Arrays.asList("A"));
        Genome genome = new Genome(false, null, null);
        CipherKeyChromosome chromosome = new CipherKeyChromosome(genome, cipher, 1);

        Gene gene = dao.findProbabilisticGene(chromosome);

        assertTrue(gene instanceof CipherKeyGene);
        assertEquals("a", ((CipherKeyGene) gene).getValue());
    }

    @Test
    public void given_validInput_when_findRandomGene_then_returnsSingleCharacter() throws Exception {
        CipherKeyGeneDao dao = buildDao();

        Cipher cipher = new Cipher("test", 1, 1);
        cipher.setCiphertext(Arrays.asList("A"));
        Genome genome = new Genome(false, null, null);
        CipherKeyChromosome chromosome = new CipherKeyChromosome(genome, cipher, 1);

        Gene gene = dao.findRandomGene(chromosome);

        assertTrue(gene instanceof CipherKeyGene);
        assertNotNull(((CipherKeyGene) gene).getValue());
        assertEquals(1, ((CipherKeyGene) gene).getValue().length());
    }

    private CipherKeyGeneDao buildDao() throws Exception {
        CipherKeyGeneDao dao = new CipherKeyGeneDao();

        ArrayMarkovModel model = new ArrayMarkovModel(1, 0.1f);
        TreeNGram node = new TreeNGram("a");
        node.setCount(1L);
        node.setLogProbability(-1.0d);
        model.addNode(node);

        ReflectionTestUtils.setField(dao, "letterMarkovModel", model);

        Field field = CipherKeyGeneDao.class.getDeclaredField("letterUnigramProbabilities");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> list = (List<?>) field.get(null);
        list.clear();

        dao.init();

        return dao;
    }
}