/**
 * Copyright 2017 George Belden
 * 
 * This file is part of Zenith.
 * 
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.inference.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.ciphertool.zenith.inference.entities.Cipher;

@Component
public class CipherDao {
	private static Logger	log	= LoggerFactory.getLogger(CipherDao.class);

	@Autowired
	private MongoOperations	mongoOperations;

	public Cipher findByCipherName(String name) {
		if (name == null || name.isEmpty()) {
			log.warn("Attempted to find cipher with null or empty name.  Returning null.");

			return null;
		}

		Query selectByNameQuery = new Query(Criteria.where("name").is(name));

		return mongoOperations.findOne(selectByNameQuery, Cipher.class);
	}

	public List<Cipher> findAll() {
		return mongoOperations.findAll(Cipher.class);
	}
}
