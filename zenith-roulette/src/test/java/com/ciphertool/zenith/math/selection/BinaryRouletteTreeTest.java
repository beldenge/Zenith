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

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class BinaryRouletteTreeTest {
    @Test
    public void given_validInput_when_insertSetsRootAndPositionsChildren_then_returnsSameInstance() {
        BinaryRouletteTree tree = new BinaryRouletteTree();

        BinaryRouletteNode root = node(1, 0.5d);
        BinaryRouletteNode left = node(0, 0.2d);
        BinaryRouletteNode right = node(2, 0.8d);
        BinaryRouletteNode duplicate = node(3, 0.5d);

        tree.insert(root);
        tree.insert(left);
        tree.insert(right);
        tree.insert(duplicate);

        BinaryRouletteNode actualRoot = getRoot(tree);
        assertSame(root, actualRoot);
        assertSame(left, actualRoot.getLessThan());
        assertSame(right, actualRoot.getGreaterThan());
        assertSame(duplicate, right.getLessThan());
    }

    @Test
    public void given_validInput_when_findExactAndBetweenValues_then_returnsSameInstance() {
        BinaryRouletteTree tree = new BinaryRouletteTree();

        BinaryRouletteNode root = node(1, 0.5d);
        BinaryRouletteNode left = node(0, 0.2d);
        BinaryRouletteNode right = node(2, 0.9d);

        tree.insert(root);
        tree.insert(left);
        tree.insert(right);

        assertSame(left, tree.find(0.2d));
        assertSame(root, tree.find(0.5d));
        assertSame(right, tree.find(0.9d));

        assertSame(root, tree.find(0.3d));
        assertSame(left, tree.find(0.01d));
        assertSame(right, tree.find(0.7d));
    }

    @Test
    public void given_validInput_when_findSingleNodeValueAbove_then_returnsSameInstance() {
        BinaryRouletteTree tree = new BinaryRouletteTree();
        BinaryRouletteNode root = node(0, 0.5d);

        tree.insert(root);

        assertSame(root, tree.find(0.75d));
        assertEquals(0, tree.find(0.75d).getIndex());
    }

    private BinaryRouletteNode node(int index, double value) {
        return new BinaryRouletteNode(index, value);
    }

    private BinaryRouletteNode getRoot(BinaryRouletteTree tree) {
        try {
            Field field = BinaryRouletteTree.class.getDeclaredField("root");
            field.setAccessible(true);
            return (BinaryRouletteNode) field.get(tree);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new AssertionError("Unable to access BinaryRouletteTree root field.", exception);
        }
    }
}
