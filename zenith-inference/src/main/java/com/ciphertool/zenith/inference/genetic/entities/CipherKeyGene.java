 /**
 * Copyright 2017-2019 George Belden
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

package com.ciphertool.zenith.inference.genetic.entities;

 import com.ciphertool.zenith.genetic.entities.Chromosome;
import com.ciphertool.zenith.genetic.entities.Gene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CipherKeyGene implements Gene {
	private static Logger	log	= LoggerFactory.getLogger(CipherKeyGene.class);

	private Chromosome chromosome;

	private String			value;

	private boolean			hasMatch;

	public CipherKeyGene() {
	}

	/**
	 * Full-args constructor
	 * 
	 * @param chromosome
	 *            the Chromosome
	 * @param value
	 *            the String value for this cipher key
	 */
	public CipherKeyGene(Chromosome chromosome, String value) {
		if (null == value || value.isEmpty()) {
			log.error("Found null value in full-args constructor.  Unable to construct CipherKeyGene.");

			return;
		}

		this.chromosome = chromosome;
		this.value = value;
	}

	@Override
	public void setChromosome(Chromosome chromosome) {
		this.chromosome = chromosome;
	}

	@Override
	public Chromosome getChromosome() {
		return this.chromosome;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
		this.chromosome.setEvaluationNeeded(true);
	}

	@Override
	public Gene clone() {
		CipherKeyGene copyGene = new CipherKeyGene();

		/*
		 * The Chromosome should be set at a higher level, so we just set it to null which should be overwritten.
		 */
		copyGene.chromosome = null;
		copyGene.value = this.value;

		return copyGene;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());

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
		if (!(obj instanceof CipherKeyGene)) {
			return false;
		}

		CipherKeyGene other = (CipherKeyGene) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "CipherKeyGene [value=" + value + "]";
	}

	@Override
	public boolean hasMatch() {
		return this.hasMatch;
	}

	@Override
	public void setHasMatch(boolean hasMatch) {
		this.hasMatch = hasMatch;
	}
}
