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

package com.ciphertool.zenith.math.selection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class BinaryRouletteNodeTest {
    @Test
    public void given_validInput_when_constructing_then_returnsNull() {
        BinaryRouletteNode node = new BinaryRouletteNode(7, 0.42d);

        assertEquals(7, node.getIndex());
        assertEquals(0.42d, node.getValue());
        assertNull(node.getLessThan());
        assertNull(node.getGreaterThan());

        BinaryRouletteNode lessThan = new BinaryRouletteNode(1, 0.1d);
        BinaryRouletteNode greaterThan = new BinaryRouletteNode(9, 0.9d);

        node.setLessThan(lessThan);
        node.setGreaterThan(greaterThan);

        assertSame(lessThan, node.getLessThan());
        assertSame(greaterThan, node.getGreaterThan());
    }
}
