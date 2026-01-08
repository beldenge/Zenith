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

package com.ciphertool.zenith.api.graphql;

import com.ciphertool.zenith.inference.dao.CipherDao;
import com.ciphertool.zenith.inference.entities.Cipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class CipherController {
    @Autowired
    private CipherDao cipherDao;

    @QueryMapping
    public List<Cipher> ciphers() {
        return cipherDao.findAll();
    }

    @SchemaMapping
    public String name(Cipher cipher) {
        return cipher.getName();
    }

    @SchemaMapping
    public int rows(Cipher cipher) {
        return cipher.getRows();
    }

    @SchemaMapping
    public int columns(Cipher cipher) {
        return cipher.getColumns();
    }

    @SchemaMapping
    public boolean readOnly(Cipher cipher) {
        return cipher.isReadOnly();
    }

    @SchemaMapping
    public List<String> ciphertext(Cipher cipher) {
        return cipher.getCiphertext();
    }

    @SchemaMapping
    public Map<String, String> knownSolutionKey(Cipher cipher) {
        return cipher.getKnownSolutionKey();
    }
}
