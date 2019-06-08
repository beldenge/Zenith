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
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Component
public class LetterNGramDao {
	private Logger				log							= LoggerFactory.getLogger(getClass());

	@Value("${language-model.filename}")
	private String modelFilename;

	public List<TreeNGram> findAll() {
		long startCount = System.currentTimeMillis();

		List<TreeNGram> treeNGrams = new ArrayList<>();

		try(Reader reader = Files.newBufferedReader(Paths.get(modelFilename))) {
			List<TreeNGram> records = new CsvToBeanBuilder(reader)
					.withType(TreeNGram.class)
					.build()
					.parse();

			treeNGrams.addAll(records);
		} catch (IOException e) {
			log.error("Unable to find ngrams from file: {}.", modelFilename, e);
			throw new IllegalStateException(e);
		}

		log.info("Finished finding nodes in {}ms.", (System.currentTimeMillis() - startCount));

		return treeNGrams;
	}

	public synchronized void addAll(List<TreeNGram> nodes) {
		if (nodes == null || nodes.isEmpty()) {
			return;
		}

		try(Writer writer = Files.newBufferedWriter(Paths.get(modelFilename), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			StatefulBeanToCsv sbc = new StatefulBeanToCsvBuilder(writer)
					.build();

			sbc.write(nodes);
		} catch(IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
			log.error("Unable to add nodes to output file: {}.", modelFilename, e);
			throw new IllegalStateException(e);
		}
	}

	public void deleteAll() {
		if (Files.exists(Paths.get(modelFilename))) {
			try {
				Files.delete(Paths.get(modelFilename));
			} catch (IOException e) {
				log.error("Unable to delete file at path: {}.", modelFilename, e);
				throw new IllegalStateException(e);
			}
		}
	}
}
