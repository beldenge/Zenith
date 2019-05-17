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

package com.ciphertool.zenith.inference.selection;

import java.math.BigDecimal;

public class BinaryRouletteTree {
	private BinaryRouletteNode root;

	public BinaryRouletteTree() {
	}

	public void insert(BinaryRouletteNode toInsert) {
		if (this.root == null) {
			this.root = toInsert;

			return;
		}

		insertNode(root, toInsert);
	}

	protected void insertNode(BinaryRouletteNode parent, BinaryRouletteNode toInsert) {
		if (toInsert.getValue().compareTo(parent.getValue()) < 0) {
			if (parent.getLessThan() == null) {
				parent.setLessThan(toInsert);

				return;
			}

			insertNode(parent.getLessThan(), toInsert);

			return;
		}

		if (parent.getGreaterThan() == null) {
			parent.setGreaterThan(toInsert);

			return;
		}

		insertNode(parent.getGreaterThan(), toInsert);
	}

	public BinaryRouletteNode find(BigDecimal value) {
		return findNode(this.root, value, null);
	}

	protected BinaryRouletteNode findNode(BinaryRouletteNode current, BigDecimal value, BinaryRouletteNode closestSoFar) {
		if (value.compareTo(current.getValue()) <= 0) {
			if (current.getLessThan() == null) {
				return current;
			}

			if (value.compareTo(current.getLessThan().getValue()) > 0) {
				closestSoFar = current;
			}

			return findNode(current.getLessThan(), value, closestSoFar);
		}

		if (current.getGreaterThan() == null) {
			return closestSoFar;
		}

		return findNode(current.getGreaterThan(), value, closestSoFar);
	}
}
