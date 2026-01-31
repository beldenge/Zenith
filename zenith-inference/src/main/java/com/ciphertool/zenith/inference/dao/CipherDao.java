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

package com.ciphertool.zenith.inference.dao;

import com.ciphertool.zenith.inference.entities.Cipher;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CipherDao {
    private static Logger log = LoggerFactory.getLogger(CipherDao.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CIPHERS_DIRECTORY_PATH = "./ciphers";

    private List<Cipher> ciphers = new ArrayList<>();

    @Autowired
    private Validator validator;

    @PostConstruct
    public void init() {
        // First read configuration from the classpath
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        Resource[] resources;

        try {
            resources = resolver.getResources("classpath*:/ciphers/*.json");
        } catch (IOException ioe) {
            throw new IllegalStateException("Unable to read configuration from classpath directory=ciphers/", ioe);
        }

        for (Resource resource : resources) {
            try (InputStream inputStream = resource.getInputStream()) {
                Cipher cipher = OBJECT_MAPPER.readValue(inputStream, Cipher.class);
                validateInputWithInjectedValidator(cipher);
                ciphers.add(cipher);
            } catch (IOException e) {
                log.error("Unable to read cipher from file: {}.", resource.getFilename(), e);
                throw new IllegalStateException(e);
            }
        }

        // Secondly, attempt to read configuration from the local directory on the filesystem
        File localConfigDirectory = new File(Paths.get(CIPHERS_DIRECTORY_PATH).toAbsolutePath().toString());

        if (localConfigDirectory.exists() && localConfigDirectory.isDirectory()) {
            for (File file : localConfigDirectory.listFiles((d, name) -> name.toLowerCase().endsWith(".json"))) {
                try {
                    Cipher cipher = OBJECT_MAPPER.readValue(file, Cipher.class);
                    validateInputWithInjectedValidator(cipher);
                    ciphers.add(cipher);
                } catch (JacksonException e) {
                    log.error("Unable to read cipher from file: {}.", file.getPath(), e);
                    throw new IllegalStateException(e);
                }
            }
        }

        // Validate cipher uniqueness
        List<Cipher> uniqueCiphers = new ArrayList<>();

        for (Cipher nextCipher : ciphers) {
            if (uniqueCiphers.stream().anyMatch(cipher -> cipher.getName().equals(nextCipher.getName()))) {
                throw new IllegalArgumentException("Cipher with name " + nextCipher.getName() + " already imported.  Cannot import duplicate ciphers.");
            }

            uniqueCiphers.add(nextCipher);
        }
    }

    private void validateInputWithInjectedValidator(Cipher cipher) {
        Set<ConstraintViolation<Cipher>> violations = validator.validate(cipher);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    public Cipher findByCipherName(String name) {
        if (StringUtils.isBlank(name)) {
            log.warn("Attempted to find cipher with null or empty name.  Returning null.");

            return null;
        }

        return ciphers.stream()
                .filter(cipher -> name.equalsIgnoreCase(cipher.getName()))
                .findAny()
                .orElse(null);
    }

    public List<Cipher> findAll() {
        return ciphers;
    }
}
