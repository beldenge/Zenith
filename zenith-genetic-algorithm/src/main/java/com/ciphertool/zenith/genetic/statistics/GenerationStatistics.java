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

import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class GenerationStatistics {
    @Getter
    @Setter
    private ExecutionStatistics executionStatistics;

    @Getter
    @Setter
    private int generation;

    @Getter
    @Setter
    private Double bestFitness;

    @Getter
    @Setter
    private Double averageFitness;

    @Getter
    @Setter
    private BigDecimal entropy;

    @Getter
    @Setter
    private Double knownSolutionProximity;

    @Getter
    @Setter
    private int numberOfCrossovers;

    @Getter
    @Setter
    private int numberOfMutations;

    @Getter
    @Setter
    private int numberOfEvaluations;

    @Getter
    @Setter
    private int numberOfMajorEvaluations;

    @Getter
    @Setter
    private int numberRandomlyGenerated;

    @Getter
    @Setter
    private int numberSelectedOut;

    @Getter
    @Setter
    private PerformanceStatistics performanceStatistics = new PerformanceStatistics();

    /**
     * @param executionStatistics
     *            the executionStatistics to set
     * @param generation
     *            the generation to set
     */
    public GenerationStatistics(ExecutionStatistics executionStatistics, int generation) {
        this.executionStatistics = executionStatistics;
        this.generation = generation;
    }
}
