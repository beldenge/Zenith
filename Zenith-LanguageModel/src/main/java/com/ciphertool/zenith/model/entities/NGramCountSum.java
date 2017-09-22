/**
 * Copyright 2017 George Belden
 * 
 * This file is part of Zenith.
 * 
 * Zenith is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Zenith is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.model.entities;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class NGramCountSum {
	@Id
	private ObjectId	id;

	private Integer		order;

	private Boolean		includesWordBoundaries;

	private Long		sum;

	/**
	 * @param order
	 *            the Markov order
	 * @param includesWordBoundaries
	 *            whether this sum includes word boundaries
	 * @param sum
	 *            the sum of n-gram counts at this order
	 */
	public NGramCountSum(int order, boolean includesWordBoundaries, long sum) {
		this.order = order;
		this.includesWordBoundaries = includesWordBoundaries;
		this.sum = sum;
	}

	/**
	 * @return the id
	 */
	public ObjectId getId() {
		return id;
	}

	/**
	 * @return the order
	 */
	public Integer getOrder() {
		return order;
	}

	/**
	 * @return the includesWordBoundaries
	 */
	public Boolean isIncludesWordBoundaries() {
		return includesWordBoundaries;
	}

	/**
	 * @return the sum
	 */
	public Long getSum() {
		return sum;
	}
}
