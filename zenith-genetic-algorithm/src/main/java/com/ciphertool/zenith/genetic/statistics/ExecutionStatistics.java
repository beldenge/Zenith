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

package com.ciphertool.zenith.genetic.statistics;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ExecutionStatistics {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer populationSize;
    private Double mutationRate;
    private String crossoverAlgorithm;
    private String fitnessEvaluator;
    private String mutationAlgorithm;

    private List<GenerationStatistics> generationStatisticsList = new ArrayList<>();

    public ExecutionStatistics(LocalDateTime startDateTime, GeneticAlgorithmStrategy strategy) {
        this.startDateTime = startDateTime;

        if (strategy == null) {
            return;
        }

        this.populationSize = strategy.getPopulationSize();
        this.mutationRate = strategy.getMutationRate();
        this.crossoverAlgorithm = (strategy.getCrossoverAlgorithm() != null) ? strategy.getCrossoverAlgorithm().getClass().getSimpleName() : null;
        this.fitnessEvaluator = (strategy.getFitnessEvaluator() != null) ? strategy.getFitnessEvaluator().getClass().getSimpleName() : null;
        this.mutationAlgorithm = (strategy.getMutationAlgorithm() != null) ? strategy.getMutationAlgorithm().getClass().getSimpleName() : null;
    }

    /**
     * @return an unmodifiable List of GenerationStatistics
     */
    public List<GenerationStatistics> getGenerationStatisticsList() {
        return Collections.unmodifiableList(this.generationStatisticsList);
    }

    /**
     * @param generationStatistics the GenerationStatistics to add
     */
    public void addGenerationStatistics(GenerationStatistics generationStatistics) {
        this.generationStatisticsList.add(generationStatistics);
    }

    /**
     * @param generationStatistics the GenerationStatistics to remove
     */
    public void removeGenerationStatistics(GenerationStatistics generationStatistics) {
        this.generationStatisticsList.remove(generationStatistics);
    }
}