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

package com.ciphertool.zenith.inference.genetic.entities;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.population.Population;
import com.ciphertool.zenith.inference.entities.Cipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CipherKeyChromosome implements Chromosome<String> {
    private static Logger log = LoggerFactory.getLogger(CipherKeyChromosome.class);

    private static final int KEY_SIZE = 54;

    protected Cipher cipher;

    protected boolean evaluationNeeded = true;

    private Double fitness = 0d;

    private Map<String, Gene> genes;

    private Population population;

    public CipherKeyChromosome() {
        genes = new HashMap<>();
    }

    public CipherKeyChromosome(Cipher cipher, int numGenes) {
        if (cipher == null) {
            throw new IllegalArgumentException("Cannot construct CipherKeyChromosome with null cipher.");
        }

        this.cipher = cipher;

        genes = new HashMap<>(numGenes);
    }

    /**
     * @return the cipher
     */
    public Cipher getCipher() {
        return this.cipher;
    }

    /**
     * @param cipher the cipher to set
     */
    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    /**
     * @return the evaluationNeeded
     */
    @Override
    public boolean isEvaluationNeeded() {
        return evaluationNeeded;
    }

    /**
     * @param evaluationNeeded the evaluationNeeded to set
     */
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
            log.warn("Attempted to insert a null Gene to CipherKeyChromosome.  Returning. " + this);

            return;
        }

        if (this.genes.get(key) != null) {
            log.warn("Attempted to insert a Gene to CipherKeyChromosome with key " + key
                    + ", but the key already exists.  If this was intentional, please use replaceGene() instead.  Returning. "
                    + this);

            return;
        }

        gene.setChromosome(this);

        this.genes.put(key, gene);
        this.evaluationNeeded = true;
    }

    @Override
    public Gene removeGene(String key) {
        if (null == this.genes || null == this.genes.get(key)) {
            log.warn("Attempted to remove a Gene from CipherKeyChromosome with key " + key
                    + ", but this key does not exist.  Returning null.");

            return null;
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
            log.warn("Attempted to replace a Gene from CipherKeyChromosome, but the supplied Gene was null.  Cannot continue. "
                    + this);

            return;
        }

        if (null == this.genes || null == this.genes.get(key)) {
            log.warn("Attempted to replace a Gene from CipherKeyChromosome with key " + key
                    + ", but this key does not exist.  Cannot continue.");

            return;
        }

        newGene.setChromosome(this);

        this.genes.put(key, newGene);
        this.evaluationNeeded = true;
    }

    @Override
    public Integer actualSize() {
        return this.genes.size();
    }

    @Override
    public Integer targetSize() {
        return KEY_SIZE;
    }

    @Override
    public Chromosome clone() {
        CipherKeyChromosome copyChromosome = new CipherKeyChromosome(this.cipher, this.genes.size());

        Gene nextGene;
        for (String key : this.genes.keySet()) {
            nextGene = this.genes.get(key).clone();

            copyChromosome.putGene(key, nextGene);
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

    /*
     * Prints the properties of the solution and then outputs the entire plaintext list in block format.
     *
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Solution [fitness=" + fitness + ", evaluationNeeded=" + evaluationNeeded + "]\n");

        if (this.cipher != null) {
            CipherKeyGene nextPlaintext;
            int actualSize = this.cipher.getCiphertextCharacters().size();
            for (int i = 0; i < actualSize; i++) {

                nextPlaintext = (CipherKeyGene) this.genes.get(this.cipher.getCiphertextCharacters().get(i).getValue());

                sb.append(" ");
                sb.append(nextPlaintext.getValue());
                sb.append(" ");

                /*
                 * Print a newline if we are at the end of the row. Add 1 to the index so the modulus function doesn't
                 * break.
                 */
                if (((i + 1) % this.cipher.getColumns()) == 0) {
                    sb.append("\n");
                } else {
                    sb.append(" ");
                }
            }
        }

        return sb.toString();
    }

    @Override
    public double similarityTo(Chromosome other) {
        int total = 0;

        for (Map.Entry<String, Gene> entry : this.genes.entrySet()) {
            if (((CipherKeyGene) entry.getValue()).getValue().equals(((CipherKeyGene) ((CipherKeyChromosome) other).genes.get(entry.getKey())).getValue())) {
                total++;
            }
        }

        return ((double) total) / ((double) this.genes.size());
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
    public Chromosome<String> getValue() {
        return this;
    }

    @Override
    public Double getProbability() {
        return this.fitness / this.population.getTotalFitness();
    }

    @Override
    public int compareTo(Chromosome other) {
        return getProbability().compareTo(other.getProbability());
    }
}
