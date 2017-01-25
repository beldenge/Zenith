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

package com.ciphertool.zenith.inference.entities;

public class Ciphertext {

	private Integer	ciphertextId;

	private String	value;

	public Ciphertext() {
	}

	public Ciphertext(Integer ciphertextId, String value) {
		this.ciphertextId = ciphertextId;
		this.value = value;
	}

	public Integer getCiphertextId() {
		return ciphertextId;
	}

	public void setCiphertextId(Integer ciphertextId) {
		this.ciphertextId = ciphertextId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Ciphertext clone() {
		return new Ciphertext(this.ciphertextId, this.value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ciphertextId == null) ? 0 : ciphertextId.hashCode());
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

		if (getClass() != obj.getClass()) {
			return false;
		}

		Ciphertext other = (Ciphertext) obj;
		if (ciphertextId == null) {
			if (other.ciphertextId != null) {
				return false;
			}
		} else if (!ciphertextId.equals(other.ciphertextId)) {
			return false;
		}

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
		return "Ciphertext [value=" + value + ", ciphertextId=" + ciphertextId + "]";
	}
}
