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

package com.ciphertool.zenith.neural.sample;

import com.ciphertool.zenith.model.ModelConstants;
import org.nd4j.linalg.api.ndarray.INDArray;

public class CharacterSequence {
    public static String fromOneHotVectors(INDArray oneHotVectors) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < oneHotVectors.size(0); i ++) {
            // TODO: is there an Nd4j way to find the index equaling zero?  Perhaps the cond() method?
            for (int j = 0; j < oneHotVectors.size(1); j ++) {
                if (oneHotVectors.getFloat(i, j) == 1.0f) {
                    sb.append(ModelConstants.LOWERCASE_LETTERS.get(j));
                    break;
                }
            }
        }

        return sb.toString();
    }
}
