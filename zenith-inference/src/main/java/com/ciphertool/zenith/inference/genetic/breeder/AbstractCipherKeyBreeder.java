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

package com.ciphertool.zenith.inference.genetic.breeder;

import com.ciphertool.zenith.genetic.Breeder;
import com.ciphertool.zenith.inference.entities.Cipher;
import com.ciphertool.zenith.inference.entities.Ciphertext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCipherKeyBreeder implements Breeder {
    @Autowired
    protected Cipher cipher;

    protected String[] keys;

    @PostConstruct
    public void init() {
        List<String> keyList = cipher.getCiphertextCharacters().stream()
                .map(Ciphertext::getValue)
                .distinct()
                .collect(Collectors.toList());

        keys = keyList.toArray(new String[keyList.size()]);
    }
}
