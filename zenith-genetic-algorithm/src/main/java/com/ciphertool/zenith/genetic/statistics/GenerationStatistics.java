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

import java.math.BigDecimal;

public class GenerationStatistics {
	private ExecutionStatistics		executionStatistics;

	private int						generation;

	private Double				bestFitness;

	private Double				averageFitness;

	private BigDecimal				entropy;

	private Double				knownSolutionProximity;

	private int						numberOfCrossovers;

	private int						numberOfMutations;

	private int						numberOfEvaluations;

	private int						numberOfMajorEvaluations;

	private int						numberRandomlyGenerated;

	private int						numberSelectedOut;

	private PerformanceStatistics	performanceStatistics;

	/**
	 * Default no-args constructor
	 */
	public GenerationStatistics() {
	}

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

	/**
	 * @return the executionStatistics
	 */
	public ExecutionStatistics getExecutionStatistics() {
		return executionStatistics;
	}

	/**
	 * @param executionStatistics
	 *            the executionStatistics to set
	 */
	public void setExecutionStatistics(ExecutionStatistics executionStatistics) {
		this.executionStatistics = executionStatistics;
	}

	/**
	 * @return the generation
	 */
	public int getGeneration() {
		return generation;
	}

	/**
	 * @param generation
	 *            the generation to set
	 */
	public void setGeneration(int generation) {
		this.generation = generation;
	}

	/**
	 * @return the bestFitness
	 */
	public Double getBestFitness() {
		return bestFitness;
	}

	/**
	 * @param bestFitness
	 *            the bestFitness to set
	 */
	public void setBestFitness(Double bestFitness) {
		this.bestFitness = bestFitness;
	}

	/**
	 * @return the averageFitness
	 */
	public Double getAverageFitness() {
		return averageFitness;
	}

	/**
	 * @param averageFitness
	 *            the averageFitness to set
	 */
	public void setAverageFitness(Double averageFitness) {
		this.averageFitness = averageFitness;
	}

	/**
	 * @return the entropy
	 */
	public BigDecimal getEntropy() {
		return entropy;
	}

	/**
	 * @param entropy
	 *            the entropy to set
	 */
	public void setEntropy(BigDecimal entropy) {
		this.entropy = entropy;
	}

	/**
	 * @return the knownSolutionProximity
	 */
	public Double getKnownSolutionProximity() {
		return knownSolutionProximity;
	}

	/**
	 * @param knownSolutionProximity
	 *            the knownSolutionProximity to set
	 */
	public void setKnownSolutionProximity(Double knownSolutionProximity) {
		this.knownSolutionProximity = knownSolutionProximity;
	}

	/**
	 * @return the numberOfCrossovers
	 */
	protected int getNumberOfCrossovers() {
		return numberOfCrossovers;
	}

	/**
	 * @param numberOfCrossovers
	 *            the numberOfCrossovers to set
	 */
	public void setNumberOfCrossovers(int numberOfCrossovers) {
		this.numberOfCrossovers = numberOfCrossovers;
	}

	/**
	 * @return the numberOfMutations
	 */
	protected int getNumberOfMutations() {
		return numberOfMutations;
	}

	/**
	 * @param numberOfMutations
	 *            the numberOfMutations to set
	 */
	public void setNumberOfMutations(int numberOfMutations) {
		this.numberOfMutations = numberOfMutations;
	}

	/**
	 * @return the numberOfEvaluations
	 */
	public int getNumberOfEvaluations() {
		return numberOfEvaluations;
	}

	/**
	 * @param numberOfEvaluations
	 *            the numberOfEvaluations to set
	 */
	public void setNumberOfEvaluations(int numberOfEvaluations) {
		this.numberOfEvaluations = numberOfEvaluations;
	}

	/**
	 * @return the numberOfMajorEvaluations
	 */
	public int getNumberOfMajorEvaluations() {
		return numberOfMajorEvaluations;
	}

	/**
	 * @param numberOfMajorEvaluations
	 *            the numberOfMajorEvaluations to set
	 */
	public void setNumberOfMajorEvaluations(int numberOfMajorEvaluations) {
		this.numberOfMajorEvaluations = numberOfMajorEvaluations;
	}

	/**
	 * @return the numberRandomlyGenerated
	 */
	protected int getNumberRandomlyGenerated() {
		return numberRandomlyGenerated;
	}

	/**
	 * @param numberRandomlyGenerated
	 *            the numberRandomlyGenerated to set
	 */
	public void setNumberRandomlyGenerated(int numberRandomlyGenerated) {
		this.numberRandomlyGenerated = numberRandomlyGenerated;
	}

	/**
	 * @return the numberSelectedOut
	 */
	protected int getNumberSelectedOut() {
		return numberSelectedOut;
	}

	/**
	 * @param numberSelectedOut
	 *            the numberSelectedOut to set
	 */
	public void setNumberSelectedOut(int numberSelectedOut) {
		this.numberSelectedOut = numberSelectedOut;
	}

	/**
	 * @return the performanceStatistics
	 */
	public PerformanceStatistics getPerformanceStatistics() {
		if (performanceStatistics == null) {
			this.performanceStatistics = new PerformanceStatistics();
		}

		return performanceStatistics;
	}

	/**
	 * @param performanceStatistics
	 *            the performanceStatistics to set
	 */
	public void setPerformanceStatistics(PerformanceStatistics performanceStatistics) {
		this.performanceStatistics = performanceStatistics;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((averageFitness == null) ? 0 : averageFitness.hashCode());
		result = prime * result + ((bestFitness == null) ? 0 : bestFitness.hashCode());
		result = prime * result + ((entropy == null) ? 0 : entropy.hashCode());
		result = prime * result + ((executionStatistics == null) ? 0 : executionStatistics.hashCode());
		result = prime * result + generation;
		result = prime * result + ((knownSolutionProximity == null) ? 0 : knownSolutionProximity.hashCode());
		result = prime * result + numberOfCrossovers;
		result = prime * result + numberOfEvaluations;
		result = prime * result + numberOfMajorEvaluations;
		result = prime * result + numberOfMutations;
		result = prime * result + numberRandomlyGenerated;
		result = prime * result + numberSelectedOut;
		result = prime * result + ((performanceStatistics == null) ? 0 : performanceStatistics.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof GenerationStatistics)) {
			return false;
		}
		GenerationStatistics other = (GenerationStatistics) obj;
		if (averageFitness == null) {
			if (other.averageFitness != null) {
				return false;
			}
		} else if (!averageFitness.equals(other.averageFitness)) {
			return false;
		}
		if (bestFitness == null) {
			if (other.bestFitness != null) {
				return false;
			}
		} else if (!bestFitness.equals(other.bestFitness)) {
			return false;
		}
		if (entropy == null) {
			if (other.entropy != null) {
				return false;
			}
		} else if (!entropy.equals(other.entropy)) {
			return false;
		}
		if (executionStatistics == null) {
			if (other.executionStatistics != null) {
				return false;
			}
		} else if (!executionStatistics.equals(other.executionStatistics)) {
			return false;
		}
		if (generation != other.generation) {
			return false;
		}
		if (knownSolutionProximity == null) {
			if (other.knownSolutionProximity != null) {
				return false;
			}
		} else if (!knownSolutionProximity.equals(other.knownSolutionProximity)) {
			return false;
		}
		if (numberOfCrossovers != other.numberOfCrossovers) {
			return false;
		}
		if (numberOfEvaluations != other.numberOfEvaluations) {
			return false;
		}
		if (numberOfMajorEvaluations != other.numberOfMajorEvaluations) {
			return false;
		}
		if (numberOfMutations != other.numberOfMutations) {
			return false;
		}
		if (numberRandomlyGenerated != other.numberRandomlyGenerated) {
			return false;
		}
		if (numberSelectedOut != other.numberSelectedOut) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String proximity = (this.knownSolutionProximity == null) ? "" : ", proximityToKnown="
				+ String.format("%1$,.2f", this.knownSolutionProximity) + "%";

		return "[generation=" + generation + (averageFitness == null ? "" : ", averageFitness=" + averageFitness)
				+ (bestFitness == null ? "" : ", bestFitness=" + bestFitness) + proximity + ", entropy="
				+ String.format("%1$,.4f", entropy) + ", crossovers=" + numberOfCrossovers + ", evals="
				+ numberOfEvaluations + ", majorEvals=" + numberOfMajorEvaluations + ", mutations=" + numberOfMutations
				+ ", deaths=" + numberSelectedOut + ", births=" + numberRandomlyGenerated + ", performance="
				+ performanceStatistics.toString() + "]";
	}
}
