/*
 * Copyright 2017-2026 George Belden
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
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.population.Population;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FitnessSpeciationOperator implements SpeciationOperator {
    @Override
    public List<Population> diverge(GeneticAlgorithmStrategy strategy, Population population) {
        int speciationFactor = strategy.getSpeciationFactor();

        if (speciationFactor <= 0) {
            throw new IllegalArgumentException("Speciation factor must be positive, but was: " + speciationFactor);
        }

        if (population.size() == 0) {
            throw new IllegalArgumentException("Cannot speciate an empty population");
        }

        if (speciationFactor > population.size()) {
            throw new IllegalArgumentException("Speciation factor (" + speciationFactor +
                    ") cannot be greater than population size (" + population.size() + ")");
        }

        List<Population> populations = new ArrayList<>(speciationFactor);

        population.sortIndividuals();

        List<Genome> sortedIndividuals = population.getIndividuals();
        int sliceSize = sortedIndividuals.size() / speciationFactor;

        for (int i = 0; i < speciationFactor; i++) {
            Population newPopulation = population.getInstance();
            populations.add(newPopulation);

            int startIndex = i * sliceSize;
            int endIndex = (i + 1) * sliceSize;

            if (i + 1 == speciationFactor) {
                // Handle the case where the population is not evenly divisible by the speciation factor,
                // in which case we just add the remainder to the last "new" population
                endIndex = sortedIndividuals.size();
            }

            sortedIndividuals.subList(startIndex, endIndex).forEach(newPopulation::addIndividual);
        }

        return populations;
    }
}
