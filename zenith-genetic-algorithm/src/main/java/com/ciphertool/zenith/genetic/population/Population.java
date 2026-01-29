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

package com.ciphertool.zenith.genetic.population;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import com.ciphertool.zenith.genetic.entities.Genome;
import com.ciphertool.zenith.genetic.entities.Parents;
import com.ciphertool.zenith.genetic.statistics.GenerationStatistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Population {
    Population getInstance();

    void init(GeneticAlgorithmStrategy strategy);

    void setStrategy(GeneticAlgorithmStrategy strategy);

    Genome evaluateFitness(GenerationStatistics generationStatistics);

    List<Genome> breed(int numberToBreed);

    List<Parents> select();

    void clearIndividuals();

    int size();

    List<Genome> getIndividuals();

    boolean addIndividual(Genome individual);

    void sortIndividuals();

    @SuppressWarnings({"unchecked"})
    default BigDecimal calculateEntropy() {
        List<BigDecimal> entropies = new ArrayList<>(getIndividuals().get(0).getChromosomes().size());

        for (int i = 0; i < getIndividuals().get(0).getChromosomes().size(); i ++) {
            Map<Object, Map<Object, Integer>> symbolCounts = new HashMap<>();

            Object geneKey;
            Object geneValue;
            Integer currentCount;
            Map<Object, Integer> symbolCountMap;

            // Count occurrences of each Gene value
            for (Genome genome : getIndividuals()) {
                Chromosome chromosome = genome.getChromosomes().get(i);

                for (Map.Entry<Object, Gene> entry : ((Chromosome<Object>) chromosome).getGenes().entrySet()) {
                    geneKey = entry.getKey();

                    if (!symbolCounts.containsKey(geneKey)) {
                        symbolCounts.put(geneKey, new HashMap<>());
                    }

                    symbolCountMap = symbolCounts.get(geneKey);

                    geneValue = entry.getValue();
                    currentCount = symbolCountMap.get(geneValue);

                    symbolCountMap.put(geneValue, (currentCount != null) ? (currentCount + 1) : 1);
                }
            }

            Map<Object, Map<Object, Double>> symbolProbabilities = new HashMap<>();

            double populationSize = this.size();

            Map<Object, Double> probabilityMap;

            // Calculate probability of each Gene value
            for (Map.Entry<Object, Map<Object, Integer>> entry : symbolCounts.entrySet()) {
                probabilityMap = new HashMap<>();

                symbolProbabilities.put(entry.getKey(), probabilityMap);

                for (Map.Entry<Object, Integer> entryInner : entry.getValue().entrySet()) {
                    probabilityMap.put(entryInner.getKey(), ((double) entryInner.getValue() / populationSize));
                }
            }

            int base = symbolCounts.size();

            double totalEntropy = 0.0;

            // Calculate the Shannon entropy of each Gene independently, and add it to the total entropy value
            for (Map.Entry<Object, Map<Object, Double>> entry : symbolProbabilities.entrySet()) {
                for (Map.Entry<Object, Double> entryInner : entry.getValue().entrySet()) {
                    totalEntropy += (entryInner.getValue() * logBase(entryInner.getValue(), base));
                }
            }

            totalEntropy *= -1.0;

            // Use the average entropy among the symbols
            entropies.add(BigDecimal.valueOf(totalEntropy / (double) symbolProbabilities.size()));
        }

        // Return the average entropy among all chromosomes
        return BigDecimal.valueOf(entropies.stream().mapToDouble(BigDecimal::doubleValue).sum() / entropies.size());
    }

    // Use the change of base formula to calculate the logarithm with an arbitrary base
    static double logBase(double num, int base) {
        return (Math.log(num) / Math.log(base));
    }

    Double getTotalFitness();

    Double getTotalProbability();
}
