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

package com.ciphertool.zenith.inference;

import com.ciphertool.zenith.inference.entities.Plaintext;

public class CiphertextMapping {
	private String		ciphertext;
	private Plaintext	plaintext;

	/**
	 * @param ciphertext
	 *            the ciphertext String
	 * @param plaintext
	 *            the Plaintext
	 */
	public CiphertextMapping(String ciphertext, Plaintext plaintext) {
		this.ciphertext = ciphertext;
		this.plaintext = plaintext;
	}

	/**
	 * @return the ciphertext
	 */
	public String getCiphertext() {
		return ciphertext;
	}

	/**
	 * @return the plaintext
	 */
	public Plaintext getPlaintext() {
		return plaintext;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ciphertext == null) ? 0 : ciphertext.hashCode());
		result = prime * result + ((plaintext == null) ? 0 : plaintext.hashCode());
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
		if (!(obj instanceof CiphertextMapping)) {
			return false;
		}
		CiphertextMapping other = (CiphertextMapping) obj;
		if (ciphertext == null) {
			if (other.ciphertext != null) {
				return false;
			}
		} else if (!ciphertext.equals(other.ciphertext)) {
			return false;
		}
		if (plaintext == null) {
			if (other.plaintext != null) {
				return false;
			}
		} else if (!plaintext.equals(other.plaintext)) {
			return false;
		}
		return true;
	}
}
