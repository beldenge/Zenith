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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.model.entities.TreeNGram;

@Component
public class LetterNGramDao {
	private Logger				log							= LoggerFactory.getLogger(getClass());

	private static final String	ID_KEY						= "id";
	private static final String	COUNT_KEY					= "count";
	private static final String	PROBABILITY_KEY				= "probability";
	private static final String	CONDITIONAL_PROBABILITY_KEY	= "conditionalProbability";
	private static final String	CHAINED_PROBABILITY_KEY		= "chainedProbability";
	private static final String	CUMULATIVE_STRING_KEY		= "cumulativeString";
	private static final String	ORDER_KEY					= "order";

	@Value("${collection.letter.ngram.with.spaces.name}")
	private String				collectionWithSpaces;

	@Value("${collection.letter.ngram.without.spaces.name}")
	private String				collectionWithoutSpaces;

	@Value("${mongodb.parallelScan.batchSize}")
	private int					batchSize;

	@Value("${mongodb.parallelScan.numCursors}")
	private int					numCursors;

	@Autowired
	private MongoOperations		mongoOperations;

	// TODO: right now we are not using the minimumCount at all -- as the query was not returning anything
    public List<TreeNGram> findAll(int minimumCount, boolean includeWordBoundaries) {
        long startCount = System.currentTimeMillis();
        log.info("Finding nodes with counts greater than or equal to the minimum of {}.", minimumCount);

        BasicQuery query = new BasicQuery("{ count : { $gte : " + minimumCount + " } }");

        List<TreeNGram> nGrams = mongoOperations.findAll(TreeNGram.class, (includeWordBoundaries ? collectionWithSpaces : collectionWithoutSpaces));//.find(query, TreeNGram.class, (includeWordBoundaries ? collectionWithSpaces : collectionWithoutSpaces));

        log.info("Finished finding nodes greater than or equal to the minimum of {} in {}ms.", minimumCount, (System.currentTimeMillis()
                - startCount));

        return nGrams;
    }

    // TODO: the parallel scan seems to no longer work in Spring Data MongoDB 2.0
	public List<TreeNGram> findAllParallel(Integer minimumCount, Boolean includeWordBoundaries) {
		DBCollection collection = ((MongoTemplate) mongoOperations).getMongoDbFactory().getLegacyDb().getCollection((includeWordBoundaries ? collectionWithSpaces : collectionWithoutSpaces));

		List<Cursor> cursors = collection.parallelScan(ParallelScanOptions.builder().batchSize(batchSize).numCursors(numCursors).build());

		List<TreeNGram> nodesToReturn = Collections.synchronizedList(new ArrayList<>());

		cursors.parallelStream().forEach(cursor -> {
			DBObject next;
			TreeNGram nextNode;

			while (cursor.hasNext()) {
				next = cursor.next();

				if (Float.parseFloat((String) next.get(COUNT_KEY)) > (float) minimumCount) {
					nextNode = new TreeNGram((String) next.get(CUMULATIVE_STRING_KEY));

					nextNode.setId((ObjectId) next.get(ID_KEY));
					nextNode.setCount(Float.parseFloat((String) next.get(COUNT_KEY)));
					nextNode.setOrder((int) next.get(ORDER_KEY));

					if (next.containsField(PROBABILITY_KEY)) {
						nextNode.setProbability(Float.parseFloat((String) next.get(PROBABILITY_KEY)));
					}

					if (next.containsField(CONDITIONAL_PROBABILITY_KEY)) {
						nextNode.setConditionalProbability(Float.parseFloat((String) next.get(CONDITIONAL_PROBABILITY_KEY)));
					}

					if (next.containsField(CHAINED_PROBABILITY_KEY)) {
						nextNode.setChainedProbability(Float.parseFloat((String) next.get(CHAINED_PROBABILITY_KEY)));
					}

					nodesToReturn.add(nextNode);
				}
			}
		});

		return nodesToReturn;
	}

	public long countLessThan(int minimumCount, boolean includeWordBoundaries) {
		long startCount = System.currentTimeMillis();
		log.info("Counting nodes with counts below the minimum of {}.", minimumCount);

		BasicQuery query = new BasicQuery("{ count : { $lt : " + minimumCount + " } }");

		long result = mongoOperations.count(query, TreeNGram.class, (includeWordBoundaries ? collectionWithSpaces : collectionWithoutSpaces));

		log.info("Finished counting nodes below the minimum of {} in {}ms.", minimumCount, (System.currentTimeMillis()
				- startCount));

		return result;
	}

	public void addAll(List<TreeNGram> nodes, boolean includeWordBoundaries) {
		if (nodes == null || nodes.isEmpty()) {
			return;
		}

		mongoOperations.insert(nodes, (includeWordBoundaries ? collectionWithSpaces : collectionWithoutSpaces));
	}

	public void deleteAll(boolean includeWordBoundaries) {
		String collection = (includeWordBoundaries ? collectionWithSpaces : collectionWithoutSpaces);

		if (mongoOperations.collectionExists(collection)) {
			mongoOperations.dropCollection(collection);
		}

		mongoOperations.createCollection(collection);
		mongoOperations.indexOps(collection).ensureIndex(new Index("count", Direction.DESC));
	}
}
