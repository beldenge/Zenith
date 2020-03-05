/*
 * Copyright 2017-2020 George Belden
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

package com.ciphertool.zenith.model.etl.persisters;

import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.etl.importers.LetterNGramMarkovImporter;
import com.ciphertool.zenith.model.markov.TreeMarkovModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Component
public class NGramPersister {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private LetterNGramMarkovImporter letterNGramMarkovImporter;

    @Autowired
    private LetterNGramDao letterNGramDao;

    @Value("${ngram.persistence.batch-size}")
    private int batchSize;

    public void persistNGrams() {
        long startDelete = System.currentTimeMillis();

        log.info("Deleting all existing n-grams.");

        letterNGramDao.deleteAll();

        log.info("Completed deletion of n-grams in {}ms.", (System.currentTimeMillis() - startDelete));

        TreeMarkovModel markovModel = letterNGramMarkovImporter.importCorpus();

        long count = markovModel.size();

        log.info("Total nodes: {}", count);

        long startAdd = System.currentTimeMillis();

        log.info("Starting persistence of n-grams.");

        List<FutureTask<Void>> futures = new ArrayList<>(26);
        FutureTask<Void> task;

        for (Map.Entry<Character, TreeNGram> entry : (markovModel).getRootNode().getTransitions().entrySet()) {
            if (entry.getValue() != null) {
                task = new FutureTask<>(new PersistNodesTask(entry.getValue()));
                futures.add(task);
                this.taskExecutor.execute(task);
            }
        }

        for (FutureTask<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ie) {
                log.error("Caught InterruptedException while waiting for PersistNodesTask ", ie);
            } catch (ExecutionException ee) {
                log.error("Caught ExecutionException while waiting for PersistNodesTask ", ee);
            }
        }

        log.info("Completed persistence of n-grams in {}ms.", (System.currentTimeMillis() - startAdd));
    }

    protected List<TreeNGram> persistNodes(TreeNGram node) {
        List<TreeNGram> nGrams = new ArrayList<>();

        nGrams.add(node);

        for (Map.Entry<Character, TreeNGram> entry : node.getTransitions().entrySet()) {
            nGrams.addAll(persistNodes(entry.getValue()));

            if (nGrams.size() >= batchSize) {
                letterNGramDao.addAll(nGrams);

                nGrams = new ArrayList<>();
            }
        }

        return nGrams;
    }

    /**
     * A concurrent task for computing the conditional probability of a Markov node.
     */
    protected class PersistNodesTask implements Callable<Void> {
        private TreeNGram node;

        /**
         * @param node
         *            the root node
         */
        public PersistNodesTask(TreeNGram node) {
            this.node = node;
        }

        @Override
        public Void call() {
            List<TreeNGram> nGrams = persistNodes(node);

            letterNGramDao.addAll(nGrams);

            return null;
        }
    }
}
