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

package com.ciphertool.zenith.genetic.algorithms.crossover;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CrossoverProgressDtoTest {
	private static final int AMOUNT_TO_ADVANCE = 5;

	@Test
	public void testConstructor() {
		CrossoverProgressDto crossoverProgressDto = new CrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getFirstChromosomeGeneIndex());
		assertEquals(0, crossoverProgressDto.getFirstChromosomeSequencePosition());
		assertEquals(0, crossoverProgressDto.getSecondChromosomeGeneIndex());
		assertEquals(0, crossoverProgressDto.getSecondChromosomeSequencePosition());
	}

	@Test
	public void testAdvanceChildSequencePositionBy() {
		CrossoverProgressDto crossoverProgressDto = new CrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getFirstChromosomeSequencePosition());

		crossoverProgressDto.advanceFirstChromosomeSequencePositionBy(AMOUNT_TO_ADVANCE);

		assertEquals(AMOUNT_TO_ADVANCE, crossoverProgressDto.getFirstChromosomeSequencePosition());
	}

	@Test
	public void testAdvanceParentSequencePositionBy() {
		CrossoverProgressDto crossoverProgressDto = new CrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getSecondChromosomeSequencePosition());

		crossoverProgressDto.advanceSecondChromosomeSequencePositionBy(AMOUNT_TO_ADVANCE);

		assertEquals(AMOUNT_TO_ADVANCE, crossoverProgressDto.getSecondChromosomeSequencePosition());
	}

	@Test
	public void testAdvanceChildGeneIndexBy() {
		CrossoverProgressDto crossoverProgressDto = new CrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getFirstChromosomeGeneIndex());

		crossoverProgressDto.advanceFirstChromosomeGeneIndexBy(AMOUNT_TO_ADVANCE);

		assertEquals(AMOUNT_TO_ADVANCE, crossoverProgressDto.getFirstChromosomeGeneIndex());
	}

	@Test
	public void testAdvanceParentGeneIndexBy() {
		CrossoverProgressDto crossoverProgressDto = new CrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getSecondChromosomeGeneIndex());

		crossoverProgressDto.advanceSecondChromosomeGeneIndexBy(AMOUNT_TO_ADVANCE);

		assertEquals(AMOUNT_TO_ADVANCE, crossoverProgressDto.getSecondChromosomeGeneIndex());
	}
}
