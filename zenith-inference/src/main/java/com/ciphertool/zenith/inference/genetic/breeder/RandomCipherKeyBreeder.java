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

import com.ciphertool.zenith.genetic.Breeder;
import com.ciphertool.zenith.genetic.dao.GeneDao;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "decipherment.optimizer", havingValue = "GeneticAlgorithmSolutionOptimizer")
public class RandomCipherKeyBreeder implements Breeder {
    private Logger log = LoggerFactory.getLogger(getClass());

    private static final String[] KEYS = {"a", "anchor", "b", "backc", "backd", "backe", "backf", "backj",
            "backk", "backl", "backp", "backq", "backr", "backslash", "box", "boxdot", "carrot", "circledot", "d", "e",
            "f", "flipt", "forslash", "fullbox", "fullcircle", "fulltri", "g", "h", "horstrike", "i", "j", "k", "l",
            "lrbox", "m", "n", "o", "p", "pi", "plus", "q", "r", "s", "t", "tri", "tridot", "u", "v", "vertstrike", "w",
            "x", "y", "z", "zodiac"};

    @Autowired
    private Cipher cipher;

    @Autowired
    private GeneDao geneDao;

    /**
     * Default no-args constructor
     */
    public RandomCipherKeyBreeder() {
    }

    @Override
    public Chromosome breed() {
        CipherKeyChromosome chromosome = new CipherKeyChromosome(cipher, KEYS.length);

        for (int i = 0; i < KEYS.length; i++) {
            // Should never happen, but we check just in case
            if (chromosome.actualSize() >= chromosome.targetSize()) {
                throw new IllegalStateException(
                        "Attempted to add a Gene to CipherKeyChromosome, but the maximum number of Genes ("
                                + chromosome.targetSize() + ") have already been allocated.");
            }

            Gene newGene = geneDao.findRandomGene(chromosome);

            chromosome.putGene(KEYS[i], newGene);
        }

        log.debug(chromosome.toString());

        return chromosome;
    }
}
