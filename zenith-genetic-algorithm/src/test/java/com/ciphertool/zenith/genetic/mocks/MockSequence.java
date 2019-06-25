/**
 * Copyright 2017-2019 George Belden
 * <p>
 * This file is part of Zenith.
 * <p>
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.genetic.mocks;

import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.Sequence;

public class MockSequence implements Sequence {

    private Gene gene;
    private String value;
    private Integer sequenceId = 1;

    public MockSequence() {
    }

    public MockSequence(Object value) {
        this.value = (String) value;
    }

    public MockSequence(Object value, Integer sequenceId) {
        this.value = (String) value;
        this.sequenceId = sequenceId;
    }

    @Override
    public Integer getSequenceId() {
        return this.sequenceId;
    }

    @Override
    public Gene getGene() {
        return this.gene;
    }

    @Override
    public void setGene(Gene gene) {
        this.gene = gene;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public void setValue(Object obj) {
        this.value = (String) obj;
    }

    @Override
    public MockSequence clone() {
        MockSequence clone;

        try {
            clone = (MockSequence) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }

        // We intentionally do not override the cloning of the Gene
        clone.value = this.value.toString();
        clone.sequenceId = this.sequenceId.intValue();

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
        MockSequence other = (MockSequence) obj;
        if (sequenceId == null) {
            if (other.sequenceId != null) {
                return false;
            }
        } else if (!sequenceId.equals(other.sequenceId)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MockSequence [value=" + value + "]";
    }
}
