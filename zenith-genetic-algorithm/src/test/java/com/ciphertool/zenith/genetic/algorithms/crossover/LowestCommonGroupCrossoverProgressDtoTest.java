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

public class LowestCommonGroupCrossoverProgressDtoTest {
	private static final int AMOUNT_TO_ADVANCE = 5;

	@Test
	public void testConstructor() {
		LowestCommonGroupCrossoverProgressDto crossoverProgressDto = new LowestCommonGroupCrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getFirstChromosomeBeginGeneIndex());
		assertEquals(0, crossoverProgressDto.getFirstChromosomeEndGeneIndex());
		assertEquals(0, crossoverProgressDto.getFirstChromosomeSequencePosition());
		assertEquals(0, crossoverProgressDto.getSecondChromosomeBeginGeneIndex());
		assertEquals(0, crossoverProgressDto.getSecondChromosomeEndGeneIndex());
		assertEquals(0, crossoverProgressDto.getSecondChromosomeSequencePosition());
	}

	@Test
	public void testAdvanceChildSequencePositionBy() {
		LowestCommonGroupCrossoverProgressDto crossoverProgressDto = new LowestCommonGroupCrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getFirstChromosomeSequencePosition());

		crossoverProgressDto.advanceFirstChromosomeSequencePositionBy(AMOUNT_TO_ADVANCE);

		assertEquals(AMOUNT_TO_ADVANCE, crossoverProgressDto.getFirstChromosomeSequencePosition());
	}

	@Test
	public void testAdvanceParentSequencePositionBy() {
		LowestCommonGroupCrossoverProgressDto crossoverProgressDto = new LowestCommonGroupCrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getSecondChromosomeSequencePosition());

		crossoverProgressDto.advanceSecondChromosomeSequencePositionBy(AMOUNT_TO_ADVANCE);

		assertEquals(AMOUNT_TO_ADVANCE, crossoverProgressDto.getSecondChromosomeSequencePosition());
	}

	@Test
	public void testAdvanceChildEndGeneIndexBy() {
		LowestCommonGroupCrossoverProgressDto crossoverProgressDto = new LowestCommonGroupCrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getFirstChromosomeEndGeneIndex());

		crossoverProgressDto.advanceFirstChromosomeEndGeneIndexBy(AMOUNT_TO_ADVANCE);

		assertEquals(AMOUNT_TO_ADVANCE, crossoverProgressDto.getFirstChromosomeEndGeneIndex());
	}

	@Test
	public void testAdvanceParentEndGeneIndexBy() {
		LowestCommonGroupCrossoverProgressDto crossoverProgressDto = new LowestCommonGroupCrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getSecondChromosomeEndGeneIndex());

		crossoverProgressDto.advanceSecondChromosomeEndGeneIndexBy(AMOUNT_TO_ADVANCE);

		assertEquals(AMOUNT_TO_ADVANCE, crossoverProgressDto.getSecondChromosomeEndGeneIndex());
	}

	@Test
	public void testSetFirstChromosomeBeginGeneIndex() {
		LowestCommonGroupCrossoverProgressDto crossoverProgressDto = new LowestCommonGroupCrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getFirstChromosomeBeginGeneIndex());

		crossoverProgressDto.setFirstChromosomeBeginGeneIndex(AMOUNT_TO_ADVANCE);

		assertEquals(AMOUNT_TO_ADVANCE, crossoverProgressDto.getFirstChromosomeBeginGeneIndex());
	}

	@Test
	public void testSetSecondChromosomeBeginGeneIndex() {
		LowestCommonGroupCrossoverProgressDto crossoverProgressDto = new LowestCommonGroupCrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getSecondChromosomeBeginGeneIndex());

		crossoverProgressDto.setSecondChromosomeBeginGeneIndex(AMOUNT_TO_ADVANCE);

		assertEquals(AMOUNT_TO_ADVANCE, crossoverProgressDto.getSecondChromosomeBeginGeneIndex());
	}

	@Test
	public void testSetFirstChromosomeSequencePosition() {
		LowestCommonGroupCrossoverProgressDto crossoverProgressDto = new LowestCommonGroupCrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getFirstChromosomeSequencePosition());

		crossoverProgressDto.setFirstChromosomeSequencePosition(AMOUNT_TO_ADVANCE);

		assertEquals(AMOUNT_TO_ADVANCE, crossoverProgressDto.getFirstChromosomeSequencePosition());
	}

	@Test
	public void testSetSecondChromosomeSequencePosition() {
		LowestCommonGroupCrossoverProgressDto crossoverProgressDto = new LowestCommonGroupCrossoverProgressDto();

		assertEquals(0, crossoverProgressDto.getSecondChromosomeSequencePosition());

		crossoverProgressDto.setSecondChromosomeSequencePosition(AMOUNT_TO_ADVANCE);

		assertEquals(AMOUNT_TO_ADVANCE, crossoverProgressDto.getSecondChromosomeSequencePosition());
	}
}
