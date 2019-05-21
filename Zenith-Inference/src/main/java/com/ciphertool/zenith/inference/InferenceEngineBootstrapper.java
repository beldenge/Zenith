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

package com.ciphertool.zenith.inference;

import com.ciphertool.zenith.model.dao.LetterNGramDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackageClasses = { InferenceEngineBootstrapper.class, LetterNGramDao.class })
@EnableCaching
public class InferenceEngineBootstrapper implements CommandLineRunner {
	@Autowired
	private DecipherManager manager;

	public static void main(String[] args) {
		SpringApplication.run(InferenceEngineBootstrapper.class, args).close();
	}

	/**
	 * Spins up the Spring application context
	 * 
	 * @throws Exception
	 *             if there's an error during initialization
	 */
	@Override
	public void run(String... arg0) {
		manager.run();
	}
}
