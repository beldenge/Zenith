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

import java.io.BufferedReader;
import java.util.concurrent.atomic.AtomicInteger;

public class FileReadingContext {
    private AtomicInteger recordsRead;
    private BufferedReader reader;

    public FileReadingContext(AtomicInteger recordsRead) {
        this.recordsRead = recordsRead;
    }

    public AtomicInteger getRecordsRead() {
        return recordsRead;
    }

    public void setRecordsRead(AtomicInteger recordsRead) {
        this.recordsRead = recordsRead;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }
}
