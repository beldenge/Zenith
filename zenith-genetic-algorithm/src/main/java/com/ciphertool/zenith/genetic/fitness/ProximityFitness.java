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

package com.ciphertool.zenith.genetic.fitness;

public class ProximityFitness  extends AbstractFitness {
    private double target;

    public ProximityFitness(double target, double value) {
        this.target = target;
        this.value = value;
    }

    @Override
    public int compareTo(Fitness o) {
        double thisDistance = Math.abs(target - value);
        double otherDistance = Math.abs(((ProximityFitness) o).target - ((ProximityFitness) o).value);

        if (thisDistance < otherDistance) {
            return 1;
        } else if (thisDistance == otherDistance) {
            return 0;
        }

        return -1;
    }

    @Override
    public ProximityFitness clone() {
        return new ProximityFitness(target, value);
    }

    @Override
    public String toString() {
        return "ProximityFitness{" +
                "value=" + value +
                '}';
    }
}
