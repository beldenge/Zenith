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

package com.ciphertool.zenith.neural.generate.zodiac408;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
@Profile("zodiac408")
public class EnglishParagraphDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    public boolean exists() {
        return mongoTemplate.collectionExists(EnglishParagraph.class);
    }

    public Long count() {
        return mongoTemplate.count(new Query(), EnglishParagraph.class);
    }

    public EnglishParagraph findBySequence(long sequence) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sequence").is(sequence));

        return mongoTemplate.findOne(query, EnglishParagraph.class);
    }

    public void add(EnglishParagraph paragraph) {
        mongoTemplate.insert(paragraph);
    }

    public void reinitialize() {
        if (mongoTemplate.collectionExists(EnglishParagraph.class)) {
            mongoTemplate.dropCollection(EnglishParagraph.class);
        }

        mongoTemplate.createCollection(EnglishParagraph.class);
        mongoTemplate.indexOps(EnglishParagraph.class).ensureIndex(new Index("sequence", Direction.ASC).unique());
    }
}
