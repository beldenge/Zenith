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
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.Ciphertext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CipherKeyChromosome implements Chromosome<String> {
    private Genome genome;

    private Cipher cipher;

    private Map<String, Gene> genes;

    public CipherKeyChromosome(Genome genome, Cipher cipher, int numGenes) {
        if (cipher == null) {
            throw new IllegalArgumentException("Cannot construct CipherKeyChromosome with null cipher.");
        }

        this.genome = genome;
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

        if (this.genome != null) {
            this.genome.setEvaluationNeeded(true);
        }
    }

    @Override
    public Gene removeGene(String key) {
        if (null == this.genes || null == this.genes.get(key)) {
            throw new IllegalArgumentException("Attempted to remove a Gene from CipherKeyChromosome with key " + key + ", but this key does not exist.  Returning null.");
        }

        if (this.genome != null) {
            this.genome.setEvaluationNeeded(true);
        }

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

        if (this.genome != null && !this.genome.isEvaluationNeeded()) {
            this.genome.setEvaluationNeeded(!((CipherKeyGene) this.genes.get(key)).getValue().equals(((CipherKeyGene) newGene).getValue()));
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
    public Genome getGenome() {
        return genome;
    }

    @Override
    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    @Override
    public Chromosome clone() {
        CipherKeyChromosome copyChromosome = new CipherKeyChromosome(null, this.cipher, this.genes.size());

        for (Map.Entry<String, Gene> entry : this.genes.entrySet()) {
            copyChromosome.putGene(entry.getKey(), entry.getValue().clone());
        }

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
}
