package com.ciphertool.zenith.model.dao;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ciphertool.zenith.model.entities.NGramCountSum;

public class NGramCountSumDao {
	private MongoOperations	mongoOperations;

	private String			collectionName;

	public void add(NGramCountSum nGramCountSum) {
		mongoOperations.insert(nGramCountSum, collectionName);
	}

	public NGramCountSum find(int order, boolean includesWordBoundaries, boolean isTypeMasked) {
		Query query = new Query();
		query.addCriteria(Criteria.where("order").is(order).and("includesWordBoundaries").is(includesWordBoundaries).and("isTypeMasked").is(isTypeMasked));

		return mongoOperations.findOne(query, NGramCountSum.class, collectionName);
	}

	public void deleteAll(boolean includesWordBoundaries, boolean isTypeMasked) {
		Query query = new Query();
		query.addCriteria(Criteria.where("includesWordBoundaries").is(includesWordBoundaries).and("isTypeMasked").is(isTypeMasked));

		mongoOperations.remove(query, NGramCountSum.class, collectionName);
	}

	@Required
	public void setMongoTemplate(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	/**
	 * @param collectionName
	 *            the collectionName to set
	 */
	@Required
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}
}
