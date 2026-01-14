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

package com.ciphertool.zenith.genetic.entities;

import com.ciphertool.zenith.genetic.fitness.Fitness;
import com.ciphertool.zenith.genetic.population.AbstractPopulation;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.math.selection.Probability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Genome implements Comparable<Genome>, Probability {
    private boolean evaluationNeeded;
    private Fitness[] fitnesses;
    private List<Chromosome> chromosomes = new ArrayList<>();
    private Population population;
    private double crowdingValue;

    public Genome(boolean evaluationNeeded, Fitness[] fitnesses, Population population) {
        this.evaluationNeeded = evaluationNeeded;
        this.population = population;

        // We need to clone the fitnesses since this is usually called after cloning a Chromosome
        if (fitnesses != null) {
            Fitness[] newFitnesses = new Fitness[fitnesses.length];
            for (int i = 0; i < fitnesses.length; i++) {
                newFitnesses[i] = fitnesses[i].clone();
            }
            this.fitnesses = newFitnesses;
        }
    }

    public List<Chromosome> getChromosomes() {
        return Collections.unmodifiableList(chromosomes);
    }

    public void addChromosome(Chromosome chromosome) {
        chromosomes.add(chromosome);
    }

    public Fitness[] getFitnesses() {
        return fitnesses;
    }

    public void setFitnesses(Fitness[] fitnesses) {
        this.fitnesses = fitnesses;
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

    public double getCrowdingValue() {
        return crowdingValue;
    }

    public void setCrowdingValue(double crowdingValue) {
        this.crowdingValue = crowdingValue;
    }

    @Override
    public Genome getValue() {
        return this;
    }

    @Override
    public Double getProbability() {
        if (fitnesses.length > 1) {
            throw new IllegalStateException("Probability calculation is only supported for single-objective fitness functions.");
        }

        return AbstractPopulation.convertFromLogProbability(Double.valueOf(this.fitnesses[0].getValue())) / this.population.getTotalProbability();
    }

    @Override
    public int compareTo(Genome other) {
        if (fitnesses.length == 1) {
            return fitnesses[0].compareTo(other.fitnesses[0]);
        }

        int dominating = 0;
        int equivalent = 0;

        // Calculate domination per the pareto front
        for (int i = 0; i < fitnesses.length; i ++) {
           if (fitnesses[i].compareTo(other.fitnesses[i]) > 0) {
               dominating ++;
           } else if (fitnesses[i].compareTo(other.fitnesses[i]) == 0) {
               equivalent ++;
           }
        }

        if (dominating > 0 && (dominating + equivalent) == fitnesses.length) {
            return 1;
        } else if (dominating > 0) {
            return 0;
        }

        return -1;
    }
}
