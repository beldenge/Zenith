/*
 * Copyright 2017-2020 George Belden
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
package com.ciphertool.zenith.genetic.algorithms.selection;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.math.selection.RouletteSampler;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RouletteSelector implements Selector {
    private RouletteSampler<Chromosome> rouletteSampler = new RouletteSampler<>();

    @Override
    public synchronized void reIndex(List<Chromosome> individuals) {
        Collections.sort(individuals);

        rouletteSampler.reIndex(individuals);
    }

    @Override
    public int getNextIndex(List<Chromosome> individuals, GeneticAlgorithmStrategy strategy) {
        return rouletteSampler.getNextIndex();
    }

    @Override
    public int getNextIndexThreadSafe(List<Chromosome> individuals, GeneticAlgorithmStrategy strategy) {
        RouletteSampler<Chromosome> localRouletteSampler = new RouletteSampler<>();

        Collections.sort(individuals);

        localRouletteSampler.reIndex(individuals);

        return localRouletteSampler.getNextIndex();
    }
}
