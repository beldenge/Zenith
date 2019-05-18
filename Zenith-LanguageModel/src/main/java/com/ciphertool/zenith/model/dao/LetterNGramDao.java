/**
 * Copyright 2017-2019 George Belden
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

import com.ciphertool.zenith.model.entities.TreeNGram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LetterNGramDao {
	private Logger				log							= LoggerFactory.getLogger(getClass());

	private static final String COLLECTION_NAME = "letterNGrams";

	@Value("${mongodb.parallelScan.batchSize}")
	private int					batchSize;

	@Value("${mongodb.parallelScan.numCursors}")
	private int					numCursors;

	@Autowired
	private MongoOperations		mongoOperations;

	public List<TreeNGram> findAll() {
		long startCount = System.currentTimeMillis();

		List<TreeNGram> nGrams = mongoOperations.findAll(TreeNGram.class, COLLECTION_NAME);

		log.info("Finished finding nodes in {}ms.", (System.currentTimeMillis() - startCount));

		return nGrams;
	}

	public void addAll(List<TreeNGram> nodes) {
		if (nodes == null || nodes.isEmpty()) {
			return;
		}

		mongoOperations.insert(nodes, COLLECTION_NAME);
	}

	public void deleteAll() {
		mongoOperations.dropCollection(COLLECTION_NAME);
		mongoOperations.createCollection(COLLECTION_NAME);
		mongoOperations.indexOps(COLLECTION_NAME).ensureIndex(new Index("count", Direction.DESC));
	}
}
