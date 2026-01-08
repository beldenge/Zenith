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

package com.ciphertool.zenith.model.dto;

import java.util.HashMap;
import java.util.Map;

public class ParseResults {
    private long total;
    private Map<Integer, Long> levelTotals = new HashMap<>();
    private long unique;

    /**
     * @param total
     *            the total count
     * @param unique
     *            the unique count
     */
    public ParseResults(long total, long unique) {
        this.total = total;
        this.unique = unique;
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }

    /**
     * @return the levelTotals
     */
    public Map<Integer, Long> getLevelTotals() {
        return levelTotals;
    }

    /**
     * @return the unique
     */
    public long getUnique() {
        return unique;
    }
}
