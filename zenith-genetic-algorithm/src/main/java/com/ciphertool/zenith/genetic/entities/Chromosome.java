/**
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

package com.ciphertool.zenith.genetic.entities;

import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.math.selection.Probability;

import java.util.Map;

public interface Chromosome<T> extends Cloneable, Comparable<Chromosome>, Probability {
    Double getFitness();

    /**
     * @param fitness
     */
    void setFitness(Double fitness);

    /*
     * Returns the size as the number of gene sequences
     */
    Integer actualSize();

    Integer targetSize();

    Chromosome clone();

    /*
     * Whether this Chromosome has changed since it was last evaluated.
     */
    boolean isEvaluationNeeded();

    /**
     * @param evaluationNeeded the evaluationNeeded value to set
     */
    void setEvaluationNeeded(boolean evaluationNeeded);

    /**
     * @return this Chromosome's Population
     */
    Population getPopulation();

    /**
     * @param population the population to set
     */
    void setPopulation(Population population);

    /**
     * @return an unmodifiable Map of this Chromosome's Genes
     */
    Map<T, Gene> getGenes();

    /**
     * Adds a Gene at the specified key.
     */
    void putGene(T key, Gene gene);

    /**
     * Removes a Gene at the specified key.
     */
    Gene removeGene(T key);

    /**
     * Replaces a Gene at the specified key.
     */
    void replaceGene(T key, Gene newGene);

    boolean hasKnownSolution();

    Double knownSolutionProximity();
}
