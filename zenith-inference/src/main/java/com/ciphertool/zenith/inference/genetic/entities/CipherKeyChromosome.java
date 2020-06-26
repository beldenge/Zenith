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

package com.ciphertool.zenith.inference.genetic.entities;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.population.AbstractPopulation;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import com.ciphertool.zenith.inference.genetic.util.ChromosomeToCipherSolutionMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CipherKeyChromosome implements Chromosome<String> {
    private Cipher cipher;

    private boolean evaluationNeeded = true;

    private Double fitness = Double.MIN_VALUE;

    private Map<String, Gene> genes;

    private Population population;

    public CipherKeyChromosome(Cipher cipher, int numGenes) {
        if (cipher == null) {
            throw new IllegalArgumentException("Cannot construct CipherKeyChromosome with null cipher.");
        }

        this.cipher = cipher;
        this.genes = new HashMap<>(numGenes);
    }

    public Cipher getCipher() {
        return this.cipher;
    }

    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    @Override
    public boolean isEvaluationNeeded() {
        return evaluationNeeded;
    }

    @Override
    public void setEvaluationNeeded(boolean evaluationNeeded) {
        this.evaluationNeeded = evaluationNeeded;
    }

    @Override
    public Double getFitness() {
        return fitness;
    }

    @Override
    public void setFitness(Double fitness) {
        this.fitness = fitness;
        this.evaluationNeeded = false;
    }

    @Override
    public Map<String, Gene> getGenes() {
        return Collections.unmodifiableMap(genes);
    }

    @Override
    public void putGene(String key, Gene gene) {
        if (null == gene) {
            throw new IllegalArgumentException("Attempted to insert a null Gene to CipherKeyChromosome.  Returning. " + this);
        }

        if (this.genes.get(key) != null) {
            throw new IllegalArgumentException("Attempted to insert a Gene to CipherKeyChromosome with key " + key
                    + ", but the key already exists.  If this was intentional, please use replaceGene() instead.  Returning. " + this);
        }

        gene.setChromosome(this);

        this.genes.put(key, gene);
        // TODO: it may be worth testing to see if there's already a Gene mapped to this key and if the value is the same, then don't evaluate
        this.evaluationNeeded = true;
    }

    @Override
    public Gene removeGene(String key) {
        if (null == this.genes || null == this.genes.get(key)) {
            throw new IllegalArgumentException("Attempted to remove a Gene from CipherKeyChromosome with key " + key + ", but this key does not exist.  Returning null.");
        }

        this.evaluationNeeded = true;
        return this.genes.remove(key);
    }

    /*
     * This does the same thing as putGene(), and exists solely for semantic consistency.
     */
    @Override
    public void replaceGene(String key, Gene newGene) {
        if (null == newGene) {
            throw new IllegalArgumentException("Attempted to replace a Gene from CipherKeyChromosome, but the supplied Gene was null.  Cannot continue. " + this);
        }

        if (null == this.genes || null == this.genes.get(key)) {
            throw new IllegalArgumentException("Attempted to replace a Gene from CipherKeyChromosome with key " + key + ", but this key does not exist.  Cannot continue.");
        }

        newGene.setChromosome(this);

        if (!this.evaluationNeeded) {
            this.evaluationNeeded = !((CipherKeyGene) this.genes.get(key)).getValue().equals(((CipherKeyGene) newGene).getValue());
        }

        this.genes.put(key, newGene);
    }

    @Override
    public Integer actualSize() {
        return this.genes.size();
    }

    @Override
    public Integer targetSize() {
        return (int) cipher.getCiphertextCharacters().stream()
                .map(Ciphertext::getValue)
                .distinct()
                .count();
    }

    @Override
    public Chromosome clone() {
        CipherKeyChromosome copyChromosome = new CipherKeyChromosome(this.cipher, this.genes.size());

        for (Map.Entry<String, Gene> entry : this.genes.entrySet()) {
            copyChromosome.putGene(entry.getKey(), entry.getValue().clone());
        }

        // We need to set these values last to maintain whether evaluation is needed on the clone
        copyChromosome.setFitness(this.fitness);
        copyChromosome.setEvaluationNeeded(this.evaluationNeeded);

        return copyChromosome;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cipher == null) ? 0 : cipher.hashCode());
        result = prime * result + ((genes == null) ? 0 : genes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof CipherKeyChromosome)) {
            return false;
        }

        CipherKeyChromosome other = (CipherKeyChromosome) obj;
        if (cipher == null) {
            if (other.cipher != null) {
                return false;
            }
        }

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
    public Population getPopulation() {
        return population;
    }

    @Override
    public void setPopulation(Population population) {
        this.population = population;
    }

    @Override
    public Chromosome<String> getValue() {
        return this;
    }

    @Override
    public Double getProbability() {
        return AbstractPopulation.convertFromLogProbability(this.fitness) / this.population.getTotalProbability();
    }

    @Override
    public int compareTo(Chromosome other) {
        return this.fitness.compareTo(other.getFitness());
    }

    @Override
    public boolean hasKnownSolution() {
        return cipher.hasKnownSolution();
    }

    @Override
    public Double knownSolutionProximity() {
        return Double.valueOf(ChromosomeToCipherSolutionMapper.map(this).evaluateKnownSolution());
    }
}
