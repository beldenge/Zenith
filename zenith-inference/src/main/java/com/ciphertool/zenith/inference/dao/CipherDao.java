/**
 * Copyright 2017-2019 George Belden
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

import com.ciphertool.zenith.inference.entities.Cipher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CipherDao {
	private static Logger	log	= LoggerFactory.getLogger(CipherDao.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Value("${cipher.repository-filename}")
	private String ciphersFilename;

	public Cipher findByCipherName(String name) {
		if (name == null || name.isEmpty()) {
			log.warn("Attempted to find cipher with null or empty name.  Returning null.");

			return null;
		}

		return findAll().stream()
				.filter(cipher -> name.equalsIgnoreCase(cipher.getName()))
				.findAny()
				.orElse(null);
	}

	public List<Cipher> findAll() {
		List<Cipher> ciphers = new ArrayList<>();

		try {
			InputStream is = new ClassPathResource(ciphersFilename).getInputStream();
			ciphers.addAll(Arrays.asList(OBJECT_MAPPER.readValue(is, Cipher[].class)));
		} catch (IOException e) {
			log.error("Unable to read Ciphers from file: {}.", ciphersFilename, e);
			throw new IllegalStateException(e);
		}

		return ciphers;
	}
}
