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

import java.math.BigDecimal;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class NGram {
	@Id
	protected ObjectId		id;

	protected long			count	= 0L;

	protected BigDecimal	probability;

	protected BigDecimal	conditionalProbability;

	protected String		cumulativeString;

	protected Integer		order;

	/**
	 * @return the id
	 */
	public ObjectId getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(ObjectId id) {
		this.id = id;
	}

	public void increment() {
		this.count += 1L;
	}

	/**
	 * @return the count
	 */
	public long getCount() {
		return this.count;
	}

	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(long count) {
		this.count = count;
	}

	/**
	 * @return the probability
	 */
	public BigDecimal getProbability() {
		return this.probability;
	}

	/**
	 * All current usages of this method are thread-safe, but since it's used in a multi-threaded way, this is a
	 * defensive measure in case future usage changes are not thread-safe.
	 * 
	 * @param probability
	 *            the probability to set
	 */
	public synchronized void setProbability(BigDecimal probability) {
		this.probability = probability;
	}

	/**
	 * @return the conditionalProbability
	 */
	public BigDecimal getConditionalProbability() {
		return conditionalProbability;
	}

	/**
	 * All current usages of this method are thread-safe, but since it's used in a multi-threaded way, this is a
	 * defensive measure in case future usage changes are not thread-safe.
	 * 
	 * @param conditionalProbability
	 *            the conditionalProbability to set
	 */
	public synchronized void setConditionalProbability(BigDecimal conditionalProbability) {
		this.conditionalProbability = conditionalProbability;
	}

	/**
	 * @return the order
	 */
	public Integer getOrder() {
		return order;
	}

	/**
	 * @param order
	 *            the order to set
	 */
	public void setOrder(Integer order) {
		this.order = order;
	}

	/**
	 * @return the cumulativeString
	 */
	public String getCumulativeString() {
		return cumulativeString;
	}

	/**
	 * @param cumulativeString
	 *            the cumulativeString to set
	 */
	public void setCumulativeString(String cumulativeString) {
		this.cumulativeString = cumulativeString;
	}
}
