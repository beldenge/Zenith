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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomListElementSelectorTest {
    private static final int MAX_SELECTIONS = 100;

    @Test
    public void testSelectRandomListElement() {
        RandomListElementSelector randomListElementSelector = new RandomListElementSelector();

        List<String> threeElements = Arrays.asList("element1", "element2", "element3");

        boolean element1Selected = false, element2Selected = false, element3Selected = false;

        for (int i = 0; i < MAX_SELECTIONS; i++) {
            Integer randomIndex = randomListElementSelector.selectRandomListElement(threeElements);

            if ("element1".equals(threeElements.get(randomIndex))) {
                element1Selected = true;
            }

            if ("element2".equals(threeElements.get(randomIndex))) {
                element2Selected = true;
            }

            if ("element3".equals(threeElements.get(randomIndex))) {
                element3Selected = true;
            }

            if (element1Selected && element2Selected && element3Selected) {
                break;
            }
        }

        assertTrue(element1Selected && element2Selected && element3Selected);
    }
}
