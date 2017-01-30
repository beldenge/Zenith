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

package com.ciphertool.zenith.model.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;

import com.ciphertool.zenith.model.markov.NGramIndexNode;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ParallelScanOptions;

public class LetterNGramDao {
	private static final String	COLLECTION_WITH_SPACES		= "letterNGrams_withSpaces";
	private static final String	COLLECTION_WITHOUT_SPACES	= "letterNGrams_withoutSpaces";
	private static final String	ID_KEY						= "id";
	private static final String	LEVEL_KEY					= "level";
	private static final String	COUNT_KEY					= "count";
	private static final String	PROBABILITY_KEY				= "probability";
	private static final String	CONDITIONAL_PROBABILITY_KEY	= "conditionalProbability";
	private static final String	CUMULATIVE_STRING_KEY		= "cumulativeString";

	private MongoOperations		mongoOperations;
	private int					batchSize;
	private int					numCursors;

	public List<NGramIndexNode> findAll(int minimumCount, boolean includeWordBoundaries) {
		DBCollection collection = mongoOperations.getCollection((includeWordBoundaries ? COLLECTION_WITH_SPACES : COLLECTION_WITHOUT_SPACES));
		List<Cursor> cursors = collection.parallelScan(ParallelScanOptions.builder().batchSize(batchSize).numCursors(numCursors).build());

		List<NGramIndexNode> nodesToReturn = new ArrayList<>();

		DBObject next;
		NGramIndexNode nextNode;

		for (Cursor cursor : cursors) {
			while (cursor.hasNext()) {
				next = cursor.next();

				if (((long) next.get(COUNT_KEY)) >= minimumCount) {
					nextNode = new NGramIndexNode(null, (String) next.get(CUMULATIVE_STRING_KEY),
							(int) next.get(LEVEL_KEY));

					nextNode.setId((ObjectId) next.get(ID_KEY));
					nextNode.setCount((long) next.get(COUNT_KEY));

					if (next.containsField(PROBABILITY_KEY)) {
						nextNode.setProbability(new BigDecimal((String) next.get(PROBABILITY_KEY)));
					}

					if (next.containsField(CONDITIONAL_PROBABILITY_KEY)) {
						nextNode.setConditionalProbability(new BigDecimal(
								(String) next.get(CONDITIONAL_PROBABILITY_KEY)));
					}

					nodesToReturn.add(nextNode);
				}
			}
		}

		return nodesToReturn;
	}

	public long countLessThan(int minimumCount, boolean includeWordBoundaries) {
		BasicQuery query = new BasicQuery("{ count : { $lt : " + minimumCount + " } }");

		return mongoOperations.count(query, NGramIndexNode.class, (includeWordBoundaries ? COLLECTION_WITH_SPACES : COLLECTION_WITHOUT_SPACES));
	}

	public void addAll(List<NGramIndexNode> nodes, boolean includeWordBoundaries) {
		for (NGramIndexNode node : nodes) {
			mongoOperations.insert(node, (includeWordBoundaries ? COLLECTION_WITH_SPACES : COLLECTION_WITHOUT_SPACES));
		}
	}

	public void deleteAll(boolean includeWordBoundaries) {
		mongoOperations.dropCollection(includeWordBoundaries ? COLLECTION_WITH_SPACES : COLLECTION_WITHOUT_SPACES);
	}

	@Required
	public void setMongoTemplate(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	/**
	 * @param batchSize
	 *            the batchSize to set
	 */
	@Required
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	/**
	 * @param numCursors
	 *            the numCursors to set
	 */
	@Required
	public void setNumCursors(int numCursors) {
		this.numCursors = numCursors;
	}
}
