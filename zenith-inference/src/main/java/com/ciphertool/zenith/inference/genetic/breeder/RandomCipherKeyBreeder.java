/**
 * Copyright 2017-2019 George Belden
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
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RandomCipherKeyBreeder extends AbstractCipherKeyBreeder {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private GeneDao geneDao;

    @Override
    public void init(Cipher cipher) {
        super.init(cipher);
    }

    @Override
    public Chromosome breed() {
        CipherKeyChromosome chromosome = new CipherKeyChromosome(cipher, keys.length);

        for (int i = 0; i < keys.length; i++) {
            // Should never happen, but we check just in case
            if (chromosome.actualSize() >= chromosome.targetSize()) {
                throw new IllegalStateException(
                        "Attempted to add a Gene to CipherKeyChromosome, but the maximum number of Genes ("
                                + chromosome.targetSize() + ") have already been allocated.");
            }

            Gene newGene = geneDao.findRandomGene(chromosome);

            chromosome.putGene(keys[i], newGene);
        }

        log.debug(chromosome.toString());

        return chromosome;
    }
}
