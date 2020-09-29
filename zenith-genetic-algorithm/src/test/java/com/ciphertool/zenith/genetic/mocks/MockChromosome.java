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

package com.ciphertool.zenith.genetic.mocks;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.Genome;

import java.util.HashMap;
import java.util.Map;

public class MockChromosome implements Chromosome<Object> {
    private Genome genome;
    private Map<Object, Gene> genes = new HashMap<>();
    private Integer targetSize = 0;

    @Override
    public Map<Object, Gene> getGenes() {
        return this.genes;
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
    public Genome getGenome() {
        return genome;
    }

    @Override
    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    @Override
    public MockChromosome clone() {
        MockChromosome copyChromosome = new MockChromosome();

        copyChromosome.genes = new HashMap<>();

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
        return "MockChromosome [genes=" + genes + "]";
    }
}
