package com.ciphertool.zenith.model.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.model.entities.NGramCountSum;

@Component
public class NGramCountSumDao {
	@Autowired
	private MongoOperations	mongoOperations;

	@Value("${collection.ngram.count.sum.name}")
	private String			collectionName;

	public void add(NGramCountSum nGramCountSum) {
		mongoOperations.insert(nGramCountSum, collectionName);
	}

	public NGramCountSum find(int order, boolean includesWordBoundaries) {
		Query query = new Query();
		query.addCriteria(Criteria.where("order").is(order).and("includesWordBoundaries").is(includesWordBoundaries));

		return mongoOperations.findOne(query, NGramCountSum.class, collectionName);
	}

	public void delete(int order, boolean includesWordBoundaries) {
		Query query = new Query();
		query.addCriteria(Criteria.where("order").is(order).and("includesWordBoundaries").is(includesWordBoundaries));

		mongoOperations.remove(query, NGramCountSum.class, collectionName);
	}
}
