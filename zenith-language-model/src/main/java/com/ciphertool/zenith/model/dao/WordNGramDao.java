/*
 * Copyright 2017-2020 George Belden
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

package com.ciphertool.zenith.model.dao;

import com.ciphertool.zenith.model.entities.WordNGram;
import com.opencsv.bean.CsvToBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class WordNGramDao {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${language-model.filename.word-unigram}")
    private String unigramModelFilename;

    @Value("${language-model.filename.word-bigram}")
    private String bigramModelFilename;

    public List<WordNGram> findAllUnigrams() {
        long startCount = System.currentTimeMillis();

        List<WordNGram> wordUniGrams = new ArrayList<>();

        try (Reader reader = new InputStreamReader(new ClassPathResource(unigramModelFilename).getInputStream())) {
            wordUniGrams.addAll(new CsvToBeanBuilder(reader)
                    .withType(WordNGram.class)
                    .withSeparator('\t')
                    .build()
                    .parse());

            wordUniGrams.forEach(gram -> gram.setNGram(gram.getNGram().toLowerCase()));
        } catch (IOException e) {
            log.error("Unable to find word-unigrams from file: {}.", unigramModelFilename, e);
            throw new IllegalStateException(e);
        }

        log.info("Finished loading word-unigrams in {}ms.", (System.currentTimeMillis() - startCount));

        return wordUniGrams;
    }

    public List<WordNGram> findAllBigrams() {
        long startCount = System.currentTimeMillis();

        List<WordNGram> wordBiGrams = new ArrayList<>();

        try (Reader reader = new InputStreamReader(new ClassPathResource(bigramModelFilename).getInputStream())) {
            wordBiGrams.addAll(new CsvToBeanBuilder(reader)
                    .withType(WordNGram.class)
                    .withSeparator('\t')
                    .build()
                    .parse());

            wordBiGrams.forEach(gram -> gram.setNGram(gram.getNGram().toLowerCase()));
        } catch (IOException e) {
            log.error("Unable to find word-bigrams from file: {}.", bigramModelFilename, e);
            throw new IllegalStateException(e);
        }

        log.info("Finished loading word-bigrams in {}ms.", (System.currentTimeMillis() - startCount));

        return wordBiGrams;
    }
}
