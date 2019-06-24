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

import com.ciphertool.zenith.genetic.GeneticAlgorithmStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ExecutionStatistics {
	private Date						startDateTime;

	private Date						endDateTime;

	private Integer						populationSize;

	private Double						mutationRate;

	private String						crossoverAlgorithm;

	private String						fitnessEvaluator;

	private String						mutationAlgorithm;

	private List<GenerationStatistics>	generationStatisticsList	= new ArrayList<GenerationStatistics>();

	/**
	 * Default no-args constructor
	 */
	public ExecutionStatistics() {
	}

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
	 * @return the startDateTime
	 */
	public Date getStartDateTime() {
		return startDateTime;
	}

	/**
	 * @param startDateTime
	 *            the startDateTime to set
	 */
	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}

	/**
	 * @return the endDateTime
	 */
	public Date getEndDateTime() {
		return endDateTime;
	}

	/**
	 * @param endDateTime
	 *            the endDateTime to set
	 */
	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}

	/**
	 * @return the populationSize
	 */
	public Integer getPopulationSize() {
		return populationSize;
	}

	/**
	 * @param populationSize
	 *            the populationSize to set
	 */
	public void setPopulationSize(Integer populationSize) {
		this.populationSize = populationSize;
	}

	/**
	 * @return the mutationRate
	 */
	public Double getMutationRate() {
		return mutationRate;
	}

	/**
	 * @param mutationRate
	 *            the mutationRate to set
	 */
	public void setMutationRate(Double mutationRate) {
		this.mutationRate = mutationRate;
	}

	/**
	 * @return the crossoverAlgorithm
	 */
	public String getCrossoverAlgorithm() {
		return crossoverAlgorithm;
	}

	/**
	 * @param crossoverAlgorithm
	 *            the crossoverAlgorithm to set
	 */
	public void setCrossoverAlgorithm(String crossoverAlgorithm) {
		this.crossoverAlgorithm = crossoverAlgorithm;
	}

	/**
	 * @return the fitnessEvaluator
	 */
	public String getFitnessEvaluator() {
		return fitnessEvaluator;
	}

	/**
	 * @param fitnessEvaluator
	 *            the fitnessEvaluator to set
	 */
	public void setFitnessEvaluator(String fitnessEvaluator) {
		this.fitnessEvaluator = fitnessEvaluator;
	}

	/**
	 * @return the mutationAlgorithm
	 */
	public String getMutationAlgorithm() {
		return mutationAlgorithm;
	}

	/**
	 * @param mutationAlgorithm
	 *            the mutationAlgorithm to set
	 */
	public void setMutationAlgorithm(String mutationAlgorithm) {
		this.mutationAlgorithm = mutationAlgorithm;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((crossoverAlgorithm == null) ? 0 : crossoverAlgorithm.hashCode());
		result = prime * result + ((endDateTime == null) ? 0 : endDateTime.hashCode());
		result = prime * result + ((fitnessEvaluator == null) ? 0 : fitnessEvaluator.hashCode());
		result = prime * result + ((mutationAlgorithm == null) ? 0 : mutationAlgorithm.hashCode());
		result = prime * result + ((mutationRate == null) ? 0 : mutationRate.hashCode());
		result = prime * result + ((populationSize == null) ? 0 : populationSize.hashCode());
		result = prime * result + ((startDateTime == null) ? 0 : startDateTime.hashCode());
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		ExecutionStatistics other = (ExecutionStatistics) obj;
		if (crossoverAlgorithm == null) {
			if (other.crossoverAlgorithm != null) {
				return false;
			}
		} else if (!crossoverAlgorithm.equals(other.crossoverAlgorithm)) {
			return false;
		}
		if (endDateTime == null) {
			if (other.endDateTime != null) {
				return false;
			}
		} else if (!endDateTime.equals(other.endDateTime)) {
			return false;
		}
		if (fitnessEvaluator == null) {
			if (other.fitnessEvaluator != null) {
				return false;
			}
		} else if (!fitnessEvaluator.equals(other.fitnessEvaluator)) {
			return false;
		}
		if (mutationAlgorithm == null) {
			if (other.mutationAlgorithm != null) {
				return false;
			}
		} else if (!mutationAlgorithm.equals(other.mutationAlgorithm)) {
			return false;
		}
		if (mutationRate == null) {
			if (other.mutationRate != null) {
				return false;
			}
		} else if (!mutationRate.equals(other.mutationRate)) {
			return false;
		}
		if (populationSize == null) {
			if (other.populationSize != null) {
				return false;
			}
		} else if (!populationSize.equals(other.populationSize)) {
			return false;
		}
		if (startDateTime == null) {
			if (other.startDateTime != null) {
				return false;
			}
		} else if (!startDateTime.equals(other.startDateTime)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ExecutionStatistics [startDateTime=" + startDateTime + ", endDateTime=" + endDateTime
				+ ", populationSize=" + populationSize + ", mutationRate=" + mutationRate + ", crossoverAlgorithm="
				+ crossoverAlgorithm + ", fitnessEvaluator=" + fitnessEvaluator + ", mutationAlgorithm="
				+ mutationAlgorithm + "]";
	}
}