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

package com.ciphertool.zenith.genetic.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomListElementSelectorTest {
    @Test
    public void given_validInput_when_selectRandomListElementSingleItemAlwaysZero_then_returnsExpectedValue() {
        RandomListElementSelector selector = new RandomListElementSelector();
        List<String> items = Collections.singletonList("only");

        assertEquals(0, selector.selectRandomListElement(items));
    }

    @Test
    public void given_validInput_when_selectRandomListElementWithinRange_then_returnsTrue() {
        RandomListElementSelector selector = new RandomListElementSelector();
        List<String> items = Arrays.asList("a", "b", "c", "d", "e");

        for (int i = 0; i < 100; i++) {
            int index = selector.selectRandomListElement(items);
            assertTrue(index >= 0 && index < items.size());
        }
    }
}
