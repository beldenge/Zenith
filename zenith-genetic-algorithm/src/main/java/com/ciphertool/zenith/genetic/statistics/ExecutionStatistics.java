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
    private String crossoverOperator;
    private String fitnessEvaluator;
    private String mutationOperator;

    private List<GenerationStatistics> generationStatisticsList = new ArrayList<>();

    public ExecutionStatistics(LocalDateTime startDateTime, GeneticAlgorithmStrategy strategy) {
        this.startDateTime = startDateTime;

        if (strategy == null) {
            return;
        }

        this.populationSize = strategy.getPopulationSize();
        this.mutationRate = strategy.getMutationRate();
        this.crossoverOperator = (strategy.getCrossoverOperator() != null) ? strategy.getCrossoverOperator().getClass().getSimpleName() : null;
        this.fitnessEvaluator = (strategy.getFitnessEvaluator() != null) ? strategy.getFitnessEvaluator().getClass().getSimpleName() : null;
        this.mutationOperator = (strategy.getMutationOperator() != null) ? strategy.getMutationOperator().getClass().getSimpleName() : null;
    }

    public List<GenerationStatistics> getGenerationStatisticsList() {
        return Collections.unmodifiableList(this.generationStatisticsList);
    }

    public void addGenerationStatistics(GenerationStatistics generationStatistics) {
        this.generationStatisticsList.add(generationStatistics);
    }

    public void removeGenerationStatistics(GenerationStatistics generationStatistics) {
        this.generationStatisticsList.remove(generationStatistics);
    }
}