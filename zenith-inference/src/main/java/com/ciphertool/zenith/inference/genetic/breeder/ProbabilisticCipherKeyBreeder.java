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
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProbabilisticCipherKeyBreeder extends AbstractCipherKeyBreeder {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private GeneDao geneDao;

    @Override
    public void init(Cipher cipher, List<PlaintextTransformationStep> plaintextTransformationSteps, PlaintextEvaluator plaintextEvaluator) {
        super.init(cipher, plaintextTransformationSteps, plaintextEvaluator);
    }

    @Override
    public Genome breed(Population population) {
        Genome genome = new Genome(true, null, population);
        CipherKeyChromosome chromosome = new CipherKeyChromosome(genome, cipher, keys.length);

        for (String ciphertext : keys) {
            // Pick a plaintext at random according to the language model
            chromosome.putGene(ciphertext, geneDao.findProbabilisticGene(chromosome));
        }

        log.debug(chromosome.toString());

        genome.addChromosome(chromosome);

        return genome;
    }
}
