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

package com.ciphertool.zenith.search;

import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.evaluator.MarkovModelPlaintextEvaluator;
import com.ciphertool.zenith.inference.evaluator.PlaintextEvaluator;
import com.ciphertool.zenith.inference.transformer.ciphertext.RemoveLastRowCipherTransformer;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.markov.MapMarkovModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication(scanBasePackages = {
        "com.ciphertool.zenith.search",
        "com.ciphertool.zenith.model.dao",
        "com.ciphertool.zenith.model.archive",
        "com.ciphertool.zenith.inference.dao",
        "com.ciphertool.zenith.inference.evaluator",
        "com.ciphertool.zenith.inference.optimizer",
        "com.ciphertool.zenith.inference.printer",
        "com.ciphertool.zenith.inference.transformer"
})
public class MutationSearchApplication implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${cipher.name}")
    private String cipherName;

    @Value("${markov.letter.order}")
    private int markovOrder;

    @Value("${decipherment.remove-last-row:true}")
    private boolean removeLastRow;

    @Autowired
    private TranspositionSearcher searcher;

    public static void main(String[] args) {
        SpringApplication.run(MutationSearchApplication.class, args).close();
    }

    @Override
    public void run(String... arg0) {
        searcher.run();
    }

    @Bean
    public Cipher cipher(RemoveLastRowCipherTransformer lastRowCipherTransformer, CipherDao cipherDao) {
        Cipher cipher = cipherDao.findByCipherName(cipherName);

        if (removeLastRow) {
            cipher = lastRowCipherTransformer.transform(cipher);
        }

        return cipher;
    }

    @Bean
    public MapMarkovModel letterMarkovModel(LetterNGramDao letterNGramDao) {
        long startFindAll = System.currentTimeMillis();
        log.info("Beginning retrieval of all n-grams.");

        /*
         * Begin setting up letter n-gram model
         */
        List<TreeNGram> nGramNodes = letterNGramDao.findAll();

        log.info("Finished retrieving {} n-grams in {}ms.", nGramNodes.size(), (System.currentTimeMillis() - startFindAll));

        MapMarkovModel letterMarkovModel = new MapMarkovModel(markovOrder);

        long startAdding = System.currentTimeMillis();
        log.info("Adding nodes to the model.");

        nGramNodes.stream().forEach(letterMarkovModel::addNode);

        log.info("Finished adding nodes to the letter n-gram model in {}ms.", (System.currentTimeMillis() - startAdding));

        Double unknownLetterNGramProbability = 1d / (double) letterMarkovModel.getTotalNumberOfNgrams();
        letterMarkovModel.setUnknownLetterNGramProbability(unknownLetterNGramProbability);
        letterMarkovModel.setUnknownLetterNGramLogProbability(Math.log(unknownLetterNGramProbability));

        return letterMarkovModel;
    }

    @Bean
    public PlaintextEvaluator plaintextEvaluator(MarkovModelPlaintextEvaluator markovModelPlaintextEvaluator) {
        return markovModelPlaintextEvaluator;
    }
}
