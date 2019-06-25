/**
 * Copyright 2015 George Belden
 * <p>
 * This file is part of Genie.
 * <p>
 * Genie is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * Genie is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Genie. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.genetic.statistics;

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class ExecutionStatistics {
    @Getter
    @Setter
    private Date startDateTime;

    @Getter
    @Setter
    private Date endDateTime;

    @Getter
    @Setter
    private Integer populationSize;

    @Getter
    @Setter
    private Double mutationRate;

    @Getter
    @Setter
    private String crossoverAlgorithm;

    @Getter
    @Setter
    private String fitnessEvaluator;

    @Getter
    @Setter
    private String mutationAlgorithm;

    private List<GenerationStatistics> generationStatisticsList = new ArrayList<>();

    public ExecutionStatistics(Date startDateTime, GeneticAlgorithmStrategy strategy) {
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
     * @param generationStatistics
     *            the GenerationStatistics to add
     */
    public void addGenerationStatistics(GenerationStatistics generationStatistics) {
        this.generationStatisticsList.add(generationStatistics);
    }

    /**
     * @param generationStatistics
     *            the GenerationStatistics to remove
     */
    public void removeGenerationStatistics(GenerationStatistics generationStatistics) {
        this.generationStatisticsList.remove(generationStatistics);
    }
}