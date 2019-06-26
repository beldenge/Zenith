/**
 * Copyright 2017-2019 George Belden
 * <p>
 * This file is part of Zenith.
 * <p>
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.genetic.mocks;

import com.ciphertool.zenith.genetic.Breeder;
import com.ciphertool.zenith.genetic.entities.Chromosome;

public class MockBreeder implements Breeder {
    @SuppressWarnings("unused")
    private Object geneticStructure;

    @Override
    public Chromosome breed() {
        throw new UnsupportedOperationException("Method stub not yet implemented");
    }
}
