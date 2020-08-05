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

package com.ciphertool.zenith.genetic.operators.speciation;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.population.LatticePopulation;
import com.ciphertool.zenith.genetic.population.Population;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProximitySpeciationOperator implements SpeciationOperator {
    @Override
    public List<Population> diverge(GeneticAlgorithmStrategy strategy, Population population) {
        if (!(population instanceof LatticePopulation)) {
            throw new IllegalStateException(getClass().getSimpleName() + " is only compatible with " + LatticePopulation.class.getSimpleName());
        }

        List<Population> populations = new ArrayList<>(strategy.getSpeciationFactor());

        int sliceSize = population.size() / strategy.getSpeciationFactor();

        // Always split on the X-axis
        for (int i = 0; i < strategy.getSpeciationFactor(); i ++) {
            Population newPopulation = population.getInstance();
            populations.add(newPopulation);

            int endIndex = (i + 1) * sliceSize;

            if (i + 1 == strategy.getSpeciationFactor()) {
                // Handle the case where the population is not evenly divisible by the speciation factor,
                // in which case we just add the remainder to the last "new" population
                endIndex = population.size();
            }

            ((LatticePopulation) population).getIndividualsUnsorted().subList(i * sliceSize, endIndex).stream().forEach(newPopulation::addIndividual);
        }

        return populations;
    }
}
