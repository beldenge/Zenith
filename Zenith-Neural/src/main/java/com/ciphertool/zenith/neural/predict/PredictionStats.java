/**
 * Copyright 2017 George Belden
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

package com.ciphertool.zenith.neural.predict;

public class PredictionStats {
    private int correctCount;
    private int bestProbabilityCount;
    private int totalPredictions;

    public PredictionStats(int correctCount, int bestProbabilityCount, int totalPredictions) {
        this.correctCount = correctCount;
        this.bestProbabilityCount = bestProbabilityCount;
        this.totalPredictions = totalPredictions;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void incrementCorrectCount() {
        this.correctCount ++;
    }

    public int getBestProbabilityCount() {
        return bestProbabilityCount;
    }

    public void incrementBestProbabilityCount() {
        this.bestProbabilityCount ++;
    }

    public int getTotalPredictions() {
        return totalPredictions;
    }

    public void incrementTotalPredictions() {
        this.totalPredictions ++;
    }
}
