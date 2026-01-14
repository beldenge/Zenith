/*
 * Copyright 2017-2026 George Belden
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

package com.ciphertool.zenith.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = {
        "com.ciphertool.zenith.api",
        "com.ciphertool.zenith.inference.configuration",
        "com.ciphertool.zenith.inference.dao",
        "com.ciphertool.zenith.inference.evaluator",
        "com.ciphertool.zenith.inference.genetic",
        "com.ciphertool.zenith.inference.optimizer",
        "com.ciphertool.zenith.inference.printer",
        "com.ciphertool.zenith.inference.probability",
        "com.ciphertool.zenith.inference.segmentation",
        "com.ciphertool.zenith.inference.statistics",
        "com.ciphertool.zenith.inference.transformer",
        "com.ciphertool.zenith.inference.util"
})
@EnableCaching
public class ApiApplication {
    public static void main(String[] args) {
            SpringApplication.run(ApiApplication.class, args);
        }
}
