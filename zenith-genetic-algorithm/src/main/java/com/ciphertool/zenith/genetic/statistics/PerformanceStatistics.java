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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
public class PerformanceStatistics {
    @Getter
    @Setter
    private long totalMillis;

    @Getter
    @Setter
    private long selectionMillis;

    @Getter
    @Setter
    private long crossoverMillis;

    @Getter
    @Setter
    private long mutationMillis;

    @Getter
    @Setter
    private long evaluationMillis;

    @Getter
    @Setter
    private long majorEvaluationMillis;

    @Getter
    @Setter
    private long entropyMillis;
}
