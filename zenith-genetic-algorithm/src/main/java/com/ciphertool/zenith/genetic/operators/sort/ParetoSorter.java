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

package com.ciphertool.zenith.genetic.operators.sort;

import com.ciphertool.zenith.genetic.entities.Genome;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParetoSorter {
    public static void sort(List<Genome> individuals) {
        List<Genome> sorted = new ArrayList<>(individuals.size());

        if (individuals.get(0).getFitnesses().length == 1) {
            sorted = individuals.stream()
                    .sorted()
                    .collect(Collectors.toList());
        } else {
            List<List<Genome>> paretoBuckets = new ArrayList<>();

            for (Genome individual : individuals) {
                List<Genome> bucket = null;

                if (paretoBuckets.isEmpty()) {
                    bucket = new ArrayList<>();
                    paretoBuckets.add(bucket);
                } else {
                    for (int i = 0; i < paretoBuckets.size(); i ++) {
                        int comparison = paretoCompareTo(individual, paretoBuckets.get(i));

                        if (comparison < 0) {
                            bucket = new ArrayList<>();
                            paretoBuckets.add(i, bucket);
                            break;
                        } else if (comparison == 0) {
                            bucket = paretoBuckets.get(i);
                            break;
                        }
                    }
                }

                if (bucket == null) {
                    bucket = new ArrayList<>();
                    paretoBuckets.add(bucket);
                }

                bucket.add(individual);
            }

            for (List<Genome> bucket : paretoBuckets) {
                for (Genome genome : bucket) {
                    sorted.add(genome);
                }
            }
        }

        individuals.clear();
        sorted.forEach(individuals::add);
    }

    private static int paretoCompareTo(Genome individual, List<Genome> paretoBucket) {
        for (Genome other : paretoBucket) {
            int comparison = individual.compareTo(other);

            if (comparison < 0) {
                return -1;
            } else if (comparison > 0) {
                return 1;
            }
        }

        return 0;
    }
}
