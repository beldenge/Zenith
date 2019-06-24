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

package com.ciphertool.zenith.genetic.algorithms.mutation.impl;

 import com.ciphertool.zenith.genetic.algorithms.mutation.MutationHelper;
 import org.junit.Test;

 import java.util.HashMap;
 import java.util.Map;

 import static org.junit.Assert.assertTrue;

public class MutationHelperTest {
	@Test
	public void testGetNumMutations() {
		int maxMutations = 50;
		double mutationCountFactor = 0.1;
		int size = 50;
		int attempts = 500;

		MutationHelper mutationHelper = new MutationHelper();
		mutationHelper.setMaxMutations(maxMutations);
		mutationHelper.setMutationCountFactor(mutationCountFactor);

		Map<Integer, Integer> ints = new HashMap<>();
		for (int i = 0; i < attempts; i++) {
			int numMutations = mutationHelper.getNumMutations(size);

			if (!ints.containsKey(numMutations)) {
				ints.put(numMutations, 0);
			}

			ints.put(numMutations, ints.get(numMutations) + 1);

			assertTrue(numMutations >= 1 && numMutations <= maxMutations);
		}
	}
}
