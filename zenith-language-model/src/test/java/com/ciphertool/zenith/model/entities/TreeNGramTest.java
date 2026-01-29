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

package com.ciphertool.zenith.model.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TreeNGramTest {
    @Test
    public void testAddOrIncrementChildAsync_AddsThenIncrements() {
        TreeNGram root = new TreeNGram("a");

        assertTrue(root.addOrIncrementChildAsync("ab", 2));
        assertTrue(root.containsChild('b'));
        assertEquals(1L, root.getChild('b').getCount());

        assertFalse(root.addOrIncrementChildAsync("ab", 2));
        assertEquals(2L, root.getChild('b').getCount());
    }

    @Test
    public void testAddExistingNodeAsync_ReplacesExistingLeaf() {
        TreeNGram root = new TreeNGram("a");
        TreeNGram existing = new TreeNGram("ab");
        existing.setCount(1L);
        existing.setProbability(0.1d);
        existing.setConditionalProbability(0.2d);
        existing.setLogProbability(-0.3d);
        existing.setLogConditionalProbability(-0.4d);
        root.putChild('b', existing);

        TreeNGram incoming = new TreeNGram("ab");
        incoming.setCount(5L);
        incoming.setProbability(0.6d);
        incoming.setConditionalProbability(0.7d);
        incoming.setLogProbability(-0.8d);
        incoming.setLogConditionalProbability(-0.9d);

        TreeNGram result = root.addExistingNodeAsync(incoming, 2);

        assertNull(result);
        TreeNGram updated = root.getChild('b');
        assertEquals(5L, updated.getCount());
        assertEquals(0.6d, updated.getProbability(), 0.000001d);
        assertEquals(0.7d, updated.getConditionalProbability(), 0.000001d);
        assertEquals(-0.8d, updated.getLogProbability(), 0.000001d);
        assertEquals(-0.9d, updated.getLogConditionalProbability(), 0.000001d);
    }

    @Test
    public void testAddExistingNodeAsync_CreatesIntermediateNode() {
        TreeNGram root = new TreeNGram("a");
        TreeNGram incoming = new TreeNGram("abc");

        TreeNGram next = root.addExistingNodeAsync(incoming, 2);

        assertNotNull(next);
        assertEquals("ab", next.getCumulativeString());
    }

    @Test
    public void testPutChildRejectsInvalidCharacter() {
        TreeNGram root = new TreeNGram("a");

        assertThrows(IllegalArgumentException.class, () -> root.putChild('!', new TreeNGram("a!")));
    }
}
