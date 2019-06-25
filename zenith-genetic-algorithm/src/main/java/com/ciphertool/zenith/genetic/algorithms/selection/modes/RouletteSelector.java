/**
 * Copyright 2017-2019 George Belden
 * <p>
 * This file is part of Zenith.
 * <p>
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */
package com.ciphertool.zenith.genetic.algorithms.selection.modes;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.math.selection.BinaryRouletteNode;
import com.ciphertool.zenith.math.selection.BinaryRouletteTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RouletteSelector implements Selector {
    private Logger log = LoggerFactory.getLogger(getClass());

    private BinaryRouletteTree rouletteWheel;

    @Override
    public synchronized void reIndex(List<Chromosome> individuals) {
        this.rouletteWheel = new BinaryRouletteTree();

        List<BinaryRouletteNode> nodes = new ArrayList<>();

        Double totalFitness = 0d;

        for (int i = 0; i < individuals.size(); i++) {
            if (individuals.get(i) == null || individuals.get(i).getFitness() == 0d) {
                continue;
            }

            if (individuals.get(i).getFitness() == null) {
                log.warn("Attempted to spin roulette wheel but an individual was found with a null fitness value.  Please make a call to evaluateFitness() before attempting to spin the roulette wheel. {}", individuals.get(i));

                continue;
            }

            totalFitness += individuals.get(i).getFitness();

            nodes.add(new BinaryRouletteNode(i, totalFitness));
        }

        if (totalFitness > 0d) {
            addToTreeBalanced(nodes);
        }
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

    @Override
    public int getNextIndex(List<Chromosome> individuals, Double totalFitness) {
        if (individuals == null || individuals.isEmpty()) {
            log.warn("Attempted to select an individual from a null or empty population.  Unable to continue.");

            return -1;
        }

        if (totalFitness == null) {
            log.warn("This Selector implementation requires a non-null total fitness.  Unable to continue.");

            return -1;
        }

        if (totalFitness == 0d) {
            // If all the individuals have zero fitness, then pick one at random
            return ThreadLocalRandom.current().nextInt(0, individuals.size());
        }

        Double randomIndex = ThreadLocalRandom.current().nextDouble() * totalFitness;

        BinaryRouletteNode winner = this.rouletteWheel.find(randomIndex);

        return winner.getIndex();
    }
}
