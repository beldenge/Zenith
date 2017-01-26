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

package com.ciphertool.zenith.inference.selection;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.inference.probability.LetterProbability;
import com.ciphertool.zenith.inference.probability.SolutionProbability;

public class RouletteSamplerTest {

	@Test
	public void testSampling() {
		List<LetterProbability> letterUnigramProbabilities = new ArrayList<>();

		letterUnigramProbabilities.add(new LetterProbability('a', BigDecimal.valueOf(0.07916)));
		letterUnigramProbabilities.add(new LetterProbability('b', BigDecimal.valueOf(0.01543)));
		letterUnigramProbabilities.add(new LetterProbability('c', BigDecimal.valueOf(0.03037)));
		letterUnigramProbabilities.add(new LetterProbability('d', BigDecimal.valueOf(0.03962)));
		letterUnigramProbabilities.add(new LetterProbability('e', BigDecimal.valueOf(0.12454)));
		letterUnigramProbabilities.add(new LetterProbability('f', BigDecimal.valueOf(0.02233)));
		letterUnigramProbabilities.add(new LetterProbability('g', BigDecimal.valueOf(0.01981)));
		letterUnigramProbabilities.add(new LetterProbability('h', BigDecimal.valueOf(0.05323)));
		letterUnigramProbabilities.add(new LetterProbability('i', BigDecimal.valueOf(0.07300)));
		letterUnigramProbabilities.add(new LetterProbability('j', BigDecimal.valueOf(0.00163)));
		letterUnigramProbabilities.add(new LetterProbability('k', BigDecimal.valueOf(0.00701)));
		letterUnigramProbabilities.add(new LetterProbability('l', BigDecimal.valueOf(0.04058)));
		letterUnigramProbabilities.add(new LetterProbability('m', BigDecimal.valueOf(0.02403)));
		letterUnigramProbabilities.add(new LetterProbability('n', BigDecimal.valueOf(0.07289)));
		letterUnigramProbabilities.add(new LetterProbability('o', BigDecimal.valueOf(0.07603)));
		letterUnigramProbabilities.add(new LetterProbability('p', BigDecimal.valueOf(0.02002)));
		letterUnigramProbabilities.add(new LetterProbability('q', BigDecimal.valueOf(0.00104)));
		letterUnigramProbabilities.add(new LetterProbability('r', BigDecimal.valueOf(0.06124)));
		letterUnigramProbabilities.add(new LetterProbability('s', BigDecimal.valueOf(0.06438)));
		letterUnigramProbabilities.add(new LetterProbability('t', BigDecimal.valueOf(0.09284)));
		letterUnigramProbabilities.add(new LetterProbability('u', BigDecimal.valueOf(0.02905)));
		letterUnigramProbabilities.add(new LetterProbability('v', BigDecimal.valueOf(0.01068)));
		letterUnigramProbabilities.add(new LetterProbability('w', BigDecimal.valueOf(0.01930)));
		letterUnigramProbabilities.add(new LetterProbability('x', BigDecimal.valueOf(0.00226)));
		letterUnigramProbabilities.add(new LetterProbability('y', BigDecimal.valueOf(0.01888)));
		letterUnigramProbabilities.add(new LetterProbability('z', BigDecimal.valueOf(0.00068)));

		RouletteSampler<LetterProbability> rouletteSampler = new RouletteSampler<>();

		Collections.sort(letterUnigramProbabilities);

		BigDecimal totalProbability = rouletteSampler.reIndex(letterUnigramProbabilities);

		Map<Character, Integer> characterCounts = new HashMap<>();
		int nextIndex;

		int i = 0;

		for (; i < 1000000; i++) {
			nextIndex = rouletteSampler.getNextIndex(letterUnigramProbabilities, totalProbability);

			if (characterCounts.get(letterUnigramProbabilities.get(nextIndex).getValue()) == null) {
				characterCounts.put(letterUnigramProbabilities.get(nextIndex).getValue(), 1);
			}

			characterCounts.put(letterUnigramProbabilities.get(nextIndex).getValue(), characterCounts.get(letterUnigramProbabilities.get(nextIndex).getValue())
					+ 1);
		}

		for (LetterProbability letterProbability : letterUnigramProbabilities) {
			BigDecimal actual = letterProbability.getProbability().setScale(5, RoundingMode.HALF_UP);
			BigDecimal estimated = BigDecimal.valueOf(((double) characterCounts.get(letterProbability.getValue())
					/ (double) i)).setScale(5, RoundingMode.HALF_UP);
			BigDecimal difference = actual.subtract(estimated).abs().setScale(5, RoundingMode.HALF_UP);

			System.out.printf(letterProbability.getValue() + ": actual=" + actual.toString() + ", estimated="
					+ estimated.toString() + ", difference=" + difference.toString() + "\n");

			assertTrue(difference.compareTo(BigDecimal.valueOf(0.001)) < 0);
		}
	}

	@Test
	public void testSolutionSampling() {
		List<SolutionProbability> solutionProbabilities = new ArrayList<>();

		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("1", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("2", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("3", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("4", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("5", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("6", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("7", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("8", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("9", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("10", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("11", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("12", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("13", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("14", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("15", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("16", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("17", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("18", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("19", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("20", 1, 1), 0),
				BigDecimal.valueOf(0.999999999999999999999999999975)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("21", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("22", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("23", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("24", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("25", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("26", 1, 1), 0),
				BigDecimal.valueOf(0.000000000000000000000000000001)));

		RouletteSampler<SolutionProbability> rouletteSampler = new RouletteSampler<>();

		Collections.sort(solutionProbabilities);

		BigDecimal totalProbability = rouletteSampler.reIndex(solutionProbabilities);

		Map<CipherSolution, Integer> solutionCounts = new HashMap<>();
		int nextIndex;

		int i = 0;

		for (; i < 1000000; i++) {
			nextIndex = rouletteSampler.getNextIndex(solutionProbabilities, totalProbability);

			if (solutionCounts.get(solutionProbabilities.get(nextIndex).getValue()) == null) {
				solutionCounts.put(solutionProbabilities.get(nextIndex).getValue(), 1);
			}

			solutionCounts.put(solutionProbabilities.get(nextIndex).getValue(), solutionCounts.get(solutionProbabilities.get(nextIndex).getValue())
					+ 1);
		}

		for (SolutionProbability solutionProbability : solutionProbabilities) {
			BigDecimal actual = solutionProbability.getProbability().setScale(5, RoundingMode.HALF_UP);
			BigDecimal estimated = BigDecimal.valueOf(((double) (solutionCounts.get(solutionProbability.getValue()) == null ? 0 : solutionCounts.get(solutionProbability.getValue()))
					/ (double) i)).setScale(5, RoundingMode.HALF_UP);
			BigDecimal difference = actual.subtract(estimated).abs().setScale(5, RoundingMode.HALF_UP);

			System.out.printf(solutionProbability.getValue().getCipher().getName() + ": actual=" + actual.toString()
					+ ", estimated=" + estimated.toString() + ", difference=" + difference.toString() + "\n");

			assertTrue(difference.compareTo(BigDecimal.valueOf(0.001)) < 0);
		}
	}
}
