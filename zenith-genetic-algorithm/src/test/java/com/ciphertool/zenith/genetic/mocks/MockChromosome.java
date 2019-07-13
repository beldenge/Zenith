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

package com.ciphertool.zenith.genetic.mocks;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.population.Population;

import java.util.HashMap;
import java.util.Map;

public class MockChromosome implements Chromosome<Object> {
    private boolean needsEvaluation;
    private Double fitness = 0d;
    private Map<Object, Gene> genes = new HashMap<>();
    private Integer targetSize = 0;
    private Population population;

    @Override
    public Map<Object, Gene> getGenes() {
        return this.genes;
    }

    @Override
    public Double getFitness() {
        return this.fitness;
    }

    @Override
    public void setFitness(Double fitness) {
        this.fitness = fitness;
        this.needsEvaluation = false;
    }

    @Override
    public Integer actualSize() {
        return this.genes.size();
    }

    @Override
    public Integer targetSize() {
        return targetSize;
    }

    /**
     * Convenience method for unit tests. This will not normally be implemented this way.
     *
     * @param targetSize the targetSize to set
     */
    public void setTargetSize(int targetSize) {
        this.targetSize = targetSize;
    }

    @Override
    public boolean isEvaluationNeeded() {
        return this.needsEvaluation;
    }

    @Override
    public void setEvaluationNeeded(boolean needsEvaluation) {
        this.needsEvaluation = needsEvaluation;
    }

    @Override
    public void putGene(Object key, Gene gene) {
        gene.setChromosome(this);

        this.genes.put(key, gene);
    }

    @Override
    public Gene removeGene(Object key) {
        return this.genes.remove(key);
    }

    @Override
    public void replaceGene(Object key, Gene newGene) {
        newGene.setChromosome(this);

        this.removeGene(key);

        this.putGene(key, newGene);
    }

    @Override
    public MockChromosome clone() {
        MockChromosome copyChromosome = new MockChromosome();

        copyChromosome.genes = new HashMap<>();
        copyChromosome.setEvaluationNeeded(this.needsEvaluation);

        /*
         * Since we are copying over the fitness value, we don't need to reset the evaluationNeeded flag because the
         * cloned default is correct.
         */
        copyChromosome.setFitness(this.fitness);

        /*
         * We don't need to clone the solutionSetId or cipherId as even though they are objects, they should remain
         * static.
         */

        Gene nextGene;
        for (Object key : this.genes.keySet()) {
            nextGene = this.genes.get(key).clone();

            copyChromosome.putGene(key, nextGene);
        }

        return copyChromosome;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MockChromosome other = (MockChromosome) obj;
        if (genes == null) {
            if (other.genes != null) {
                return false;
            }
        } else if (!genes.equals(other.genes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MockChromosome [needsEvaluation=" + needsEvaluation + ", fitness=" + fitness + ", genes=" + genes
                + "]";
    }

    /**
     * @return the population
     */
    @Override
    public Population getPopulation() {
        return population;
    }

    /**
     * @param population the population to set
     */
    @Override
    public void setPopulation(Population population) {
        this.population = population;
    }

    @Override
    public Object getValue() {
        return this;
    }

    @Override
    public Double getProbability() {
        return this.fitness / this.population.getTotalFitness();
    }

    @Override
    public int compareTo(Chromosome other) {
        return fitness.compareTo(other.getFitness());
    }

    @Override
    public boolean hasKnownSolution() {
        return true;
    }

    @Override
    public Double knownSolutionProximity() {
        return 1.0d;
    }
}
