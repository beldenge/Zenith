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

package com.ciphertool.zenith.inference.evaluator;

import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.markov.NDArrayModel;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore
@SpringBootTest
@RunWith(SpringRunner.class)
public class MarkovModelPlaintextEvaluatorTest extends FitnessEvaluatorTestBase {
    private static Logger log = LoggerFactory.getLogger(MarkovModelPlaintextEvaluatorTest.class);

    private static NDArrayModel letterMarkovModel;

    private static CipherSolution actualSolution = new CipherSolution();
    private static CipherSolution solution2 = new CipherSolution();
    private static CipherSolution solution3 = new CipherSolution();

    @Autowired
    private LetterNGramDao letterNGramDao;

    @Autowired
    private MarkovModelPlaintextEvaluator markovModelPlaintextEvaluator;

    static {
        int lastRowBegin = (zodiac408.getColumns() * (zodiac408.getRows() - 1));
        int totalCharacters = zodiac408.getCiphertextCharacters().size();

        // Remove the last row altogether
        for (int i = lastRowBegin; i < totalCharacters; i++) {
            zodiac408.removeCiphertextCharacter(zodiac408.getCiphertextCharacters().get(lastRowBegin));
        }

        actualSolution.putMapping("tri", "i");
        actualSolution.putMapping("lrbox", "l");
        actualSolution.putMapping("p", "i");
        actualSolution.putMapping("forslash", "k");
        actualSolution.putMapping("z", "e");
        actualSolution.putMapping("u", "i");
        actualSolution.putMapping("b", "l");
        actualSolution.putMapping("backk", "i");
        actualSolution.putMapping("o", "n");
        actualSolution.putMapping("r", "g");
        actualSolution.putMapping("pi", "p");
        actualSolution.putMapping("backp", "e");
        actualSolution.putMapping("x", "o");
        actualSolution.putMapping("w", "e");
        actualSolution.putMapping("v", "b");
        actualSolution.putMapping("plus", "e");
        actualSolution.putMapping("backe", "c");
        actualSolution.putMapping("g", "a");
        actualSolution.putMapping("y", "u");
        actualSolution.putMapping("f", "s");
        actualSolution.putMapping("circledot", "e");
        actualSolution.putMapping("h", "t");
        actualSolution.putMapping("boxdot", "s");
        actualSolution.putMapping("k", "s");
        actualSolution.putMapping("anchor", "o");
        actualSolution.putMapping("backq", "m");
        actualSolution.putMapping("m", "h");
        actualSolution.putMapping("j", "f");
        actualSolution.putMapping("carrot", "n");
        actualSolution.putMapping("i", "t");
        actualSolution.putMapping("tridot", "s");
        actualSolution.putMapping("t", "o");
        actualSolution.putMapping("flipt", "r");
        actualSolution.putMapping("n", "e");
        actualSolution.putMapping("q", "f");
        actualSolution.putMapping("d", "n");
        actualSolution.putMapping("fullcircle", "t");
        actualSolution.putMapping("horstrike", "h");
        actualSolution.putMapping("s", "a");
        actualSolution.putMapping("vertstrike", "n");
        actualSolution.putMapping("fullbox", "l");
        actualSolution.putMapping("a", "w");
        actualSolution.putMapping("backf", "d");
        actualSolution.putMapping("backl", "a");
        actualSolution.putMapping("e", "e");
        actualSolution.putMapping("l", "t");
        actualSolution.putMapping("backd", "o");
        actualSolution.putMapping("backr", "r");
        actualSolution.putMapping("backslash", "r");
        actualSolution.putMapping("fulltri", "a");
        actualSolution.putMapping("zodiac", "d");
        actualSolution.putMapping("backc", "v");
        actualSolution.putMapping("backj", "x");
        actualSolution.putMapping("box", "y");

        actualSolution.setCipher(zodiac408);

        solution2.putMapping("tri", "i");
        solution2.putMapping("lrbox", "s");
        solution2.putMapping("p", "o");
        solution2.putMapping("forslash", "s");
        solution2.putMapping("z", "e");
        solution2.putMapping("u", "e");
        solution2.putMapping("b", "t");
        solution2.putMapping("backk", "a");
        solution2.putMapping("o", "t");
        solution2.putMapping("r", "h");
        solution2.putMapping("pi", "r");
        solution2.putMapping("backp", "e");
        solution2.putMapping("x", "e");
        solution2.putMapping("w", "e");
        solution2.putMapping("v", "h");
        solution2.putMapping("plus", "e");
        solution2.putMapping("backe", "r");
        solution2.putMapping("g", "e");
        solution2.putMapping("y", "a");
        solution2.putMapping("f", "s");
        solution2.putMapping("circledot", "e");
        solution2.putMapping("h", "t");
        solution2.putMapping("boxdot", "r");
        solution2.putMapping("k", "s");
        solution2.putMapping("anchor", "e");
        solution2.putMapping("backq", "s");
        solution2.putMapping("m", "h");
        solution2.putMapping("j", "r");
        solution2.putMapping("carrot", "t");
        solution2.putMapping("i", "s");
        solution2.putMapping("tridot", "s");
        solution2.putMapping("t", "o");
        solution2.putMapping("flipt", "r");
        solution2.putMapping("n", "e");
        solution2.putMapping("q", "s");
        solution2.putMapping("d", "t");
        solution2.putMapping("fullcircle", "t");
        solution2.putMapping("horstrike", "h");
        solution2.putMapping("s", "a");
        solution2.putMapping("vertstrike", "t");
        solution2.putMapping("fullbox", "n");
        solution2.putMapping("a", "t");
        solution2.putMapping("backf", "t");
        solution2.putMapping("backl", "e");
        solution2.putMapping("e", "e");
        solution2.putMapping("l", "t");
        solution2.putMapping("backd", "e");
        solution2.putMapping("backr", "s");
        solution2.putMapping("backslash", "s");
        solution2.putMapping("fulltri", "e");
        solution2.putMapping("zodiac", "r");
        solution2.putMapping("backc", "v");
        solution2.putMapping("backj", "p");
        solution2.putMapping("box", "t");

        solution2.setCipher(zodiac408);

        solution3.putMapping("tri", "i");
        solution3.putMapping("lrbox", "l");
        solution3.putMapping("p", "i");
        solution3.putMapping("forslash", "l");
        solution3.putMapping("z", "e");
        solution3.putMapping("u", "a");
        solution3.putMapping("b", "l");
        solution3.putMapping("backk", "q");
        solution3.putMapping("o", "s");
        solution3.putMapping("r", "t");
        solution3.putMapping("pi", "p");
        solution3.putMapping("backp", "e");
        solution3.putMapping("x", "o");
        solution3.putMapping("w", "e");
        solution3.putMapping("v", "s");
        solution3.putMapping("plus", "e");
        solution3.putMapping("backe", "t");
        solution3.putMapping("g", "e");
        solution3.putMapping("y", "s");
        solution3.putMapping("f", "l");
        solution3.putMapping("circledot", "e");
        solution3.putMapping("h", "s");
        solution3.putMapping("boxdot", "s");
        solution3.putMapping("k", "t");
        solution3.putMapping("anchor", "e");
        solution3.putMapping("backq", "r");
        solution3.putMapping("m", "h");
        solution3.putMapping("j", "e");
        solution3.putMapping("carrot", "n");
        solution3.putMapping("i", "t");
        solution3.putMapping("tridot", "a");
        solution3.putMapping("t", "e");
        solution3.putMapping("flipt", "r");
        solution3.putMapping("n", "e");
        solution3.putMapping("q", "a");
        solution3.putMapping("d", "s");
        solution3.putMapping("fullcircle", "t");
        solution3.putMapping("horstrike", "h");
        solution3.putMapping("s", "m");
        solution3.putMapping("vertstrike", "n");
        solution3.putMapping("fullbox", "l");
        solution3.putMapping("a", "t");
        solution3.putMapping("backf", "s");
        solution3.putMapping("backl", "e");
        solution3.putMapping("e", "e");
        solution3.putMapping("l", "t");
        solution3.putMapping("backd", "a");
        solution3.putMapping("backr", "r");
        solution3.putMapping("backslash", "r");
        solution3.putMapping("fulltri", "a");
        solution3.putMapping("zodiac", "t");
        solution3.putMapping("backc", "r");
        solution3.putMapping("backj", "m");
        solution3.putMapping("box", "l");

        solution3.setCipher(zodiac408);
    }

    @Before
    public void initializeMarkovModel() {
        if (letterMarkovModel != null) {
            return;
        }

        letterMarkovModel = new NDArrayModel(6);

        letterNGramDao.findAll().stream().forEach(letterMarkovModel::addNode);

        Float unknownLetterNGramProbability = 1f / (float) letterMarkovModel.getTotalNGramCount();
        letterMarkovModel.setUnknownLetterNGramProbability(unknownLetterNGramProbability);
        letterMarkovModel.setUnknownLetterNGramLogProbability((float) Math.log(unknownLetterNGramProbability));
    }

    @Test
    public void testEvaluate() {
        markovModelPlaintextEvaluator.evaluate(actualSolution, actualSolution.asSingleLineString(), null);
        log.info("fitness1: " + actualSolution.getLogProbability());
        log.info("solution1: " + actualSolution);

        markovModelPlaintextEvaluator.evaluate(solution2, solution2.asSingleLineString(), null);
        log.info("fitness2: " + solution2.getLogProbability());
        log.info("solution2: " + solution2);

        markovModelPlaintextEvaluator.evaluate(solution3, solution3.asSingleLineString(), null);
        log.info("fitness3: " + solution3.getLogProbability());
        log.info("solution3: " + solution3);
    }

    @Test
    public void testPerf() {
        long start = System.currentTimeMillis();
        long evaluations = 10000;

        for (int i = 0; i < evaluations; i++) {
            markovModelPlaintextEvaluator.evaluate(actualSolution, actualSolution.asSingleLineString(), null);
        }

        log.info(evaluations + " evaluations took: " + (System.currentTimeMillis() - start) + "ms.");
    }
}
