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

package com.ciphertool.zenith.genetic.entities;

import com.ciphertool.zenith.genetic.population.AbstractPopulation;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.math.selection.Probability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Genome implements Comparable<Genome>, Probability {
    private boolean evaluationNeeded = true;
    private Double fitness = Double.MIN_VALUE;
    private List<Chromosome> chromosomes = new ArrayList<>();
    private Population population;

    public Genome(boolean evaluationNeeded, Double fitness, Population population) {
        this.evaluationNeeded = evaluationNeeded;
        this.fitness = fitness;
        this.population = population;
    }

    public List<Chromosome> getChromosomes() {
        return Collections.unmodifiableList(chromosomes);
    }

    public void addChromosome(Chromosome chromosome) {
        chromosomes.add(chromosome);
    }

    public Double getFitness() {
        return fitness;
    }

    public void setFitness(Double fitness) {
        this.fitness = fitness;
        this.evaluationNeeded = false;
    }

    /*
     * Whether this Chromosome has changed since it was last evaluated.
     */
    public boolean isEvaluationNeeded() {
        return evaluationNeeded;
    }

    public void setEvaluationNeeded(boolean evaluationNeeded) {
        this.evaluationNeeded = evaluationNeeded;
    }

    public Population getPopulation() {
        return population;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }

    @Override
    public Genome getValue() {
        return this;
    }

    @Override
    public Double getProbability() {
        return AbstractPopulation.convertFromLogProbability(this.fitness) / this.population.getTotalProbability();
    }

    @Override
    public int compareTo(Genome other) {
        return this.fitness.compareTo(other.getFitness());
    }
}
