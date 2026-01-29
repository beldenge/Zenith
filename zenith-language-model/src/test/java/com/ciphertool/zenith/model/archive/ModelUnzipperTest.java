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

package com.ciphertool.zenith.model.archive;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelUnzipperTest {
    @Test
    public void testNewFile_AllowsSafeEntry() throws IOException {
        ModelUnzipper unzipper = new ModelUnzipper();
        Path tempDir = Files.createTempDirectory("model-unzipper");

        ZipEntry entry = new ZipEntry("subdir/file.txt");
        File file = unzipper.newFile(tempDir.toFile(), entry);

        String canonicalDestination = tempDir.toFile().getCanonicalPath() + File.separator;
        assertTrue(file.getCanonicalPath().startsWith(canonicalDestination));
    }

    @Test
    public void testNewFile_RejectsZipSlipEntry() throws IOException {
        ModelUnzipper unzipper = new ModelUnzipper();
        Path tempDir = Files.createTempDirectory("model-unzipper");

        ZipEntry entry = new ZipEntry("../evil.txt");

        assertThrows(IOException.class, () -> unzipper.newFile(tempDir.toFile(), entry));
    }
}
