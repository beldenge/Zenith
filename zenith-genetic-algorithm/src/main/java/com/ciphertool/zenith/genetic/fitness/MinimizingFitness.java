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

package com.ciphertool.zenith.genetic.fitness;

public class MinimizingFitness extends AbstractFitness {
    public MinimizingFitness(double value) {
        this.value = value;
    }

    public MinimizingFitness(double value, Double guardRail) {
        this.value = value;
        this.guardRail = guardRail;
    }

    @Override
    public int compareTo(Fitness o) {
        if (guardRail != null && value < guardRail && o.getValue() < guardRail) {
            return 0;
        }

        if (this.value < o.getValue()) {
            return 1;
        } else if (this.value == o.getValue()) {
            return 0;
        }

        return -1;
    }

    @Override
    public MinimizingFitness clone() {
        return new MinimizingFitness(value, guardRail == null ? null : guardRail.doubleValue());
    }

    @Override
    public String toString() {
        return "MinimizingFitness{" +
                "value=" + value +
                '}';
    }
}
