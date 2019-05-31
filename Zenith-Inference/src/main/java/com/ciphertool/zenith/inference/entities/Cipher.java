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

package com.ciphertool.zenith.inference.entities;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Document(collection = "ciphers")
public class Cipher implements Serializable {
	private static final long	serialVersionUID		= 3417112220260206089L;

	@Id
	private ObjectId			id;

	@Indexed(background = true)
	private String				name;

	private int					columns;

	private int					rows;

	private boolean				hasKnownSolution;

	private List<Ciphertext>	ciphertextCharacters	= new ArrayList<>();

	public Cipher() {
	}

	public Cipher(String name, int rows, int columns) {
		this.name = name;
		this.rows = rows;
		this.columns = columns;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public List<Ciphertext> getCiphertextCharacters() {
		return Collections.unmodifiableList(ciphertextCharacters);
	}

	public void addCiphertextCharacter(Ciphertext ciphertext) {
		this.ciphertextCharacters.add(ciphertext);
	}

	public void removeCiphertextCharacter(Ciphertext ciphertext) {
		this.ciphertextCharacters.remove(ciphertext);
	}

	public void replaceCiphertextCharacter(int index, Ciphertext ciphertext) {
		this.ciphertextCharacters.remove(index);
		this.ciphertextCharacters.add(index, ciphertext);
		ciphertext.setCiphertextId(index);
	}

	public int length() {
		return rows * columns;
	}

	/**
	 * @return the hasKnownSolution
	 */
	public boolean hasKnownSolution() {
		return hasKnownSolution;
	}

	/**
	 * @param hasKnownSolution
	 *            the hasKnownSolution to set
	 */
	public void setHasKnownSolution(boolean hasKnownSolution) {
		this.hasKnownSolution = hasKnownSolution;
	}

	public Cipher clone() {
		Cipher cloned = new Cipher(this.name, this.rows, this.columns);
		cloned.setId(this.id);
		cloned.setHasKnownSolution(this.hasKnownSolution);

		for (Ciphertext ciphertext : this.ciphertextCharacters) {
			cloned.addCiphertextCharacter(ciphertext.clone());
		}

		return cloned;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ciphertextCharacters == null) ? 0 : ciphertextCharacters.hashCode());
		result = prime * result + columns;
		result = prime * result + (hasKnownSolution ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + rows;
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
		Cipher other = (Cipher) obj;
		if (ciphertextCharacters == null) {
			if (other.ciphertextCharacters != null) {
				return false;
			}
		} else if (!ciphertextCharacters.equals(other.ciphertextCharacters)) {
			return false;
		}
		if (columns != other.columns) {
			return false;
		}
		if (hasKnownSolution != other.hasKnownSolution) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (rows != other.rows) {
			return false;
		}
		return true;
	}

	/*
	 * Prints the properties of the cipher and then outputs the entire ciphertext list in block format.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Cipher [id=" + id + ", name=" + name + ", columns=" + columns + ", rows=" + rows + ", hasKnownSolution="
				+ hasKnownSolution + ", ciphertextCharacters=" + ciphertextCharacters + "]\n");

		int maxLength = this.ciphertextCharacters.stream()
				.map(Ciphertext::getValue)
				.map(String::length)
				.max(Comparator.comparing(Integer::intValue))
				.get();

		int actualSize = this.ciphertextCharacters.size();
		for (int i = 0; i < actualSize; i++) {
			String nextValue = this.ciphertextCharacters.get(i).getValue();

			sb.append(" ");
			sb.append(nextValue);
			sb.append(" ");

			for (int j = 0; j < (maxLength - nextValue.length()); j ++) {
				sb.append(" ");
			}

			/*
			 * Print a newline if we are at the end of the row. Add 1 to the index so the modulus function doesn't
			 * break.
			 */
			if (((i + 1) % this.columns) == 0) {
				sb.append("\n");
			} else {
				sb.append(" ");
			}
		}

		return sb.toString();
	}
}
