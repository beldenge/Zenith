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

package com.ciphertool.zenith.inference.dao;

import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.CipherJson;
import com.ciphertool.zenith.inference.entities.config.ApplicationConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class CipherDao {
    private static Logger log = LoggerFactory.getLogger(CipherDao.class);

    private static final String CIPHERS_DIRECTORY = "ciphers";
    private static final String JSON_EXTENSION = ".json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

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

        for (CipherJson cipherJson : applicationConfiguration.getCiphers()){
            Cipher nextCipher = new Cipher(cipherJson);

            if (containsCipher(ciphers, nextCipher)) {
                throw new IllegalArgumentException("Cipher with name " + nextCipher.getName() + " already imported.  Cannot import duplicate ciphers.");
            }

            ciphers.add(nextCipher);
        }

        return ciphers;
    }

    public void writeToFile(Cipher cipher) throws IOException {
        // Write the file to the current working directory
        String outputFilename = Paths.get(CIPHERS_DIRECTORY).toAbsolutePath().toString() + File.separator + cipher.getName() + JSON_EXTENSION;

        if (!Files.exists(Paths.get(CIPHERS_DIRECTORY))) {
            Files.createDirectory(Paths.get(CIPHERS_DIRECTORY));
        }

        try {
            OBJECT_MAPPER.writeValue(new File(outputFilename), new CipherJson(cipher));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write Cipher with name=" + cipher.getName() + " to file=" + outputFilename + ".");
        }
    }

    private boolean containsCipher(List<Cipher> ciphers, Cipher newCipher) {
        return ciphers.stream().anyMatch(cipher -> cipher.getName().equals(newCipher.getName()));
    }
}
