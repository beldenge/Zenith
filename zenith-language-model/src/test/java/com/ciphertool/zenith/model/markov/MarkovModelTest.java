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

package com.ciphertool.zenith.model.markov;

import com.ciphertool.zenith.model.entities.TreeNGram;
import com.ciphertool.zenith.model.etl.importers.LetterNGramMarkovImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Random;

import static org.mockito.Mockito.spy;

public class MarkovModelTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    private static final int ORDER = 6;

    private static LetterNGramMarkovImporter importer;
    private static TreeMarkovModel model;

    // @BeforeClass
    public static void setUp() {
        ThreadPoolTaskExecutor taskExecutorSpy = spy(new ThreadPoolTaskExecutor());
        taskExecutorSpy.setCorePoolSize(4);
        taskExecutorSpy.setMaxPoolSize(4);
        taskExecutorSpy.setQueueCapacity(100);
        taskExecutorSpy.setKeepAliveSeconds(1);
        taskExecutorSpy.setAllowCoreThreadTimeOut(true);
        taskExecutorSpy.initialize();

        importer = new LetterNGramMarkovImporter();

        Field corpusDirectoryField = ReflectionUtils.findField(LetterNGramMarkovImporter.class, "corpusDirectory");
        ReflectionUtils.makeAccessible(corpusDirectoryField);
        ReflectionUtils.setField(corpusDirectoryField, importer, "/Users/george/Desktop/corpus");

        Field taskExecutorField = ReflectionUtils.findField(LetterNGramMarkovImporter.class, "taskExecutor");
        ReflectionUtils.makeAccessible(taskExecutorField);
        ReflectionUtils.setField(taskExecutorField, importer, taskExecutorSpy);

        Field orderField = ReflectionUtils.findField(LetterNGramMarkovImporter.class, "order");
        ReflectionUtils.makeAccessible(orderField);
        ReflectionUtils.setField(orderField, importer, ORDER);

        // model = importer.importCorpus(false, false);
    }

    // @Test
    public void generate() {
        StringBuilder sb = new StringBuilder();
        String root = "happyh";
        sb.append(root);

        for (int i = 0; i < 100; i++) {
            TreeNGram match = model.findLongest(root);

            Map<Character, TreeNGram> transitions = null;

            if (match != null) {
                transitions = match.getTransitions();
            }

            if (transitions == null || transitions.isEmpty()) {
                log.info("Could not find transition for root: " + root);

                break;
            }

            int count = 0;
            for (Map.Entry<Character, TreeNGram> entry : transitions.entrySet()) {
                if (entry.getValue() != null) {
                    count++;
                }
            }

            char[] tempArray = new char[count];

            count = 0;
            for (Map.Entry<Character, TreeNGram> entry : transitions.entrySet()) {
                if (entry.getValue() != null) {
                    tempArray[count] = entry.getKey();

                    count++;
                }
            }

            Random rand = new Random();
            int randomIndex = rand.nextInt(tempArray.length);

            char nextSymbol = tempArray[randomIndex];
            sb.append(nextSymbol);

            root = root.substring(1) + nextSymbol;
        }

        log.info(sb.toString());
    }
}
