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
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.SolutionScorer;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyChromosome;
import com.ciphertool.zenith.inference.genetic.entities.CipherKeyGene;
import com.ciphertool.zenith.inference.genetic.fitness.PlaintextEvaluatorWrappingFitnessEvaluator;
import com.ciphertool.zenith.inference.transformer.plaintext.PlaintextTransformer;
import com.ciphertool.zenith.inference.util.IndexOfCoincidenceEvaluator;
import com.ciphertool.zenith.model.LanguageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class HillClimbingCipherKeyBreeder extends AbstractCipherKeyBreeder {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${genetic-algorithm.breeder.hill-climbing.iterations}")
    private int samplerIterations;

    @Autowired
    private GeneDao geneDao;

    @Autowired
    private PlaintextEvaluator plaintextEvaluator;

    @Autowired
    @Qualifier("activePlaintextTransformers")
    private List<PlaintextTransformer> plaintextTransformers;

    @Autowired
    private SolutionScorer solutionScorer;

    @Autowired
    private IndexOfCoincidenceEvaluator indexOfCoincidenceEvaluator;

    private PlaintextEvaluatorWrappingFitnessEvaluator fitnessEvaluator;

    @Override
    public void init(Cipher cipher) {
        super.init(cipher);

        fitnessEvaluator = new PlaintextEvaluatorWrappingFitnessEvaluator(plaintextEvaluator, plaintextTransformers, indexOfCoincidenceEvaluator, solutionScorer);
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

        for (int i = 0; i < samplerIterations; i ++) {
            chromosome = runLetterSampler(chromosome);
        }

        log.debug(chromosome.toString());

        return chromosome;
    }

    private CipherKeyChromosome runLetterSampler(CipherKeyChromosome solution) {
        CipherKeyChromosome proposal;

        List<String> mappingList = new ArrayList<>();
        mappingList.addAll(solution.getGenes().keySet());

        String nextKey;

        // For each cipher symbol type, run the letter sampling
        for (int i = 0; i < solution.getGenes().size(); i++) {
            proposal = (CipherKeyChromosome) solution.clone();

            nextKey = mappingList.get(i);

            String letter = String.valueOf(LanguageConstants.LOWERCASE_LETTERS[ThreadLocalRandom.current().nextInt(LanguageConstants.LOWERCASE_LETTERS_SIZE)]);

            proposal.replaceGene(nextKey, new CipherKeyGene(proposal, letter));

            Double proposalScore = fitnessEvaluator.evaluate(proposal);

            solution = proposalScore > solution.getFitness() ? proposal : solution;
        }

        return solution;
    }
}
