/**
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

package com.ciphertool.zenith.model.archive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@ConditionalOnExpression("'LanguageModelApplication' ne '${spring.application.name:#{null}}'")
public class ModelUnzipper {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${language-model.archive-filename}")
    private String modelArchiveFilename;

    public void unzip() {
        File destinationDirectory = new File(Paths.get("").toAbsolutePath().toString());
        byte[] buffer = new byte[1024];

        try (ZipInputStream zis = new ZipInputStream(new ClassPathResource(modelArchiveFilename).getInputStream())) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                File newFile = newFile(destinationDirectory, zipEntry);

                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }

                zipEntry = zis.getNextEntry();
            }
        } catch (IOException ioe) {
            log.error("Unable to unzip language model archive file: {}", modelArchiveFilename, ioe);
            throw new IllegalStateException(ioe);
        }
    }

    // Protect against zip slip (https://snyk.io/research/zip-slip-vulnerability)
    protected File newFile(File destinationDirectory, ZipEntry zipEntry) throws IOException {
        File destinationFile = new File(destinationDirectory, zipEntry.getName());
        String canonicalDestinationFile = destinationFile.getCanonicalPath();

        if (!canonicalDestinationFile.startsWith(destinationDirectory.getCanonicalPath() + File.separator)) {
            log.error("Zip slip vulnerability detected for destination file: {}.  Halting execution.", canonicalDestinationFile);
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destinationFile;
    }
}
