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

package com.ciphertool.zenith.data;

import com.ciphertool.zenith.model.dao.LetterNGramDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@EnableAsync
@Validated
@ConfigurationProperties
@SpringBootApplication(scanBasePackageClasses = { DataGeneratorApplication.class, LetterNGramDao.class })
public class DataGeneratorApplication implements CommandLineRunner {
    private static Logger log	= LoggerFactory.getLogger(DataGeneratorApplication.class);

    @Value("${taskExecutor.poolSize.override:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int						corePoolSize;

    @Value("${taskExecutor.poolSize.override:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int						maxPoolSize;

    @Min(1)
    @Value("${taskExecutor.queueCapacity}")
    private int						queueCapacity;

    @Autowired
    private EnglishSampleCreator englishSampleCreator;

    @Autowired
    private UniformSampleCreator uniformSampleCreator;

    @Autowired
    private MarkovSampleCreator markovSampleCreator;

    /**
     * Main entry point for the application.
     *
     * @param args
     *            the optional, unused command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DataGeneratorApplication.class, args);
    }

    @Override
    public void run(String... arg0) {
        long start = System.currentTimeMillis();

        englishSampleCreator.createSamples();

        log.info("Finished generating English samples in {}ms.", (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();

        uniformSampleCreator.createSamples();

        log.info("Finished generating uniform samples in {}ms.", (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();

        markovSampleCreator.createSamples();

        log.info("Finished generating Markov samples in {}ms.", (System.currentTimeMillis() - start));
    }

    @Bean
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