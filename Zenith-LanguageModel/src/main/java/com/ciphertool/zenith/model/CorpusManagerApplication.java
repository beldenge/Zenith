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

package com.ciphertool.zenith.model;

import com.ciphertool.zenith.model.etl.persisters.NGramPersister;
import com.ciphertool.zenith.model.etl.transformers.CorpusTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class CorpusManagerApplication implements CommandLineRunner {
	private static Logger			log	= LoggerFactory.getLogger(CorpusManagerApplication.class);

	@Value("${taskExecutor.poolSize.override:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
	private int						corePoolSize;

	@Value("${taskExecutor.poolSize.override:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
	private int						maxPoolSize;

	@Value("${taskExecutor.queueCapacity}")
	private int						queueCapacity;

	@Value("${corpus.output.directory}")
	private String					outputDirectory;

	@Autowired
	private CorpusTransformer		xmlCorpusTransformer;

	@Autowired
	private CorpusTransformer		textCorpusTransformer;

	@Autowired
	private NGramPersister			nGramPersister;

	@Autowired
	private ThreadPoolTaskExecutor	taskExecutor;

	/**
	 * Main entry point for the application.
	 * 
	 * @param args
	 *            the optional, unused command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(CorpusManagerApplication.class, args);
	}

	@Override
	public void run(String... arg0) throws Exception {
		log.info("TaskExecutor core pool size: {}", taskExecutor.getCorePoolSize());
		log.info("TaskExecutor max pool size: {}", taskExecutor.getMaxPoolSize());

		if (!Files.exists(Paths.get(outputDirectory))) {
			textCorpusTransformer.transformCorpus();
			xmlCorpusTransformer.transformCorpus();
		}

		nGramPersister.persistNGrams();
	}

	@Bean
	@Primary
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

		taskExecutor.setCorePoolSize(corePoolSize);
		taskExecutor.setMaxPoolSize(maxPoolSize);
		taskExecutor.setQueueCapacity(queueCapacity);
		taskExecutor.setKeepAliveSeconds(5);
		taskExecutor.setAllowCoreThreadTimeOut(true);

		return taskExecutor;
	}
}
