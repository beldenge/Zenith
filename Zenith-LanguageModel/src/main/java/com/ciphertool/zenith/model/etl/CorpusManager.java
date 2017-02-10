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

package com.ciphertool.zenith.model.etl;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.ciphertool.zenith.model.etl.persisters.NGramPersister;
import com.ciphertool.zenith.model.etl.transformers.CorpusTransformer;

public class CorpusManager {
	private static Logger				log						= LoggerFactory.getLogger(CorpusManager.class);

	private static final String			TRANSFORM_CORPUS_KEY	= "corpus.transformation.required";

	private static BeanFactory			persistenceContext;
	private static BeanFactory			beanFactory;
	private static CorpusTransformer	xmlCorpusTransformer;
	private static CorpusTransformer	textCorpusTransformer;

	/**
	 * Bootstraps the Spring application context.
	 */
	private static void setUp() {
		log.info("Starting Spring application context");

		long start = System.currentTimeMillis();

		beanFactory = new ClassPathXmlApplicationContext("corpusContext.xml");

		log.info("Spring application context created successfully in {}ms.", (System.currentTimeMillis() - start));

		ThreadPoolTaskExecutor taskExecutor = beanFactory.getBean(ThreadPoolTaskExecutor.class);
		log.info("TaskExecutor core pool size: {}", taskExecutor.getCorePoolSize());
		log.info("TaskExecutor max pool size: {}", taskExecutor.getMaxPoolSize());

		textCorpusTransformer = (CorpusTransformer) beanFactory.getBean("textCorpusTransformer");
		xmlCorpusTransformer = (CorpusTransformer) beanFactory.getBean("xmlCorpusTransformer");
	}

	/**
	 * Main entry point for the word import tools.
	 * 
	 * @param args
	 *            the optional, unused command-line arguments
	 * @throws ParserConfigurationException
	 *             if there is an error transforming the corpus
	 */
	public static void main(String[] args) throws ParserConfigurationException {
		setUp();

		if (Boolean.valueOf(((ClassPathXmlApplicationContext) beanFactory).getEnvironment().getProperty(TRANSFORM_CORPUS_KEY))) {
			textCorpusTransformer.transformCorpus();
			xmlCorpusTransformer.transformCorpus();
		}

		persistenceContext = new ClassPathXmlApplicationContext("beans-persistence.xml");

		NGramPersister nGramPersister = (NGramPersister) persistenceContext.getBean("nGramPersister");

		nGramPersister.persistNGrams();
	}
}
