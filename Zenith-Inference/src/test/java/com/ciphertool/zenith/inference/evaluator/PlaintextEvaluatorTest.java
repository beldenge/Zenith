/**
 * Copyright 2017 George Belden
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

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.entities.Plaintext;
import com.ciphertool.zenith.math.BigDecimalFunctions;
import com.ciphertool.zenith.model.dao.LetterNGramDao;
import com.ciphertool.zenith.model.entities.NGramIndexNode;
import com.ciphertool.zenith.model.markov.MarkovModel;

public class PlaintextEvaluatorTest extends FitnessEvaluatorTestBase {
	private static Logger				log				= LoggerFactory.getLogger(PlaintextEvaluatorTest.class);

	private static MarkovModel			letterMarkovModel;

	private static PlaintextEvaluator	plaintextEvaluator;

	private static CipherSolution		actualSolution	= new CipherSolution();
	private static CipherSolution		solution2		= new CipherSolution();
	private static CipherSolution		solution3		= new CipherSolution();

	static {
		int lastRowBegin = (zodiac408.getColumns() * (zodiac408.getRows() - 1));
		int totalCharacters = zodiac408.getCiphertextCharacters().size();

		// Remove the last row altogether
		for (int i = lastRowBegin; i < totalCharacters; i++) {
			zodiac408.removeCiphertextCharacter(zodiac408.getCiphertextCharacters().get(lastRowBegin));
		}

		actualSolution.putMapping("tri", new Plaintext("i"));
		actualSolution.putMapping("lrbox", new Plaintext("l"));
		actualSolution.putMapping("p", new Plaintext("i"));
		actualSolution.putMapping("forslash", new Plaintext("k"));
		actualSolution.putMapping("z", new Plaintext("e"));
		actualSolution.putMapping("u", new Plaintext("i"));
		actualSolution.putMapping("b", new Plaintext("l"));
		actualSolution.putMapping("backk", new Plaintext("i"));
		actualSolution.putMapping("o", new Plaintext("n"));
		actualSolution.putMapping("r", new Plaintext("g"));
		actualSolution.putMapping("pi", new Plaintext("p"));
		actualSolution.putMapping("backp", new Plaintext("e"));
		actualSolution.putMapping("x", new Plaintext("o"));
		actualSolution.putMapping("w", new Plaintext("e"));
		actualSolution.putMapping("v", new Plaintext("b"));
		actualSolution.putMapping("plus", new Plaintext("e"));
		actualSolution.putMapping("backe", new Plaintext("c"));
		actualSolution.putMapping("g", new Plaintext("a"));
		actualSolution.putMapping("y", new Plaintext("u"));
		actualSolution.putMapping("f", new Plaintext("s"));
		actualSolution.putMapping("circledot", new Plaintext("e"));
		actualSolution.putMapping("h", new Plaintext("t"));
		actualSolution.putMapping("boxdot", new Plaintext("s"));
		actualSolution.putMapping("k", new Plaintext("s"));
		actualSolution.putMapping("anchor", new Plaintext("o"));
		actualSolution.putMapping("backq", new Plaintext("m"));
		actualSolution.putMapping("m", new Plaintext("h"));
		actualSolution.putMapping("j", new Plaintext("f"));
		actualSolution.putMapping("carrot", new Plaintext("n"));
		actualSolution.putMapping("i", new Plaintext("t"));
		actualSolution.putMapping("tridot", new Plaintext("s"));
		actualSolution.putMapping("t", new Plaintext("o"));
		actualSolution.putMapping("flipt", new Plaintext("r"));
		actualSolution.putMapping("n", new Plaintext("e"));
		actualSolution.putMapping("q", new Plaintext("f"));
		actualSolution.putMapping("d", new Plaintext("n"));
		actualSolution.putMapping("fullcircle", new Plaintext("t"));
		actualSolution.putMapping("horstrike", new Plaintext("h"));
		actualSolution.putMapping("s", new Plaintext("a"));
		actualSolution.putMapping("vertstrike", new Plaintext("n"));
		actualSolution.putMapping("fullbox", new Plaintext("l"));
		actualSolution.putMapping("a", new Plaintext("w"));
		actualSolution.putMapping("backf", new Plaintext("d"));
		actualSolution.putMapping("backl", new Plaintext("a"));
		actualSolution.putMapping("e", new Plaintext("e"));
		actualSolution.putMapping("l", new Plaintext("t"));
		actualSolution.putMapping("backd", new Plaintext("o"));
		actualSolution.putMapping("backr", new Plaintext("r"));
		actualSolution.putMapping("backslash", new Plaintext("r"));
		actualSolution.putMapping("fulltri", new Plaintext("a"));
		actualSolution.putMapping("zodiac", new Plaintext("d"));
		actualSolution.putMapping("backc", new Plaintext("v"));
		actualSolution.putMapping("backj", new Plaintext("x"));
		actualSolution.putMapping("box", new Plaintext("y"));

		actualSolution.addWordBoundary(0); // I
		actualSolution.addWordBoundary(4); // like
		actualSolution.addWordBoundary(11); // killing
		actualSolution.addWordBoundary(17); // people
		actualSolution.addWordBoundary(24); // because
		actualSolution.addWordBoundary(26); // it
		actualSolution.addWordBoundary(28); // is
		actualSolution.addWordBoundary(30); // so
		actualSolution.addWordBoundary(34); // much
		actualSolution.addWordBoundary(37); // fun
		actualSolution.addWordBoundary(39); // it
		actualSolution.addWordBoundary(41); // is
		actualSolution.addWordBoundary(45); // more
		actualSolution.addWordBoundary(48); // fun
		actualSolution.addWordBoundary(52); // than
		actualSolution.addWordBoundary(59); // killing
		actualSolution.addWordBoundary(63); // wild
		actualSolution.addWordBoundary(67); // game
		actualSolution.addWordBoundary(69); // in
		actualSolution.addWordBoundary(72); // the
		actualSolution.addWordBoundary(79); // forrest
		actualSolution.addWordBoundary(86); // because
		actualSolution.addWordBoundary(89); // man
		actualSolution.addWordBoundary(91); // is
		actualSolution.addWordBoundary(94); // the
		actualSolution.addWordBoundary(98); // most
		actualSolution.addWordBoundary(107); // dangeroue
		actualSolution.addWordBoundary(113); // anamal
		actualSolution.addWordBoundary(115); // of
		actualSolution.addWordBoundary(118); // all
		actualSolution.addWordBoundary(120); // to
		actualSolution.addWordBoundary(124); // kill
		actualSolution.addWordBoundary(133); // something
		actualSolution.addWordBoundary(138); // gives
		actualSolution.addWordBoundary(140); // me
		actualSolution.addWordBoundary(143); // the
		actualSolution.addWordBoundary(147); // moat
		actualSolution.addWordBoundary(156); // thrilling
		actualSolution.addWordBoundary(165); // experence
		actualSolution.addWordBoundary(167); // it
		actualSolution.addWordBoundary(169); // is
		actualSolution.addWordBoundary(173); // even
		actualSolution.addWordBoundary(179); // better
		actualSolution.addWordBoundary(183); // than
		actualSolution.addWordBoundary(190); // getting
		actualSolution.addWordBoundary(194); // your
		actualSolution.addWordBoundary(199); // rocks
		actualSolution.addWordBoundary(202); // off
		actualSolution.addWordBoundary(206); // with
		actualSolution.addWordBoundary(207); // a
		actualSolution.addWordBoundary(211); // girl
		actualSolution.addWordBoundary(214); // the
		actualSolution.addWordBoundary(218); // best
		actualSolution.addWordBoundary(222); // part
		actualSolution.addWordBoundary(224); // of
		actualSolution.addWordBoundary(226); // it
		actualSolution.addWordBoundary(228); // ia
		actualSolution.addWordBoundary(232); // thae
		actualSolution.addWordBoundary(236); // when
		actualSolution.addWordBoundary(237); // i
		actualSolution.addWordBoundary(240); // die
		actualSolution.addWordBoundary(241); // i
		actualSolution.addWordBoundary(245); // will
		actualSolution.addWordBoundary(247); // be
		actualSolution.addWordBoundary(253); // reborn
		actualSolution.addWordBoundary(255); // in
		actualSolution.addWordBoundary(263); // paradice
		actualSolution.addWordBoundary(266); // snd
		actualSolution.addWordBoundary(269); // all
		actualSolution.addWordBoundary(272); // the
		actualSolution.addWordBoundary(273); // i
		actualSolution.addWordBoundary(277); // have
		actualSolution.addWordBoundary(283); // killed
		actualSolution.addWordBoundary(287); // will
		actualSolution.addWordBoundary(293); // become
		actualSolution.addWordBoundary(295); // my
		actualSolution.addWordBoundary(301); // slaves
		actualSolution.addWordBoundary(302); // i
		actualSolution.addWordBoundary(306); // will
		actualSolution.addWordBoundary(309); // not
		actualSolution.addWordBoundary(313); // give
		actualSolution.addWordBoundary(316); // you
		actualSolution.addWordBoundary(318); // my
		actualSolution.addWordBoundary(322); // name
		actualSolution.addWordBoundary(329); // because
		actualSolution.addWordBoundary(332); // you
		actualSolution.addWordBoundary(336); // will
		actualSolution.addWordBoundary(339); // try
		actualSolution.addWordBoundary(341); // to
		actualSolution.addWordBoundary(345); // sloi
		actualSolution.addWordBoundary(349); // down
		actualSolution.addWordBoundary(351); // or
		actualSolution.addWordBoundary(355); // stop
		actualSolution.addWordBoundary(357); // my
		actualSolution.addWordBoundary(367); // collecting
		actualSolution.addWordBoundary(369); // of
		actualSolution.addWordBoundary(375); // slaves
		actualSolution.addWordBoundary(378); // for
		actualSolution.addWordBoundary(380); // my
		actualSolution.addWordBoundary(389); // afterlife
		actualSolution.addWordBoundary(390); // e

		actualSolution.setCipher(zodiac408);

		solution2.putMapping("tri", new Plaintext("i"));
		solution2.putMapping("lrbox", new Plaintext("s"));
		solution2.putMapping("p", new Plaintext("o"));
		solution2.putMapping("forslash", new Plaintext("s"));
		solution2.putMapping("z", new Plaintext("e"));
		solution2.putMapping("u", new Plaintext("e"));
		solution2.putMapping("b", new Plaintext("t"));
		solution2.putMapping("backk", new Plaintext("a"));
		solution2.putMapping("o", new Plaintext("t"));
		solution2.putMapping("r", new Plaintext("h"));
		solution2.putMapping("pi", new Plaintext("r"));
		solution2.putMapping("backp", new Plaintext("e"));
		solution2.putMapping("x", new Plaintext("e"));
		solution2.putMapping("w", new Plaintext("e"));
		solution2.putMapping("v", new Plaintext("h"));
		solution2.putMapping("plus", new Plaintext("e"));
		solution2.putMapping("backe", new Plaintext("r"));
		solution2.putMapping("g", new Plaintext("e"));
		solution2.putMapping("y", new Plaintext("a"));
		solution2.putMapping("f", new Plaintext("s"));
		solution2.putMapping("circledot", new Plaintext("e"));
		solution2.putMapping("h", new Plaintext("t"));
		solution2.putMapping("boxdot", new Plaintext("r"));
		solution2.putMapping("k", new Plaintext("s"));
		solution2.putMapping("anchor", new Plaintext("e"));
		solution2.putMapping("backq", new Plaintext("s"));
		solution2.putMapping("m", new Plaintext("h"));
		solution2.putMapping("j", new Plaintext("r"));
		solution2.putMapping("carrot", new Plaintext("t"));
		solution2.putMapping("i", new Plaintext("s"));
		solution2.putMapping("tridot", new Plaintext("s"));
		solution2.putMapping("t", new Plaintext("o"));
		solution2.putMapping("flipt", new Plaintext("r"));
		solution2.putMapping("n", new Plaintext("e"));
		solution2.putMapping("q", new Plaintext("s"));
		solution2.putMapping("d", new Plaintext("t"));
		solution2.putMapping("fullcircle", new Plaintext("t"));
		solution2.putMapping("horstrike", new Plaintext("h"));
		solution2.putMapping("s", new Plaintext("a"));
		solution2.putMapping("vertstrike", new Plaintext("t"));
		solution2.putMapping("fullbox", new Plaintext("n"));
		solution2.putMapping("a", new Plaintext("t"));
		solution2.putMapping("backf", new Plaintext("t"));
		solution2.putMapping("backl", new Plaintext("e"));
		solution2.putMapping("e", new Plaintext("e"));
		solution2.putMapping("l", new Plaintext("t"));
		solution2.putMapping("backd", new Plaintext("e"));
		solution2.putMapping("backr", new Plaintext("s"));
		solution2.putMapping("backslash", new Plaintext("s"));
		solution2.putMapping("fulltri", new Plaintext("e"));
		solution2.putMapping("zodiac", new Plaintext("r"));
		solution2.putMapping("backc", new Plaintext("v"));
		solution2.putMapping("backj", new Plaintext("p"));
		solution2.putMapping("box", new Plaintext("t"));

		solution2.setCipher(zodiac408);

		solution3.putMapping("tri", new Plaintext("i"));
		solution3.putMapping("lrbox", new Plaintext("l"));
		solution3.putMapping("p", new Plaintext("i"));
		solution3.putMapping("forslash", new Plaintext("l"));
		solution3.putMapping("z", new Plaintext("e"));
		solution3.putMapping("u", new Plaintext("a"));
		solution3.putMapping("b", new Plaintext("l"));
		solution3.putMapping("backk", new Plaintext("q"));
		solution3.putMapping("o", new Plaintext("s"));
		solution3.putMapping("r", new Plaintext("t"));
		solution3.putMapping("pi", new Plaintext("p"));
		solution3.putMapping("backp", new Plaintext("e"));
		solution3.putMapping("x", new Plaintext("o"));
		solution3.putMapping("w", new Plaintext("e"));
		solution3.putMapping("v", new Plaintext("s"));
		solution3.putMapping("plus", new Plaintext("e"));
		solution3.putMapping("backe", new Plaintext("t"));
		solution3.putMapping("g", new Plaintext("e"));
		solution3.putMapping("y", new Plaintext("s"));
		solution3.putMapping("f", new Plaintext("l"));
		solution3.putMapping("circledot", new Plaintext("e"));
		solution3.putMapping("h", new Plaintext("s"));
		solution3.putMapping("boxdot", new Plaintext("s"));
		solution3.putMapping("k", new Plaintext("t"));
		solution3.putMapping("anchor", new Plaintext("e"));
		solution3.putMapping("backq", new Plaintext("r"));
		solution3.putMapping("m", new Plaintext("h"));
		solution3.putMapping("j", new Plaintext("e"));
		solution3.putMapping("carrot", new Plaintext("n"));
		solution3.putMapping("i", new Plaintext("t"));
		solution3.putMapping("tridot", new Plaintext("a"));
		solution3.putMapping("t", new Plaintext("e"));
		solution3.putMapping("flipt", new Plaintext("r"));
		solution3.putMapping("n", new Plaintext("e"));
		solution3.putMapping("q", new Plaintext("a"));
		solution3.putMapping("d", new Plaintext("s"));
		solution3.putMapping("fullcircle", new Plaintext("t"));
		solution3.putMapping("horstrike", new Plaintext("h"));
		solution3.putMapping("s", new Plaintext("m"));
		solution3.putMapping("vertstrike", new Plaintext("n"));
		solution3.putMapping("fullbox", new Plaintext("l"));
		solution3.putMapping("a", new Plaintext("t"));
		solution3.putMapping("backf", new Plaintext("s"));
		solution3.putMapping("backl", new Plaintext("e"));
		solution3.putMapping("e", new Plaintext("e"));
		solution3.putMapping("l", new Plaintext("t"));
		solution3.putMapping("backd", new Plaintext("a"));
		solution3.putMapping("backr", new Plaintext("r"));
		solution3.putMapping("backslash", new Plaintext("r"));
		solution3.putMapping("fulltri", new Plaintext("a"));
		solution3.putMapping("zodiac", new Plaintext("t"));
		solution3.putMapping("backc", new Plaintext("r"));
		solution3.putMapping("backj", new Plaintext("m"));
		solution3.putMapping("box", new Plaintext("l"));

		solution3.setCipher(zodiac408);
	}

	// @BeforeClass
	public static void setUp() throws InterruptedException, ExecutionException {
		LetterNGramDao letterNGramDao = new LetterNGramDao();
		letterMarkovModel = new MarkovModel(6, null);

		List<NGramIndexNode> nodes = letterNGramDao.findAll(2, false);

		for (NGramIndexNode node : nodes) {
			letterMarkovModel.addNode(node);
		}

		plaintextEvaluator = new PlaintextEvaluator();
		plaintextEvaluator.setBigDecimalFunctions(new BigDecimalFunctions());
		plaintextEvaluator.setIncludeWordBoundaries(false);
	}

	// @Test
	public void testEvaluate() {
		log.info("fitness1: " + plaintextEvaluator.evaluate(letterMarkovModel, actualSolution, null));
		log.info("solution1: " + actualSolution);

		log.info("fitness2: " + plaintextEvaluator.evaluate(letterMarkovModel, solution2, null));
		log.info("solution2: " + solution2);

		log.info("fitness3: " + plaintextEvaluator.evaluate(letterMarkovModel, solution3, null));
		log.info("solution3: " + solution3);
	}

	// @Test
	public void testPerf() {
		long start = System.currentTimeMillis();
		long evaluations = 10000;

		for (int i = 0; i < evaluations; i++) {
			plaintextEvaluator.evaluate(letterMarkovModel, actualSolution, null);
		}

		log.info(evaluations + " evaluations took: " + (System.currentTimeMillis() - start) + "ms.");
	}
}
