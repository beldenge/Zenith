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

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.mongodb.core.MongoOperations;

import com.ciphertool.zenith.model.markov.NGramIndexNode;

public class LetterNGramDao {
	private static final String	COLLECTION_WITH_SPACES		= "letterNGrams_withSpaces";
	private static final String	COLLECTION_WITHOUT_SPACES	= "letterNGrams_withoutSpaces";

	private MongoOperations		mongoOperations;

	public List<NGramIndexNode> findAllWithSpaces() {
		return mongoOperations.findAll(NGramIndexNode.class, COLLECTION_WITH_SPACES);
	}

	public List<NGramIndexNode> findAllWithoutSpaces() {
		return mongoOperations.findAll(NGramIndexNode.class, COLLECTION_WITHOUT_SPACES);
	}

	public void addAllWithSpaces(List<NGramIndexNode> nodes) {
		for (NGramIndexNode node : nodes) {
			mongoOperations.insert(node, COLLECTION_WITH_SPACES);
		}
	}

	public void addAllWithoutSpaces(List<NGramIndexNode> nodes) {
		for (NGramIndexNode node : nodes) {
			mongoOperations.insert(node, COLLECTION_WITHOUT_SPACES);
		}
	}

	public void deleteAllWithSpaces() {
		mongoOperations.dropCollection(COLLECTION_WITH_SPACES);
	}

	public void deleteAllWithoutSpaces() {
		mongoOperations.dropCollection(COLLECTION_WITHOUT_SPACES);
	}

	@Required
	public void setMongoTemplate(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}
}
