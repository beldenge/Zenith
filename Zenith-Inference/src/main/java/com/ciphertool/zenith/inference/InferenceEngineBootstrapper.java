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

package com.ciphertool.zenith.inference;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class InferenceEngineBootstrapper {
	private static Logger				log	= LoggerFactory.getLogger(InferenceEngineBootstrapper.class);

	private static ApplicationContext	context;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		initializeContext();
	}

	/**
	 * Spins up the Spring application context
	 * 
	 * @throws IOException
	 *             if the properties file cannot be read
	 * @throws FileNotFoundException
	 *             if the properties file does not exist
	 */
	protected static void initializeContext() throws FileNotFoundException, IOException {
		log.info("Starting Spring application context");

		long start = System.currentTimeMillis();

		context = new ClassPathXmlApplicationContext("bootstrapContext.xml");

		log.info("Spring application context started successfully in " + (System.currentTimeMillis() - start) + "ms.");

		ThreadPoolTaskExecutor taskExecutor = context.getBean(ThreadPoolTaskExecutor.class);
		log.info("TaskExecutor core pool size: {}", taskExecutor.getCorePoolSize());
		log.info("TaskExecutor max pool size: {}", taskExecutor.getMaxPoolSize());

		BayesianDecipherManager manager = context.getBean(BayesianDecipherManager.class);
		manager.run();

		// Apparently this is required to cleanup after Ehcache, but I'm not entirely sure.
		((ConfigurableApplicationContext) context).close();
	}
}
