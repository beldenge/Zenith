/**
 * Copyright 2015 George Belden
 * 
 * This file is part of Genie.
 * 
 * Genie is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Genie is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Genie. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.genetic.statistics;

public class PerformanceStatistics {
	private long				totalMillis;
	private long				selectionMillis;
	private long				crossoverMillis;
	private long				mutationMillis;
	private long				evaluationMillis;
	private long				majorEvaluationMillis;
	private long				entropyMillis;

	/**
	 * Default no-args constructor
	 */
	public PerformanceStatistics() {
	}

	/**
	 * @return the totalMillis
	 */
	public long getTotalMillis() {
		return totalMillis;
	}

	/**
	 * @param totalMillis
	 *            the totalMillis to set
	 */
	public void setTotalMillis(long totalMillis) {
		this.totalMillis = totalMillis;
	}

	/**
	 * @return the selectionMillis
	 */
	public long getSelectionMillis() {
		return selectionMillis;
	}

	/**
	 * @param selectionMillis
	 *            the selectionMillis to set
	 */
	public void setSelectionMillis(long selectionMillis) {
		this.selectionMillis = selectionMillis;
	}

	/**
	 * @return the crossoverMillis
	 */
	public long getCrossoverMillis() {
		return crossoverMillis;
	}

	/**
	 * @param crossoverMillis
	 *            the crossoverMillis to set
	 */
	public void setCrossoverMillis(long crossoverMillis) {
		this.crossoverMillis = crossoverMillis;
	}

	/**
	 * @return the mutationMillis
	 */
	public long getMutationMillis() {
		return mutationMillis;
	}

	/**
	 * @param mutationMillis
	 *            the mutationMillis to set
	 */
	public void setMutationMillis(long mutationMillis) {
		this.mutationMillis = mutationMillis;
	}

	/**
	 * @return the evaluationMillis
	 */
	public long getEvaluationMillis() {
		return evaluationMillis;
	}

	/**
	 * @param evaluationMillis
	 *            the evaluationMillis to set
	 */
	public void setEvaluationMillis(long evaluationMillis) {
		this.evaluationMillis = evaluationMillis;
	}

	/**
	 * @return the majorEvaluationMillis
	 */
	public long getMajorEvaluationMillis() {
		return majorEvaluationMillis;
	}

	/**
	 * @param majorEvaluationMillis
	 *            the majorEvaluationMillis to set
	 */
	public void setMajorEvaluationMillis(long majorEvaluationMillis) {
		this.majorEvaluationMillis = majorEvaluationMillis;
	}

	/**
	 * @return the entropyMillis
	 */
	public long getEntropyMillis() {
		return entropyMillis;
	}

	/**
	 * @param entropyMillis
	 *            the entropyMillis to set
	 */
	public void setEntropyMillis(long entropyMillis) {
		this.entropyMillis = entropyMillis;
	}

	@Override
	public String toString() {
		return "[total=" + totalMillis + "ms, selection=" + selectionMillis + "ms, crossover=" + crossoverMillis
				+ "ms, mutation=" + mutationMillis + "ms, evaluation=" + evaluationMillis + "ms, major="
				+ majorEvaluationMillis + "ms, entropy=" + entropyMillis + "ms]";
	}
}
