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

package com.ciphertool.zenith.math.selection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RouletteSampler<T extends Probability> {
    private Logger log = LoggerFactory.getLogger(getClass());

    private BinaryRouletteTree rouletteWheel;

    public synchronized double reIndex(List<T> probabilities) {
        // TODO: check if list is sorted first, and throw an Exception if it is not

        if (probabilities == null || probabilities.isEmpty()) {
            log.error("Attempted to index a null or empty probability distribution.  Unable to continue.");

            return -1;
        }

        this.rouletteWheel = new BinaryRouletteTree();

        List<BinaryRouletteNode> nodes = new ArrayList<>();

        double totalProbability = 0d;

        for (int i = 0; i < probabilities.size(); i++) {
            if (probabilities.get(i) == null || probabilities.get(i).getProbability() == 0d) {
                continue;
            }

            if (probabilities.get(i).getProbability() == null) {
                log.warn("Attempted to index the roulette wheel but an individual was found with a null fitness value: {}", probabilities.get(i));

                continue;
            }

            totalProbability += probabilities.get(i).getProbability();

            nodes.add(new BinaryRouletteNode(i, totalProbability));
        }

        if (totalProbability > 0) {
            addToTreeBalanced(nodes);
        }

        if (Math.abs(1d - totalProbability) > 0.0001d) {
            log.error("Attempted to index a probability distribution that does not sum to 1.  The sum is {}.  Unable to continue.", totalProbability);

            return -1;
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

    public int getNextIndex() {
        BinaryRouletteNode winner = this.rouletteWheel.find(ThreadLocalRandom.current().nextDouble());

        return winner.getIndex();
    }
}
