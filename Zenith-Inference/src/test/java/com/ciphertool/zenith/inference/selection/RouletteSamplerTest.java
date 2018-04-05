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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ciphertool.zenith.math.sampling.RouletteSampler;
import org.junit.Test;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherSolution;
import com.ciphertool.zenith.model.probability.LetterProbability;
import com.ciphertool.zenith.inference.probability.SolutionProbability;

public class RouletteSamplerTest {

	@Test
	public void testSampling() {
		List<LetterProbability> letterUnigramProbabilities = new ArrayList<>();

		letterUnigramProbabilities.add(new LetterProbability('a', 0.07916f));
		letterUnigramProbabilities.add(new LetterProbability('b', 0.01543f));
		letterUnigramProbabilities.add(new LetterProbability('c', 0.03037f));
		letterUnigramProbabilities.add(new LetterProbability('d', 0.03962f));
		letterUnigramProbabilities.add(new LetterProbability('e', 0.12454f));
		letterUnigramProbabilities.add(new LetterProbability('f', 0.02233f));
		letterUnigramProbabilities.add(new LetterProbability('g', 0.01981f));
		letterUnigramProbabilities.add(new LetterProbability('h', 0.05323f));
		letterUnigramProbabilities.add(new LetterProbability('i', 0.07300f));
		letterUnigramProbabilities.add(new LetterProbability('j', 0.00163f));
		letterUnigramProbabilities.add(new LetterProbability('k', 0.00701f));
		letterUnigramProbabilities.add(new LetterProbability('l', 0.04058f));
		letterUnigramProbabilities.add(new LetterProbability('m', 0.02403f));
		letterUnigramProbabilities.add(new LetterProbability('n', 0.07289f));
		letterUnigramProbabilities.add(new LetterProbability('o', 0.07603f));
		letterUnigramProbabilities.add(new LetterProbability('p', 0.02002f));
		letterUnigramProbabilities.add(new LetterProbability('q', 0.00104f));
		letterUnigramProbabilities.add(new LetterProbability('r', 0.06124f));
		letterUnigramProbabilities.add(new LetterProbability('s', 0.06438f));
		letterUnigramProbabilities.add(new LetterProbability('t', 0.09284f));
		letterUnigramProbabilities.add(new LetterProbability('u', 0.02905f));
		letterUnigramProbabilities.add(new LetterProbability('v', 0.01068f));
		letterUnigramProbabilities.add(new LetterProbability('w', 0.01930f));
		letterUnigramProbabilities.add(new LetterProbability('x', 0.00226f));
		letterUnigramProbabilities.add(new LetterProbability('y', 0.01888f));
		letterUnigramProbabilities.add(new LetterProbability('z', 0.00068f));

		RouletteSampler<LetterProbability> rouletteSampler = new RouletteSampler<>();

		Collections.sort(letterUnigramProbabilities);

		Float totalProbability = rouletteSampler.reIndex(letterUnigramProbabilities);

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
			Float actual = letterProbability.getProbability();
			Float estimated = (((float) characterCounts.get(letterProbability.getValue())
					/ (float) i));
			Float difference = Math.abs(actual - estimated);

			System.out.printf(letterProbability.getValue() + ": actual=" + actual.toString() + ", estimated="
					+ estimated.toString() + ", difference=" + difference.toString() + "\n");

			assertTrue(difference.compareTo((0.001f)) < 0);
		}
	}

	@Test
	public void testSolutionSampling() {
		List<SolutionProbability> solutionProbabilities = new ArrayList<>();

		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("1", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("2", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("3", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("4", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("5", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("6", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("7", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("8", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("9", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("10", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("11", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("12", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("13", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("14", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("15", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("16", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("17", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("18", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("19", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("20", 1, 1), 0),
				(0.9999975f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("21", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("22", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("23", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("24", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("25", 1, 1), 0),
				(0.0000001f)));
		solutionProbabilities.add(new SolutionProbability(new CipherSolution(new Cipher("26", 1, 1), 0),
				(0.0000001f)));

		RouletteSampler<SolutionProbability> rouletteSampler = new RouletteSampler<>();

		Collections.sort(solutionProbabilities);

		Float totalProbability = rouletteSampler.reIndex(solutionProbabilities);

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
			Float actual = solutionProbability.getProbability();
			Float estimated = (((float) (solutionCounts.get(solutionProbability.getValue()) == null ? 0 : solutionCounts.get(solutionProbability.getValue()))
					/ (float) i));
			Float difference = Math.abs(actual - estimated);

			System.out.printf(solutionProbability.getValue().getCipher().getName() + ": actual=" + actual.toString()
					+ ", estimated=" + estimated.toString() + ", difference=" + difference.toString() + "\n");

			assertTrue(difference.compareTo((0.001f)) < 0);
		}
	}
}
