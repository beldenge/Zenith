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
import java.util.Collections;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;

import com.ciphertool.zenith.model.markov.NGramIndexNode;
import com.ciphertool.zenith.model.markov.UnidirectionalNGramIndexNode;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ParallelScanOptions;

public class LetterNGramDao {
	private String				collectionWithSpaces;
	private String				collectionWithoutSpaces;
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
		DBCollection collection = mongoOperations.getCollection((includeWordBoundaries ? collectionWithSpaces : collectionWithoutSpaces));
		List<Cursor> cursors = collection.parallelScan(ParallelScanOptions.builder().batchSize(batchSize).numCursors(numCursors).build());

		List<NGramIndexNode> nodesToReturn = Collections.synchronizedList(new ArrayList<>());

		cursors.parallelStream().forEach(cursor -> {
			DBObject next;
			UnidirectionalNGramIndexNode nextNode;

			while (cursor.hasNext()) {
				next = cursor.next();

				if (((long) next.get(COUNT_KEY)) >= minimumCount) {
					nextNode = new UnidirectionalNGramIndexNode(null, (String) next.get(CUMULATIVE_STRING_KEY),
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
		});

		return nodesToReturn;
	}

	public long countLessThan(int minimumCount, boolean includeWordBoundaries) {
		BasicQuery query = new BasicQuery("{ count : { $lt : " + minimumCount + " } }");

		return mongoOperations.count(query, NGramIndexNode.class, (includeWordBoundaries ? collectionWithSpaces : collectionWithoutSpaces));
	}

	public void addAll(List<NGramIndexNode> nodes, boolean includeWordBoundaries) {
		mongoOperations.bulkOps(BulkMode.UNORDERED, NGramIndexNode.class, (includeWordBoundaries ? collectionWithSpaces : collectionWithoutSpaces)).insert(nodes).execute();
	}

	public void deleteAll(boolean includeWordBoundaries) {
		// This is better than dropping the collection because we won't have to re-ensure indexes and such
		mongoOperations.bulkOps(BulkMode.UNORDERED, NGramIndexNode.class, (includeWordBoundaries ? collectionWithSpaces : collectionWithoutSpaces)).remove(new Query()).execute();
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

	/**
	 * @param collectionWithSpaces
	 *            the collectionWithSpaces to set
	 */
	@Required
	public void setCollectionWithSpaces(String collectionWithSpaces) {
		this.collectionWithSpaces = collectionWithSpaces;
	}

	/**
	 * @param collectionWithoutSpaces
	 *            the collectionWithoutSpaces to set
	 */
	@Required
	public void setCollectionWithoutSpaces(String collectionWithoutSpaces) {
		this.collectionWithoutSpaces = collectionWithoutSpaces;
	}
}
