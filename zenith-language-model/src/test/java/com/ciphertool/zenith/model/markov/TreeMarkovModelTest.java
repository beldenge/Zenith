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

package com.ciphertool.zenith.model.markov;

import com.ciphertool.zenith.model.entities.TreeNGram;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;

import static org.junit.jupiter.api.Assertions.*;

public class TreeMarkovModelTest {
    @Test
    public void given_addLetterTransitionFindExactAndLongest_when_invoked_then_expected() {
        TreeMarkovModel model = new TreeMarkovModel(3);

        assertTrue(model.addLetterTransition("abc"));
        assertFalse(model.addLetterTransition("abc"));
        assertTrue(model.addLetterTransition("abd"));

        TreeNGram exact = model.findExact("abc");
        assertNotNull(exact);
        assertEquals("abc", exact.getCumulativeString());
        assertEquals(2L, exact.getCount());

        TreeNGram longest = model.findLongest("abz");
        assertNotNull(longest);
        assertEquals("ab", longest.getCumulativeString());
        assertEquals(3L, longest.getCount());
    }

    @Test
    public void given_addNodeRootAndUpdates_when_invoked_then_expected() {
        TreeMarkovModel model = new TreeMarkovModel(2);

        TreeNGram root = new TreeNGram("");
        root.setCount(4L);
        root.setConditionalProbability(0.25d);
        root.setLogConditionalProbability(Math.log(0.25d));
        root.setProbability(0.4d);
        root.setLogProbability(Math.log(0.4d));

        model.addNode(root);

        TreeNGram storedRoot = model.getRootNode();
        assertEquals(4L, storedRoot.getCount());
        assertEquals(0.25d, storedRoot.getConditionalProbability(), 0.00001d);
        assertEquals(Math.log(0.25d), storedRoot.getLogConditionalProbability(), 0.00001d);
        assertEquals(0.4d, storedRoot.getProbability(), 0.00001d);
        assertEquals(Math.log(0.4d), storedRoot.getLogProbability(), 0.00001d);

        TreeNGram node = new TreeNGram("xy");
        node.setCount(5L);
        node.setProbability(0.12d);
        node.setLogProbability(Math.log(0.12d));
        model.addNode(node);

        TreeNGram found = model.findExact("xy");
        assertNotNull(found);
        assertEquals(5L, found.getCount());
        assertEquals(0.12d, found.getProbability(), 0.00001d);
        assertEquals(Math.log(0.12d), found.getLogProbability(), 0.00001d);

        TreeNGram updated = new TreeNGram("xy");
        updated.setCount(9L);
        updated.setProbability(0.5d);
        updated.setLogProbability(Math.log(0.5d));
        model.addNode(updated);

        TreeNGram updatedFound = model.findExact("xy");
        assertNotNull(updatedFound);
        assertEquals(9L, updatedFound.getCount());
        assertEquals(0.5d, updatedFound.getProbability(), 0.00001d);
        assertEquals(Math.log(0.5d), updatedFound.getLogProbability(), 0.00001d);
    }

    @Test
    public void given_normalizeAndSize_when_invoked_then_expected() {
        TreeMarkovModel model = new TreeMarkovModel(2);

        model.addLetterTransition("ab");
        model.addLetterTransition("ac");

        TaskExecutor taskExecutor = task -> task.run();
        model.normalize(2, 2L, taskExecutor);

        TreeNGram ab = model.findExact("ab");
        TreeNGram ac = model.findExact("ac");

        assertNotNull(ab);
        assertNotNull(ac);
        assertEquals(0.5d, ab.getProbability(), 0.00001d);
        assertEquals(Math.log(0.5d), ab.getLogProbability(), 0.00001d);
        assertEquals(0.5d, ac.getProbability(), 0.00001d);
        assertEquals(Math.log(0.5d), ac.getLogProbability(), 0.00001d);

        assertEquals(4L, model.size());
    }

    @Test
    public void given_findexactReturnsNullWhenNotFound_when_invoked_then_expected() {
        TreeMarkovModel model = new TreeMarkovModel(3);
        model.addLetterTransition("abc");

        assertNull(model.findExact("xyz"));
        assertNull(model.findExact("abz"));
    }

    @Test
    public void given_findlongestReturnsNullWhenNoMatch_when_invoked_then_expected() {
        TreeMarkovModel model = new TreeMarkovModel(3);
        model.addLetterTransition("abc");

        // 'x' doesn't exist as a first character
        assertNull(model.findLongest("xyz"));
    }

    /**
     * Verifies that toString() correctly displays the parent path for each node.
     * Bug fix: Previously the parent path was incorrectly built using the child's key
     * instead of the current symbol, causing output like "[b] ->b" instead of "[a] ->b".
     */
    @Test
    public void given_toString_when_invoked_then_expected() {
        TreeMarkovModel model = new TreeMarkovModel(2);
        model.addLetterTransition("ab");

        String result = model.toString();

        // The root 'a' should show empty parent: "[] ->a"
        assertTrue(result.contains("[] ->a"));
        // The child 'b' under 'a' should show parent 'a': "[a] ->b"
        assertTrue(result.contains("[a] ->b"));
        // Should NOT have incorrect path like "[b] ->b" (the old buggy behavior)
        assertFalse(result.contains("[b] ->b"));
    }

    @Test
    public void given_unknownLetterNGramProbability_when_invoked_then_expected() {
        TreeMarkovModel model = new TreeMarkovModel(3);

        assertNull(model.getUnknownLetterNGramProbability());
        assertNull(model.getUnknownLetterNGramLogProbability());

        model.setUnknownLetterNGramProbability(0.001d);
        model.setUnknownLetterNGramLogProbability(Math.log(0.001d));

        assertEquals(0.001d, model.getUnknownLetterNGramProbability(), 0.00001d);
        assertEquals(Math.log(0.001d), model.getUnknownLetterNGramLogProbability(), 0.00001d);
    }
}
