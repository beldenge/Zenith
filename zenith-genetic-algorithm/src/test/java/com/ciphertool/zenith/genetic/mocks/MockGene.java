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

package com.ciphertool.zenith.genetic.mocks;

import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;

public class MockGene implements Gene {

    private Chromosome chromosome;

    @Override
    public void setChromosome(Chromosome chromosome) {
        this.chromosome = chromosome;
    }

    @Override
    public Chromosome getChromosome() {
        return this.chromosome;
    }

    @Override
    public MockGene clone() {
        MockGene clone;

        try {
            clone = (MockGene) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }

        return clone;
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

        return true;
    }
}
