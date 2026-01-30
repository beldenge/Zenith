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

package com.ciphertool.zenith.genetic.statistics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PerformanceStatisticsTest {
    @Test
    public void given_defaultInstance_when_settingFields_then_returnsExpected() {
        PerformanceStatistics stats = new PerformanceStatistics();

        stats.setTotalMillis(10L);
        stats.setSelectionMillis(2L);
        stats.setCrossoverMillis(3L);
        stats.setMutationMillis(4L);
        stats.setEvaluationMillis(5L);
        stats.setEntropyMillis(6L);

        assertEquals(10L, stats.getTotalMillis());
        assertEquals(2L, stats.getSelectionMillis());
        assertEquals(3L, stats.getCrossoverMillis());
        assertEquals(4L, stats.getMutationMillis());
        assertEquals(5L, stats.getEvaluationMillis());
        assertEquals(6L, stats.getEntropyMillis());
        assertNotNull(stats.toString());
    }
}