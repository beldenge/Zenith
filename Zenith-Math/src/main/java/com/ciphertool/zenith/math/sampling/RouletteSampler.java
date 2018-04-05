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

package com.ciphertool.zenith.math.sampling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciphertool.zenith.math.probability.Probability;

public class RouletteSampler<T extends Probability<?>> {
	private Logger				log	= LoggerFactory.getLogger(getClass());

	private BinaryRouletteTree	rouletteWheel;

	public synchronized Double reIndex(List<T> probabilities) {
		this.rouletteWheel = new BinaryRouletteTree();

		List<BinaryRouletteNode> nodes = new ArrayList<BinaryRouletteNode>();

		Double totalProbability = 0.0;

		for (int i = 0; i < probabilities.size(); i++) {
			if (probabilities.get(i) == null || probabilities.get(i).getProbability() == 0.0) {
				continue;
			}

			totalProbability = totalProbability + probabilities.get(i).getProbability();

			nodes.add(new BinaryRouletteNode(i, totalProbability));
		}

		if (totalProbability > 0.0) {
			addToTreeBalanced(nodes);
		}

		return totalProbability;
	}

	protected void addToTreeBalanced(List<BinaryRouletteNode> nodes) {
		int half = nodes.size() / 2;

		this.rouletteWheel.insert(nodes.get(half));

		if (nodes.size() == 1) {
			return;
		}

		addToTreeBalanced(nodes.subList(0, half));

		if (nodes.size() == 2) {
			return;
		}

		addToTreeBalanced(nodes.subList(half + 1, nodes.size()));
	}

	public int getNextIndex(List<T> probabilities, Double totalProbability) {
		if (probabilities == null || probabilities.isEmpty()) {
			log.error("Attempted to select a probability from a null or empty distribution.  Unable to continue.");

			return -1;
		}

		if (Math.abs(1.0 - totalProbability) > 0.0001) {
			log.error("Attempted to select from a probability distribution that does not sum to 1.  The sum is "
					+ totalProbability + ".  Unable to continue.");

			return -1;
		}

		Double randomIndex = ThreadLocalRandom.current().nextDouble() * totalProbability;

		BinaryRouletteNode winner = this.rouletteWheel.find(randomIndex);

		return winner.getIndex();
	}
}
